package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.runtime.Choice;
import com.bladecoder.ink.runtime.Story;
import com.bladecoder.ink.runtime.StoryException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogLetterEffect;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.*;
import fr.loudo.narrativecraft.screens.choices.ChoicesScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryHandler {

    private final List<CharacterStory> currentCharacters;
    private final List<TypedSoundInstance> typedSoundInstanceList;
    private final InkTagTranslators inkTagTranslators;
    private StorySave save;
    private PlayerSession playerSession;
    private Story story;
    private String currentDialog, currentCharacterTalking;
    private Dialog currentDialogBox;
    private List<Choice> currentChoices;
    private KeyframeCoordinate currentKeyframeCoordinate;
    private boolean isRunning, isDebugMode, OnCutscene, onChoice, isSaving;
    private final List<InkAction> inkActionList;


    public StoryHandler() {
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());
        playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(serverPlayer, null, null);
        currentCharacters = new ArrayList<>();
        isRunning = true;
        inkTagTranslators = new InkTagTranslators(this);
        typedSoundInstanceList = new ArrayList<>();
        inkActionList = new ArrayList<>();
        currentChoices = new ArrayList<>();
        onChoice = false;
        save = NarrativeCraftFile.getSave();
        isSaving = false;
    }

    public StoryHandler(Chapter chapter, Scene scene) {
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());
        playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(serverPlayer, chapter, scene);
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        currentCharacters = new ArrayList<>();
        isRunning = true;
        inkTagTranslators = new InkTagTranslators(this);
        typedSoundInstanceList = new ArrayList<>();
        inkActionList = new ArrayList<>();
        currentChoices = new ArrayList<>();
        onChoice = false;
        save = NarrativeCraftFile.getSave();
        isSaving = false;
    }

    public void start() {
        try {
            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkins();
            for(Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
                for(Scene scene : chapter.getSceneList()) {
                    for(CharacterStory npc : scene.getNpcs()) {
                        NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(npc);
                    }
                }
            }
            if(NarrativeCraftMod.getInstance().getStoryHandler() != null) {
                NarrativeCraftMod.getInstance().getStoryHandler().stop();
            }
            String content = NarrativeCraftFile.getStoryFile();
            story = new Story(content);

            Chapter loadChapter = playerSession.getChapter();
            Scene loadScene = playerSession.getScene();

            Chapter firstChapter = NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst();
            Scene firstScene = firstChapter.getSceneList().getFirst();
            playerSession.setChapter(firstChapter);
            playerSession.setScene(firstScene);

            if(save != null) {
                story.getState().loadJson(save.getInkSave());
                PlayerSession playerSessionFromSave = save.getPlayerSession();
                playerSession.setChapter(playerSessionFromSave.getChapter());
                playerSession.setScene(playerSessionFromSave.getScene());
            }
            if(loadScene != null) {
                story.choosePathString(NarrativeCraftFile.getChapterSceneSneakCase(loadScene));
                playerSession.setChapter(loadChapter);
                playerSession.setScene(loadScene);
                save = null;
            }
            isRunning = true;
            NarrativeCraftMod.getInstance().setStoryHandler(this);
            next();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        isRunning = false;
        for(CharacterStory characterStory : currentCharacters) {
            characterStory.kill();
        }
        for(Playback playback : NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks()) {
            playback.forceStop();
        }
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().clear();
        StoryHandler.changePlayerCutsceneMode(playerSession.getPlayer(), Playback.PlaybackType.PRODUCTION, false);
        for(SimpleSoundInstance simpleSoundInstance : typedSoundInstanceList) {
            Minecraft.getInstance().getSoundManager().stop(simpleSoundInstance);
        }
        inkActionList.clear();
        currentKeyframeCoordinate = null;
        playerSession.reset();
        currentCharacters.clear();
        NarrativeCraftMod.getInstance().setStoryHandler(null);
    }

    public boolean next() {
        try {
            if(!story.canContinue() && currentChoices.isEmpty() && save == null) {
                stop();
                return false;
            }
            if(!currentChoices.isEmpty()) {
                onChoice = true;
                showChoices();
                return false;
            }
            onChoice = false;
            if(save != null) {
                currentDialog = story.getCurrentText();
                boolean isNewScene = story.getCurrentTags().getFirst().equals("on enter") && !story.getCurrentTags().contains("save");
                int breakIndex = 0;
                List<String> oldTags = List.copyOf(story.getCurrentTags());
                for(String tag : story.getCurrentTags()) {
                    breakIndex++;
                    if(isNewScene && tag.equals("on enter") || !isNewScene && tag.equals("save")) break;
                }
                story.getCurrentTags().clear();
                for (int i = breakIndex; i < oldTags.size(); i++) {
                    story.getCurrentTags().add(oldTags.get(i));
                }
                PlayerSession playerSessionFromSave = save.getPlayerSession();
                playerSession.setKeyframeControllerBase(playerSessionFromSave.getKeyframeControllerBase());
                playerSession.setSoloCam(playerSessionFromSave.getSoloCam());
                for(CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
                    characterStoryData.spawn(playerSession.getPlayer().serverLevel());
                    currentCharacters.add(characterStoryData.getCharacterStory());
                }
                for(StorySave.AnimationInfo animationInfo : save.getAnimationInfoList()) {
                    Animation animation = playerSessionFromSave.getScene().getAnimationByName(animationInfo.getName());
                    Playback playback = new Playback(
                            animation,
                            playerSession.getPlayer().serverLevel(),
                            animation.getCharacter(),
                            Playback.PlaybackType.PRODUCTION,
                            animationInfo.wasLooping()
                    );
                    playback.start();
                    currentCharacters.add(playback.getCharacter());
                    inkActionList.add(new AnimationPlayInkAction(this, animation));
                }
                for(StorySave.SubsceneInfo subsceneInfo : save.getSubsceneInfoList()) {
                    Subscene subscene = playerSessionFromSave.getScene().getSubsceneByName(subsceneInfo.getName());
                    subscene.start(playerSession.getPlayer().serverLevel(), Playback.PlaybackType.PRODUCTION, subsceneInfo.wasLooping());
                    for(Playback playback : subscene.getPlaybackList()) {
                        currentCharacters.add(playback.getCharacter());
                    }
                    inkActionList.add(new SubscenePlayInkAction(this, subscene));
                }
                if(save.getDialogSaveData() != null) {
                    StorySave.DialogSaveData dialogSaveData = save.getDialogSaveData();
                    Entity entity = null;
                    for(CharacterStory characterStory : currentCharacters) {
                        if(characterStory.getName().equals(dialogSaveData.getCharacterName())) {
                            entity = characterStory.getEntity();
                        }
                    }
                    currentCharacterTalking = dialogSaveData.getCharacterName();
                    currentDialog = dialogSaveData.getText();
                    currentDialogBox = new Dialog(
                            entity,
                            dialogSaveData.getText(),
                            dialogSaveData.getTextColor(),
                            dialogSaveData.getBackgroundColor(),
                            dialogSaveData.getPaddingX(),
                            dialogSaveData.getPaddingY(),
                            dialogSaveData.getScale(),
                            dialogSaveData.getLetterSpacing(),
                            dialogSaveData.getGap(),
                            dialogSaveData.getMaxWidth()
                    );
                    currentDialogBox.setInstantSpawn(true);
                    currentDialogBox.setDialogOffset(dialogSaveData.getOffset());
                    currentDialogBox.setCharacterName(dialogSaveData.getCharacterName());
                    showDialog();
                }
            } else {
                currentDialog = story.Continue();
            }
            currentChoices = story.getCurrentChoices();
            if(inkTagTranslators.executeCurrentTags()) {
                if(!currentChoices.isEmpty()) {
                    showChoices();
                } else {
                    if(!currentCharacters.isEmpty()) {
                        showDialog();
                    }
                }
            }
            save = null;
            if(story.canContinue() && currentCharacters.isEmpty() && playerSession.getSoloCam() == null && playerSession.getKeyframeControllerBase() == null) {
                stop();
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§c" + Translation.message("story.load.scene.fail").getString()),
                        false
                );
            }
            StoryHandler.changePlayerCutsceneMode(playerSession.getPlayer(), Playback.PlaybackType.PRODUCTION, playerSession.getSoloCam() != null || playerSession.getKeyframeControllerBase() != null);
        } catch (StoryException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void showChoices() {
        if(!currentChoices.isEmpty()) {
            if(currentDialogBox != null) {
                currentDialogBox.endDialog();
            }
            ChoicesScreen choicesScreen = new ChoicesScreen(currentChoices);
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(choicesScreen));
        }
    }

    public boolean isFinished() {
        return !story.canContinue() && currentChoices.isEmpty() && currentDialog.isEmpty();
    }

    public void showDialog() {
        String[] splitDialog = currentDialog.split(":");
        if(splitDialog.length < 2 || onChoice) return;
        String characterName = splitDialog[0].trim();
        String dialogContent = splitDialog[1].trim();

        if (dialogContent.startsWith("\"") && dialogContent.endsWith("\"")) {
            dialogContent = dialogContent.substring(1, dialogContent.length() - 1);
        }

        ParsedDialog parsed = parseDialogContent(dialogContent);

        if (characterName.equalsIgnoreCase(currentCharacterTalking) && currentDialogBox != null) {
            currentDialogBox.getDialogAnimationScrollText().setText(parsed.cleanedText);
            currentDialogBox.reset();
        } else {
            if (currentDialogBox != null) {
                currentDialogBox.endDialog();
                return;
            } else {
                CharacterStory currentCharacter = currentCharacters.stream()
                        .filter(characterStory -> characterStory.getName().equalsIgnoreCase(characterName))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Character not found: " + characterName));
                currentDialogBox = new Dialog(
                        currentCharacter.getEntity(),
                        parsed.cleanedText,
                        -1, 1, 3,
                        4, 0.8F, 0.1F, 10,
                        90
                );
                currentDialogBox.setCharacterName(currentCharacter.getName());
            }
            currentCharacterTalking = characterName;
        }
        applyTextEffects(parsed.effects);
    }

    public static List<InkAction.ErrorLine> validateStory() {
        List<InkAction.ErrorLine> errorLineList = new ArrayList<>();
        Pattern inlineTagPattern = Pattern.compile("#[^#]*?(?=\\s*#|$)");
        Pattern commentPattern = Pattern.compile("^\\s*//");

        for (Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
            for (Scene scene : chapter.getSceneList()) {
                List<String> lines = NarrativeCraftFile.readSceneLines(scene);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String rawLine = line;

                    if (commentPattern.matcher(line).find()) continue;

                    line = line.replaceFirst("^\\s+", "");

                    if (i + 1 == 2 && !line.equals("# on enter")) {
                        errorLineList.add(
                                new InkAction.ErrorLine(
                                        i + 1,
                                        scene,
                                        Translation.message("validation.on_enter").getString(),
                                        line
                                )
                        );
                        break;
                    }

                    Matcher matcher = inlineTagPattern.matcher(line);
                    while (matcher.find()) {
                        String tag = matcher.group().trim();
                        if (tag.startsWith("#")) {
                            tag = tag.substring(1).trim();
                        }

                        String[] command = tag.split(" ");
                        InkAction.InkTagType tagType = InkAction.getInkActionTypeByTag(tag);
                        if (tagType != null) {
                            InkAction inkAction = getInkAction(tagType);
                            if (inkAction != null) {
                                InkAction.ErrorLine errorLine = inkAction.validate(command, i + 1, matcher.group(), scene);
                                if (errorLine != null) {
                                    errorLineList.add(errorLine);
                                }
                            }
                        }
                    }
                }
            }
        }
        return errorLineList;
    }




    private static @Nullable InkAction getInkAction(InkAction.InkTagType tagType) {
        InkAction inkAction = null;
        switch (tagType) {
            case CUTSCENE -> inkAction = new CutsceneInkAction();
            case CAMERA_ANGLE ->  inkAction = new CameraAngleInkAction();
            case SONG_SFX_START, SONG_SFX_STOP, SOUND_STOP_ALL -> inkAction = new SongSfxInkAction();
            case FADE -> inkAction = new FadeScreenInkAction();
            case WAIT -> inkAction = new WaitInkAction();
            case SUBSCENE -> inkAction = new SubscenePlayInkAction();
            case ANIMATION -> inkAction = new AnimationPlayInkAction();
            case DAYTIME -> inkAction = new ChangeDayTimeInkAction();
            case WEATHER -> inkAction = new WeatherChangeInkAction();
            case MINECRAFT_COMMAND -> inkAction = new CommandMinecraftInkAction();
            case DIALOG_VALUES -> inkAction = new DialogValuesInkAction();
        }
        return inkAction;
    }

    /**
     * This translates text effects such as waiving and shaking into effects in-game, as well as giving a cleaned dialog text
     * @param rawText
     * @return A ParsedDialog instance
     */
    private ParsedDialog parseDialogContent(String rawText) {
        List<TextEffect> effects = new ArrayList<>();
        StringBuilder cleanText = new StringBuilder();

        Pattern pattern = Pattern.compile("\\[(\\w+)((?:\\s+\\w+=\\S+)*?)\\](.*?)\\[/\\1\\]");
        Matcher matcher = pattern.matcher(rawText);

        int currentIndex = 0;

        while (matcher.find()) {
            cleanText.append(rawText, currentIndex, matcher.start());

            String effectName = matcher.group(1);
            String paramString = matcher.group(2).trim();
            String innerText = matcher.group(3);

            int effectStart = cleanText.length();
            cleanText.append(innerText);
            int effectEnd = cleanText.length();

            Map<String, String> params = new HashMap<>();
            if (!paramString.isEmpty()) {
                String[] parts = paramString.split("\\s+");
                for (String part : parts) {
                    String[] kv = part.split("=");
                    if (kv.length == 2) {
                        params.put(kv[0], kv[1]);
                    }
                }
            }

            DialogAnimationType type;
            try {
                type = DialogAnimationType.valueOf(effectName.toUpperCase());
            } catch (IllegalArgumentException e) {
                continue;
            }

            effects.add(new TextEffect(type, effectStart, effectEnd, params));
            currentIndex = matcher.end();
        }

        cleanText.append(rawText.substring(currentIndex));

        return new ParsedDialog(cleanText.toString(), effects);
    }

    private void applyTextEffects(List<TextEffect> effects) {
        if(effects.isEmpty()) {
            DialogLetterEffect dialogEffect = new DialogLetterEffect(
                    DialogAnimationType.NONE
            );
            currentDialogBox.getDialogAnimationScrollText().setDialogLetterEffect(dialogEffect);
            return;
        }
        for (TextEffect effect : effects) {
            double time = Double.parseDouble(effect.parameters.getOrDefault("time", "1"));
            float force = Float.parseFloat(effect.parameters.getOrDefault("force", "0"));

            switch (effect.type) {
                case WAVING -> {
                    time = 0.3;
                    force = 1f;
                }
                case SHAKING -> {
                    time = 0.02;
                    force = 0.5f;
                }
            }

            DialogLetterEffect dialogEffect = new DialogLetterEffect(
                    effect.type,
                    (long) (time * 1000L),
                    force,
                    effect.startIndex,
                    effect.endIndex
            );
            currentDialogBox.getDialogAnimationScrollText().setDialogLetterEffect(dialogEffect);
        }
    }

    public TypedSoundInstance playSound(SoundEvent sound, float volume, float pitch, boolean loop, SongSfxInkAction.SoundType soundType) {
        TypedSoundInstance soundInstance = new TypedSoundInstance(sound.location(), SoundSource.MASTER, volume, pitch, SoundInstance.createUnseededRandom(), loop, soundType);
        typedSoundInstanceList.add(soundInstance);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
        return soundInstance;
    }

    public void stopSound(SoundEvent sound) {
        for(SimpleSoundInstance simpleSoundInstance : typedSoundInstanceList) {
            if(simpleSoundInstance.getSound().getLocation().getPath().equals(sound.location().getPath())) {
                Minecraft.getInstance().getSoundManager().stop(simpleSoundInstance);
            }
        }
    }

    public void stopAllSoundByType(SongSfxInkAction.SoundType soundType) {
        List<TypedSoundInstance> typedSoundInstances = typedSoundInstanceList.stream()
                .filter(s -> s.getSoundType() == soundType)
                .toList();
        for(TypedSoundInstance typedSoundInstance : typedSoundInstances) {
            Minecraft.getInstance().getSoundManager().stop(typedSoundInstance);
        }
    }

    public void stopAllSound() {
        for(SimpleSoundInstance simpleSoundInstance : typedSoundInstanceList) {
            Minecraft.getInstance().getSoundManager().stop(simpleSoundInstance);
        }
    }


    public static void changePlayerCutsceneMode(ServerPlayer player, Playback.PlaybackType playbackType, boolean state) {
        NarrativeCraftMod.getInstance().setCutsceneMode(state);
        if(state) {
            player.setGameMode(GameType.SPECTATOR);
        } else {
            if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
                PlayerSession playerSession1 = Utils.getSessionOrNull(player);
                if(playerSession1 != null && playerSession1.getKeyframeControllerBase() == null) {
                    player.setGameMode(GameType.CREATIVE);
                }
            } else if(playbackType == Playback.PlaybackType.PRODUCTION) {
                player.setGameMode(GameType.ADVENTURE);
            }
        }
    }


    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public List<CharacterStory> getCurrentCharacters() {
        return currentCharacters;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Story getStory() {
        return story;
    }

    public Dialog getCurrentDialogBox() {
        return currentDialogBox;
    }

    public String getCurrentDialog() {
        return currentDialog;
    }

    public void setCurrentDialogBox(Dialog currentDialogBox) {
        this.currentDialogBox = currentDialogBox;
    }

    public KeyframeCoordinate getCurrentKeyframeCoordinate() {
        return currentKeyframeCoordinate;
    }

    public void setCurrentKeyframeCoordinate(KeyframeCoordinate currentKeyframeCoordinate) {
        this.currentKeyframeCoordinate = currentKeyframeCoordinate;
    }

    public InkTagTranslators getInkTagTranslators() {
        return inkTagTranslators;
    }

    public List<TypedSoundInstance> getTypedSoundInstanceList() {
        return typedSoundInstanceList;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    public List<InkAction> getInkActionList() {
        return inkActionList;
    }

    public List<Choice> getCurrentChoices() {
        return currentChoices;
    }

    public boolean isSaving() {
        return isSaving;
    }

    public void setSaving(boolean saving) {
        isSaving = saving;
    }

    public void save() {
        NarrativeCraftFile.writeSave(this);
        isSaving = true;
        StorySave.startTimeSaveIcon = System.currentTimeMillis();
    }

    public enum FadeCurrentState {
        FADE_IN,
        STAY,
        FADE_OUT
    }

    private static class TextEffect {
        public DialogAnimationType type;
        public int startIndex;
        public int endIndex;
        public Map<String, String> parameters;

        public TextEffect(DialogAnimationType type, int startIndex, int endIndex, Map<String, String> parameters) {
            this.type = type;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.parameters = parameters;
        }
    }

    private static class ParsedDialog {
        public String cleanedText;
        public List<TextEffect> effects;

        public ParsedDialog(String cleanedText, List<TextEffect> effects) {
            this.cleanedText = cleanedText;
            this.effects = effects;
        }
    }

}
