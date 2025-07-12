package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.dialog.Dialog2d;
import fr.loudo.narrativecraft.narrative.dialog.DialogImpl;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StorySave;
import fr.loudo.narrativecraft.narrative.story.inkAction.BorderInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.FadeScreenInkAction;
import fr.loudo.narrativecraft.narrative.story.inkAction.InkAction;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.List;

public class OnHudRender {

    public static void dialogHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {

        Dialog2d dialog2dTest = NarrativeCraftMod.getInstance().getTestDialog2d();
        if(dialog2dTest != null) {
            dialog2dTest.render(guiGraphics, deltaTracker);
        }

        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        if(storyHandler.isLoading()) return;
        DialogImpl dialog = storyHandler.getCurrentDialogBox();
        if(dialog instanceof Dialog2d dialog2d) {
            dialog2d.render(guiGraphics, deltaTracker);
        }
    }

    public static void fadeRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        if(!storyHandler.isRunning()) return;
        if(storyHandler.isLoading()) return;
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

    public static void keyframeControllerBaseRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        if(playerSession == null) return;
        if(playerSession.getKeyframeControllerBase() == null) return;
        if(playerSession.getKeyframeControllerBase().getPlaybackType() == Playback.PlaybackType.DEVELOPMENT) {
            if(playerSession.getKeyframeControllerBase().getCurrentPreviewKeyframe() == null) {
                playerSession.getKeyframeControllerBase().renderHUDInfo(guiGraphics);
            }
        }
    }

    public static void borderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        if(storyHandler.isLoading()) return;
        for(InkAction inkAction : storyHandler.getInkActionList()) {
            if(inkAction instanceof BorderInkAction borderInkAction) {
                borderInkAction.render(guiGraphics, deltaTracker);
            }
        }
    }

    public static void loadingHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        StoryHandler storyHandler = NarrativeCraftMod.getInstance().getStoryHandler();
        if(storyHandler == null) return;
        if(!storyHandler.isLoading()) return;
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.fill(
                0,
                0,
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight(),
                ARGB.colorFromFloat(1, 0, 0, 0)
        );
        Component loadingComp = Translation.message("screen.main_screen.loading");
        guiGraphics.drawString(
                minecraft.font,
                loadingComp,
                guiGraphics.guiWidth() - minecraft.font.width(loadingComp) - 10,
                guiGraphics.guiHeight() - minecraft.font.lineHeight - 10,
                ARGB.colorFromFloat(1, 1, 1, 1)
        );
    }
}
