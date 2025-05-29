package fr.loudo.narrativecraft.screens.animations;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.utils.Easing;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class AnimationCharacterLinkScreen extends OptionsSubScreen {
    private CutsceneEasingsList cutsceneEasingsList;
    private final Animation animation;

    public AnimationCharacterLinkScreen(Screen lastScreen, Animation animation) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Link animation to character"));
        this.animation = animation;
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
        CharacterStory selectedCharacter = entry.characterStory;
        CharacterStory oldCharacter = animation.getCharacter();
        animation.setCharacter(selectedCharacter);
        if(lastScreen != null) {
            this.minecraft.setScreen(animation.reloadScreen());
        } else {
            this.minecraft.setScreen(null);
        }
        if(!NarrativeCraftFile.updateAnimationFile(animation)) {
            animation.setCharacter(oldCharacter);
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.animation_manager.update.failed", animation.getName()));
        }
    }

    class CutsceneEasingsList extends ObjectSelectionList<CutsceneEasingsList.Entry> {
        public CutsceneEasingsList(Minecraft minecraft) {
            super(minecraft, AnimationCharacterLinkScreen.this.width, AnimationCharacterLinkScreen.this.height - 33 - 53, 33, 18);
            String selectedCharacter;
            if(animation.getCharacter() != null) {
                selectedCharacter = animation.getCharacter().getName();
            } else {
                selectedCharacter = "";
            }
            NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories().forEach(characterStory1 -> {
                Entry entry = new Entry(characterStory1);
                this.addEntry(entry);
                if(selectedCharacter.equals(characterStory1.getName())) {
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

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final CharacterStory characterStory;

            public Entry(CharacterStory characterStory) {
                this.characterStory = characterStory;
            }

            public void render(GuiGraphics p_345300_, int p_345469_, int p_345328_, int p_345700_, int p_345311_, int p_345185_, int p_344805_, int p_345963_, boolean p_345912_, float p_346091_) {
                p_345300_.drawCenteredString(AnimationCharacterLinkScreen.this.font, this.characterStory.getName(), CutsceneEasingsList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    AnimationCharacterLinkScreen.this.onClose();
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
                return Component.literal(characterStory.getName());
            }

        }
    }
}
