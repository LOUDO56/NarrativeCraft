package fr.loudo.narrativecraft.screens.cutscenes;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeGroup;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeTriggerScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CutsceneControllerScreen extends Screen {

    private final Minecraft client = Minecraft.getInstance();
    private final Component pauseText = Component.literal("⏸");
    private final Component playText = Component.literal("▶");
    private final String previousText = "- %.1fs";
    private final String nextText = "+ %.1fs";

    private final int BUTTON_HEIGHT = 20;

    private int initialY;
    private int totalWidthControllerBtn;
    private Button previousSkip;
    private Button controllerButton;
    private Button nextSkip;

    private final CutsceneController cutsceneController;
    private final PlayerSession playerSession;

    public CutsceneControllerScreen(CutsceneController cutsceneController) {
        super(Component.literal("Cutscene Controller Screen"));
        this.cutsceneController = cutsceneController;
        this.playerSession = Utils.getSessionOrNull(client.player.getUUID());
    }

    @Override
    protected void init() {
        initialY = this.height - 80;
        initControllerButtons();
        initKeyframesButton();
        initSettingsButton();
    }

    private void initControllerButtons() {
        int pauseBtnWidth = 20;
        int btnWidth = 50;
        int gap = 5;
        totalWidthControllerBtn = pauseBtnWidth + (btnWidth * 2) + (gap * 2);
        int startX = (this.width - totalWidthControllerBtn) / 2;

        previousSkip = Button.builder(Component.literal(String.format(previousText, cutsceneController.getCurrentSkipCount() / 20)), button -> {
            cutsceneController.previousSecondSkip();
        }).bounds(startX, initialY, btnWidth, BUTTON_HEIGHT).build();

        controllerButton = Button.builder(cutsceneController.isPlaying() ? pauseText : playText, button -> {
            playOrPause();
        }).bounds(startX + btnWidth + gap, initialY, pauseBtnWidth, BUTTON_HEIGHT).build();

        nextSkip = Button.builder(Component.literal(String.format(nextText, cutsceneController.getCurrentSkipCount() / 20)), button -> {
            cutsceneController.nextSecondSkip();
        }).bounds(startX + btnWidth + gap + pauseBtnWidth + gap, initialY, btnWidth, BUTTON_HEIGHT).build();

        this.addRenderableWidget(previousSkip);
        this.addRenderableWidget(controllerButton);
        this.addRenderableWidget(nextSkip);
    }

    private void initKeyframesButton() {
        int btnWidth = 30;
        int gap = 5;
        int totalWidth = (btnWidth * 3) + (gap * 2) + 15;
        int controllerStartX = (this.width - totalWidthControllerBtn) / 2;
        int startX = controllerStartX - gap - totalWidth;

        Button createKeyframeGroup = Button.builder(ImageFontConstants.CREATE_KEYFRAME_GROUP, button -> {
            KeyframeGroup keyframeGroup = cutsceneController.createKeyframeGroup();
            client.player.displayClientMessage(Translation.message("cutscene.keyframegroup.created", keyframeGroup.getId()), false);
        }).bounds(startX, initialY, btnWidth, BUTTON_HEIGHT).build();
        createKeyframeGroup.setTooltip(Tooltip.create(Translation.message("screen.cutscene_controller.tooltip.keyframe_group")));

        Button addKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME, button -> {
            if (cutsceneController.addKeyframe()) {
                client.player.displayClientMessage(
                        Translation.message("cutscene.keyframe.added", cutsceneController.getSelectedKeyframeGroup().getId()),
                        false
                );
            } else {
                client.player.displayClientMessage(
                        Translation.message("cutscene.keyframe.added.fail"),
                        false
                );
            }
        }).bounds(startX + btnWidth + gap, initialY, btnWidth, BUTTON_HEIGHT).build();
        addKeyframe.setTooltip(Tooltip.create(Translation.message("screen.cutscene_controller.tooltip.keyframe")));

        Button addTriggerKeyframe = Button.builder(ImageFontConstants.ADD_KEYFRAME_TRIGGER, button -> {
            KeyframeTriggerScreen screen = new KeyframeTriggerScreen(cutsceneController, cutsceneController.getCurrentTick());
            minecraft.setScreen(screen);
        }).bounds(startX + (btnWidth + gap) * 2, initialY, btnWidth, BUTTON_HEIGHT).build();
        addTriggerKeyframe.setTooltip(Tooltip.create(Translation.message("screen.cutscene_controller.tooltip.keyframe_trigger")));

        this.addRenderableWidget(createKeyframeGroup);
        this.addRenderableWidget(addKeyframe);
        this.addRenderableWidget(addTriggerKeyframe);
    }

    private void initSettingsButton() {
        int btnWidth = 30;
        int controllerStartX = (this.width - totalWidthControllerBtn) / 2;
        int startX = controllerStartX + totalWidthControllerBtn + 15;

        Button settingsButton = Button.builder(ImageFontConstants.SETTINGS, button -> {
            client.execute(() -> client.setScreen(new CutsceneSettingsScreen(cutsceneController, this, Translation.message("screen.cutscenes_settings.name"))));
        }).bounds(startX, initialY, btnWidth, BUTTON_HEIGHT).build();
        this.addRenderableWidget(settingsButton);

        startX = settingsButton.getX() + settingsButton.getWidth() + 5;

        Button saveButton = Button.builder(ImageFontConstants.SAVE, button -> {
            NarrativeCraftMod.server.execute(() -> {
                cutsceneController.stopSession(true);
            });
            this.onClose();
        }).bounds(startX, initialY, btnWidth, BUTTON_HEIGHT).build();
        this.addRenderableWidget(saveButton);

        startX = saveButton.getX() + saveButton.getWidth() + 5;

        Button closeButton = Button.builder(Component.literal("✖"), button -> {
            NarrativeCraftMod.server.execute(() -> {
                cutsceneController.stopSession(false);
            });
            this.onClose();
        }).bounds(startX, initialY, btnWidth, BUTTON_HEIGHT).build();
        this.addRenderableWidget(closeButton);
    }


    private void playOrPause() {
        if(cutsceneController.isPlaying()) {
            cutsceneController.pause();
            controllerButton.setMessage(playText);
        } else {
            cutsceneController.resume();
            controllerButton.setMessage(pauseText);
        }
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public Button getControllerButton() {
        return controllerButton;
    }

    public Component getPauseText() {
        return pauseText;
    }

    public Component getPlayText() {
        return playText;
    }
}
