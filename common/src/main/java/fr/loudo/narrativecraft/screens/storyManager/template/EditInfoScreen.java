package fr.loudo.narrativecraft.screens.storyManager.template;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.StoryDetails;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.animations.AnimationsScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cutscenes.CutscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes.SubscenesScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EditInfoScreen extends Screen {

    private final int WIDGET_WIDTH = 190;
    private final int EDIT_BOX_NAME_HEIGHT = 20;
    private final int EDIT_BOX_DESCRIPTION_HEIGHT = 90;
    private final int BUTTON_HEIGHT = 20;
    private final int GAP = 5;

    private String name, description;
    private EditBox nameBox;
    private MultiLineEditBox descriptionBox;
    private Screen lastScreen;
    private StoryDetails storyDetails;

    public EditInfoScreen(Screen lastScreen) {
        super(Component.literal("Edit info"));
        this.name = "";
        this.description = "";
        this.lastScreen = lastScreen;
    }

    public EditInfoScreen(Screen lastScreen, StoryDetails storyDetails) {
        super(Component.literal("Edit info"));
        this.name = storyDetails.getName();
        this.description = storyDetails.getDescription();
        this.lastScreen = lastScreen;
        this.storyDetails = storyDetails;
    }

    @Override
    protected void init() {
        Component title = getScreenTitle();
        Component buttonActionMessage = storyDetails == null ? Translation.message("screen.add.text") : Translation.message("screen.update.text");
        int titleX = this.width / 2 - this.font.width(title) / 2;

        int labelHeight = this.font.lineHeight + 5;

        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = this.height / 2 - (labelHeight + EDIT_BOX_NAME_HEIGHT + GAP + labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + (BUTTON_HEIGHT * 2)) / 2;

        StringWidget titleWidget = ScreenUtils.text(title, this.font, titleX, centerY - labelHeight);
        this.addRenderableWidget(titleWidget);

        Component nameLabel = Translation.message("screen.story.name")
                .append(Component.literal(" *").withStyle(style -> style.withColor(0xE62E37)));

        nameBox = labelBox(nameLabel,
                WIDGET_WIDTH,
                EDIT_BOX_NAME_HEIGHT,
                centerX,
                centerY
        );
        nameBox.setValue(name);
        nameBox.setFilter(text -> !text.matches(".*[\\\\/:*?\"<>|].*"));
        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;
        descriptionBox = multiLineBoxLabel(
                Translation.message("screen.story.description").getString(),
                WIDGET_WIDTH,
                EDIT_BOX_DESCRIPTION_HEIGHT,
                centerX,
                centerY
        );
        descriptionBox.setValue(description);

        centerY += labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + GAP;
        Button actionButton = Button.builder(buttonActionMessage, button -> {
            String name = nameBox.getValue();
            String desc = descriptionBox.getValue();
            if(name.isEmpty()) {
                ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.story.name.required"));
                return;
            }
            if(storyDetails == null && lastScreen instanceof ChaptersScreen) {
                addChapterAction(name, desc);
            }
            if(storyDetails == null && lastScreen instanceof ScenesScreen) {
                addSceneAction(name, desc);
            }
            if(storyDetails == null && lastScreen instanceof CutscenesScreen) {
                addCutsceneAction(name, desc);
            }
            if(storyDetails == null && lastScreen instanceof SubscenesScreen) {
                addSubsceneAction(name, desc);
            }
            if(storyDetails != null && storyDetails instanceof Chapter) {
                updateChapterAction(name, desc);
            }
            if(storyDetails != null && storyDetails instanceof Scene) {
                updateSceneAction(name, desc);
            }
            if(storyDetails != null && storyDetails instanceof Cutscene) {
                updateCutsceneAction(name, desc);
            }
            if(storyDetails != null && storyDetails instanceof Subscene) {
                updateSubsceneAction(name, desc);
            }
            if(storyDetails != null && storyDetails instanceof Animation) {
                updateAnimationAction(name, desc);
            }
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(actionButton);

        centerY += BUTTON_HEIGHT + GAP;
        Button backButton = Button.builder(CommonComponents.GUI_BACK, button -> {
            this.minecraft.setScreen(lastScreen);
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(backButton);
    }

    private Component getScreenTitle() {
        Component title = Component.literal("");
        if(storyDetails == null && lastScreen instanceof ChaptersScreen) {
            title = Translation.message("screen.chapter_manager.add.title");
        }
        if(storyDetails == null && lastScreen instanceof ScenesScreen screen) {
            title = Translation.message("screen.scene_manager.add.title", screen.getChapter().getIndex());
        }
        if(storyDetails == null && lastScreen instanceof CutscenesScreen screen) {
            title = Translation.message("screen.cutscene_manager.add.title", screen.getScene().getName());
        }
        if(storyDetails == null && lastScreen instanceof SubscenesScreen screen) {
            title = Translation.message("screen.subscene_manager.add.title", screen.getScene().getName());
        }
        if(storyDetails != null && storyDetails instanceof Chapter chapter) {
            title = Translation.message("screen.chapter_manager.edit.title", chapter.getIndex());
        }
        if(storyDetails != null && storyDetails instanceof Scene scene) {
            title = Translation.message("screen.scene_manager.edit.title", scene.getName(), scene.getChapter().getIndex());
        }
        if(storyDetails != null && storyDetails instanceof Cutscene cutscene) {
            title = Translation.message("screen.cutscene_manager.edit.title", cutscene.getName(), cutscene.getScene().getName());
        }
        if(storyDetails != null && storyDetails instanceof Subscene subscene) {
            title = Translation.message("screen.subscene_manager.edit.title", subscene.getName(), subscene.getScene().getName());
        }
        return title;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    private EditBox labelBox(Component text, int width, int height, int x, int y) {
        StringWidget stringWidget = ScreenUtils.text(text, this.font, x, y);
        EditBox editBox = new EditBox(
                this.font,
                x,
                y + this.font.lineHeight + 5,
                width,
                height,
                Component.literal(text + " value")
        );
        this.addRenderableWidget(stringWidget);
        this.addRenderableWidget(editBox);
        return editBox;
    }

    private MultiLineEditBox multiLineBoxLabel(String text, int width, int height, int x, int y) {
        StringWidget stringWidget = ScreenUtils.text(Component.literal(text), this.font, x, y);
        MultiLineEditBox multiLineEditBox = new MultiLineEditBox(
                this.font,
                x,
                y + this.font.lineHeight + 5,
                width,
                height,
                Component.literal("Once upon a time... In a wild... wild world... there were two wolf brothers, living in their home lair with their papa wolf..."),
                Component.literal("")
        );
        this.addRenderableWidget(stringWidget);
        this.addRenderableWidget(multiLineEditBox);
        return multiLineEditBox;
    }

    private void addChapterAction(String name, String description) {
        if(!NarrativeCraftMod.getInstance().getChapterManager().addChapter(name, description)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.chapter_manager.add.failed"));
            return;
        }
        ChaptersScreen screen = new ChaptersScreen();
        this.minecraft.setScreen(screen);
    }

    private void addSceneAction(String name, String description) {
        Chapter chapter = ((ScenesScreen)lastScreen).getChapter();
        if(chapter.sceneExists(name)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.scene_manager.add.already_exists"));
            return;
        }
        Scene scene = new Scene(name, description, chapter);
        if(!chapter.addScene(scene)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.scene_manager.add.failed"));
            return;
        }
        ScenesScreen screen = new ScenesScreen(chapter);
        this.minecraft.setScreen(screen);
    }

    private void addCutsceneAction(String name, String desc) {

        Scene scene = ((CutscenesScreen)lastScreen).getScene();
        if(scene.cutsceneExists(name)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.cutscene_manager.add.already_exists"));
            return;
        }
        Cutscene cutscene = new Cutscene(scene, name, desc);
        if(!scene.addCutscene(cutscene)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.cutscene_manager.add.failed", name));
            return;
        }
        CutscenesScreen screen = new CutscenesScreen(scene);
        this.minecraft.setScreen(screen);
    }

    private void addSubsceneAction(String name, String desc) {
        Scene scene = ((SubscenesScreen)lastScreen).getScene();
        if(scene.subsceneExists(name)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.subscene_manager.add.already_exists"));
            return;
        }
        Subscene subscene = new Subscene(scene, name, desc);
        if(!scene.addSubscene(subscene)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.subscene_manager.add.failed", name));
            return;
        }
        SubscenesScreen screen = new SubscenesScreen(scene);
        this.minecraft.setScreen(screen);
    }

    private void updateChapterAction(String name, String description) {
        Chapter chapter = (Chapter) storyDetails;
        if(!NarrativeCraftFile.updateChapterDetails(chapter, name, description)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.chapter_manager.update.failed"));
            return;
        }
        chapter.setName(name);
        chapter.setDescription(description);
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated", chapter.getIndex()));
        ChaptersScreen screen = new ChaptersScreen();
        this.minecraft.setScreen(screen);
    }

    private void updateSceneAction(String name, String description) {
        Scene scene = (Scene) storyDetails;
        if(!NarrativeCraftFile.updateSceneDetails(scene, name, description)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.scene_manager.update.failed", scene.getName()));
            return;
        }
        scene.setName(name);
        scene.setDescription(description);
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated", scene.getName(), scene.getChapter().getIndex()));
        ScenesScreen screen = new ScenesScreen(scene.getChapter());
        this.minecraft.setScreen(screen);
    }

    private void updateCutsceneAction(String name, String desc) {
        Cutscene cutscene = (Cutscene) storyDetails;
        cutscene.setName(name);
        cutscene.setDescription(desc);
        if(!NarrativeCraftFile.updateCutsceneFile(cutscene.getScene())) {
            cutscene.setName(this.name);
            cutscene.setDescription(description);
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.cutscene_manager.update.failed", cutscene.getName()));
            return;
        }
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated"));
        CutscenesScreen screen = new CutscenesScreen(cutscene.getScene());
        this.minecraft.setScreen(screen);
    }

    private void updateSubsceneAction(String name, String desc) {
        Subscene subscene = (Subscene) storyDetails;
        subscene.setName(name);
        subscene.setDescription(desc);
        if(!NarrativeCraftFile.updateSubsceneFile(subscene.getScene())) {
            subscene.setName(this.name);
            subscene.setDescription(description);
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.subscene_manager.update.failed", subscene.getName()));
            return;
        }
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated"));
        SubscenesScreen screen = new SubscenesScreen(subscene.getScene());
        this.minecraft.setScreen(screen);
    }

    private void updateAnimationAction(String name, String desc) {
        Animation animation = (Animation) storyDetails;
        NarrativeCraftFile.removeAnimationFileFromScene(animation);
        animation.setName(name);
        animation.setDescription(desc);
        if(!NarrativeCraftFile.updateAnimationFile(animation)) {
            animation.setName(this.name);
            animation.setDescription(description);
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.animation_manager.update.failed", animation.getName()));
            return;
        }
        ScreenUtils.sendToast(Translation.message("toast.info"), Translation.message("toast.description.updated"));
        AnimationsScreen screen = new AnimationsScreen(animation.getScene());
        this.minecraft.setScreen(screen);
    }

}
