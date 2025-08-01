package fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners;

import fr.loudo.narrativecraft.narrative.recordings.actions.EmoteAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;

public class EmoteCraftListeners extends ModsListenerImpl {

    private final ServerEmoteEvents.EmotePlayEvent emotePlayEvent;
    private final ServerEmoteEvents.EmoteStopEvent emoteStopEvent;

    public EmoteCraftListeners(ActionDifferenceListener actionDifferenceListener) {
        super(actionDifferenceListener);
        emotePlayEvent = (emoteData, tick, userID) -> {
            if(actionDifferenceListener.getActionsData().getEntity().getUUID().equals(userID)) {
                EmoteAction emoteAction = new EmoteAction(actionDifferenceListener.getRecording().getTick(), emoteData.get());
                actionDifferenceListener.getActionsData().addAction(emoteAction);
            }
        };
        emoteStopEvent = (uuid, uuid1) -> {
            if(actionDifferenceListener.getActionsData().getEntity().getUUID().equals(uuid1)) {
                EmoteAction emoteAction = new EmoteAction(actionDifferenceListener.getRecording().getTick(), null);
                actionDifferenceListener.getActionsData().addAction(emoteAction);
            }
        };

    }

    @Override
    public void start() {
        ServerEmoteEvents.EMOTE_PLAY.register(emotePlayEvent);
        ServerEmoteEvents.EMOTE_STOP_BY_USER.register(emoteStopEvent);
    }

    @Override
    public void stop() {
        ServerEmoteEvents.EMOTE_PLAY.unregister(emotePlayEvent);
        ServerEmoteEvents.EMOTE_STOP_BY_USER.unregister(emoteStopEvent);
    }
}
