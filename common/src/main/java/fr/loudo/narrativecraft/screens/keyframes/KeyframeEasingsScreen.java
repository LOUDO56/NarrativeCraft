package fr.loudo.narrativecraft.screens.keyframes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class KeyframeEasingsScreen extends OptionsSubScreen {
    private CutsceneEasingsList cutsceneEasingsList;
    private final Keyframe keyframe;

    public KeyframeEasingsScreen(Screen lastScreen, Options options, Keyframe keyframe) {
        super(lastScreen, options, Translation.message("screen.keyframe_advanced.easings"));
        this.keyframe = keyframe;
    }

    protected void addContents() {
        this.cutsceneEasingsList = this.layout.addToContents(new CutsceneEasingsList(this.minecraft));
    }

    protected void addOptions() {
    }

    protected void repositionElements() {
        super.repositionElements();
        this.cutsceneEasingsList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        CutsceneEasingsList.Entry entry = this.cutsceneEasingsList.getSelected();
        String selectedEasing = entry.easing.name();
        keyframe.setEasing(Easing.valueOf(selectedEasing));
        this.minecraft.setScreen(this.lastScreen);
        super.onClose();
    }

    class CutsceneEasingsList extends ObjectSelectionList<CutsceneEasingsList.Entry> {
        public CutsceneEasingsList(Minecraft minecraft) {
            super(minecraft, KeyframeEasingsScreen.this.width, KeyframeEasingsScreen.this.height - 33 - 53, 33, 18);
            String selectedEasing = keyframe.getEasing().name();
            Easing.getEasings().forEach(easing -> {
                Entry entry = new Entry(easing);
                this.addEntry(entry);
                if(selectedEasing.equals(easing.name())) {
                    this.setSelected(entry);
                }
            });
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }

        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class Entry extends ObjectSelectionList.Entry<CutsceneEasingsList.Entry> {
            private final Easing easing;

            public Entry(Easing easing) {
                this.easing = easing;
            }

            public void render(GuiGraphics p_345300_, int p_345469_, int p_345328_, int p_345700_, int p_345311_, int p_345185_, int p_344805_, int p_345963_, boolean p_345912_, float p_346091_) {
                p_345300_.drawCenteredString(KeyframeEasingsScreen.this.font, this.easing.name(), CutsceneEasingsList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    KeyframeEasingsScreen.this.onClose();
                    return true;
                } else {
                    return super.keyPressed(p_346403_, p_345881_, p_345858_);
                }
            }

            public boolean mouseClicked(double p_344965_, double p_345385_, int p_345080_) {
                this.select();
                return super.mouseClicked(p_344965_, p_345385_, p_345080_);
            }

            private void select() {
                CutsceneEasingsList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(easing.name());
            }

        }
    }
}
