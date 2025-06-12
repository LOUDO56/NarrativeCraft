package fr.loudo.narrativecraft.screens.components;

import com.mojang.datafixers.util.Pair;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.ItemChangeAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AddCharacterListScreen extends OptionsSubScreen {
    private CharacterList characterList;
    private final CameraAngleGroup cameraAngleGroup;

    public AddCharacterListScreen(CameraAngleGroup cameraAngleGroup) {
        super(null, Minecraft.getInstance().options, Component.literal("Spawn character"));
        this.cameraAngleGroup = cameraAngleGroup;
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
        CharacterStory selectedCharacter = entry.characterStory;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        Vec3 position = localPlayer.position();
        List<Action> itemChangeActions = new ArrayList<>();
        for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = localPlayer.getItemBySlot(equipmentSlot);
            ItemChangeAction itemChangeAction = new ItemChangeAction(
                    0,
                    ActionType.ITEM_CHANGE,
                    equipmentSlot.name(),
                    BuiltInRegistries.ITEM.getId(itemStack.getItem())
            );
            itemChangeActions.add(itemChangeAction);
        }
        CharacterStoryData characterStoryData = cameraAngleGroup.addCharacter(
                selectedCharacter,
                selectedCharacter.getCharacterSkinController().getMainSkinFile().getName(),
                position.x,
                position.y,
                position.z,
                localPlayer.getXRot(),
                localPlayer.getYRot(),
                Playback.PlaybackType.DEVELOPMENT
        );
        if(characterStoryData.getCharacterStory().getEntity() instanceof FakePlayer fakePlayer) {
            for(EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = localPlayer.getItemBySlot(equipmentSlot);
                fakePlayer.getServer().getPlayerList().broadcastAll(new ClientboundSetEquipmentPacket(
                        fakePlayer.getId(),
                        List.of(new Pair<>(equipmentSlot, itemStack))
                ));
                fakePlayer.setItemSlot(equipmentSlot, itemStack);
            }
        }
        minecraft.setScreen(null);
    }

    class CharacterList extends ObjectSelectionList<CharacterList.Entry> {
        public CharacterList(Minecraft minecraft) {
            super(minecraft, AddCharacterListScreen.this.width, AddCharacterListScreen.this.height - 33 - 53, 33, 18);
            String selectedCharacter = "";
            NarrativeCraftMod.getInstance().getCharacterManager().getCharacterStories().forEach(characterStory1 -> {
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
                p_345300_.drawCenteredString(AddCharacterListScreen.this.font, this.characterStory.getName(), CharacterList.this.width / 2, p_345328_ + p_345185_ / 2 - 4, -1);
            }

            public boolean keyPressed(int p_346403_, int p_345881_, int p_345858_) {
                if (CommonInputs.selected(p_346403_)) {
                    this.select();
                    AddCharacterListScreen.this.onClose();
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
