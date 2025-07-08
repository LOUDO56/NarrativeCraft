package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.mixin.fields.AbstractBoatFields;
import fr.loudo.narrativecraft.mixin.fields.AbstractHorseFields;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.AbstractBoat;

public class AbstractBoatBubbleAction extends Action {

    private final int currentByte;
    private final int oldByte;

    public AbstractBoatBubbleAction(int tick, int currentByte, int oldByte) {
        super(tick, ActionType.ABSTRACT_BOAT_BUBBLE);
        this.currentByte = currentByte;
        this.oldByte = oldByte;
    }

    @Override
    public void execute(Entity entity) {
        Entity entity1 = entity.level().getEntity(entity.getId());
        if(entity1 instanceof AbstractBoat) {
            entity.getEntityData().set(AbstractBoatFields.getDATA_ID_BUBBLE_TIME(), currentByte);
        }
    }

    @Override
    public void rewind(Entity entity) {
        Entity entity1 = entity.level().getEntity(entity.getId());
        if(entity1 instanceof AbstractBoat) {
            entity.getEntityData().set(AbstractBoatFields.getDATA_ID_BUBBLE_TIME(), oldByte);
        }
    }
}
