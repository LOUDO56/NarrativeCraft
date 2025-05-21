package fr.loudo.narrativecraft.screens.storyManager.components;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.screens.storyManager.chapters.ChaptersScreen;
import fr.loudo.narrativecraft.screens.storyManager.characters.CharactersScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cameraAngles.CameraAnglesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.cutscenes.CutscenesScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.subscenes.SubscenesScreen;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EditInfoScreen extends Screen {

    protected final int WIDGET_WIDTH = 190;
    protected final int EDIT_BOX_NAME_HEIGHT = 20;
    protected final int EDIT_BOX_DESCRIPTION_HEIGHT = 90;
    protected final int BUTTON_HEIGHT = 20;
    protected final int GAP = 5;

    protected String name, description;
    protected Button actionButton, backButton;
    protected ScreenUtils.LabelBox nameBox;
    protected ScreenUtils.MultilineLabelBox descriptionBox;
    protected StringWidget titleWidget;
    protected Screen lastScreen;
    protected NarrativeEntry narrativeEntry;

    public EditInfoScreen(Screen lastScreen) {
        super(Component.literal("Edit info"));
        this.name = "";
        this.description = "";
        this.lastScreen = lastScreen;
    }

    public EditInfoScreen(Screen lastScreen, NarrativeEntry narrativeEntry) {
        super(Component.literal("Edit info"));
        this.name = narrativeEntry.getName();
        this.description = narrativeEntry.getDescription();
        this.lastScreen = lastScreen;
        this.narrativeEntry = narrativeEntry;
    }

    @Override
    protected void init() {
        Component title = getScreenTitle();
        Component buttonActionMessage = narrativeEntry == null ? Translation.message("screen.add.text") : Translation.message("screen.update.text");
        int titleX = this.width / 2 - this.font.width(title) / 2;

        int labelHeight = this.font.lineHeight + 5;

        int centerX = this.width / 2 - WIDGET_WIDTH / 2;
        int centerY = this.height / 2 - (labelHeight + EDIT_BOX_NAME_HEIGHT + GAP + labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + (BUTTON_HEIGHT * 2)) / 2;

        titleWidget = ScreenUtils.text(title, this.font, titleX, centerY - labelHeight);
        this.addRenderableWidget(titleWidget);

        Component nameLabel = Translation.message("screen.story.name")
                .append(Component.literal(" *").withStyle(style -> style.withColor(0xE62E37)));

        nameBox = new ScreenUtils.LabelBox(
                nameLabel,
                font,
                WIDGET_WIDTH,
                EDIT_BOX_NAME_HEIGHT,
                centerX,
                centerY,
                ScreenUtils.Align.VERTICAL
        );
        nameBox.getEditBox().setValue(name);
        nameBox.getEditBox().setFilter(text -> !text.matches(".*[\\\\/:*?\"<>|].*"));
        this.addRenderableWidget(nameBox.getStringWidget());
        this.addRenderableWidget(nameBox.getEditBox());

        centerY += labelHeight + EDIT_BOX_NAME_HEIGHT + GAP;
        descriptionBox = new ScreenUtils.MultilineLabelBox(
                Translation.message("screen.story.description"),
                font,
                WIDGET_WIDTH,
                EDIT_BOX_DESCRIPTION_HEIGHT,
                centerX,
                centerY
        );
        descriptionBox.getMultiLineEditBox().setValue(description);

        this.addRenderableWidget(descriptionBox.getStringWidget());
        this.addRenderableWidget(descriptionBox.getMultiLineEditBox());

        centerY += labelHeight + EDIT_BOX_DESCRIPTION_HEIGHT + GAP;
        actionButton = Button.builder(buttonActionMessage, button -> {
            String name = nameBox.getEditBox().getValue();
            String desc = descriptionBox.getMultiLineEditBox().getValue();
            if(name.isEmpty()) {
                ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.story.name.required"));
                return;
            }
            if(narrativeEntry == null && lastScreen instanceof ChaptersScreen) {
                addChapterAction(name, desc);
            }
            if(narrativeEntry == null && lastScreen instanceof ScenesScreen) {
                addSceneAction(name, desc);
            }
            if(narrativeEntry == null && lastScreen instanceof CutscenesScreen) {
                addCutsceneAction(name, desc);
            }
            if(narrativeEntry == null && lastScreen instanceof SubscenesScreen) {
                addSubsceneAction(name, desc);
            }
            if(narrativeEntry == null && lastScreen instanceof CameraAnglesScreen) {
                addCameraAnglesAction(name, desc);
            }
            if(narrativeEntry != null) {
                narrativeEntry.update(name, desc);
            }
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(actionButton);

        centerY += BUTTON_HEIGHT + GAP;
        backButton = Button.builder(CommonComponents.GUI_BACK, button -> {
            this.minecraft.setScreen(lastScreen);
        }).bounds(centerX, centerY, WIDGET_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(backButton);
    }

    private Component getScreenTitle() {
        Component title = Component.literal("");
        if(narrativeEntry == null && lastScreen instanceof ChaptersScreen) {
            title = Translation.message("screen.chapter_manager.add.title");
        }
        if(narrativeEntry == null && lastScreen instanceof ScenesScreen screen) {
            title = Translation.message("screen.scene_manager.add.title", screen.getChapter().getIndex());
        }
        if(narrativeEntry == null && lastScreen instanceof CutscenesScreen screen) {
            title = Translation.message("screen.cutscene_manager.add.title", screen.getScene().getName());
        }
        if(narrativeEntry == null && lastScreen instanceof SubscenesScreen screen) {
            title = Translation.message("screen.subscene_manager.add.title", screen.getScene().getName());
        }
        if(narrativeEntry == null && lastScreen instanceof CameraAnglesScreen screen) {
            title = Translation.message("screen.camera_angles_manager.add.title", screen.getScene().getName());
        }
        if(narrativeEntry == null && lastScreen instanceof CharactersScreen) {
            title = Translation.message("screen.characters_manager.add.title");
        }
        if(narrativeEntry != null && narrativeEntry instanceof Chapter chapter) {
            title = Translation.message("screen.chapter_manager.edit.title", chapter.getIndex());
        }
        if(narrativeEntry != null && narrativeEntry instanceof Scene scene) {
            title = Translation.message("screen.scene_manager.edit.title", scene.getName(), scene.getChapter().getIndex());
        }
        if(narrativeEntry != null && narrativeEntry instanceof Cutscene cutscene) {
            title = Translation.message("screen.cutscene_manager.edit.title", cutscene.getName(), cutscene.getScene().getName());
        }
        if(narrativeEntry != null && narrativeEntry instanceof Subscene subscene) {
            title = Translation.message("screen.subscene_manager.edit.title", subscene.getName(), subscene.getScene().getName());
        }
        if(narrativeEntry != null && narrativeEntry instanceof CameraAngleGroup cameraAngleGroup) {
            title = Translation.message("screen.camera_angles_manager.edit.title", cameraAngleGroup.getName(), cameraAngleGroup.getScene().getName());
        }
        if(narrativeEntry != null && lastScreen instanceof CharactersScreen charactersScreen) {
            title = Translation.message("screen.characters_manager.edit.title");
        }
        return title;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
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

    private void addCameraAnglesAction(String name, String desc) {
        Scene scene = ((CameraAnglesScreen)lastScreen).getScene();
        if(scene.cameraAnglesGroupExists(name)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.camera_angles_manager.add.already_exists"));
            return;
        }
        CameraAngleGroup cameraAngleGroup = new CameraAngleGroup(scene, name, desc);
        if(!scene.addCameraAnglesGroup(cameraAngleGroup)) {
            ScreenUtils.sendToast(Translation.message("toast.error"), Translation.message("screen.camera_angles_manager.add.failed", name));
            return;
        }
        CameraAnglesScreen screen = new CameraAnglesScreen(scene);
        this.minecraft.setScreen(screen);
    }

}
