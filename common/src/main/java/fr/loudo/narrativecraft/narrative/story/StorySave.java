package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class StorySave {

    private final int chapterIndex;
    private final String sceneName;
    private final KeyframeCoordinate soloCam;
    private final String inkSave;
    private final List<CharacterStoryData> characterStoryDataList;
    public static long startTimeSaveIcon;

    public StorySave(StoryHandler storyHandler) {
        characterStoryDataList = new ArrayList<>();
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
            for(CharacterStory characterStory : storyHandler.getCurrentCharacters()) {
                characterStoryDataList.add(new CharacterStoryData(characterStory));
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

    public String getInkSave() {
        return inkSave;
    }

    public List<CharacterStoryData> getCharacterStoryDataList() {
        return characterStoryDataList;
    }
}
