package fr.loudo.narrativecraft.screens.storyManager.scenes;

import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.storyManager.scenes.animations.AnimationsScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cutscenes.CutscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes.SubscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScenesMenuScreen extends OptionsSubScreen {

    private final Scene scene;
    private StoryElementList storyElementList;

    public ScenesMenuScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.scene_menu.title", Component.literal(scene.getName()).withColor(0x5896ED)));
        this.scene = scene;
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    @Override
    public void onClose() {
        ScenesScreen screen = new ScenesScreen(scene.getChapter());
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        Button animationsButton = Button.builder(Translation.message("global.animations"), button -> {
            AnimationsScreen screen = new AnimationsScreen(scene);
            this.minecraft.setScreen(screen);
        }).build();
        Button cutscenesButton = Button.builder(Translation.message("global.cutscenes"), button -> {
            CutscenesScreen screen = new CutscenesScreen(scene);
            this.minecraft.setScreen(screen);
        }).build();
        Button subscenesButton = Button.builder(Translation.message("global.subscenes"), button -> {
            SubscenesScreen screen = new SubscenesScreen(scene);
            this.minecraft.setScreen(screen);
        }).build();
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, List.of(animationsButton, cutscenesButton, subscenesButton), List.of()));
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (p_345997_) -> this.onClose()).width(200).build());
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
