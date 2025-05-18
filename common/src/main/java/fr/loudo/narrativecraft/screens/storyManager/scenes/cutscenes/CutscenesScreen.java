package fr.loudo.narrativecraft.screens.storyManager.scenes.cutscenes;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutsceneController;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.scenes.ScenesMenuScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.PickElementScreen;
import fr.loudo.narrativecraft.screens.storyManager.template.StoryElementList;
import fr.loudo.narrativecraft.utils.ImageFontConstants;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CutscenesScreen extends StoryElementScreen {

    private final Scene scene;

    public CutscenesScreen(Scene scene) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.cutscene_manager.title", Component.literal(scene.getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.scene = scene;
    }

    @Override
    public void onClose() {
        ScenesMenuScreen screen = new ScenesMenuScreen(scene);
        this.minecraft.setScreen(screen);
    }

    @Override
    protected void addContents() {
        List<StoryElementList.StoryEntryData> entries = new ArrayList<>();

        for (Cutscene cutscene : scene.getCutsceneList()) {
            List<Button> extraButtons = new ArrayList<>();

            Button settingsButton = createSettingsButton(cutscene);
            extraButtons.add(settingsButton);

            Button mainButton = Button.builder(Component.literal(cutscene.getName()), btn -> {
                CutsceneController controller = new CutsceneController(cutscene, Utils.getServerPlayerByUUID(minecraft.player.getUUID()), Playback.PlaybackType.DEVELOPMENT);
                NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(minecraft.player, scene.getChapter(), scene).setKeyframeControllerBase(controller);
                controller.startSession();
            }).build();

            entries.add(new StoryElementList.StoryEntryData(mainButton, cutscene, extraButtons));
        }

        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));
    }

    private Button createSettingsButton(Cutscene cutscene) {
        List<Subscene> subscenesAvailable = scene.getSubsceneList().stream()
                .filter(sub -> cutscene.getSubsceneList().stream()
                        .noneMatch(s -> s.getName().equals(sub.getName())))
                .toList();

        List<Animation> animationsAvailable = scene.getAnimationList().stream()
                .filter(anim -> cutscene.getAnimationList().stream()
                        .noneMatch(a -> a.getName().equals(anim.getName())))
                .toList();
        return Button.builder(ImageFontConstants.SETTINGS, button -> {
            PickElementScreen screen;
            if(Screen.hasShiftDown()) {
                screen = new PickElementScreen(
                        this,
                        Translation.message("screen.selector.cutscene.title", Translation.message("global.animations"), Component.literal(cutscene.getName()).withColor(StoryElementScreen.CUTSCENE_NAME_COLOR)),
                        Translation.message("global.animations"),
                        animationsAvailable,
                        cutscene.getAnimationList(),
                        entries -> {
                            if(NarrativeCraftFile.cutscenesFileExist(scene)) {
                                List<String> selectedAnimationsString = new ArrayList<>();
                                List<Animation> selectedAnimations = new ArrayList<>();
                                for(PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                                    Animation animation = (Animation) entry.getStoryDetails();
                                    selectedAnimationsString.add(animation.getName());
                                    selectedAnimations.add(animation);
                                }
                                cutscene.setAnimationListString(selectedAnimationsString);
                                cutscene.setAnimationList(selectedAnimations);
                                NarrativeCraftFile.updateCutsceneFile(scene);
                            }
                            this.minecraft.setScreen(new CutscenesScreen(scene));
                        }
                );
            } else {
                screen = new PickElementScreen(
                        this,
                        Translation.message("screen.selector.cutscene.title", Translation.message("global.subscenes"), Component.literal(cutscene.getName()).withColor(StoryElementScreen.CUTSCENE_NAME_COLOR)),
                        Translation.message("global.subscenes"),
                        subscenesAvailable,
                        cutscene.getSubsceneList(),
                        entries -> {
                            if(NarrativeCraftFile.cutscenesFileExist(scene)) {
                                List<Subscene> selectedSubscene = new ArrayList<>();
                                for(PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                                    Subscene subscene = (Subscene) entry.getStoryDetails();
                                    selectedSubscene.add(subscene);
                                }
                                cutscene.setSubsceneList(selectedSubscene);
                                NarrativeCraftFile.updateCutsceneFile(scene);
                            }
                            this.minecraft.setScreen(new CutscenesScreen(scene));
                        }
                );
            }
            this.minecraft.setScreen(screen);
        }).width(20).build();
    }

    public Scene getScene() {
        return scene;
    }

}
