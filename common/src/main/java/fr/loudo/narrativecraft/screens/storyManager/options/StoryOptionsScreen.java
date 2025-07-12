package fr.loudo.narrativecraft.screens.storyManager.options;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.MainScreenController;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;

import java.util.ArrayList;
import java.util.List;

public class StoryOptionsScreen extends StoryElementScreen {

    public StoryOptionsScreen() {
        super(null, Minecraft.getInstance().options, Translation.message("screen.story_options.title"));
    }

    @Override
    protected void addTitle() {
        linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
            openFolder();
        }).width(25).build());
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose()).width(200).build());
    }

    protected void openFolder() {
        Util.getPlatform().openPath(NarrativeCraftFile.dataDirectory.toPath());
    }

    @Override
    protected void addContents() {

        List<StoryElementList.StoryEntryData> entries = new ArrayList<>();

        entries.add(new StoryElementList.StoryEntryData(
                Button.builder(Translation.message("screen.story_options.main_screen"), button -> {
                    CameraAngleGroup group = NarrativeCraftFile.getMainScreenBackgroundFile();
                    MainScreenController mainScreenController = new MainScreenController(group, Utils.getServerPlayerByUUID(this.minecraft.player.getUUID()), Playback.PlaybackType.DEVELOPMENT);
                    mainScreenController.startSession();
                }).build()
        ));

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

}
