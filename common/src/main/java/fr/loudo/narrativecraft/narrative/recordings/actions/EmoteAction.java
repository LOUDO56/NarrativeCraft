package fr.loudo.narrativecraft.narrative.recordings.actions;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.utils.FakePlayer;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class EmoteAction extends Action {

    private final UUID emoteId;

    public EmoteAction(int tick, ActionType actionType, UUID emoteId) {
        super(tick, actionType);
        this.emoteId = emoteId;
    }

    @Override
    public void execute(LivingEntity entity) {
        if(entity instanceof FakePlayer fakePlayer) {
            KeyframeAnimation emote = null;
            if(emoteId != null) {
                for(KeyframeAnimation keyframeAnimation : ClientEmoteAPI.clientEmoteList()) {
                    if(keyframeAnimation.getUuid().equals(emoteId)) {
                        emote = keyframeAnimation;
                    }
                }
            }
            ServerEmoteAPI.playEmote(fakePlayer.getUUID(), emote, false);
        }
    }


}
