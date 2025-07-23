package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.runtime.Choice;
import com.bladecoder.ink.runtime.Story;
import com.bladecoder.ink.runtime.StoryException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.NarrativeUserOptions;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.mixin.fields.PlayerListFields;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.*;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogLetterEffect;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.SongSfxInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.enums.InkTagType;
import fr.loudo.narrativecraft.narrative.story.inkAction.validation.ErrorLine;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.screens.choices.ChoicesScreen;
import fr.loudo.narrativecraft.screens.components.CrashScreen;
import fr.loudo.narrativecraft.screens.credits.CreditsScreen;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryHandler {

    private final NarrativeUserOptions narrativeUserOptions = NarrativeCraftMod.getInstance().getNarrativeUserOptions();
    private final PlayerSession playerSession;
    private final List<CharacterStory> currentCharacters;
    private final List<TypedSoundInstance> typedSoundInstanceList;
    private final InkTagTranslators inkTagTranslators;
    private StorySave save;
    private Story story;
    private String currentDialog, currentCharacterTalking;
    private DialogImpl currentDialogBox;
    private List<Choice> currentChoices;
    private KeyframeCoordinate currentKeyframeCoordinate;
    private boolean isDebugMode, isLoading, isSaving;
    private DialogData globalDialogValue;
    private final List<InkAction> inkActionList;

    public StoryHandler() {
        playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        currentCharacters = new ArrayList<>();
        inkTagTranslators = new InkTagTranslators(this);
        typedSoundInstanceList = new ArrayList<>();
        inkActionList = new ArrayList<>();
        save = NarrativeCraftFile.getSave();
        isSaving = false;
    }

    public StoryHandler(Chapter chapter, Scene scene) {
        playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        currentCharacters = new ArrayList<>();
        inkTagTranslators = new InkTagTranslators(this);
        typedSoundInstanceList = new ArrayList<>();
        inkActionList = new ArrayList<>();
        save = NarrativeCraftFile.getSave();
        isSaving = false;
    }

    public void start() {
        if(NarrativeCraftMod.getInstance().getChapterManager().getChapters().isEmpty()) return;
        try {
            if(NarrativeCraftMod.getInstance().getStoryHandler() != null) {
                NarrativeCraftMod.getInstance().getStoryHandler().stop(true);
            }
            NarrativeCraftMod.getInstance().setStoryHandler(this);
            Minecraft.getInstance().options.hideGui = true;
            inkActionList.clear();
            globalDialogValue = new DialogData(DialogData.globalDialogData);

            Chapter loadChapter = playerSession.getChapter();
            Scene loadScene = playerSession.getScene();

            NarrativeCraftMod.getInstance().getCharacterManager().reloadSkins();
            for(Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
                for(Scene scene : chapter.getSceneList()) {
                    for(CharacterStory npc : scene.getNpcs()) {
                        NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(npc);
                    }
                }
            }
            String content = NarrativeCraftFile.getStoryContent();
            story = new Story(content);

            if(save != null) {
                story.getState().loadJson(save.getInkSave());
                if(loadScene == null) {
                    PlayerSession playerSessionFromSave = save.getPlayerSession();
                    playerSession.setChapter(playerSessionFromSave.getChapter());
                    playerSession.setScene(playerSessionFromSave.getScene());
                    for(String tag : save.getTagList()) {
                        inkTagTranslators.executeTag(tag);
                    }
                }
            }
            if(loadScene != null) {
                story.choosePathString(NarrativeCraftFile.getChapterSceneSnakeCase(loadScene));
                playerSession.setChapter(loadChapter);
                playerSession.setScene(loadScene);
                save = null;
            } else {
                Chapter firstChapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(1);
                if(NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst().getSceneList().isEmpty()) return;
                Scene firstScene = firstChapter.getSortedSceneList().getFirst();
                playerSession.setChapter(firstChapter);
                playerSession.setScene(firstScene);
            }
            if(next()) {
                if(!isDebugMode) {
                    NarrativeCraftFile.writeSave(this, true);
                }
            }
        } catch (Exception e) {
            crash(e, false);
        }
    }

    public void stop(boolean force) {
        if(!isRunning()) return;
        if(!force) {
            if(!isDebugMode) {
                CreditsScreen creditsScreen = new CreditsScreen(false, !NarrativeCraftMod.getInstance().getNarrativeUserOptions().FINISHED_STORY);
                NarrativeCraftMod.getInstance().getNarrativeUserOptions().FINISHED_STORY = true;
                NarrativeCraftFile.updateUserOptions();
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(creditsScreen));
            }
        }
        for(CharacterStory characterStory : currentCharacters) {
            characterStory.kill();
        }
        for(Playback playback : NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks()) {
            playback.forceStop();
        }
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().clear();
        StoryHandler.changePlayerCutsceneMode(Playback.PlaybackType.PRODUCTION, false);
        for(SimpleSoundInstance simpleSoundInstance : typedSoundInstanceList) {
            Minecraft.getInstance().getSoundManager().stop(simpleSoundInstance);
        }
        currentKeyframeCoordinate = null;
        inkActionList.clear();
        currentCharacters.clear();
        playerSession.reset();
        Minecraft.getInstance().options.hideGui = false;
        NarrativeCraftMod.getInstance().setStoryHandler(null);
    }

    public boolean next() {
        try {
            if(!story.canContinue() && story.getCurrentChoices().isEmpty() && save == null) {
                stop(false);
                return false;
            }
            if(!story.getCurrentChoices().isEmpty() && save == null) {
                showChoices();
                return false;
            }
            if(save != null) {
                currentDialog = story.getCurrentText();
                boolean isNewScene = story.getCurrentTags().contains("on enter") && !story.getCurrentTags().contains("save");
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
                    if(!characterStoryData.isOnlyTemplate()) {
                        characterStoryData.spawn(Utils.getServerLevel());
                        currentCharacters.add(characterStoryData.getCharacterStory());
                    }
                }
                if(save.getDialogSaveData() != null) {
                    DialogData dialogSaveData = save.getDialogSaveData();
                    globalDialogValue = dialogSaveData;
                    if(!currentCharacters.isEmpty()) {
                        if(dialogSaveData.getCharacterName() == null && dialogSaveData.getText() == null) { // If dialog save data is only global parameters and not last dialog saved
                            showDialog();
                        } else {
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
                                    parseDialogContent(dialogSaveData.getText()).cleanedText,
                                    dialogSaveData.getTextColor(),
                                    dialogSaveData.getBackgroundColor(),
                                    dialogSaveData.getPaddingX(),
                                    dialogSaveData.getPaddingY(),
                                    dialogSaveData.getScale(),
                                    dialogSaveData.getLetterSpacing(),
                                    dialogSaveData.getGap(),
                                    dialogSaveData.getMaxWidth(),
                                    dialogSaveData.getOffset()
                            );
                            currentDialogBox.setUnSkippable(dialogSaveData.isUnSkippable());
                            currentDialogBox.setForcedEndTime(dialogSaveData.getEndForceEndTime());
                            ((Dialog)currentDialogBox).setCharacterName(dialogSaveData.getCharacterName());
                        }
                    }
                }
            } else {
                currentDialog = story.Continue();
            }
            if(playerSession.getChapter() == null || playerSession.getScene() == null) {
                initChapterSceneSession();
            }
            if(inkTagTranslators.executeCurrentTags()) {
                if(!story.getCurrentChoices().isEmpty() && currentDialog.isEmpty()) {
                    showChoices();
                } else {
                    showDialog();
                }
            } else {
                if(currentDialogBox != null) currentDialogBox.endDialogAndDontSkip();
            }
            if(NarrativeCraftMod.getInstance().getStoryHandler() == null) return false;
            save = null;
            if(story.canContinue() && currentCharacters.isEmpty() && playerSession.getSoloCam() == null && playerSession.getKeyframeControllerBase() == null) {
                stop(true);
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal(Translation.message("story.load.scene.fail").getString()).withStyle(ChatFormatting.RED),
                        false
                );
            }
            StoryHandler.changePlayerCutsceneMode(Playback.PlaybackType.PRODUCTION, playerSession.getSoloCam() != null || playerSession.getKeyframeControllerBase() != null);
        } catch (StoryException e) {
            crash(new Exception(e.getMessage() + String.format("\nCrash in Chapter %s Scene %s.", playerSession.getChapter().getIndex(), playerSession.getScene().getName())), true);
            return false;
        } catch (Exception e) {
            crash(e, false);
            return false;
        }
        return true;
    }

    public void crash(Exception exception, boolean creatorFault) {
        stop(true);
        Component message;
        CrashReport report = new CrashReport(exception.getMessage(), exception);
        Minecraft.saveReport(NarrativeCraftFile.mainDirectory, report);
        if(!isDebugMode) {
            CrashScreen crashScreen = new CrashScreen(creatorFault, report);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> minecraft.setScreen(crashScreen));
        } else {
            if(creatorFault) {
                message = Translation.message("user.crash.his-fault").withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(exception.getMessage())))).withStyle(ChatFormatting.RED);
            } else {
                message = Translation.message("user.crash.not-his-fault").withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(report.getSaveFile()))).withStyle(ChatFormatting.RED);
            }
            Minecraft.getInstance().player.displayClientMessage(
                    message,
                    false
            );
        }
    }

    public void showChoices() {
        if(!story.getCurrentChoices().isEmpty()) {
            if(currentDialogBox != null) {
                currentDialogBox.endDialogAndDontSkip();
            }
            ChoicesScreen choicesScreen = new ChoicesScreen(story.getCurrentChoices(), true);
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(choicesScreen));
        }
    }

    public void choiceChoiceIndex(int index) {
        try {
            story.chooseChoiceIndex(index);
            story.getCurrentChoices().clear();
            next();
        } catch (Exception ignored) {}
    }

    public boolean isFinished() {
        return !story.canContinue() && story.getCurrentChoices().isEmpty() && currentDialog.isEmpty();
    }

    public void showDialog() {

        ParsedDialog parsed = parseDialogContent(currentDialog);

        if (parsed.characterName.equalsIgnoreCase(currentCharacterTalking) && currentDialogBox != null) {
            currentDialogBox.getDialogAnimationScrollText().setText(parsed.cleanedText);
            currentDialogBox.reset();
        } else {
            if (currentDialogBox != null && (!currentDialogBox.isDialogAutoSkipped() || narrativeUserOptions.AUTO_SKIP)) {
                currentDialogBox.endDialog();
                return;
            } else {
                if(!parsed.characterName.isEmpty()) {
                    CharacterStory currentCharacter = currentCharacters.stream()
                            .filter(characterStory -> characterStory.getName().equalsIgnoreCase(parsed.characterName))
                            .findFirst()
                            .orElse(null);
                    if(currentCharacter == null) {
                        crash(new Exception(Translation.message("user.crash.character_not_found", parsed.characterName, playerSession.getChapter().getIndex(), playerSession.getScene().getName()).getString()), true);
                        return;
                    }
                    currentDialogBox = new Dialog(
                            currentCharacter.getEntity(),
                            parsed.cleanedText,
                            globalDialogValue.getTextColor(), globalDialogValue.getBackgroundColor(), globalDialogValue.getPaddingX(),
                            globalDialogValue.getPaddingY(), globalDialogValue.getScale(), globalDialogValue.getLetterSpacing(), globalDialogValue.getGap(),
                            globalDialogValue.getMaxWidth(), globalDialogValue.getOffset()
                    );
                    ((Dialog)currentDialogBox).getDialogEntityBobbing().setNoiseShakeStrength(globalDialogValue.getBobbingNoiseShakeStrength());
                    ((Dialog)currentDialogBox).getDialogEntityBobbing().setNoiseShakeSpeed(globalDialogValue.getBobbingNoiseShakeSpeed());
                    ((Dialog)currentDialogBox).setCharacterName(currentCharacter.getName());
                } else {
                    currentDialogBox = new Dialog2d(
                            parsed.cleanedText,
                            400,
                            90,
                            (int) globalDialogValue.getPaddingX(),
                            (int) globalDialogValue.getPaddingY(),
                            1.4f,
                            (int) globalDialogValue.getLetterSpacing(),
                            (int) globalDialogValue.getGap(),
                            30,
                            globalDialogValue.getTextColor(),
                            globalDialogValue.getBackgroundColor()
                    );
                }
                currentDialogBox.setUnSkippable(globalDialogValue.isUnSkippable());
                currentDialogBox.setForcedEndTime(globalDialogValue.getEndForceEndTime());
            }
            currentCharacterTalking = parsed.characterName;
        }
        if(globalDialogValue.getEndForceEndTime() == 0) {
            if(narrativeUserOptions.AUTO_SKIP) {
                currentDialogBox.setForcedEndTime(currentDialog.replaceAll("\\s+", "").length() * 80L);
            }
        }
        applyTextEffects(parsed.effects);
    }

    public static List<ErrorLine> validateStory() {
        List<ErrorLine> errorLineList = new ArrayList<>();
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

                    if (i + 1 == 2 && !line.startsWith("#") && !line.contains("on enter")) {
                        errorLineList.add(
                                new ErrorLine(
                                        i + 1,
                                        scene,
                                        Translation.message("validation.on_enter").getString(),
                                        line,
                                        false
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
                        InkTagType tagType = InkTagType.resolveType(tag);
                        if (tagType != null) {
                            InkAction inkAction = null;
                            if(tagType == InkTagType.EMOTE) {
                                if(!Services.PLATFORM.isModLoaded("emotecraft")) {
                                    errorLineList.add(new ErrorLine(
                                            i + 1,
                                            scene,
                                            Translation.message("validation.emotecraft").getString(),
                                            matcher.group(),
                                            false
                                    ));
                                }
                            } else {
                                inkAction = tagType.getDefaultInstance();
                            }
                            if (inkAction != null) {
                                ErrorLine errorLine = inkAction.validate(command, i + 1, matcher.group(), scene);
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

    public void initChapterSceneSession() {
        if(story.getState().getCurrentKnot() == null) return;
        String[] chapterSceneName = story.getState().getCurrentKnot().split("_");
        int chapterIndex = Integer.parseInt(chapterSceneName[1]);
        List<String> splitSceneName = Arrays.stream(chapterSceneName).toList().subList(2, chapterSceneName.length);
        String sceneName = String.join(" ", splitSceneName);
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        Scene scene = chapter.getSceneByName(sceneName);
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
    }

    public boolean characterInStory(CharacterStory characterStory) {
        for(CharacterStory characterStory1 : currentCharacters) {
            if(characterStory.getName().equals(characterStory1.getName())) {
                return true;
            }
        }
        return false;
    }

    public void addCharacter(CharacterStory characterStory) {
        if(!characterInStory(characterStory)) currentCharacters.add(characterStory);
    }

    public void removeCharacter(CharacterStory characterStory) {
        if(characterInStory(characterStory)) {
            if(characterStory.getEntity() != null) {
                characterStory.getEntity().remove(Entity.RemovalReason.KILLED);
                if(characterStory.getEntity() instanceof FakePlayer fakePlayer) {
                    NarrativeCraftMod.server.getPlayerList().remove(fakePlayer);
                    ((PlayerListFields)NarrativeCraftMod.server.getPlayerList()).getPlayersByUUID().remove(fakePlayer.getUUID());
                }
            }
        }
        currentCharacters.remove(characterStory);
    }

    /**
     * This translates text effects such as waiving and shaking into effects in-game, as well as giving a cleaned dialog text
     * @param rawText
     * @return A ParsedDialog instance
     */
    private ParsedDialog parseDialogContent(String rawText) {

        String characterName = "";
        String dialogContent = rawText;

        String[] splitText = rawText.split(":");
        if(splitText.length > 1) {
            characterName = splitText[0].trim();
            dialogContent = splitText[1].trim();
        }

        if (dialogContent.startsWith("\"") && dialogContent.endsWith("\"")) {
            dialogContent = dialogContent.substring(1, dialogContent.length() - 1);
        }

        List<TextEffect> effects = new ArrayList<>();
        StringBuilder cleanText = new StringBuilder();

        Pattern pattern = Pattern.compile("\\[(\\w+)((?:\\s+\\w+=\\S+)*?)\\](.*?)\\[/\\1\\]");
        Matcher matcher = pattern.matcher(dialogContent);

        int currentIndex = 0;

        while (matcher.find()) {
            cleanText.append(dialogContent, currentIndex, matcher.start());

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
        dialogContent = dialogContent.replace("\n", "");
        cleanText.append(dialogContent.substring(currentIndex));

        return new ParsedDialog(cleanText.toString(), effects, characterName);
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
        TypedSoundInstance soundInstance = new TypedSoundInstance(sound.location(), SoundSource.MASTER, volume, pitch, loop, soundType);
        typedSoundInstanceList.add(soundInstance);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
        return soundInstance;
    }

    public void stopSound(SoundEvent sound) {
        for(SimpleSoundInstance simpleSoundInstance : typedSoundInstanceList) {
            String soundInstancePath = simpleSoundInstance.getSound().getLocation().getPath().replace("/", ".");
            if(soundInstancePath.equals(sound.location().getPath())) {
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


    public static void changePlayerCutsceneMode(Playback.PlaybackType playbackType, boolean state) {
        NarrativeCraftMod.getInstance().setCutsceneMode(state);
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());
        if(state) {
            serverPlayer.setGameMode(GameType.SPECTATOR);
        } else {
            if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
                serverPlayer.setGameMode(GameType.CREATIVE);
            } else if(playbackType == Playback.PlaybackType.PRODUCTION) {
                serverPlayer.setGameMode(GameType.ADVENTURE);
            }
        }
    }

    public CharacterStory getCharacter(String name) {
        for(CharacterStory characterStory : currentCharacters) {
            if(characterStory.getName().equalsIgnoreCase(name)) {
                return characterStory;
            }
        }
        return null;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public List<CharacterStory> getCurrentCharacters() {
        return currentCharacters;
    }

    public boolean isRunning() {
        return story != null;
    }

    public Story getStory() {
        return story;
    }

    public DialogImpl getCurrentDialogBox() {
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

    public boolean isSaving() {
        return isSaving;
    }

    public void setSaving(boolean saving) {
        isSaving = saving;
    }

    public DialogData getGlobalDialogValue() {
        return globalDialogValue;
    }

    public void setGlobalDialogValue(DialogData globalDialogValue) {
        this.globalDialogValue = globalDialogValue;
    }

    public String getCurrentCharacterTalking() {
        return currentCharacterTalking;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public void save(boolean newScene) {
        if(!isDebugMode) {
            if(!NarrativeCraftFile.writeSave(this, newScene)) {
                return;
            }
        }
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
        public String characterName;

        public ParsedDialog(String cleanedText, List<TextEffect> effects, String characterName) {
            this.cleanedText = cleanedText;
            this.effects = effects;
            this.characterName = characterName;
        }
    }

}
