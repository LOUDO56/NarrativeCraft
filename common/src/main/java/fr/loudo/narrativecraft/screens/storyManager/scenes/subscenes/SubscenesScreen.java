package fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.components.EditInfoScreen;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.components.PickElementScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SubscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public SubscenesScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.subscene_manager.title", Component.literal(scene.getName()).withColor(0x5896ED)));
        this.scene = scene;
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
        linearlayout.addChild(Button.builder(ImageFontConstants.ADD, button -> {
            EditInfoScreen screen = new EditInfoScreen(this);
            this.minecraft.setScreen(screen);
        }).width(25).build());
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = new ArrayList<>();
        for (Subscene subscene : scene.getSubsceneList()) {
            List<Animation> availableAnimations = scene.getAnimationList().stream()
                    .filter(anim -> subscene.getAnimationList().stream().noneMatch(a -> a.getName().equals(anim.getName())))
                    .toList();

            Button settingsButton = Button.builder(ImageFontConstants.SETTINGS, b -> {
                PickElementScreen screen = new PickElementScreen(this,
                        Translation.message("screen.selector.subscene.title", Translation.message("global.animations"), Component.literal(subscene.getName())),
                        Translation.message("global.animations"),
                        availableAnimations,
                        subscene.getAnimationList(),
                        entries1 -> {
                            if (NarrativeCraftFile.subscenesFileExist(scene)) {
                                List<Animation> selected = new ArrayList<>();
                                List<String> names = new ArrayList<>();
                                for (var entry : entries1) {
                                    Animation a = (Animation) entry.getNarrativeEntry();
                                    selected.add(a);
                                    names.add(a.getName());
                                }
                                subscene.setAnimationList(selected);
                                subscene.setAnimationNameList(names);
                                NarrativeCraftFile.updateSubsceneFile(scene);
                            }
                            this.minecraft.setScreen(new SubscenesScreen(scene));
                        });
                this.minecraft.setScreen(screen);
            }).width(20).build();

            Button mainButton = Button.builder(Component.literal(subscene.getName()), b -> {
                //
            }).build();

            entries.add(new StoryElementList.StoryEntryData(mainButton, subscene, List.of(settingsButton)));
        }
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }


    public Scene getScene() {
        return scene;
    }

    @Override
    protected void openFolder() {}
}
