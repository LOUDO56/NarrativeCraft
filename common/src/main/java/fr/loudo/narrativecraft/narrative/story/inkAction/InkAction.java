package fr.loudo.narrativecraft.narrative.story.inkAction;

import fr.loudo.narrativecraft.narrative.story.StoryHandler;

public abstract class InkAction {

    protected String name;
    protected StoryHandler storyHandler;

    public InkAction(StoryHandler storyHandler) {
        this.storyHandler = storyHandler;
    }

    public abstract boolean execute(String[] command);
    abstract void sendDebugDetails();

}
