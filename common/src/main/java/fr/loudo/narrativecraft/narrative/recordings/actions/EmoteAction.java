package fr.loudo.narrativecraft.narrative.recordings.actions;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;

import java.util.UUID;

public class EmoteAction extends Action {

    private final UUID emoteId;

    public EmoteAction(int tick, UUID emoteId) {
        super(tick, ActionType.EMOTE);
        this.emoteId = emoteId;
    }

    @Override
    public void execute(Playback.PlaybackData playbackData) {
        if(playbackData.getEntity() instanceof FakePlayer fakePlayer) {
            KeyframeAnimation emote = getEmote();
            ServerEmoteAPI.playEmote(fakePlayer.getUUID(), emote, false);
        }
    }

    @Override
    public void rewind(Playback.PlaybackData playbackData) {}

    public KeyframeAnimation getEmote() {
        if(emoteId != null) {
            for(KeyframeAnimation keyframeAnimation : ClientEmoteAPI.clientEmoteList()) {
                if(keyframeAnimation.getUuid().equals(emoteId)) {
                    return keyframeAnimation;
                }
            }
        }
        return null;
    }

}
