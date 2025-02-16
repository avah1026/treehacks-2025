package tech.pacia.tinderswiper

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.serialization.Serializable
import okio.Buffer
import java.io.FileNotFoundException

@Serializable
data class MyOwnPreferences(
    val lookingFor: List<String>,
    val dealBreakers: List<String>,
    val interests: List<String>,
    val ageRange: String,
    val locationPreference: String,
)

data class ExternalProfile(
    val bio: String,
    val imageDescriptions: List<String>,
)

class Gemini(private val apiKey: String) {

    // ... existing code ...

    suspend fun compareProfiles(myOwnPrefs: MyOwnPreferences, externalProfile: ExternalProfile): Int {
        try {
            // val preferencesFile = File("user_preferences.json").readText()
            // val preferences = Json.decodeFromString<Map<String, MyOwnPreferences>>(preferencesFile)
            // val userPrefs = preferences.values.first()

            // Create prompt for Gemini to analyze compatibility
            val prompt = """
                Analyze this dating profile against the user's preferences. Rate compatibility from 0-1.
                
                Profile Bio: ${externalProfile.bio}
                Profile Images: ${externalProfile.imageDescriptions.joinToString("\n")}
                
                User Preferences:
                - Looking for: ${myOwnPrefs.lookingFor.joinToString(", ")}
                - Deal breakers: ${myOwnPrefs.dealBreakers.joinToString(", ")}
                - Interests: ${myOwnPrefs.interests.joinToString(", ")}
                - Age Range: ${myOwnPrefs.ageRange}
                - Location: ${myOwnPrefs.locationPreference}
                
                Provide a number between 0 and 1, where:
                0 = Not compatible at all (deal breakers present)
                1 = Highly compatible
            """.trimIndent()

            // Call Gemini API for analysis
            val response = callGeminiAPI(prompt)

            // Parse response to get compatibility score (0-1)
            val score = response.toDoubleOrNull() ?: 0.5
            return (score * 100).toInt() // Convert to 0-100 scale
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException -> println("Error: user_preferences.json not found")
                else -> println("Error reading or parsing preferences: ${e.message}")
            }
            throw e
        }
    }

    suspend fun extractProfile(imageBuffers: List<Buffer>): ExternalProfile {
        try {
            val imageDescriptions = mutableListOf<String>()

            for (imageBuffer in imageBuffers) {
                val prompt = """
                    Analyze this dating profile image. Describe:
                    1. The person's appearance
                    2. The setting/background
                    3. Any visible activities or interests
                    4. Relevant details for dating context
                    Be concise but thorough.
                """.trimIndent()

                // Call Gemini API for each image
                val copiedImageBuffer = imageBuffer.copy()
                val description = callGeminiAPI(copiedImageBuffer, prompt)
                imageDescriptions.add(description)
            }

            // Extract bio from the first image (assuming it contains profile text)
            val bioPrompt =
                "Extract any visible profile text or bio from this image. If none, describe the main person."
            val bio = callGeminiAPI(imageBuffers.firstOrNull(), bioPrompt)

            return ExternalProfile(
                bio = bio,
                imageDescriptions = imageDescriptions,
            )
        } catch (e: Exception) {
            println("Error extracting text from images: ${e.message}")
            throw e
        }
    }

    private suspend fun callGeminiAPI(prompt: String): String {
        val model = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
        )

        val response = model.generateContent(prompt)
        return response.text?.trim() ?: throw IllegalStateException("No response from Gemini")
    }

    private suspend fun callGeminiAPI(image: Buffer?, prompt: String): String {
        val model = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
        )

        val imageBytes = image?.readByteArray()

        // Create content with both image and text
        val content = content {
            if (imageBytes != null) {
                image(imageBytes)
            }
            text(prompt)
        }

        val response = model.generateContent(content)
        return response.text?.trim() ?: throw IllegalStateException("No response from Gemini")
    }

    private fun getImages() {
    }

    /**
     * Returns 1 if profile is to be swiped right, 0 otherwise.
     */
    suspend fun analyzeProfile(
        myOwnPrefs: MyOwnPreferences,
        images: List<Buffer>
    ): Int {
        val externalProfile = extractProfile(images)
        println("Extracted Text: $externalProfile")

        val decision = compareProfiles(
            myOwnPrefs = myOwnPrefs,
            externalProfile = externalProfile,
        )
        return decision
    }
}

/*
suspend fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Please provide an image path as argument")
        return
    }

    try {
        val apiKey = System.getenv("GEMINI_API_KEY") ?: throw IllegalStateException("GEMINI_API_KEY not set")
        val gemini = Gemini(apiKey)
        val result = gemini.analyzeProfile(args[0])
        println(result)
    } catch (e: Exception) {
        println("Error in main: ${e.message}")
    }
}
*/
