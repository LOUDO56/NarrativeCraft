package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.screens.components.PickElementScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CameraAngleAddRecord extends OptionsSubScreen {

    private final CameraAngleGroup cameraAngleGroup;
    private StoryElementList storyElementList;

    public CameraAngleAddRecord(CameraAngleGroup cameraAngleGroup) {
        super(null, Minecraft.getInstance().options, Translation.message("screen.scene_record.title", Component.literal(cameraAngleGroup.getScene().getName()).withColor(StoryElementScreen.SCENE_NAME_COLOR)));
        this.cameraAngleGroup = cameraAngleGroup;
    }
    @Override
    protected void addTitle() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.horizontal()).spacing(8);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(new StringWidget(this.title, this.font));
    }

    @Override
    protected void addContents() {

        List<Animation> selectedAnimations = cameraAngleGroup.getAnimations();

        List<Animation> availableAnimations = cameraAngleGroup.getScene().getAnimationList().stream()
                .filter(animation -> selectedAnimations.stream().noneMatch(a -> a.getName().equals(animation.getName())))
                .toList();

        PickElementScreen pickElementScreenAnimation = new PickElementScreen(
                this,
                Component.literal("Add animation to camera angle"),
                Translation.message("global.animations"),
                availableAnimations,
                selectedAnimations,
                entries -> {
                    cameraAngleGroup.clearAnimations();
                    List<Animation> selectedAnimation = new ArrayList<>();
                    for(PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                        Animation animation = (Animation) entry.getNarrativeEntry();
                        if(!cameraAngleGroup.animationExists(animation)) selectedAnimation.add(animation);
                    }
                    cameraAngleGroup.getTemplateRecord().addAll(selectedAnimation);
                    this.minecraft.setScreen(new CameraAngleAddRecord(cameraAngleGroup));
                    cameraAngleGroup.spawnCharactersTemp();
                }
        );

        List<Cutscene> selectedCutscenes = cameraAngleGroup.getCutscenes();

        List<Cutscene> availableCutscenes = cameraAngleGroup.getScene().getCutsceneList().stream()
                .filter(cutscene -> selectedCutscenes.stream().noneMatch(c -> c.getName().equals(cutscene.getName())))
                .toList();

        PickElementScreen pickElementScreenCutscene = new PickElementScreen(
                this,
                Component.literal("Add cutscene to camera angle"),
                Translation.message("global.cutscenes"),
                availableCutscenes,
                selectedCutscenes,
                entries -> {
                    cameraAngleGroup.clearCutscenes();
                    List<Cutscene> selectedCutscene = new ArrayList<>();
                    for(PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                        Cutscene cutscene = (Cutscene) entry.getNarrativeEntry();
                        if(!cameraAngleGroup.cutsceneExists(cutscene)) selectedCutscene.add(cutscene);
                    }
                    cameraAngleGroup.getTemplateRecord().addAll(selectedCutscene);
                    this.minecraft.setScreen(new CameraAngleAddRecord(cameraAngleGroup));
                    cameraAngleGroup.spawnCharactersTemp();
                }
        );

        List<Subscene> selectedSubscene = cameraAngleGroup.getSubscenes();

        List<Subscene> availableSubscene = cameraAngleGroup.getScene().getSubsceneList().stream()
                .filter(subscene -> selectedSubscene.stream().noneMatch(s -> s.getName().equals(subscene.getName())))
                .toList();

        PickElementScreen pickElementScreenSubscene = new PickElementScreen(
                this,
                Component.literal("Add subscene to camera angle"),
                Translation.message("global.subscenes"),
                availableSubscene,
                selectedSubscene,
                entries -> {
                    cameraAngleGroup.clearSubscenes();
                    List<Subscene> selectedSubscenes = new ArrayList<>();
                    for(PickElementScreen.TransferableStorySelectionList.Entry entry : entries) {
                        Subscene subscene = (Subscene) entry.getNarrativeEntry();
                        if(!cameraAngleGroup.subsceneExists(subscene)) selectedSubscenes.add(subscene);
                    }
                    cameraAngleGroup.getTemplateRecord().addAll(selectedSubscenes);
                    this.minecraft.setScreen(new CameraAngleAddRecord(cameraAngleGroup));
                    cameraAngleGroup.spawnCharactersTemp();
                }
        );

        List<StoryElementList.StoryEntryData> entries = List.of(
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.animations"), b -> minecraft.setScreen(pickElementScreenAnimation)).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.cutscenes"), b -> minecraft.setScreen(pickElementScreenCutscene)).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.subscenes"), b -> minecraft.setScreen(pickElementScreenSubscene)).build())
        );
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));

    }

    @Override
    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }
}
