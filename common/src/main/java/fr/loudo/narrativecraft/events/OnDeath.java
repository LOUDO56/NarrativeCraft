package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.actions.DeathAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.RespawnAction;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class OnDeath {

    public static void death(LivingEntity entity) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(Minecraft.getInstance().player);
        if(recording != null && recording.isRecording()) {
            ActionsData actionsData = recording.getActionDataFromEntity(entity);
            if(actionsData == null) return;
            DeathAction deathAction = new DeathAction(recording.getTick(), actionsData.getEntityIdRecording());
            actionsData.addAction(deathAction);
        }
    }

}
