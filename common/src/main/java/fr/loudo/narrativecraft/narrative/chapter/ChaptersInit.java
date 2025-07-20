package fr.loudo.narrativecraft.narrative.chapter;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterSkinController;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionGsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChaptersInit {
    public static List<Chapter> init() {
        List<Chapter> chapters = new ArrayList<>();

        try {
            DialogData.globalDialogData = NarrativeCraftFile.getGlobalDialogValues();
        } catch (Exception e) {
            NarrativeCraftMod.LOG.warn("Failed to load global dialog data: {}", e.getMessage());
            DialogData.globalDialogData = DialogData.defaultValues();
        }

        File chapterDirectory = NarrativeCraftFile.chaptersDirectory;
        if (chapterDirectory == null || !chapterDirectory.exists() || !chapterDirectory.isDirectory()) {
            NarrativeCraftMod.LOG.warn("Chapter directory is null, doesn't exist, or is not a directory");
            return null;
        }

        File[] chapterIndexFolder = chapterDirectory.listFiles();
        if (chapterIndexFolder != null) {
            for (File chapterIndex : chapterIndexFolder) {
                if (chapterIndex == null || !chapterIndex.isDirectory()) {
                    NarrativeCraftMod.LOG.warn("Skipping invalid chapter folder: {}",
                            chapterIndex != null ? chapterIndex.getName() : "null");
                    continue;
                }

                try {
                    int index = Integer.parseInt(chapterIndex.getName());
                    Chapter chapter = new Chapter(index);
                    String name = "";
                    String desc = "";

                    File detailsFile = NarrativeCraftFile.getDetailsFile(chapterIndex);
                    if (detailsFile.exists()) {
                        try {
                            String content = Files.readString(detailsFile.toPath());
                            if (!content.trim().isEmpty()) {
                                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                                if (json != null) {
                                    JsonElement nameElement = json.get("name");
                                    JsonElement descElement = json.get("description");
                                    name = nameElement != null && !nameElement.isJsonNull() ? nameElement.getAsString() : "";
                                    desc = descElement != null && !descElement.isJsonNull() ? descElement.getAsString() : "";
                                }
                            }
                        } catch (IOException e) {
                            NarrativeCraftMod.LOG.warn("Failed to read settings file of chapter {}: {}", index, e.getMessage());
                        } catch (Exception e) {
                            NarrativeCraftMod.LOG.warn("Failed to parse JSON for chapter {}: {}", index, e.getMessage());
                        }
                    }

                    chapter.setName(name);
                    chapter.setDescription(desc);
                    initScenesOfChapter(chapterIndex, chapter);
                    chapters.add(chapter);

                } catch (NumberFormatException e) {
                    NarrativeCraftMod.LOG.warn("Chapter folder name is not a valid number: {}", chapterIndex.getName());
                } catch (Exception e) {
                    NarrativeCraftMod.LOG.warn("Failed to initialize chapter {}: {}", chapterIndex.getName(), e.getMessage());
                }
            }
        }
        return chapters;
    }

    private static void initScenesOfChapter(File chapterIndex, Chapter chapter) {
        if (chapterIndex == null || chapter == null) {
            NarrativeCraftMod.LOG.warn("Chapter index or chapter is null, skipping scene initialization");
            return;
        }

        File scenesDirectory = new File(chapterIndex.getAbsoluteFile(), "scenes");
        if (!scenesDirectory.exists() || !scenesDirectory.isDirectory()) {
            return; // No scenes directory, which is fine
        }

        File[] scenesFolder = scenesDirectory.listFiles();
        if (scenesFolder != null) {
            List<Scene> sceneList = new ArrayList<>();
            for (File sceneFolder : scenesFolder) {
                if (sceneFolder == null || !sceneFolder.isDirectory()) {
                    NarrativeCraftMod.LOG.warn("Skipping invalid scene folder");
                    continue;
                }

                try {
                    File dataFolder = new File(sceneFolder.getAbsoluteFile(), "data");
                    String name = sceneFolder.getName();
                    String desc = "";
                    Scene scene = new Scene(name, desc, chapter);
                    scene.setChapter(chapter);
                    scene.setNpcs(new ArrayList<>());

                    if (dataFolder.exists() && dataFolder.isDirectory()) {
                        File detailsFile = NarrativeCraftFile.getDetailsFile(dataFolder);
                        if (detailsFile.exists()) {
                            try {
                                String content = Files.readString(detailsFile.toPath());
                                if (!content.trim().isEmpty()) {
                                    JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                                    if (json != null) {
                                        JsonElement nameElement = json.get("name");
                                        JsonElement descElement = json.get("description");
                                        JsonElement placementElement = json.get("placement");

                                        name = nameElement != null && !nameElement.isJsonNull() ? nameElement.getAsString() : name;
                                        desc = descElement != null && !descElement.isJsonNull() ? descElement.getAsString() : "";
                                        int placement = placementElement != null && !placementElement.isJsonNull() ? placementElement.getAsInt() : 0;

                                        scene.setName(name);
                                        scene.setDescription(desc);
                                        scene.setPlacement(placement);
                                    }
                                }
                            } catch (IOException e) {
                                NarrativeCraftMod.LOG.warn("Failed to read settings file of scene {} of chapter {}: {}",
                                        sceneFolder.getName(), chapterIndex.getName(), e.getMessage());
                            } catch (Exception e) {
                                NarrativeCraftMod.LOG.warn("Failed to parse JSON for scene {} of chapter {}: {}",
                                        sceneFolder.getName(), chapterIndex.getName(), e.getMessage());
                            }
                        }
                        initSceneData(sceneFolder, scene);
                    }
                    sceneList.add(scene);

                } catch (Exception e) {
                    NarrativeCraftMod.LOG.warn("Failed to initialize scene {}: {}", sceneFolder.getName(), e.getMessage());
                }
            }

            if (chapter.getSceneList() != null) {
                chapter.getSceneList().clear();
                chapter.getSceneList().addAll(sceneList);
            }
        }
    }

    private static void initSceneData(File sceneFolder, Scene scene) {
        if (sceneFolder == null || scene == null) {
            NarrativeCraftMod.LOG.warn("Scene folder or scene is null, skipping scene data initialization");
            return;
        }

        File dataFolder = new File(sceneFolder, "data");
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            return; 
        }

        // Npcs
        initNpcs(dataFolder, scene);

        // Animations
        initAnimations(dataFolder, scene);

        // Subscenes
        initSubscenes(dataFolder, scene);

        // Cutscenes
        initCutscenes(dataFolder, scene);

        // Camera Angles
        initCameraAngles(dataFolder, scene);
    }

    private static void initNpcs(File dataFolder, Scene scene) {
        File npcFolder = new File(dataFolder, "npc");
        if (!npcFolder.exists() || !npcFolder.isDirectory()) {
            return;
        }

        File[] characterList = npcFolder.listFiles();
        if (characterList != null) {
            for (File characterFolder : characterList) {
                if (characterFolder == null || !characterFolder.isDirectory()) {
                    continue;
                }

                try {
                    File dataFile = new File(characterFolder, "data" + NarrativeCraftFile.EXTENSION_DATA_FILE);
                    if (!dataFile.exists()) {
                        NarrativeCraftMod.LOG.warn("Character data file does not exist: {}", dataFile.getPath());
                        continue;
                    }

                    String dataContent = Files.readString(dataFile.toPath());
                    if (dataContent.trim().isEmpty()) {
                        NarrativeCraftMod.LOG.warn("Character data file is empty: {}", dataFile.getPath());
                        continue;
                    }

                    CharacterStory characterStory = new Gson().fromJson(dataContent, CharacterStory.class);
                    if (characterStory == null) {
                        NarrativeCraftMod.LOG.warn("Failed to parse character story from: {}", dataFile.getPath());
                        continue;
                    }

                    characterStory.setCharacterSkinController(new CharacterSkinController(characterStory));
                    File skin = new File(characterFolder, "main.png");
                    if (skin.exists()) {
                        characterStory.getCharacterSkinController().getSkins().add(skin);
                        characterStory.getCharacterSkinController().setCurrentSkin(skin);
                        characterStory.getCharacterSkinController().cacheSkins();
                    }

                    characterStory.setScene(scene);

                    if (characterStory.getEntityTypeId() >= 0) {
                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.byId(characterStory.getEntityTypeId());
                        characterStory.setEntityType(entityType);
                    }

                    scene.addNpc(characterStory);

                } catch (IOException e) {
                    NarrativeCraftMod.LOG.warn("Failed to read NPC data from folder {}: {}", characterFolder.getName(), e.getMessage());
                } catch (Exception e) {
                    NarrativeCraftMod.LOG.warn("Failed to initialize NPC from folder {}: {}", characterFolder.getName(), e.getMessage());
                }
            }
        }
    }

    private static void initAnimations(File dataFolder, Scene scene) {
        File animationsFolder = new File(dataFolder, "animations");
        if (!animationsFolder.exists() || !animationsFolder.isDirectory()) {
            return;
        }

        Gson gson = new GsonBuilder().registerTypeAdapter(Action.class, new ActionGsonParser()).create();
        File[] animationsFile = animationsFolder.listFiles();
        if (animationsFile != null) {
            for (File animationFile : animationsFile) {
                if (animationFile == null || !animationFile.isFile()) {
                    continue;
                }

                try {
                    String content = Files.readString(animationFile.toPath());
                    if (content.trim().isEmpty()) {
                        NarrativeCraftMod.LOG.warn("Animation file is empty: {}", animationFile.getName());
                        continue;
                    }

                    Animation animation = gson.fromJson(content, Animation.class);
                    if (animation == null) {
                        NarrativeCraftMod.LOG.warn("Failed to parse animation from: {}", animationFile.getName());
                        continue;
                    }

                    CharacterStory characterStory = null;
                    if (animation.getCharacter() != null) {
                        if (animation.getCharacter().getCharacterType() == CharacterStory.CharacterType.MAIN) {
                            characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(animation.getCharacter().getName());
                            if (characterStory != null && characterStory.getCharacterSkinController() != null && animation.getSkinName() != null) {
                                File skinFile = characterStory.getCharacterSkinController().getSkinFile(animation.getSkinName());
                                if (skinFile != null) {
                                    characterStory.getCharacterSkinController().setCurrentSkin(skinFile);
                                }
                            }
                        } else if (animation.getCharacter().getCharacterType() == CharacterStory.CharacterType.NPC) {
                            characterStory = scene.getNpc(animation.getCharacter().getName());
                        }
                        if (characterStory != null) {
                            animation.setCharacter(characterStory);
                        }
                    }
                    animation.setScene(scene);

                    if (animation.getActionsData() != null) {
                        for (ActionsData actionsData : animation.getActionsData()) {
                            if (actionsData != null && actionsData.getActions() != null) {
                                actionsData.getActions().removeIf(Objects::isNull);
                            }
                        }
                    }
                    scene.addAnimation(animation);

                } catch (IOException e) {
                    NarrativeCraftMod.LOG.warn("Failed to read animation file {}: {}", animationFile.getName(), e.getMessage());
                } catch (Exception e) {
                    NarrativeCraftMod.LOG.warn("Failed to initialize animation from file {}: {}", animationFile.getName(), e.getMessage());
                }
            }
        }
    }

    private static void initSubscenes(File dataFolder, Scene scene) {
        File subsceneFile = new File(dataFolder, "subscenes" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if (!subsceneFile.exists()) {
            return;
        }

        try {
            String content = Files.readString(subsceneFile.toPath());
            if (content.trim().isEmpty()) {
                NarrativeCraftMod.LOG.warn("Subscenes file is empty");
                return;
            }

            Type listType = new TypeToken<List<Subscene>>() {}.getType();
            List<Subscene> subscenes = new Gson().fromJson(content, listType);
            if (subscenes != null) {
                for (Subscene subscene : subscenes) {
                    if (subscene == null) continue;

                    subscene.setScene(scene);
                    subscene.setAnimationList(new ArrayList<>());

                    if (subscene.getAnimationNameList() != null && scene.getAnimationList() != null) {
                        for (Animation animation : scene.getAnimationList()) {
                            if (animation != null && animation.getName() != null &&
                                    subscene.getAnimationNameList().contains(animation.getName())) {
                                subscene.getAnimationList().add(animation);
                            }
                        }
                    }
                }
                scene.setSubsceneList(subscenes);
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOG.warn("Failed to read subscenes file: {}", e.getMessage());
        } catch (Exception e) {
            NarrativeCraftMod.LOG.warn("Failed to initialize subscenes: {}", e.getMessage());
        }
    }

    private static void initCutscenes(File dataFolder, Scene scene) {
        File cutsceneFile = new File(dataFolder, "cutscenes" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if (!cutsceneFile.exists()) {
            return;
        }

        try {
            String content = Files.readString(cutsceneFile.toPath());
            if (content.trim().isEmpty()) {
                NarrativeCraftMod.LOG.warn("Cutscenes file is empty");
                return;
            }

            Type listType = new TypeToken<List<Cutscene>>() {}.getType();
            List<Cutscene> cutscenes = new Gson().fromJson(content, listType);
            if (cutscenes != null) {
                for (Cutscene cutscene : cutscenes) {
                    if (cutscene == null) continue;

                    cutscene.setAnimationList(new ArrayList<>());
                    if (cutscene.getAnimationListString() == null) {
                        cutscene.setAnimationListString(new ArrayList<>());
                    }
                    cutscene.setScene(scene);

                    if (cutscene.getAnimationListString() != null) {
                        for (String animationName : cutscene.getAnimationListString()) {
                            if (animationName == null) continue;

                            Animation animation = scene.getAnimationByName(animationName);
                            if (animation != null) {
                                animation.setScene(scene);
                                cutscene.getAnimationList().add(animation);
                            }
                        }
                    }

                    if (cutscene.getSubsceneList() != null) {
                        for (Subscene subscene : cutscene.getSubsceneList()) {
                            if (subscene == null) continue;

                            subscene.setAnimationList(new ArrayList<>());
                            if (subscene.getAnimationNameList() != null) {
                                for (String animationName : subscene.getAnimationNameList()) {
                                    if (animationName == null) continue;

                                    Animation animation = scene.getAnimationByName(animationName);
                                    if (animation != null) {
                                        animation.setScene(scene);
                                        subscene.getAnimationList().add(animation);
                                    }
                                }
                            }
                            subscene.setScene(scene);
                        }
                    }
                }
                scene.setCutsceneList(cutscenes);
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOG.warn("Failed to read cutscenes file: {}", e.getMessage());
        } catch (Exception e) {
            NarrativeCraftMod.LOG.warn("Failed to initialize cutscenes: {}", e.getMessage());
        }
    }

    private static void initCameraAngles(File dataFolder, Scene scene) {
        File cameraAnglesFile = new File(dataFolder, "camera_angles" + NarrativeCraftFile.EXTENSION_DATA_FILE);
        if (!cameraAnglesFile.exists()) {
            return;
        }

        try {
            Gson gson1 = new GsonBuilder().create();
            String content = Files.readString(cameraAnglesFile.toPath());
            if (content.trim().isEmpty()) {
                NarrativeCraftMod.LOG.warn("Camera angles file is empty");
                return;
            }

            Type listType = new TypeToken<List<CameraAngleGroup>>() {}.getType();
            List<CameraAngleGroup> cameraAngleGroupList = gson1.fromJson(content, listType);
            if (cameraAngleGroupList != null) {
                for (CameraAngleGroup cameraAngleGroup : cameraAngleGroupList) {
                    if (cameraAngleGroup == null) continue;

                    cameraAngleGroup.setScene(scene);
                    if (cameraAngleGroup.getCameraAngleList() == null) {
                        cameraAngleGroup.setCameraAngleList(new ArrayList<>());
                    }
                    if (cameraAngleGroup.getKeyframeTriggerList() == null) {
                        cameraAngleGroup.setKeyframeTriggerList(new ArrayList<>());
                    }

                    if (cameraAngleGroup.getCharacterStoryDataList() != null) {
                        for (CharacterStoryData characterStoryData : cameraAngleGroup.getCharacterStoryDataList()) {
                            if (characterStoryData == null || characterStoryData.getCharacterStory() == null) continue;

                            CharacterStory characterStory = null;
                            if (characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.MAIN) {
                                characterStory = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterStoryData.getCharacterStory().getName());
                                if (characterStory != null && characterStory.getCharacterSkinController() != null && characterStoryData.getSkinName() != null) {
                                    File skinFile = characterStory.getCharacterSkinController().getSkinFile(characterStoryData.getSkinName());
                                    if (skinFile != null) {
                                        characterStory.getCharacterSkinController().setCurrentSkin(skinFile);
                                    }
                                }
                            } else if (characterStoryData.getCharacterStory().getCharacterType() == CharacterStory.CharacterType.NPC) {
                                characterStory = scene.getNpc(characterStoryData.getCharacterStory().getName());
                            }
                            if (characterStory != null) {
                                characterStoryData.setCharacterStory(characterStory);
                            }
                        }
                    }
                }
                scene.setCameraAngleGroupList(cameraAngleGroupList);
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOG.warn("Failed to read camera angles file: {}", e.getMessage());
        } catch (Exception e) {
            NarrativeCraftMod.LOG.warn("Failed to initialize camera angles: {}", e.getMessage());
        }
    }
}
