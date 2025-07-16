package fr.loudo.narrativecraft.screens.choices;

import com.bladecoder.ink.runtime.Choice;
import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.keys.ModKeys;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.components.ChoiceButtonWidget;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class ChoicesScreen extends Screen {

    private static final int APPEAR_TIME = 130;
    private static final int OFFSET = 10;

    private final List<Choice> choiceList;
    private final List<AnimatedChoice> animatedChoices;
    private boolean initiated;
    private int baseY;
    private int spacing;
    private long startTime;
    private double t;

    public ChoicesScreen(List<Choice> choiceList, boolean animate) {
        super(Component.literal("Choice screen"));
        this.choiceList = choiceList;
        this.animatedChoices = new ArrayList<>();
        initiated = !animate;
    }

    public static ChoicesScreen fromStrings(List<String> stringChoiceList) {
        List<Choice> choices = new ArrayList<>();
        for (String choiceString : stringChoiceList) {
            Choice choice = new Choice();
            choice.setIndex(0);
            choice.setText(choiceString);
            choices.add(choice);
        }
        return new ChoicesScreen(choices, true);
    }

    @Override
    protected void init() {
        if(!initiated) {
            ResourceLocation soundRes = ResourceLocation.withDefaultNamespace("sfx.choice_appear");
            SoundEvent sound = SoundEvent.createVariableRangeEvent(soundRes);
            this.minecraft.player.playSound(sound, 1.0F, 1.0F);
        }
        List<ChoiceButtonWidget> choiceButtonWidgetList = new ArrayList<>();
        for(Choice choice : choiceList) {
            choiceButtonWidgetList.add(new ChoiceButtonWidget(
                    choice
            ));
        }
        spacing = 10;
        baseY = 60;
        int maxWidthUpDown = 0;
        for (int i = 0; i < choiceButtonWidgetList.size(); i++) {
            if(i % 2 != 0) {
                if(choiceButtonWidgetList.get(i).getWidth() > maxWidthUpDown) {
                    maxWidthUpDown = choiceButtonWidgetList.get(i).getWidth();
                }
            }
        }
        for (int i = 0; i < choiceButtonWidgetList.size(); i++) {
            ChoiceButtonWidget choiceButtonWidget = choiceButtonWidgetList.get(i);
            choiceButtonWidget.setOpacity(5);
            choiceButtonWidget.setCanPress(false);
            int currentX = 0;
            int offsetX = 0;
            int offsetY = 0;
            int currentY = this.height - baseY;
            if(choiceButtonWidgetList.size() == 4) currentY -= choiceButtonWidget.getHeight();
            switch (i) {
                case 0:
                    if(choiceButtonWidgetList.size() == 1) {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                    } else if(choiceButtonWidgetList.size() > 2) {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() - maxWidthUpDown / 2;
                    } else {
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() - spacing;
                    }
                    offsetX = OFFSET;
                    break;
                case 1:
                    if(choiceButtonWidgetList.size() > 2) {
                        currentY -= choiceButtonWidget.getHeight() + spacing;
                        currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                        offsetY = OFFSET;
                    } else {
                        currentX = this.width / 2 + spacing;
                        offsetX = -OFFSET;
                    }
                    break;
                case 2:
                    currentX = this.width / 2 + maxWidthUpDown / 2;
                    offsetX = -OFFSET;
                    break;
                case 3:
                    currentY += choiceButtonWidget.getHeight() + spacing;
                    currentX = this.width / 2 - choiceButtonWidget.getWidth() / 2;
                    offsetY = -OFFSET;
                    break;
            }
            choiceButtonWidget.setX(currentX);
            choiceButtonWidget.setY(currentY);
            this.addRenderableWidget(choiceButtonWidget);
            AnimatedChoice animatedChoice = new AnimatedChoice(choiceButtonWidget, offsetX, offsetY);
            animatedChoices.add(animatedChoice);
        }
        if(!initiated) {
            t = 0;
            startTime = System.currentTimeMillis();
            initiated = true;
        } else {
            t = 1;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == InputConstants.KEY_ESCAPE) return false;
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        minecraft.setScreen(null);
        if(storyHandler == null) return super.keyPressed(keyCode, scanCode, modifiers);
        boolean choiceSelected = false;
        try {
            if(keyCode == ModKeys.SELECT_CHOICE_1.getDefaultKey().getValue()) {
                storyHandler.getStory().chooseChoiceIndex(0);
                choiceSelected = true;
            } else if (keyCode == ModKeys.SELECT_CHOICE_2.getDefaultKey().getValue()) {
                storyHandler.getStory().chooseChoiceIndex(1);
                choiceSelected = true;
            } else if (keyCode == ModKeys.SELECT_CHOICE_3.getDefaultKey().getValue()) {
                storyHandler.getStory().chooseChoiceIndex(2);
                choiceSelected = true;
            } else if (keyCode == ModKeys.SELECT_CHOICE_4.getDefaultKey().getValue()) {
                storyHandler.getStory().chooseChoiceIndex(3);
                choiceSelected = true;
            }
            if(choiceSelected) {
                storyHandler.getCurrentChoices().clear();
                storyHandler.next();
            }
        } catch (Exception ignored) {}
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        long elapsedTime = now - startTime;
        for (AnimatedChoice ac : animatedChoices) {
            int newOpacity = (int) MathUtils.lerp(5, 255, t);
            guiGraphics.pose().pushPose();
            if(choiceList.size() > 1) {
                guiGraphics.pose().translate(
                        MathUtils.lerp(ac.offsetX, 0, t),
                        MathUtils.lerp(ac.offsetY, 0, t),
                        0
                );
            }
            ac.widget.setOpacity(newOpacity);
            ac.widget.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.pose().popPose();
            if(t >= 1.0) {
                ac.widget.setCanPress(true);
            }
        }
        t = Math.min(1.0, (double) elapsedTime / APPEAR_TIME);

    }

    @Override
    protected void repositionElements() {
        animatedChoices.clear();
        super.repositionElements();
    }

    @Override
    public void onClose() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    private record AnimatedChoice(ChoiceButtonWidget widget, int offsetX, int offsetY) {
    }
}
