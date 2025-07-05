package fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners;

import fr.loudo.narrativecraft.narrative.recordings.actions.EmoteAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;

public class EmoteCraftListeners extends ModsListenerImpl {

    private final ClientEmoteEvents.EmotePlayEvent emotePlayEvent;
    private final ClientEmoteEvents.LocalEmoteStopEvent localEmoteStopEvent;

    public EmoteCraftListeners(ActionDifferenceListener actionDifferenceListener) {
        super(actionDifferenceListener);
        emotePlayEvent = (emoteData, tick, userID) -> {
            EmoteAction emoteAction = new EmoteAction(actionDifferenceListener.getRecording().getTick(), emoteData.getUuid());
            actionDifferenceListener.getActionsData().addAction(emoteAction);
        };
        localEmoteStopEvent = () -> {
            EmoteAction emoteAction = new EmoteAction(actionDifferenceListener.getRecording().getTick(), null);
            actionDifferenceListener.getActionsData().addAction(emoteAction);
        };

    }

    @Override
    public void start() {
        ClientEmoteEvents.EMOTE_PLAY.register(emotePlayEvent);
        ClientEmoteEvents.LOCAL_EMOTE_STOP.register(localEmoteStopEvent);
    }

    @Override
    public void stop() {
        ClientEmoteEvents.EMOTE_PLAY.unregister(emotePlayEvent);
        ClientEmoteEvents.LOCAL_EMOTE_STOP.unregister(localEmoteStopEvent);
    }
}
