package fr.loudo.narrativecraft.screens.storyManager.scenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.animations.AnimationsScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles.CameraAnglesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cutscenes.CutscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.npcs.NpcScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes.SubscenesScreen;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.List;

public class ScenesMenuScreen extends OptionsSubScreen {

    private final Scene scene;
    private StoryElementList storyElementList;

    public ScenesMenuScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.scene_menu.title", Component.literal(scene.getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(ImageFontConstants.FOLDER, button -> {
            Util.getPlatform().openPath(new File(NarrativeCraftFile.getSceneFolder(scene), "data").toPath());
        }).width(25).build());
    }

    @Override
    public void onClose() {
        if(Utils.getSessionOrNull(this.minecraft.player.getUUID()) != null) {
            this.minecraft.setScreen(null);
        } else {
            ScenesScreen screen = new ScenesScreen(scene.getChapter());
            this.minecraft.setScreen(screen);
        }
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = List.of(
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.animations"), b -> minecraft.setScreen(new AnimationsScreen(scene))).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.camera_angles"), b -> minecraft.setScreen(new CameraAnglesScreen(scene))).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.cutscenes"), b -> minecraft.setScreen(new CutscenesScreen(scene))).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.subscenes"), b -> minecraft.setScreen(new SubscenesScreen(scene))).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.npc"), b -> minecraft.setScreen(new NpcScreen(scene))).build())
        );
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }


    @Override
    protected void addFooter() {
        int width = 200;
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        if(Utils.getSessionOrNull(this.minecraft.getUser().getProfileId()) != null) {
            width = 100;
            linearLayout.addChild(Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> {
                ScenesScreen screen = new ScenesScreen(scene.getChapter());
                this.minecraft.setScreen(screen);
            }).width(width).build());
        }

        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, (p_345997_) -> this.onClose()).width(width).build());
    }

    @Override
    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }

    public Scene getScene() {
        return scene;
    }
}
