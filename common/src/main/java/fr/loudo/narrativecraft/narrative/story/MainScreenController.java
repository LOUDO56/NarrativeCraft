package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngle;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeCoordinate;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreenControllerScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MainScreenController extends CameraAngleController {

    public MainScreenController(CameraAngleGroup cameraAngleGroup, ServerPlayer player, Playback.PlaybackType playbackType) {
        super(cameraAngleGroup, player, playbackType);
        if(cameraAngleGroup == null) {
            this.cameraAngleGroup = new CameraAngleGroup(null, "main_screen", "blip blop boop x0x");
        }
        for(CharacterStoryData characterStoryData : this.cameraAngleGroup.getCharacterStoryDataList()) {
            CharacterStory originalCharacter = NarrativeCraftMod.getInstance().getCharacterManager().getCharacter(characterStoryData.getCharacterStory().getName());
            characterStoryData.setCharacterStory(originalCharacter);
        }
        initOldData();
    }

    @Override
    public void initOldData() {
        CameraAngleGroup oldCameraAngleGroup = NarrativeCraftFile.getMainScreenBackgroundFile();
        if(oldCameraAngleGroup == null) return;
        oldCameraAngleDataList = oldCameraAngleGroup.getCameraAngleList();
        oldCharacterStoryDataList = oldCameraAngleGroup.getCharacterStoryDataList();
        for(CharacterStoryData characterStoryData : oldCharacterStoryDataList) {
            CharacterStory originalCharacter = cameraAngleGroup.getCharacterStoryData(characterStoryData.getCharacterStory().getName()).getCharacterStory();
            characterStoryData.setCharacterStory(originalCharacter);
        }
    }

    @Override
    public void startSession() {
        super.startSession();
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().getPlayerSession(player.getUUID());
        playerSession.setKeyframeControllerBase(this);
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new MainScreenControllerScreen(this)));
        }
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        for(KeyframeTrigger keyframeTrigger : cameraAngleGroup.getKeyframeTriggerList()) {
            String[] tags = keyframeTrigger.getCommands().split("\n");
            for(String tag : tags) {
                storyHandler.getInkTagTranslators().executeTag(tag);
            }
        }
    }

    @Override
    public void stopSession(boolean save) {
        cameraAngleGroup.killCharacters();
        if(playbackType == Playback.PlaybackType.DEVELOPMENT) {
            for(CameraAngle cameraAngle : cameraAngleGroup.getCameraAngleList()) {
                cameraAngle.removeKeyframeFromClient(player);
            }
            for(KeyframeTrigger keyframeTrigger : cameraAngleGroup.getKeyframeTriggerList()) {
                keyframeTrigger.removeKeyframeFromClient(player);
            }
            player.setGameMode(GameType.CREATIVE);
            if(save) {
                NarrativeCraftFile.updateMainScreenBackgroundFile(cameraAngleGroup);
            } else {
                if(oldCameraAngleDataList != null) {
                    cameraAngleGroup.getCameraAngleList().clear();
                    cameraAngleGroup.getCameraAngleList().addAll(oldCameraAngleDataList);
                }
                if(oldCharacterStoryDataList != null) {
                    cameraAngleGroup.getCharacterStoryDataList().clear();
                    cameraAngleGroup.getCharacterStoryDataList().addAll(oldCharacterStoryDataList);
                }
            }
        }
        PlayerSession playerSession = Utils.getSessionOrNull(player);
        playerSession.setKeyframeControllerBase(null);
    }

    public void addKeyframeTrigger(String commands, int tick) {
        Vec3 playerPos = player.position();
        KeyframeCoordinate keyframeCoordinate = new KeyframeCoordinate(playerPos.x(), playerPos.y() + player.getEyeHeight(), playerPos.z(), player.getXRot(), player.getYRot(), Minecraft.getInstance().options.fov().get());
        keyframeCoordinate.setXRot(0);
        KeyframeTrigger keyframeTrigger = new KeyframeTrigger(keyframeCounter.incrementAndGet(), keyframeCoordinate, tick, commands);
        keyframeTrigger.showKeyframeToClient(player);
        cameraAngleGroup.getKeyframeTriggerList().add(keyframeTrigger);
    }

    public void removeKeyframeTrigger() {
        if(cameraAngleGroup.getKeyframeTriggerList().isEmpty()) return;
        KeyframeTrigger keyframeTrigger = cameraAngleGroup.getKeyframeTriggerList().getFirst();
        if(keyframeTrigger == null) return;
        keyframeTrigger.removeKeyframeFromClient(player);
        cameraAngleGroup.getKeyframeTriggerList().remove(keyframeTrigger);
    }

    @Override
    public void addKeyframe(String name) {
        if(cameraAngleGroup.getCameraAngleList().isEmpty()) {
            super.addKeyframe(name);
        } else {
            player.sendSystemMessage(Component.literal("Only one keyframe"));
        }
    }

    @Override
    public void renderHUDInfo(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String infoText = Translation.message("main_screen.hud").getString();
        int width = minecraft.getWindow().getGuiScaledWidth();
        guiGraphics.drawString(
                font,
                infoText,
                width / 2 - font.width(infoText) / 2,
                10,
                ChatFormatting.WHITE.getColor()
        );
    }

    public CameraAngle getMainCamera() {
        return cameraAngleGroup.getCameraAngleList().getFirst();
    }

    public KeyframeTrigger getKeyframeTriggerByEntity(Entity entity) {
        return cameraAngleGroup.getKeyframeTriggerByEntity(entity);
    }
}
