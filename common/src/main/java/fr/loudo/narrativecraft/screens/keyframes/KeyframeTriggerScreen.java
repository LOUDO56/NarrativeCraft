package fr.loudo.narrativecraft.screens.keyframes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.KeyframeTrigger;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class KeyframeTriggerScreen extends Screen {

    private final int gap = 5;
    private final int tickBoxHeight = 20;
    private final int tickBoxWidth = 60;
    private final int commandBoxHeight = 120;
    private final int globalWidth = 240;

    private int defaultTick;
    private ScreenUtils.LabelBox tickBox;
    private ScreenUtils.MultilineLabelBox commandBox;

    private CutsceneController cutsceneController;
    private KeyframeTrigger keyframeTrigger;

    public KeyframeTriggerScreen(CutsceneController cutsceneController, int defaultTick) {
        super(Component.literal("Keyframe Trigger Screen"));
        this.cutsceneController = cutsceneController;
        this.defaultTick = defaultTick;
    }

    public KeyframeTriggerScreen(CutsceneController cutsceneController, KeyframeTrigger keyframeTrigger) {
        super(Component.literal("Keyframe Trigger Screen"));
        this.cutsceneController = cutsceneController;
        this.keyframeTrigger = keyframeTrigger;
    }

    @Override
    protected void init() {

        int totalHeight = tickBoxHeight + gap +
                commandBoxHeight + gap +
                20 + gap +
                20;

        if(keyframeTrigger != null) {
            totalHeight += 20 + gap + 20;
        }

        int startY = (this.height - totalHeight) / 2;

        int xGlobal = (this.width - globalWidth) / 2;

        int currentY = startY;

        tickBox = new ScreenUtils.LabelBox(
                Component.literal("Tick"),
                minecraft.font,
                tickBoxWidth,
                tickBoxHeight,
                xGlobal,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        tickBox.getEditBox().setFilter(s -> s.matches("^[+]?\\d+([.]\\d+)?$"));
        this.addRenderableWidget(tickBox.getStringWidget());
        this.addRenderableWidget(tickBox.getEditBox());

        currentY += tickBoxHeight + gap;

        commandBox = new ScreenUtils.MultilineLabelBox(
                Component.literal("Tags"),
                minecraft.font,
                globalWidth,
                commandBoxHeight,
                xGlobal,
                currentY,
                Component.literal("animation start cathy_walk\ntime set 6000 to 90000 for 6 seconds\n...")
        );
        this.addRenderableWidget(commandBox.getStringWidget());
        this.addRenderableWidget(commandBox.getMultiLineEditBox());

        currentY += commandBoxHeight + commandBox.getStringWidget().getHeight() + gap * 2;

        Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            List<String> tags = Arrays.stream(commandBox.getMultiLineEditBox().getValue().split("\n")).toList();
            for (int i = 0; i < tags.size(); i++) {
                String tag = tags.get(i);
                if(tag.startsWith("wait")) {
                    ScreenUtils.sendToast(
                            Translation.message("global.error"),
                            Translation.message("cutscene.keyframe-trigger.wait.unsupported")
                    );
                    return;
                }
                if(tag.equals("save")) {
                    ScreenUtils.sendToast(
                            Translation.message("global.error"),
                            Translation.message("cutscene.keyframe-trigger.save.unsupported")
                    );
                    return;
                }
                if(tag.equals("on enter")) {
                    ScreenUtils.sendToast(
                            Translation.message("global.error"),
                            Translation.message("cutscene.keyframe-trigger.on-enter.unsupported")
                    );
                    return;
                }
                InkAction.InkTagType tagType = InkAction.getInkActionTypeByTag(tag);
                if(tagType != null) {
                    InkAction inkAction = InkAction.getInkAction(tagType);
                    if(inkAction != null) {
                        InkAction.ErrorLine errorLine = inkAction.validate(tag.split(" "), i + 1, tag, cutsceneController.getCutscene().getScene());
                        if(errorLine != null) {
                            String lineText = errorLine.getLineText();
                            if(lineText.length() >= 40) {
                                lineText = lineText.substring(0, 40) + "...";
                            }
                            ScreenUtils.sendToast(
                                    Component.literal(lineText) ,
                                    Component.empty().append(errorLine.getMessage()).withColor(0xF24949)
                            );
                            return;
                        }
                    }
                }
            }
            if(keyframeTrigger == null) {
                cutsceneController.addKeyframeTrigger(commandBox.getMultiLineEditBox().getValue(), Integer.parseInt(tickBox.getEditBox().getValue()));
            } else {
                keyframeTrigger.setTick(Integer.parseInt(tickBox.getEditBox().getValue()));
                keyframeTrigger.setCommands(commandBox.getMultiLineEditBox().getValue());
            }
            this.onClose();
        }).width(globalWidth).pos(xGlobal, currentY).build();
        this.addRenderableWidget(doneButton);

        currentY += 20 + gap;

        Button closeButton = Button.builder(Translation.message("global.close"), button -> {
            this.onClose();
        }).width(globalWidth).pos(xGlobal, currentY).build();
        this.addRenderableWidget(closeButton);

        if(keyframeTrigger != null) {
            currentY += 20 + gap;

            Button removeButton = Button.builder(Translation.message("global.remove"), button -> {
                ConfirmScreen confirmScreen = new ConfirmScreen(b -> {
                    if(b) {
                        cutsceneController.removeKeyframeTrigger(keyframeTrigger);
                        onClose();
                    } else {
                        KeyframeTriggerScreen screen = new KeyframeTriggerScreen(cutsceneController, keyframeTrigger);
                        minecraft.setScreen(screen);
                    }
                }, Component.literal(""), Translation.message("global.confirm_delete"),
                        CommonComponents.GUI_YES, CommonComponents.GUI_CANCEL);
                minecraft.setScreen(confirmScreen);
            }).width(globalWidth).pos(xGlobal, currentY).build();

            this.addRenderableWidget(removeButton);
        }

        if(keyframeTrigger != null) {
            tickBox.getEditBox().setValue(String.valueOf(keyframeTrigger.getTick()));
            commandBox.getMultiLineEditBox().setValue(keyframeTrigger.getCommands());
        } else {
            tickBox.getEditBox().setValue(String.valueOf(defaultTick));
        }

    }
}
