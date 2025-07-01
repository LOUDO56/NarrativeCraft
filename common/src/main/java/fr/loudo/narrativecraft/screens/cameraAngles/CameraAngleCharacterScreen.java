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
            });
            totalWidth = BUTTON_WIDTH * 3 + 5;
        } else if(keyframeControllerBase instanceof CutsceneController) {
            screen = new ChangeSkinLinkScreen(animation.getCharacter(), skin -> {
                animation.setSkinName(skin);
                NarrativeCraftFile.updateAnimationFile(animation);
            });
            totalWidth = BUTTON_WIDTH + 5;
        } else {
            screen = null;
        }
        
        CharacterStory characterStory = null;
        if(keyframeControllerBase instanceof CameraAngleController) {
            characterStory = characterStoryData.getCharacterStory();
        } else if(keyframeControllerBase instanceof CutsceneController) {
            characterStory = animation.getCharacter();
        }

        if(characterStory.getCharacterType() == CharacterStory.CharacterType.MAIN) {
            totalWidth += BUTTON_WIDTH + 5;
        } else {
            totalWidth += BUTTON_WIDTH * 2 + 5;
        }

        int startX = (this.width - totalWidth) / 2;
        int y = this.height / 2 - 10;

        if(characterStory.getCharacterType() == CharacterStory.CharacterType.MAIN) {
            Button changeSkinButton = Button.builder(Translation.message("screen.camera_angle_character.change_skin"), button -> {
                minecraft.setScreen(screen);
            }).bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            this.addRenderableWidget(changeSkinButton);
        }

        int closeX = startX + BUTTON_WIDTH + 5;
        if(keyframeControllerBase instanceof CameraAngleController cameraAngleController) {
            Button changePose = Button.builder(Translation.message("screen.camera_angle_character.change_pose"), button -> {
                CameraAngleChangePoseScreen screen1 = new CameraAngleChangePoseScreen(characterStoryData);
                minecraft.setScreen(screen1);
            }).bounds(startX + BUTTON_WIDTH + 5, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            closeX += BUTTON_WIDTH + 5;
            Button removeButton = Button.builder(Translation.message("global.remove"), button -> {
                ConfirmScreen confirm = new ConfirmScreen(b -> {
                    if (b) {
                        NarrativeCraftMod.server.execute(() -> {
                            cameraAngleController.removeCharacter(characterStoryData.getCharacterStory().getEntity());
                        });
                    }
                    minecraft.setScreen(null);
                }, Component.literal(""), Translation.message("global.confirm_delete"),
                        CommonComponents.GUI_YES, CommonComponents.GUI_CANCEL);
                minecraft.setScreen(confirm);
            }).bounds(startX + BUTTON_WIDTH * 2 + 5 * 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            this.addRenderableWidget(changePose);
            this.addRenderableWidget(removeButton);
            closeX += BUTTON_WIDTH + 5;
        }

        Button closeButton = Button.builder(Translation.message("global.close"), button -> {
            this.onClose();
        }).bounds(closeX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(closeButton);
    }


}
