import { GoogleGenerativeAI } from "@google/generative-ai";
import { promises as fs } from "fs";
import dotenv from 'dotenv';
// import * as readline from 'node:readline/promises';
// import { stdin as input, stdout as output } from 'node:process';

// Initialize the API
dotenv.config();
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

/*
async function extractFromImage(imagePath) {
  try {
    // Load the image file and convert to base64
    const imageBuffer = await fs.readFile(imagePath);
    const base64Image = imageBuffer.toString("base64");

    // Initialize the model - 
    const model = genAI.getGenerativeModel({ model: "gemini-2.0-flash-lite-preview-02-05" });

    // Prepare the image data
    const imagePart = {
      inlineData: {
        data: base64Image,
        mimeType: "image/png"
      }
    };

    // Generate content from the image
    const bio_result = await model.generateContent(["Extract the written biography from the profile image, as well as the interest tags if they are present. Return only the text, no explanations.", imagePart]);
    const bio = await bio_result.response;
 
    const image_description_result = await model.generateContent([
      "Describe notable physical characteristics (e.g. hair color, height) and environmental features (e.g. background, clothing, etc.). Describe the activities being done in the photo, or any key qualities implied by the photo. Return only the descriptions, no explanations.",
       imagePart]);
    const image_description = await image_description_result.response;

    return {
      bio: bio.text(),
      image_description: image_description.text(),
    };
  } catch (error) {
    console.error("Error extracting text from image:", error);
    throw error;
  }
}
*/

async function compareProfiles(potential_match_profile) {
    try {
        // Read and parse JSON file
        const preferences_file = JSON.parse(await fs.readFile('user_preferences.json', 'utf8'));
        // Get the first user's preferences (assuming single user for now)
        const user_prefs = preferences_file[Object.keys(preferences_file)[0]];
        
        const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

        const response = await model.generateContent([
            `
            Analyze this dating profile and determine compatibility based on these preferences:

            Looking for: ${user_prefs.looking_for.join(', ')}
            Deal breakers: ${user_prefs.deal_breakers.join(', ')}
            Shared interests: ${user_prefs.interests.join(', ')}
            Age preference: ${user_prefs.age_range}
            Location preference: ${user_prefs.location_preference}

            Profile to analyze: "Bio:  ${potential_match_profile.bio} and Image Descriptions: ${potential_match_profile.image_description}." 

            Determine the profile's compatibility with the user's preferences, and so whether the user should "Swipe Right" or "Swipe Left". Provide a brief explanation of why.

            Strict Rules:
            - If any of the user preferences are labeled "None" or are empty, ignore that category.
            - If the profile contains any of the deal breakers, swipe left. 
            - if the profile is distinctly outside of the specified age preference or location preference, swipe left. 
            - Unless physical characteristics are deal breakers, weigh physical descriptors less heavily.
            `
        ]);

        const analysis = response.response.text();
        return analysis.toLowerCase().includes('right') ? 1 : 0;
        //return analysis;
    } catch (error) {
        if (error.code === 'ENOENT') {
            console.error("Error: user_preferences.json not found");
        } else {
            console.error("Error reading or parsing preferences:", error);
        }
        throw error;
    }
}

async function extractFromImages(imagePaths) {
  try {
    // Extract bio from the first image only
    const firstImageBuffer = await fs.readFile(imagePaths[0]);
    const firstBase64Image = firstImageBuffer.toString("base64");
    const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
    
    const bio_result = await model.generateContent([
      "Extract the written biography from the profile image, as well as the interest tags and age if they are present. Return only the text, no explanations.",
      {
        inlineData: {
          data: firstBase64Image,
          mimeType: "image/png"
        }
      }
    ]);
    const bio = await bio_result.response;

    // Process all images for physical descriptions
    const descriptions = await Promise.all(imagePaths.map(async (imagePath) => {
      const imageBuffer = await fs.readFile(imagePath);
      const base64Image = imageBuffer.toString("base64");
      
      const image_description_result = await model.generateContent([
        "Extract notable physical characteristics (e.g. hair color, height) and environmental features (e.g. background). Describe the activities being done in the photo, or any key qualities of the photo. Return only the descriptions, no explanations.",
        {
          inlineData: {
            data: base64Image,
            mimeType: "image/png"
          }
        }
      ]);
      return image_description_result.response.text();
    }));

    return {
      bio: bio.text(),
      image_descriptions: descriptions,
    };
  } catch (error) {
    console.error("Error extracting text from images:", error);
    throw error;
  }
}

// Modify analyzeProfile to handle directory
async function analyzeProfile(imagePath) {
  try {
    let imagePaths;
    const stats = await fs.stat(imagePath);
    
    if (stats.isDirectory()) {
      const files = await fs.readdir(imagePath);
      imagePaths = files
        .filter(file => file.match(/\.(jpg|jpeg|png)$/i))
        .map(file => `${imagePath}/${file}`);
    } else {
      imagePaths = [imagePath];
    }

    if (imagePaths.length === 0) {
      throw new Error("No valid images found in directory");
    }

    const extractedText = await extractFromImages(imagePaths);
    console.log("Extracted Text:", extractedText);

    // Combine bio with all image descriptions for comparison
    // const fullProfile = extractedText.bio + "\n" + extractedText.image_descriptions.join("\n");
    const decision = await compareProfiles(extractedText);
    console.log("Verdict:", decision);
    return decision;
    
  } catch (error) {
    console.error("Main error:", error);
    throw error;
  }
}

// Main function to handle command line arguments
async function main() {
    try {
        // Get image path from command line arguments
        const args = process.argv.slice(2);
        if (args.length === 0) {
            console.error("Please provide an image path as argument");
            process.exit(1);
        }

        const imagePath = args[0];
        const result = await analyzeProfile(imagePath);
        return result;
    } catch (error) {
        console.error("Error in main:", error);
        process.exit(1);
    }
}

// Run main if called directly (not imported)
if (process.argv[1] === import.meta.url.substring(7)) {
    main();
}

export { analyzeProfile };