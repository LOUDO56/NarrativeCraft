package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.runtime.Story;
import com.bladecoder.ink.runtime.StoryException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;
import fr.loudo.narrativecraft.narrative.dialog.animations.DialogLetterEffect;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.FadeScreenInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.SongSfxInkAction;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryHandler {

    private final List<CharacterStory> currentCharacters;
    private final List<CharacterStory> currentNpcs;
    private final List<TypedSoundInstance> typedSoundInstanceList;
    private final PlayerSession playerSession;
    private final InkTagTranslators inkTagTranslators;
    private Story story;
    private String currentDialog, currentCharacterTalking;
    private Dialog currentDialogBox;
    private KeyframeCoordinate currentKeyframeCoordinate;
    private boolean isRunning, isDebugMode;
    private FadeScreenInkAction fadeScreenInkAction;

    public StoryHandler(Chapter chapter, Scene scene) {
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());
        playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(serverPlayer, chapter, scene);
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        currentCharacters = new ArrayList<>();
        currentNpcs = new ArrayList<>();
        isRunning = true;
        inkTagTranslators = new InkTagTranslators(this);
        typedSoundInstanceList = new ArrayList<>();
        isDebugMode = false;
        initStory();
    }

    public StoryHandler(Chapter chapter, Scene scene, boolean isDebugMode) {
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(Minecraft.getInstance().player.getUUID());
        playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(serverPlayer, chapter, scene);
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        currentCharacters = new ArrayList<>();
        currentNpcs = new ArrayList<>();
        isRunning = true;
        inkTagTranslators = new InkTagTranslators(this);
        typedSoundInstanceList = new ArrayList<>();
        this.isDebugMode = isDebugMode;
        initStory();
    }

    private void initStory() {
        try {
            if(NarrativeCraftMod.getInstance().getStoryHandler() != null) {
                NarrativeCraftMod.getInstance().getStoryHandler().stop();
            }
            String content = NarrativeCraftFile.getStoryFile();
            story = new Story(content);
            isRunning = true;
            StoryHandler.changePlayerCutsceneMode(playerSession.getPlayer(), Playback.PlaybackType.PRODUCTION, true);
            next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        isRunning = false;
        for(CharacterStory characterStory : currentCharacters) {
            characterStory.kill();
        }
        for(CharacterStory characterStory : currentNpcs) {
            characterStory.kill();
        }
        StoryHandler.changePlayerCutsceneMode(playerSession.getPlayer(), Playback.PlaybackType.PRODUCTION, false);
        for(SimpleSoundInstance simpleSoundInstance : typedSoundInstanceList) {
            Minecraft.getInstance().getSoundManager().stop(simpleSoundInstance);
        }
        fadeScreenInkAction = null;
        playerSession.reset();
    }

    public void next() {
        try {
            if(!story.canContinue()) {
                stop();
                return;
            }
            currentDialog = story.Continue();
            if(inkTagTranslators.executeCurrentTags()) {
                if(!story.canContinue() && !story.getCurrentTags().isEmpty()) {
                    stop();
                    return;
                }
                showDialog();
            }
        } catch (StoryException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void showDialog() {
        String[] splittedDialog = currentDialog.split(":");
        if(splittedDialog.length < 2) return;
        String characterName = splittedDialog[0].trim();
        String dialogContent = splittedDialog[1].trim();

        if (dialogContent.startsWith("\"") && dialogContent.endsWith("\"")) {
            dialogContent = dialogContent.substring(1, dialogContent.length() - 1);
        }

        ParsedDialog parsed = parseDialogContent(dialogContent);

        if (characterName.equalsIgnoreCase(currentCharacterTalking) && currentDialogBox != null) {
            currentDialogBox.getDialogScrollText().setText(parsed.cleanedText);
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
                        parsed.cleanedText,
                        currentCharacter.getEntity(),
                        3, 4, 0.5f,
                        10, 15, 0, 100
                );
            }
            currentCharacterTalking = characterName;
        }
        applyTextEffects(parsed.effects);
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
            currentDialogBox.getDialogScrollText().setDialogLetterEffect(dialogEffect);
            return;
        }
        for (TextEffect effect : effects) {
            double time = Double.parseDouble(effect.parameters.getOrDefault("time", "1"));
            float force = Float.parseFloat(effect.parameters.getOrDefault("force", "0"));

            DialogLetterEffect dialogEffect = new DialogLetterEffect(
                    effect.type,
                    (long) (time * 1000L),
                    force,
                    effect.startIndex,
                    effect.endIndex
            );
            currentDialogBox.getDialogScrollText().setDialogLetterEffect(dialogEffect);
        }
    }

    public void playSound(SoundEvent sound, float volume, float pitch, boolean loop, SongSfxInkAction.SoundType soundType) {
        TypedSoundInstance soundInstance = new TypedSoundInstance(sound.location(), SoundSource.MASTER, volume, pitch, SoundInstance.createUnseededRandom(), loop, soundType);
        typedSoundInstanceList.add(soundInstance);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
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

    public FadeScreenInkAction getFadeScreenInkAction() {
        return fadeScreenInkAction;
    }

    public void setFadeScreenInkAction(FadeScreenInkAction fadeScreenInkAction) {
        this.fadeScreenInkAction = fadeScreenInkAction;
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
