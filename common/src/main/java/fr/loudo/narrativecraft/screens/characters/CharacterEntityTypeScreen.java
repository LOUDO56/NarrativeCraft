package fr.loudo.narrativecraft.screens.characters;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class CharacterEntityTypeScreen extends OptionsSubScreen {

    private EntityTypeList entityTypeList;
    private final List<EntityType<?>> entityTypes;
    private final CharacterStory characterStory;

    public CharacterEntityTypeScreen(Screen lastScreen, CharacterStory characterStory) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Change Character Entity Type"));
        this.entityTypes = NarrativeCraftMod.getInstance().getCharacterManager().getAvailableEntityTypes();
        this.characterStory = characterStory;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    protected void addContents() {
        this.entityTypeList = this.layout.addToContents(new CharacterEntityTypeScreen.EntityTypeList(this.minecraft));
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.entityTypeList.updateSize(this.width, this.layout);
    }

    @Override
    public void onClose() {
        EntityTypeList.Entry entry = entityTypeList.getSelected();
        if(entry == null) {
            minecraft.setScreen(null);
            return;
        }
        EntityType<?> entityType = entry.entityType;
        characterStory.updateEntityType(entityType);
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void addOptions() {}

    class EntityTypeList extends ObjectSelectionList<CharacterEntityTypeScreen.EntityTypeList.Entry> {
        public EntityTypeList(Minecraft minecraft) {
            super(minecraft, CharacterEntityTypeScreen.this.width, CharacterEntityTypeScreen.this.height - 33 - 53, 33, 18);
            int selectedEntityTypeId = characterStory.getEntityTypeId();
            entityTypes.forEach(entityType -> {
                CharacterEntityTypeScreen.EntityTypeList.Entry entry = new CharacterEntityTypeScreen.EntityTypeList.Entry(entityType);
                this.addEntry(entry);
                if(selectedEntityTypeId == BuiltInRegistries.ENTITY_TYPE.getId(entityType)) {
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

        public class Entry extends ObjectSelectionList.Entry<CharacterEntityTypeScreen.EntityTypeList.Entry> {
            private final EntityType<?> entityType;

            public Entry(EntityType<?> entityType) {
                this.entityType = entityType;
            }

            public void render(GuiGraphics p_345300_, int p_345469_, int p_345328_, int p_345700_, int p_345311_, int p_345185_, int p_344805_, int p_345963_, boolean p_345912_, float p_346091_) {
                p_345300_.drawCenteredString(CharacterEntityTypeScreen.this.font, this.entityType.getDescription().getString().toUpperCase(), CharacterEntityTypeScreen.EntityTypeList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    CharacterEntityTypeScreen.this.onClose();
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
                CharacterEntityTypeScreen.EntityTypeList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.literal(characterStory.getName());
            }

        }
    }
}
