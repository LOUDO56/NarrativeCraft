package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.narrative.story.inkAction.FadeScreenInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

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
        if(!storyHandler.isRunning()) return;
        List<InkAction> toRemove = new ArrayList<>();
        List<InkAction> inkActionToLoop = List.copyOf(storyHandler.getInkActionList());
        for(InkAction inkAction : inkActionToLoop) {
            if(inkAction instanceof FadeScreenInkAction fadeScreenInkAction) {
                fadeScreenInkAction.render(guiGraphics, deltaTracker);
                if(fadeScreenInkAction.isDoneFading()) {
                    toRemove.add(fadeScreenInkAction);
                }
            }
        }
        storyHandler.getInkActionList().removeAll(toRemove);
    }

    public static void saveIconRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        if(storyHandler.isSaving()) {
            StorySave.showSaveIcon(guiGraphics, deltaTracker);
        }
    }
}
