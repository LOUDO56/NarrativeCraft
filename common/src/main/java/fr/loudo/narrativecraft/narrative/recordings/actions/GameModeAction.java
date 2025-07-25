package fr.loudo.narrativecraft.narrative.recordings.actions;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.world.level.GameType;

public class GameModeAction extends Action {

    private final GameType gameType;
    private final GameType oldGameType;

    public GameModeAction(int tick, GameType gameType, GameType oldGameType) {
        super(tick, ActionType.GAMEMODE);
        this.gameType = gameType;
        this.oldGameType = oldGameType;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            fakePlayer.setGameMode(gameType);
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            fakePlayer.setGameMode(oldGameType);
        }
    }

    public GameType getGameType() {
        return gameType;
    }

    public GameType getOldGameType() {
        return oldGameType;
    }
}
