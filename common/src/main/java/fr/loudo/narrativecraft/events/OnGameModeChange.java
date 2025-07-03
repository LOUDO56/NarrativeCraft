package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.recordings.Recording;
import fr.loudo.narrativecraft.narrative.recordings.actions.GameModeAction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class OnGameModeChange {

    public static void gameModeChange(GameType gameType, GameType oldGameType, Player player) {
        Recording recording = NarrativeCraftMod.getInstance().getRecordingHandler().getRecordingOfPlayer(player);
        if(recording != null) {
            GameModeAction gameModeAction = new GameModeAction(recording.getTick(), gameType, oldGameType);
            recording.getActionsData().addAction(gameModeAction);
        }
    }

}
