## Fixed
- Bad transition when starting a cutscene while in a camera angle
- Incorrect repositioning of the pick element screen
- Keyframe trigger still visible in the keyframe options screen
- Crash when starting a story while in a cutscene, camera, or main screen controller

## Updated
- Character animations now reuse existing entities if they are already in the world and near the starting point of the new animation, avoiding unnecessary respawns
- The "New Game" button is now shown only if a save is present, regardless of whether the story has been completed
- Added GitHub link in LinkCommand.java
- Player must have permission level 2 to edit the world's story
- Moved recording tick handling from client to server