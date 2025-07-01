package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.dialog.*;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.inkAction.AnimationPlayInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.SubscenePlayInkAction;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class StorySave {

    private final List<AnimationInfo> animationInfoList;
    private final List<SubsceneInfo> subsceneInfoList;
    private final int chapterIndex;
    private final String sceneName;
    private final KeyframeCoordinate soloCam;
    private final String inkSave;
    private final List<CharacterStoryData> characterStoryDataList;
    private DialogData dialogSaveData;
    private List<InkAction> inkActionList;
    public static long startTimeSaveIcon;

    public StorySave(StoryHandler storyHandler) {
        characterStoryDataList = new ArrayList<>();
        animationInfoList = new ArrayList<>();
        subsceneInfoList = new ArrayList<>();
        inkActionList = new ArrayList<>();
        PlayerSession playerSession = storyHandler.getPlayerSession();
        try {
            if(playerSession.getKeyframeControllerBase() instanceof CameraAngleController cameraAngleController) {
                soloCam = cameraAngleController.getCurrentPreviewKeyframe().getKeyframeCoordinate();
            } else {
                soloCam = playerSession.getSoloCam();
            }
            inkSave = storyHandler.getStory().getState().toJson();
            chapterIndex = playerSession.getChapter().getIndex();
            sceneName = playerSession.getScene().getName();
            DialogImpl dialogImpl = storyHandler.getCurrentDialogBox();
            DialogData globalDialogData = storyHandler.getGlobalDialogValue();
            if(dialogImpl instanceof Dialog dialog) {
                dialogSaveData = new DialogData(
                        dialog.getCharacterName(),
                        storyHandler.getCurrentDialog(),
                        dialog.getDialogOffset(),
                        dialog.getTextDialogColor(),
                        dialog.getDialogBackgroundColor(),
                        dialog.getPaddingX(),
                        dialog.getPaddingY(),
                        dialog.getScale() / 0.025f,
                        dialog.getDialogAnimationScrollText().getLetterSpacing(),
                        dialog.getDialogAnimationScrollText().getGap(),
                        dialog.getDialogAnimationScrollText().getMaxWidth(),
                        dialog.isUnSkippable(),
                        dialog.getForcedEndTime(),
                        globalDialogData.getBobbingNoiseShakeSpeed(),
                        globalDialogData.getBobbingNoiseShakeStrength()
                );
            } else {
                dialogSaveData = new DialogData(
                        null,
                        null,
                        globalDialogData.getOffset(),
                        globalDialogData.getTextColor(),
                        globalDialogData.getBackgroundColor(),
                        globalDialogData.getPaddingX(),
                        globalDialogData.getPaddingY(),
                        globalDialogData.getScale(),
                        globalDialogData.getLetterSpacing(),
                        globalDialogData.getGap(),
                        globalDialogData.getMaxWidth(),
                        globalDialogData.isUnSkippable(),
                        globalDialogData.getEndForceEndTime(),
                        globalDialogData.getBobbingNoiseShakeSpeed(),
                        globalDialogData.getBobbingNoiseShakeStrength()
                );
            }


            for(InkAction inkAction : storyHandler.getInkActionList()) {
                if(inkAction instanceof SubscenePlayInkAction action) {
                    subsceneInfoList.add(
                            new SubsceneInfo(
                                    action.getName(),
                                    action.isLooping()
                            )
                    );
                } else if(inkAction instanceof AnimationPlayInkAction action) {
                    animationInfoList.add(
                            new AnimationInfo(
                                    action.getName(),
                                    action.isLooping()
                            )
                    );
                } else {
                    inkActionList.add(inkAction);
                }
            }
            for(CharacterStory characterStory : storyHandler.getCurrentCharacters()) {
                // If character spawned by playback or camera angle
                if(NarrativeCraftMod.getInstance().getPlaybackHandler().getPlaybacks().stream().noneMatch(playback -> playback.getCharacter().getName().equals(characterStory.getName()))) {
                    characterStoryDataList.add(new CharacterStoryData(characterStory));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void showSaveIcon(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {

        long fadeInDuration = 200;
        long stayDuration = 900;
        long fadeOutDuration = 200;

        long elapsed = System.currentTimeMillis() - startTimeSaveIcon;
        long totalDuration = fadeInDuration + stayDuration + fadeOutDuration;

        float alpha;
        if (elapsed < fadeInDuration) {
            alpha = Math.max((float) elapsed / fadeInDuration, 0.05f);
        } else if (elapsed < fadeInDuration + stayDuration) {
            alpha = 1f;
        } else {
            long fadeOutElapsed = elapsed - fadeInDuration - stayDuration;
            alpha = Math.max(1f - (float) fadeOutElapsed / fadeOutDuration, 0.05f);
        }

        int alphaInt = (int) (alpha * 255) & 0xFF;
        int color = (alphaInt << 24) | 0xFFFFFF;

        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        Component text = ImageFontConstants.SAVE;
        int textWidth = minecraft.font.width(text);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);

        guiGraphics.drawString(
                minecraft.font,
                text,
                width - textWidth - 30,
                height - 30,
                color,
                false
        );

        guiGraphics.pose().popPose();

        if (elapsed >= totalDuration) {
            NarrativeCraftMod.getInstance().getStoryHandler().setSaving(false);
        }
    }

    public PlayerSession getPlayerSession() {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
        Scene scene = chapter.getSceneByName(sceneName);
        PlayerSession playerSession = new PlayerSession(chapter, scene);
        playerSession.setSoloCam(soloCam);
        return playerSession;
    }

    public List<AnimationInfo> getAnimationInfoList() {
        return animationInfoList;
    }

    public List<SubsceneInfo> getSubsceneInfoList() {
        return subsceneInfoList;
    }

    public String getInkSave() {
        return inkSave;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public String getSceneName() {
        return sceneName;
    }

    public DialogData getDialogSaveData() {
        return dialogSaveData;
    }

    public List<InkAction> getInkActionList() {
        return inkActionList;
    }

    public static class AnimationInfo {
        private String name;
        private boolean wasLooping;

        public AnimationInfo(String name, boolean wasLooping) {
            this.name = name;
            this.wasLooping = wasLooping;
        }

        public String getName() {
            return name;
        }

        public boolean wasLooping() {
            return wasLooping;
        }
    }

    public static class SubsceneInfo {
        private String name;
        private boolean wasLooping;

        public SubsceneInfo(String name, boolean wasLooping) {
            this.name = name;
            this.wasLooping = wasLooping;
        }

        public String getName() {
            return name;
        }

        public boolean wasLooping() {
            return wasLooping;
        }
    }
}
