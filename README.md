# Usage

gemini_backup.js contains implementation of Gemini Flash 2.0 to extract text and image details relevant to dating profile. It requires: 

A) user_preferences.json file to be present in the root directory; this file contains the user's preferences that will determine whether a profile is a match. (Assumption is that this json will be generated after the user completes the "HeartHacks" questionnaire.)

B) A directory of images to be present in the root directory; this directory substitutes the user's local directory where the Maestro screenshots of the profiles are stored.

C) A GEMINI_API_KEY environment variable to be set.

It will return a 1 if the profile is a match (swipe right), and a 0 if it is not (swipe left).