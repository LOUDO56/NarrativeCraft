package fr.loudo.narrativecraft.screens.story_manager.template;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.screens.story_manager.StoryDetails;
import fr.loudo.narrativecraft.screens.story_manager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.story_manager.scenes.ScenesScreen;
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
        nameBox.setFilter(text -> text.matches("[a-zA-Z0-9 _-]*"));

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
            if(storyDetails != null && storyDetails instanceof Chapter) {
                updateChapterAction(name, desc);
            }
            if(storyDetails != null && storyDetails instanceof Scene) {
                updateSceneAction(name, desc);
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
        if(storyDetails != null && storyDetails instanceof Chapter chapter) {
            title = Translation.message("screen.chapter_manager.edit.title", chapter.getIndex());
        }
        if(storyDetails != null && storyDetails instanceof Scene scene) {
            title = Translation.message("screen.scene_manager.edit.title", scene.getName(), scene.getChapter().getIndex());
        }
        return title;
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
        Scene scene = new Scene(name, description, chapter);
        if(chapter.sceneExists(name)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.scene_manager.add.already_exists"));
            return;
        }
        if(!chapter.addScene(scene)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.scene_manager.add.failed"));
            return;
        }
        ScenesScreen screen = new ScenesScreen(chapter);
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
}
