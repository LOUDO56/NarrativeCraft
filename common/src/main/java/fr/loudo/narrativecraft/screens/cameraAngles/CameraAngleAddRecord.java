package fr.loudo.narrativecraft.screens.cameraAngles;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.Cutscene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.keyframes.Keyframe;
import fr.loudo.narrativecraft.narrative.chapter.scenes.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.Action;
import fr.loudo.narrativecraft.narrative.recordings.actions.PoseAction;
import fr.loudo.narrativecraft.narrative.recordings.actions.manager.ActionType;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.screens.components.ListElementScreen;
import fr.loudo.narrativecraft.screens.components.StoryElementList;
import fr.loudo.narrativecraft.screens.storyManager.StoryElementScreen;
import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


        Scene scene = cameraAngleGroup.getScene();
        List<StoryElementList.StoryEntryData> animationEntries = new ArrayList<>();
        for(Animation animation : scene.getAnimationList()) {
            Button animationButton = Button.builder(Component.literal(animation.getName()), button -> {
                spawnEntity(animation, animation.getActionsData().getMovementData().size() - 1);
                this.minecraft.setScreen(this);
            }).build();
            animationEntries.add(new StoryElementList.StoryEntryData(animationButton));
        }
        ListElementScreen animationListScreen = new ListElementScreen(this, animationEntries, Translation.message("global.animations"));

        List<StoryElementList.StoryEntryData> cutsceneEntries = new ArrayList<>();
        for(Cutscene cutscene : scene.getCutsceneList()) {
            Button cutsceneButton = Button.builder(Component.literal(cutscene.getName()), button -> {
                Keyframe lastKeyframe = cutscene.getKeyframeGroupList().getLast().getKeyframeList().getLast();
                int lastLocIndex = (int) (lastKeyframe.getTick() + 2 + ((lastKeyframe.getTransitionDelay() / 1000) * 20));
                for(Subscene subscene : cutscene.getSubsceneList()) {
                    for(Animation animation : subscene.getAnimationList()) {
                        spawnEntity(animation, Math.min(lastLocIndex, animation.getActionsData().getMovementData().size() - 1));
                    }
                }
                for(Animation animation : cutscene.getAnimationList()) {
                    spawnEntity(animation, Math.min(lastLocIndex, animation.getActionsData().getMovementData().size() - 1));
                }
                this.minecraft.setScreen(this);
            }).build();
            cutsceneEntries.add(new StoryElementList.StoryEntryData(cutsceneButton));
        }
        ListElementScreen cutsceneListScreen = new ListElementScreen(this, cutsceneEntries, Translation.message("global.cutscenes"));

        List<StoryElementList.StoryEntryData> subsceneEntries = new ArrayList<>();
        for(Subscene subscene : scene.getSubsceneList()) {
            Button subsceneButton = Button.builder(Component.literal(subscene.getName()), button -> {
                for(Animation animation : subscene.getAnimationList()) {
                    spawnEntity(animation, animation.getActionsData().getMovementData().size() - 1);
                }
                this.minecraft.setScreen(this);
            }).build();
            subsceneEntries.add(new StoryElementList.StoryEntryData(subsceneButton));
        }
        ListElementScreen subsceneListScreen = new ListElementScreen(this, subsceneEntries, Translation.message("global.subscenes"));

        List<StoryElementList.StoryEntryData> entries = List.of(
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.animations"), b -> this.minecraft.setScreen(animationListScreen)).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.cutscenes"), b -> this.minecraft.setScreen(cutsceneListScreen)).build()),
                new StoryElementList.StoryEntryData(Button.builder(Translation.message("global.subscenes"), b -> this.minecraft.setScreen(subsceneListScreen)).build())
        );
        this.storyElementList = this.layout.addToContents(new StoryElementList(this.minecraft, this, entries));

    }

    private void spawnEntity(Animation animation, int index) {
        MovementData movementData = animation.getActionsData().getMovementData().get(index);
        List<Action> actions;
        if(index >= animation.getActionsData().getActions().getLast().getTick()) {
            actions = animation.getActionsData().getActions().stream()
                    .filter(action -> animation.getActionsData().getActions().getLast().getTick() == action.getTick())
                    .toList();
        } else {
            actions = animation.getActionsData().getActions().stream()
                    .filter(action -> index >= action.getTick())
                    .toList();
        }
        cameraAngleGroup.addCharacter(
                animation.getCharacter(),
                animation.getSkinName(),
                movementData.getX(),
                movementData.getY(),
                movementData.getZ(),
                movementData.getXRot(),
                movementData.getYRot(),
                actions,
                Playback.PlaybackType.DEVELOPMENT
        );
    }

    @Override
    protected void addOptions() {}

    protected void repositionElements() {
        super.repositionElements();
        this.storyElementList.updateSize(this.width, this.layout);
    }
}
