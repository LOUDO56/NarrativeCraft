package fr.loudo.narrativecraft.screens.components;

import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class ChangeSkinLinkScreen extends OptionsSubScreen {
    private SkinList skinList;
    private final CharacterStory characterStory;
    private final Consumer<String> stringCallback;

    public ChangeSkinLinkScreen(CharacterStory characterStory, Consumer<String> stringCallback) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.change_skin_link.title", characterStory.getName()));
        this.characterStory = characterStory;
        this.stringCallback = stringCallback;
    }

    public ChangeSkinLinkScreen(Screen lastScreen, CharacterStory characterStory, Consumer<String> stringCallback) {
        super(lastScreen, Minecraft.getInstance().options, Translation.message("screen.change_skin_link.title", characterStory.getName()));
        this.characterStory = characterStory;
        this.stringCallback = stringCallback;
    }

    protected void addContents() {
        this.skinList = this.layout.addToContents(new SkinList(this.minecraft, characterStory.getCharacterSkinController().getSkins()));
    }

    protected void addOptions() {
    }

    protected void repositionElements() {
        super.repositionElements();
        this.skinList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        SkinList.Entry entry = this.skinList.getSelected();
        File selectedSkin = entry.skin;
        characterStory.getCharacterSkinController().setCurrentSkin(selectedSkin);
        handleSkin(selectedSkin.getName());
        minecraft.setScreen(lastScreen);
    }

    private void handleSkin(String skin) {
        stringCallback.accept(skin);
    }

    class SkinList extends ObjectSelectionList<SkinList.Entry> {
        public SkinList(Minecraft minecraft, List<File> skins) {
            super(minecraft, ChangeSkinLinkScreen.this.width, ChangeSkinLinkScreen.this.height - 33 - 53, 33, 18);
            String selectedSkin = characterStory.getCharacterSkinController().getCurrentSkin().getName();
            skins.forEach(file -> {
                Entry entry = new Entry(file);
                this.addEntry(entry);
                if(selectedSkin.equalsIgnoreCase(file.getName())) {
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
            private final File skin;

            public Entry(File skin) {
                this.skin = skin;
            }

            public void render(GuiGraphics p_345300_, int p_345469_, int p_345328_, int p_345700_, int p_345311_, int p_345185_, int p_344805_, int p_345963_, boolean p_345912_, float p_346091_) {
                p_345300_.drawCenteredString(ChangeSkinLinkScreen.this.font, this.skin.getName().split("\\.")[0], SkinList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    ChangeSkinLinkScreen.this.onClose();
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
                SkinList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(skin.getName());
            }

        }
    }
}
