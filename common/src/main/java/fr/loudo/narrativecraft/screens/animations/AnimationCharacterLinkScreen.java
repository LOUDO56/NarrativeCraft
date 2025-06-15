package fr.loudo.narrativecraft.screens.animations;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AnimationCharacterLinkScreen extends OptionsSubScreen {
    private CharacterList characterList;
    private final List<CharacterStory> characterStoryList;
    private final Animation animation;
    private CharacterStory.CharacterType characterType;

    public AnimationCharacterLinkScreen(Screen lastScreen, Animation animation, List<CharacterStory> characterStoryList, CharacterStory.CharacterType characterType) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Link animation to character"));
        this.animation = animation;
        this.characterStoryList = characterStoryList;
        this.characterType = characterType;
    }

    public AnimationCharacterLinkScreen(Screen lastScreen, Animation animation) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Link animation to character"));
        this.animation = animation;
        this.characterStoryList = NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories();
        this.characterType = CharacterStory.CharacterType.MAIN;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(characterType == CharacterStory.CharacterType.NPC ? Component.literal("NPC") : Component.literal("MAIN"), button -> {
            Screen screen = null;
            if(characterType == CharacterStory.CharacterType.MAIN) {
                screen = new AnimationCharacterLinkScreen(lastScreen, animation, animation.getScene().getNpcs(), CharacterStory.CharacterType.NPC);
            } else if(characterType == CharacterStory.CharacterType.NPC) {
                screen = new AnimationCharacterLinkScreen(lastScreen, animation, NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories(), CharacterStory.CharacterType.MAIN);
            }
            minecraft.setScreen(screen);
        }).width(25).build());
    }

    protected void addContents() {
        this.characterList = this.layout.addToContents(new CharacterList(this.minecraft));
    }

    protected void addOptions() {
    }

    protected void repositionElements() {
        super.repositionElements();
        this.characterList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        CharacterList.Entry entry = this.characterList.getSelected();
        if(entry == null) {
            minecraft.setScreen(null);
            return;
        }
        CharacterStory selectedCharacter = entry.characterStory;
        CharacterStory oldCharacter = animation.getCharacter();
        animation.setCharacter(selectedCharacter);
        if(lastScreen != null) {
            minecraft.setScreen(animation.reloadScreen());
        } else {
            minecraft.setScreen(null);
        }
        if(!NarrativeCraftFile.updateAnimationFile(animation)) {
            animation.setCharacter(oldCharacter);
            ScreenUtils.sendToast(Translation.message("global.error"), Translation.message("screen.animation_manager.update.failed", animation.getName()));
        }
    }

    class CharacterList extends ObjectSelectionList<CharacterList.Entry> {
        public CharacterList(Minecraft minecraft) {
            super(minecraft, AnimationCharacterLinkScreen.this.width, AnimationCharacterLinkScreen.this.height - 33 - 53, 33, 18);
            String selectedCharacter;
            if(animation.getCharacter() != null) {
                selectedCharacter = animation.getCharacter().getName();
            } else {
                selectedCharacter = "";
            }
            characterStoryList.forEach(characterStory1 -> {
                Entry entry = new Entry(characterStory1);
                this.addEntry(entry);
                if(selectedCharacter.equalsIgnoreCase(characterStory1.getName())) {
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
                p_345300_.drawCenteredString(AnimationCharacterLinkScreen.this.font, this.characterStory.getName(), CharacterList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
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
                CharacterList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(characterStory.getName());
            }

        }
    }
}
