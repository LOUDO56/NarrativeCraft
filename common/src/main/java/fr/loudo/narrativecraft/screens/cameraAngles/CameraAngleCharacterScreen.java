package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.KeyframeControllerBase;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.screens.components.ChangeSkinLinkScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class CameraAngleCharacterScreen extends Screen {

    private final int BUTTON_WIDTH = 50;
    private final int BUTTON_HEIGHT = 20;
    private final KeyframeControllerBase keyframeControllerBase;
    private CharacterStoryData characterStoryData;
    private Animation animation;

    public CameraAngleCharacterScreen(CharacterStoryData characterStoryData, KeyframeControllerBase keyframeControllerBase) {
        super(Component.literal("Character screen"));
        this.keyframeControllerBase = keyframeControllerBase;
        this.characterStoryData = characterStoryData;
    }

    public CameraAngleCharacterScreen(Animation animation, KeyframeControllerBase keyframeControllerBase) {
        super(Component.literal("Character screen"));
        this.keyframeControllerBase = keyframeControllerBase;
        this.animation = animation;
    }

    @Override
    protected void init() {
        int totalWidth = 0;

        ChangeSkinLinkScreen screen;
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            screen = new ChangeSkinLinkScreen(characterStoryData.getCharacterStory(), skin ->  {
                characterStoryData.setSkinName(skin);
                NarrativeCraftFile.updateCameraAnglesFile(cameraAngleController.getCameraAngleGroup().getScene());
            });
            totalWidth = BUTTON_WIDTH * 3 + 5;
        } else if(keyframeControllerBase instanceof CutsceneController) {
            screen = new ChangeSkinLinkScreen(animation.getCharacter(), skin -> {
                animation.setSkinName(skin);
                NarrativeCraftFile.updateAnimationFile(animation);
            });
            totalWidth = BUTTON_WIDTH * 2 + 5;
        } else {
            screen = null;
        }

        int startX = (this.width - totalWidth) / 2;

        Button changeSkinButton = Button.builder(Translation.message("screen.camera_angle_character.change_skin"), button -> {
            minecraft.setScreen(screen);
        }).bounds(startX, this.height / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build();

        int closeX = startX + BUTTON_WIDTH + 5;
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            Button removeButton = Button.builder(Translation.message("global.remove"), button -> {
                ConfirmScreen confirm = new ConfirmScreen(b -> {
                    if (b) {
                        cameraAngleController.removeCharacter(characterStoryData.getCharacterStory().getEntity());
                    }
                    minecraft.setScreen(null);
                }, Component.literal(""), Translation.message("global.confirm_delete"),
                        CommonComponents.GUI_YES, CommonComponents.GUI_CANCEL);
                minecraft.setScreen(confirm);
            }).bounds(startX + BUTTON_WIDTH + 5, this.height / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            this.addRenderableWidget(removeButton);
            closeX += BUTTON_WIDTH + 5;
        }

        Button closeButton = Button.builder(Translation.message("global.close"), button -> {
            this.onClose();
        }).bounds(closeX, this.height / 2, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(changeSkinButton);
        this.addRenderableWidget(closeButton);
    }


}
