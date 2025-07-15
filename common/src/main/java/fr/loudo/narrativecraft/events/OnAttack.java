package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;

public class OnAttack {

    public static boolean cancelAttack() {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        return storyHandler != null && storyHandler.isRunning();
    }

}
