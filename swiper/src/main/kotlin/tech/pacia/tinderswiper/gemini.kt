package tech.pacia.tinderswiper

import java.io.File
import java.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.invoke
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content

class Gemini(private val apiKey: String) {
    data class Profile(
        val bio: String,
        val imageDescriptions: List<String>
    )

    @Serializable
    data class UserPreferences(
        val lookingFor: List<String>,
        val dealBreakers: List<String>,
        val interests: List<String>,
        val ageRange: String,
        val locationPreference: String
    )

    // ... existing code ...

    suspend fun compareProfiles(potentialMatchProfile: Profile): Int {
        try {
            val preferencesFile = File("user_preferences.json").readText()
            val preferences = Json.decodeFromString<Map<String, UserPreferences>>(preferencesFile)
            val userPrefs = preferences.values.first()

            // Create prompt for Gemini to analyze compatibility
            val prompt = """
                Analyze this dating profile against the user's preferences. Rate compatibility from 0-1.
                
                Profile Bio: ${potentialMatchProfile.bio}
                Profile Images: ${potentialMatchProfile.imageDescriptions.joinToString("\n")}
                
                User Preferences:
                - Looking for: ${userPrefs.lookingFor.joinToString(", ")}
                - Deal breakers: ${userPrefs.dealBreakers.joinToString(", ")}
                - Interests: ${userPrefs.interests.joinToString(", ")}
                - Age Range: ${userPrefs.ageRange}
                - Location: ${userPrefs.locationPreference}
                
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

    suspend fun extractFromImages(imagePaths: List<String>): Profile {
        try {
            val imageDescriptions = mutableListOf<String>()

            for (imagePath in imagePaths) {
                val prompt = """
                    Analyze this dating profile image. Describe:
                    1. The person's appearance
                    2. The setting/background
                    3. Any visible activities or interests
                    4. Relevant details for dating context
                    Be concise but thorough.
                """.trimIndent()

                // Call Gemini API for each image
                val description = callGeminiAPI(imagePath, prompt)
                imageDescriptions.add(description)
            }

            // Extract bio from the first image (assuming it contains profile text)
            val bioPrompt =
                "Extract any visible profile text or bio from this image. If none, describe the main person."
            val bio = callGeminiAPI(imagePaths.first(), bioPrompt)

            return Profile(
                bio = bio,
                imageDescriptions = imageDescriptions
            )
        } catch (e: Exception) {
            println("Error extracting text from images: ${e.message}")
            throw e
        }
    }

    private suspend fun callGeminiAPI(prompt: String): String {
        val apiKey = System.getenv("GEMINI_API_KEY") ?: throw IllegalStateException("GEMINI_API_KEY not found")

        val model = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey
        )

        val response = model.generateContent(prompt)
        return response.text?.trim() ?: throw IllegalStateException("No response from Gemini")
    }

    private suspend fun callGeminiAPI(imagePath: String, prompt: String): String {
        val apiKey = System.getenv("GEMINI_API_KEY") ?: throw IllegalStateException("GEMINI_API_KEY not found")

        // Read and convert image to base64
        val image = ImageIO.read(File(imagePath))
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", baos)
        val imageBytes = baos.toByteArray()

        val model = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = apiKey
        )

        // Create content with both image and text
        val content = content {
            image(imageBytes)
            text(prompt)
        }

        val response = model.generateContent(content)
        return response.text?.trim() ?: throw IllegalStateException("No response from Gemini")
    }

    suspend fun analyzeProfile(imagePath: String): Int {
        return try {
            val file = File(imagePath)
            val imagePaths = when {
                file.isDirectory -> file.listFiles()
                    ?.filter { it.name.matches(Regex(".*\\.(jpg|jpeg|png)$", RegexOption.IGNORE_CASE)) }
                    ?.map { it.absolutePath }
                    ?: emptyList()

                else -> listOf(imagePath)
            }

            if (imagePaths.isEmpty()) {
                throw IllegalArgumentException("No valid images found in directory")
            }

            val extractedText = extractFromImages(imagePaths)
            println("Extracted Text: $extractedText")

            val decision = compareProfiles(extractedText)
            println("Verdict: $decision")
            decision
        } catch (e: Exception) {
            println("Main error: ${e.message}")
            throw e
        }
    }
}

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
