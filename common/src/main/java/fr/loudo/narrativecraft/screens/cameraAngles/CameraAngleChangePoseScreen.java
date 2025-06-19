package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.character.CharacterStoryData;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.List;


public class CameraAngleChangePoseScreen extends Screen {
    private final LivingEntity livingEntity;
    private final CharacterStoryData characterStoryData;

    public CameraAngleChangePoseScreen(CharacterStoryData characterStoryData) {
        super(Component.literal("Change pose camera angle screen"));
        this.livingEntity = characterStoryData.getCharacterStory().getEntity();
        this.characterStoryData = characterStoryData;
    }

    @Override
    protected void init() {
        List<Pose> poseList = List.of(Pose.STANDING, Pose.CROUCHING, Pose.SLEEPING, Pose.FALL_FLYING, Pose.SHOOTING);
        int gap = 3;
        int startY = this.height / 2 - gap - 5 * poseList.size() - 20;
        int startX = this.width - 80 - 10;
        for(Pose pose : poseList) {
            Button poseButton = Button.builder(Component.literal(pose.name()), button -> {
                livingEntity.setPose(pose);
                characterStoryData.setPose(pose);
                SynchedEntityData entityData = livingEntity.getEntityData();
                EntityDataAccessor<Byte> ENTITY_BYTE_MASK = new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);
                EntityDataAccessor<Byte> LIVING_ENTITY_BYTE_MASK = new EntityDataAccessor<>(8, EntityDataSerializers.BYTE);
                byte currentMask = entityData.get(ENTITY_BYTE_MASK);
                byte currentLivingEntityByte = entityData.get(LIVING_ENTITY_BYTE_MASK);
                if (pose == Pose.CROUCHING) {
                    entityData.set(ENTITY_BYTE_MASK, (byte) (currentMask | 0x02));
                    characterStoryData.setEntityByte((byte) (currentMask | 0x02));
                } else {
                    entityData.set(ENTITY_BYTE_MASK, (byte) (currentMask & ~0x02));
                    characterStoryData.setEntityByte((byte) (currentMask & ~0x02));
                }
                if(pose == Pose.SHOOTING) {
                    byte byteToAdd = 0;
                    if(!livingEntity.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
                        byteToAdd = 1;
                    } else if (!livingEntity.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                        byteToAdd = 3;
                    }
                    if(currentLivingEntityByte == 0) {
                        entityData.set(LIVING_ENTITY_BYTE_MASK, byteToAdd);
                        characterStoryData.setLivingEntityByte(byteToAdd);
                    } else {
                        entityData.set(LIVING_ENTITY_BYTE_MASK, (byte) 0);
                        characterStoryData.setLivingEntityByte((byte) 0);
                    }
                }
            }).width(80).pos(startX, startY).build();
            startY += poseButton.getHeight() + gap;
            this.addRenderableWidget(poseButton);
        }
        Button closeBtn = Button.builder(Translation.message("global.close"), button -> {
            this.onClose();
        }).width(80).pos(startX, startY).build();
        this.addRenderableWidget(closeBtn);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void renderBlurredBackground() {}
}
