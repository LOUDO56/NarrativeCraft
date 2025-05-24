package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.inkAction.FadeScreenInkAction;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class OnHudRender {

    public static void hudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(NarrativeCraftMod.getInstance().getTestDialog() != null) {
            NarrativeCraftMod.getInstance().getTestDialog().render(guiGraphics, deltaTracker);
        }
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        if(storyHandler.getCurrentDialogBox() == null) return;
        storyHandler.getCurrentDialogBox().render(guiGraphics, deltaTracker);
    }

    public static void fadeRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        FadeScreenInkAction fadeScreenInkAction = storyHandler.getFadeScreenInkAction();
        if(fadeScreenInkAction == null) return;
        fadeScreenInkAction.render(guiGraphics, deltaTracker);
    }

}
