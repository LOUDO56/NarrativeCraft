package fr.loudo.narrativecraft.narrative.story;

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

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryHandler {

    private static final long AUTO_SKIP_MULTIPLIER = 80L;
    private static final int FIRST_CHAPTER_INDEX = 1;

    private final NarrativeUserOptions narrativeUserOptions;
    private final PlayerSession playerSession;
    private final InkTagTranslators inkTagTranslators;

    private final List<CharacterStory> currentCharacters;
    private final List<TypedSoundInstance> typedSoundInstanceList;
    private final List<InkAction> inkActionList;

    private StorySave save;
    private Story story;
    private DialogData globalDialogValue;

    private String currentDialog;
    private String currentCharacterTalking;
    private DialogImpl currentDialogBox;

    private KeyframeCoordinate currentKeyframeCoordinate;

    private boolean isDebugMode;
    private boolean isLoading;
    private boolean isSaving;

    public StoryHandler() {
        this.narrativeUserOptions = NarrativeCraftMod.getInstance().getNarrativeUserOptions();
        this.playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        this.currentCharacters = new ArrayList<>();
        this.inkTagTranslators = new InkTagTranslators(this);
        this.typedSoundInstanceList = new ArrayList<>();
        this.inkActionList = new ArrayList<>();
        this.save = NarrativeCraftFile.getSave();
        this.isSaving = false;
    }

    public StoryHandler(Chapter chapter, Scene scene) {
        this();
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
    }

    public void start() {
        if (isChaptersEmpty()) return;

        try {
            initializeStory();
            loadStoryState();

            if (next() && !isDebugMode) {
                NarrativeCraftFile.writeSave(this, true);
            }
        } catch (Exception e) {
            crash(e, false);
        }
    }

    public void stop(boolean force) {
        if (!isRunning()) return;

        if (!force) {
            showCreditsScreen();
        }

        cleanup();
    }

    public boolean next() {
        try {
            if (shouldStopStory()) {
                stop(false);
                return false;
            }

            if (shouldShowChoices()) {
                showChoices();
                return false;
            }

            processStoryContent();
            updatePlayerSession();
            executeInkTags();

            save = null;
            handleSceneValidation();
            updateCutsceneMode();

        } catch (StoryException e) {
            crash(createStoryException(e), true);
            return false;
        } catch (Exception e) {
            crash(e, false);
            return false;
        }

        return true;
    }

    public void crash(Exception exception, boolean creatorFault) {
        stop(true);

        CrashReport report = new CrashReport(exception.getMessage(), exception);
        if(!creatorFault) {
            Minecraft.saveReport(NarrativeCraftFile.mainDirectory, report);
        }

        if (isDebugMode) {
            handleDebugModeCrash(exception, creatorFault, report);
        } else {
            showCrashScreen(creatorFault, report);
        }
    }

    public void showChoices() {
        if (hasCurrentChoices()) {
            endCurrentDialog();

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

    public void showDialog() {
        ParsedDialog parsed = parseDialogContent(currentDialog);

        validateCurrentDialog();

        if (shouldReuseDialog(parsed)) {
            reuseExistingDialog(parsed);
        } else {
            if (shouldEndCurrentDialog()) {
                currentDialogBox.endDialog();
                return;
            }

            createNewDialog(parsed);
        }

        updateCurrentCharacterTalking(parsed.characterName);
        configureAutoSkip();
        applyTextEffects(parsed.effects);
    }

    public void addCharacter(CharacterStory characterStory) {
        if (!characterInStory(characterStory)) {
            currentCharacters.add(characterStory);
        }
    }

    public void removeCharacter(CharacterStory characterStory) {
        if (characterInStory(characterStory)) {
            destroyCharacterEntity(characterStory);
        }
        currentCharacters.remove(characterStory);
    }

    public boolean characterInStory(CharacterStory characterStory) {
        return currentCharacters.stream()
                .anyMatch(character -> character.getName().equals(characterStory.getName()));
    }

    public CharacterStory getCharacter(String name) {
        return currentCharacters.stream()
                .filter(character -> character.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public TypedSoundInstance playSound(SoundEvent sound, float volume, float pitch, boolean loop, SongSfxInkAction.SoundType soundType) {
        TypedSoundInstance soundInstance = new TypedSoundInstance(
                sound.location(), SoundSource.MASTER, volume, pitch, loop, soundType
        );
        typedSoundInstanceList.add(soundInstance);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
        return soundInstance;
    }

    public void stopSound(SoundEvent sound) {
        typedSoundInstanceList.stream()
                .filter(instance -> matchesSoundEvent(instance, sound))
                .forEach(instance -> Minecraft.getInstance().getSoundManager().stop(instance));
    }

    public void stopAllSoundByType(SongSfxInkAction.SoundType soundType) {
        typedSoundInstanceList.stream()
                .filter(instance -> instance.getSoundType() == soundType)
                .forEach(instance -> Minecraft.getInstance().getSoundManager().stop(instance));
    }

    public void stopAllSound() {
        typedSoundInstanceList.forEach(instance ->
                Minecraft.getInstance().getSoundManager().stop(instance));
    }

    public static List<ErrorLine> validateStory() {
        List<ErrorLine> errorLineList = new ArrayList<>();
        Pattern inlineTagPattern = Pattern.compile("#[^#]*?(?=\\s*#|$)");
        Pattern commentPattern = Pattern.compile("^\\s*//");

        for (Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
            for (Scene scene : chapter.getSceneList()) {
                validateScene(scene, errorLineList, inlineTagPattern, commentPattern);
            }
        }

        return errorLineList;
    }

    public void save(boolean newScene) {
        if (!isDebugMode && NarrativeCraftFile.writeSave(this, newScene)) {
            isSaving = true;
            StorySave.startTimeSaveIcon = System.currentTimeMillis();
        }
    }

    public static void changePlayerCutsceneMode(Playback.PlaybackType playbackType, boolean state) {
        NarrativeCraftMod.getInstance().setCutsceneMode(state);
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());

        if (state) {
            serverPlayer.setGameMode(GameType.SPECTATOR);
        } else {
            GameType gameMode = (playbackType == Playback.PlaybackType.DEVELOPMENT)
                    ? GameType.CREATIVE
                    : GameType.ADVENTURE;
            serverPlayer.setGameMode(gameMode);
        }
    }

    private boolean isChaptersEmpty() {
        return NarrativeCraftMod.getInstance().getChapterManager().getChapters().isEmpty();
    }

    public void initChapterSceneSession() {
        if (story.getState().getCurrentKnot() == null) return;

        String[] chapterSceneName = story.getState().getCurrentKnot().split("_");
        int chapterIndex = Integer.parseInt(chapterSceneName[1]);

        List<String> splitSceneName = Arrays.stream(chapterSceneName)
                .toList()
                .subList(2, chapterSceneName.length);
        String sceneName = String.join(" ", splitSceneName);

        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        Scene scene = chapter.getSceneByName(sceneName);

        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
    }

    private void initializeStory() throws Exception {
        stopExistingStoryHandler();
        NarrativeCraftMod.getInstance().setStoryHandler(this);
        Minecraft.getInstance().options.hideGui = true;

        inkActionList.clear();
        globalDialogValue = new DialogData(DialogData.globalDialogData);

        reloadCharacterSkins();
        initStoryFromFile();
    }

    private void stopExistingStoryHandler() {
        StoryHandler existingHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if (existingHandler != null) {
            existingHandler.stop(true);
        }
    }

    private void reloadCharacterSkins() {
        NarrativeCraftMod.getInstance().getCharacterManager().reloadSkins();

        for (Chapter chapter : NarrativeCraftMod.getInstance().getChapterManager().getChapters()) {
            for (Scene scene : chapter.getSceneList()) {
                for (CharacterStory npc : scene.getNpcs()) {
                    NarrativeCraftMod.getInstance().getCharacterManager().reloadSkin(npc);
                }
            }
        }
    }

    private void initStoryFromFile() throws Exception, IOException {
        String content = NarrativeCraftFile.getStoryContent();
        story = new Story(content);
    }

    private void loadStoryState() throws Exception {
        if (save != null && !playerSession.sessionSet()) {
            loadFromSave();
        } else {
            loadFromSession();
        }
    }

    private void loadFromSave() throws Exception {
        story.getState().loadJson(save.getInkSave());

        if (playerSession.getScene() == null) {
            loadPlayerSessionFromSave();
            executeTagsFromSave();
        }
    }

    private void loadPlayerSessionFromSave() {
        PlayerSession playerSessionFromSave = save.getPlayerSession();
        playerSession.setChapter(playerSessionFromSave.getChapter());
        playerSession.setScene(playerSessionFromSave.getScene());
    }

    private void executeTagsFromSave() {
        for (String tag : save.getTagList()) {
            inkTagTranslators.executeTag(tag);
        }
    }

    private void loadFromSession() throws Exception {
        Scene loadScene = playerSession.getScene();

        if (loadScene != null) {
            loadSpecificScene(loadScene);
        } else {
            loadFirstScene();
        }
    }

    private void loadSpecificScene(Scene loadScene) throws Exception {
        story.choosePathString(NarrativeCraftFile.getChapterSceneSnakeCase(loadScene));
        save = null;
    }

    private void loadFirstScene() {
        if (save == null) {
            Chapter firstChapter = getFirstChapter();
            if (hasScenes(firstChapter)) {
                Scene firstScene = firstChapter.getSortedSceneList().getFirst();
                playerSession.setChapter(firstChapter);
                playerSession.setScene(firstScene);
            }
        }
    }

    private Chapter getFirstChapter() {
        return NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(FIRST_CHAPTER_INDEX);
    }

    private boolean hasScenes(Chapter chapter) {
        return !NarrativeCraftMod.getInstance().getChapterManager().getChapters().getFirst().getSceneList().isEmpty();
    }

    private boolean shouldStopStory() {
        return !story.canContinue() && story.getCurrentChoices().isEmpty() && save == null;
    }

    private boolean shouldShowChoices() {
        return !story.getCurrentChoices().isEmpty() && save == null;
    }

    private void processStoryContent() throws Exception {
        if (save != null) {
            processFromSave();
        } else {
            currentDialog = story.Continue();
        }
    }

    private void processFromSave() throws Exception {
        currentDialog = story.getCurrentText();
        boolean isNewScene = story.getCurrentTags().contains("on enter") &&
                !story.getCurrentTags().contains("save");

        processTagsFromSave(isNewScene);
        loadPlayerSessionData();
        loadCharactersFromSave();
        loadDialogFromSave();
    }

    private void processTagsFromSave(boolean isNewScene) throws Exception {
        int breakIndex = findTagBreakIndex(isNewScene);
        List<String> oldTags = List.copyOf(story.getCurrentTags());

        story.getCurrentTags().clear();
        for (int i = breakIndex; i < oldTags.size(); i++) {
            story.getCurrentTags().add(oldTags.get(i));
        }
    }

    private int findTagBreakIndex(boolean isNewScene) throws Exception {
        int breakIndex = 0;
        for (String tag : story.getCurrentTags()) {
            breakIndex++;
            if ((isNewScene && tag.equals("on enter")) || (!isNewScene && tag.equals("save"))) {
                break;
            }
        }
        return breakIndex;
    }

    private void loadPlayerSessionData() {
        PlayerSession playerSessionFromSave = save.getPlayerSession();
        playerSession.setKeyframeControllerBase(playerSessionFromSave.getKeyframeControllerBase());
        playerSession.setSoloCam(playerSessionFromSave.getSoloCam());
    }

    private void loadCharactersFromSave() {
        for (CharacterStoryData characterStoryData : save.getCharacterStoryDataList()) {
            if (!characterStoryData.isOnlyTemplate()) {
                characterStoryData.spawn(Utils.getServerLevel());
                currentCharacters.add(characterStoryData.getCharacterStory());
            }
        }
    }

    private void loadDialogFromSave() {
        if (save.getDialogSaveData() != null) {
            DialogData dialogSaveData = save.getDialogSaveData();
            globalDialogValue = dialogSaveData;

            if (!currentCharacters.isEmpty()) {
                if (isGlobalDialogOnly(dialogSaveData)) {
                    showDialog();
                } else {
                    createDialogFromSaveData(dialogSaveData);
                }
            }
        }
    }

    private boolean isGlobalDialogOnly(DialogData dialogSaveData) {
        return dialogSaveData.getCharacterName() == null && dialogSaveData.getText() == null;
    }

    private void createDialogFromSaveData(DialogData dialogSaveData) {
        Entity entity = findCharacterEntity(dialogSaveData.getCharacterName());

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

        configureDialogFromSave(dialogSaveData);
    }

    private Entity findCharacterEntity(String characterName) {
        return currentCharacters.stream()
                .filter(character -> character.getName().equals(characterName))
                .map(CharacterStory::getEntity)
                .findFirst()
                .orElse(null);
    }

    private void configureDialogFromSave(DialogData dialogSaveData) {
        currentDialogBox.setUnSkippable(dialogSaveData.isUnSkippable());
        currentDialogBox.setForcedEndTime(dialogSaveData.getEndForceEndTime());
        ((Dialog) currentDialogBox).setCharacterName(dialogSaveData.getCharacterName());
    }

    private void updatePlayerSession() {
        if (playerSession.getChapter() == null || playerSession.getScene() == null) {
            initChapterSceneSession();
        }
    }

    private void executeInkTags() throws Exception {
        if (inkTagTranslators.executeCurrentTags()) {
            if (!story.getCurrentChoices().isEmpty() && currentDialog.isEmpty()) {
                showChoices();
            } else {
                showDialog();
            }
        } else {
            endCurrentDialog();
        }
    }

    private void handleSceneValidation() {
        if (story.canContinue() && currentCharacters.isEmpty() &&
                playerSession.getSoloCam() == null && playerSession.getKeyframeControllerBase() == null) {

            stop(true);
            showSceneLoadFailMessage();
        }
    }

    private void showSceneLoadFailMessage() {
        Component message = Component.literal(
                        Translation.message("story.load.scene.fail").getString())
                .withStyle(ChatFormatting.RED);
        Minecraft.getInstance().player.displayClientMessage(message, false);
    }

    private void updateCutsceneMode() {
        boolean inCutscene = playerSession.getSoloCam() != null ||
                playerSession.getKeyframeControllerBase() != null;
        changePlayerCutsceneMode(Playback.PlaybackType.PRODUCTION, inCutscene);
    }

    private Exception createStoryException(StoryException e) {
        return new Exception(String.format("Chapter %s Scene %s\n%s",
                playerSession.getChapter().getIndex(),
                playerSession.getScene().getName(),
                e.getMessage()));
    }

    private void showCreditsScreen() {
        if (!isDebugMode) {
            boolean isFirstCompletion = !narrativeUserOptions.FINISHED_STORY;
            CreditsScreen creditsScreen = new CreditsScreen(false, isFirstCompletion);

            narrativeUserOptions.FINISHED_STORY = true;
            NarrativeCraftFile.updateUserOptions();

            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(creditsScreen));
        }
    }

    private void cleanup() {
        cleanupCharacters();
        cleanupPlaybacks();
        cleanupSounds();
        resetState();
    }

    private void cleanupCharacters() {
        currentCharacters.forEach(CharacterStory::kill);
        currentCharacters.clear();
    }

    private void cleanupPlaybacks() {
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().forEach(Playback::forceStop);
        NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().clear();
    }

    private void cleanupSounds() {
        typedSoundInstanceList.forEach(instance ->
                Minecraft.getInstance().getSoundManager().stop(instance));
    }

    private void resetState() {
        changePlayerCutsceneMode(Playback.PlaybackType.PRODUCTION, false);
        currentKeyframeCoordinate = null;
        story = null;
        inkActionList.clear();
        playerSession.reset();
        Minecraft.getInstance().options.hideGui = false;
        NarrativeCraftMod.getInstance().setStoryHandler(null);
    }

    private void handleDebugModeCrash(Exception exception, boolean creatorFault, CrashReport report) {
        Component message;
        if (creatorFault) {
            message = Translation.message("user.crash.his-fault")
                    .withStyle(style -> style.withHoverEvent(
                            new HoverEvent.ShowText(Component.literal(exception.getMessage()))))
                    .withStyle(ChatFormatting.RED);
        } else {
            message = Translation.message("user.crash.not-his-fault")
                    .withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(report.getSaveFile())))
                    .withStyle(ChatFormatting.RED);
        }

        Minecraft.getInstance().player.displayClientMessage(message, false);
    }

    private void showCrashScreen(boolean creatorFault, CrashReport report) {
        CrashScreen crashScreen = new CrashScreen(creatorFault, report);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(crashScreen));
    }

    private boolean hasCurrentChoices() {
        return !story.getCurrentChoices().isEmpty();
    }

    private void endCurrentDialog() {
        if (currentDialogBox != null) {
            currentDialogBox.endDialogAndDontSkip();
        }
    }

    private void validateCurrentDialog() {
        if (currentDialogBox instanceof Dialog dialog) {
            if (!dialog.getEntityServer().isAlive()) {
                currentDialogBox = null;
            }
        }
    }

    private boolean shouldReuseDialog(ParsedDialog parsed) {
        return parsed.characterName.equalsIgnoreCase(currentCharacterTalking) &&
                currentDialogBox != null;
    }

    private void reuseExistingDialog(ParsedDialog parsed) {
        currentDialogBox.getDialogAnimationScrollText().setText(parsed.cleanedText);
        currentDialogBox.reset();
    }

    private boolean shouldEndCurrentDialog() {
        return currentDialogBox != null &&
                (!currentDialogBox.isDialogAutoSkipped() || narrativeUserOptions.AUTO_SKIP);
    }

    private void createNewDialog(ParsedDialog parsed) {
        if (!parsed.characterName.isEmpty()) {
            createCharacterDialog(parsed);
        } else {
            create2dDialog(parsed);
        }

        configureDialog();
    }

    private void createCharacterDialog(ParsedDialog parsed) {
        CharacterStory currentCharacter = getCharacter(parsed.characterName);
        if (currentCharacter == null) {
            crash(new Exception(Translation.message("user.crash.character_not_found",
                    parsed.characterName,
                    playerSession.getChapter().getIndex(),
                    playerSession.getScene().getName()).getString()), true);
            return;
        }

        currentDialogBox = new Dialog(
                currentCharacter.getEntity(),
                parsed.cleanedText,
                globalDialogValue.getTextColor(),
                globalDialogValue.getBackgroundColor(),
                globalDialogValue.getPaddingX(),
                globalDialogValue.getPaddingY(),
                globalDialogValue.getScale(),
                globalDialogValue.getLetterSpacing(),
                globalDialogValue.getGap(),
                globalDialogValue.getMaxWidth(),
                globalDialogValue.getOffset()
        );

        configureCharacterDialog(currentCharacter);
    }

    private void configureCharacterDialog(CharacterStory currentCharacter) {
        Dialog dialog = (Dialog) currentDialogBox;
        dialog.getDialogEntityBobbing().setNoiseShakeStrength(globalDialogValue.getBobbingNoiseShakeStrength());
        dialog.getDialogEntityBobbing().setNoiseShakeSpeed(globalDialogValue.getBobbingNoiseShakeSpeed());
        dialog.setCharacterName(currentCharacter.getName());
    }

    private void create2dDialog(ParsedDialog parsed) {
        currentDialogBox = new Dialog2d(
                parsed.cleanedText,
                400, 90,
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

    private void configureDialog() {
        currentDialogBox.setUnSkippable(globalDialogValue.isUnSkippable());
        currentDialogBox.setForcedEndTime(globalDialogValue.getEndForceEndTime());
    }

    private void updateCurrentCharacterTalking(String characterName) {
        currentCharacterTalking = characterName;
    }

    private void configureAutoSkip() {
        if (globalDialogValue.getEndForceEndTime() == 0 && narrativeUserOptions.AUTO_SKIP) {
            long autoSkipTime = currentDialog.replaceAll("\\s+", "").length() * AUTO_SKIP_MULTIPLIER;
            currentDialogBox.setForcedEndTime(autoSkipTime);
        }
    }

    private void destroyCharacterEntity(CharacterStory characterStory) {
        if (characterStory.getEntity() != null) {
            characterStory.getEntity().remove(Entity.RemovalReason.KILLED);

            if (characterStory.getEntity() instanceof FakePlayer fakePlayer) {
                NarrativeCraftMod.server.getPlayerList().remove(fakePlayer);
                ((PlayerListFields) NarrativeCraftMod.server.getPlayerList())
                        .getPlayersByUUID().remove(fakePlayer.getUUID());
            }
        }
    }

    private boolean matchesSoundEvent(SimpleSoundInstance instance, SoundEvent sound) {
        String instancePath = instance.getSound().getLocation().getPath().replace("/", ".");
        return instancePath.equals(sound.location().getPath());
    }

    private static void validateScene(Scene scene, List<ErrorLine> errorLineList,
                                      Pattern inlineTagPattern, Pattern commentPattern) {
        List<String> lines = NarrativeCraftFile.readSceneLines(scene);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (commentPattern.matcher(line).find()) continue;

            line = line.replaceFirst("^\\s+", "");

            if (shouldValidateEnterTag(i, line)) {
                addEnterTagError(errorLineList, i, scene, line);
                break;
            }

            validateInlineTags(line, i, scene, errorLineList, inlineTagPattern);
        }
    }

    private static boolean shouldValidateEnterTag(int lineIndex, String line) {
        return lineIndex + 1 == 2 && !line.startsWith("#") && !line.contains("on enter");
    }

    private static void addEnterTagError(List<ErrorLine> errorLineList, int lineIndex,
                                         Scene scene, String line) {
        errorLineList.add(new ErrorLine(
                lineIndex + 1,
                scene,
                Translation.message("validation.on_enter").getString(),
                line,
                false
        ));
    }

    private static void validateInlineTags(String line, int lineIndex, Scene scene,
                                           List<ErrorLine> errorLineList, Pattern inlineTagPattern) {
        Matcher matcher = inlineTagPattern.matcher(line);

        while (matcher.find()) {
            String tag = extractTag(matcher.group());
            String[] command = tag.split(" ");
            InkTagType tagType = InkTagType.resolveType(tag);

            if (tagType != null) {
                validateTagType(tagType, command, lineIndex, matcher.group(), scene, errorLineList);
            }
        }
    }

    private static String extractTag(String rawTag) {
        String tag = rawTag.trim();
        if (tag.startsWith("#")) {
            tag = tag.substring(1).trim();
        }
        return tag;
    }

    private static void validateTagType(InkTagType tagType, String[] command, int lineIndex,
                                        String matchedGroup, Scene scene, List<ErrorLine> errorLineList) {
        if (tagType == InkTagType.EMOTE) {
            validateEmoteTag(lineIndex, scene, matchedGroup, errorLineList);
        } else {
            InkAction inkAction = tagType.getDefaultInstance();
            if (inkAction != null) {
                ErrorLine errorLine = inkAction.validate(command, lineIndex + 1, matchedGroup, scene);
                if (errorLine != null) {
                    errorLineList.add(errorLine);
                }
            }
        }
    }

    private static void validateEmoteTag(int lineIndex, Scene scene, String matchedGroup,
                                         List<ErrorLine> errorLineList) {
        if (!Services.PLATFORM.isModLoaded("emotecraft")) {
            errorLineList.add(new ErrorLine(
                    lineIndex + 1,
                    scene,
                    Translation.message("validation.emotecraft").getString(),
                    matchedGroup,
                    false
            ));
        }
    }

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

    public boolean isFinished() {
        return !story.canContinue() && story.getCurrentChoices().isEmpty() && currentDialog.isEmpty();
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