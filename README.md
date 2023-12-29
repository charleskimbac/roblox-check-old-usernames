# Roblox: Check Old Usernames
Submit Roblox username(s) (not their display name) into the text field to see their previous usernames.  
Press Enter while in the search box to instantly search.  

## Single-Search Mode (default)
Spaces entered into the text field will be converted into an underscore.  

## OCR Mode
The search should be formatted as `displayName1 @username1 displayName2 @username2 etc...`  
It is recommended to use Copilot/Bing AI to abstract the text from a screenshot of the Roblox player (`ESC`) menu.  
Recommended prompt: "Give me all of the text in the image in plain text and in one line. Make sure to include the parts with the '@' sign. Do not use commas, but separate each word with a space."  

## Todo:
- [ ] Crosschecking: displayName from UsernameData (API Call) = respective display name from input (text before '@...')  
- [ ] Format check in OCR mode: '@' every 2 words  
- [ ] If username not found in OCR mode, try changing the input for common OCR mistakes (automatically): I = l, vv = w, etc  

More view options (i.e. checkboxes):  
  - [ ] Show more than 5 (default) previous usernames  
  - [ ] Don't show those with no previous usernames  
  - [ ] Result in alphabetical order  
