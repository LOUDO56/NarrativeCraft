package fr.loudo.narrativecraft.narrative.recordings.actions.modsListeners;

import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionDifferenceListener;

public abstract class ModsListenerImpl {

    protected ActionDifferenceListener actionDifferenceListener;

    public ModsListenerImpl(ActionDifferenceListener actionDifferenceListener) {
        this.actionDifferenceListener = actionDifferenceListener;
    }

    public abstract void start();
    public abstract void stop();

}
