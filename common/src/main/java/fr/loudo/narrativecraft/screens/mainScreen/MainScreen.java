package fr.loudo.narrativecraft.screens.mainScreen;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cutscenes.CutscenePlayback;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.MainScreenController;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.screens.mainScreen.options.MainScreenOptionsScreen;
import fr.loudo.narrativecraft.screens.mainScreen.sceneSelection.ChapterSelectorScreen;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.GameType;

public class MainScreen extends Screen {

    public static boolean wasPaused = false;

    public static final ResourceLocation BACKGROUND_IMAGE = ResourceLocation.withDefaultNamespace("textures/narrativecraft_mainscreen/background.png");
    public static final ResourceLocation MUSIC = ResourceLocation.withDefaultNamespace("narrativecraft_mainscreen.music");

    public static final SimpleSoundInstance MUSIC_INSTANCE = new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(MainScreen.MUSIC).location(), SoundSource.MASTER, 0.7f, 1, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true);
    
    private final NarrativeCraftLogoRenderer narrativeCraftLogo = NarrativeCraftMod.getInstance().getNarrativeCraftLogoRenderer();
    private final int buttonWidth = 100;
    private final int buttonHeight = 20;

    private final int initialX = 50;
    private int initialY;
    private final int gap = 5;
    
    private int showDevBtnCount;
    private Button devButton;

    private final boolean finishedStory;
    private final boolean pause;

    public MainScreen(boolean finishedStory, boolean pause) {
        super(Component.literal("Main screen"));
        this.finishedStory = finishedStory;
        this.pause = pause;
    }

    private void playStory() {
        this.onClose();
        StoryHandler storyHandler = new StoryHandler();
        NarrativeCraftMod.server.execute(storyHandler::start);
    }

    @Override
    public void onClose() {
        super.onClose();
        if(!pause) {
            minecraft.options.hideGui = false;
            minecraft.getSoundManager().stop(MUSIC_INSTANCE);
            PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
            if(playerSession.getKeyframeControllerBase() != null) {
                playerSession.getKeyframeControllerBase().stopSession(false);
            }
        }
    }

    @Override
    protected void init() {
        boolean storyFinished = NarrativeCraftMod.getInstance().getNarrativeUserOptions().FINISHED_STORY;
        showDevBtnCount = 0;
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        minecraft.options.hideGui = true;
        if((playerSession == null || playerSession.getKeyframeControllerBase() == null) && !pause) {
            CameraAngleGroup cameraAngleGroup = NarrativeCraftFile.getMainScreenBackgroundFile();
            if(cameraAngleGroup != null) {
                NarrativeCraftMod.getInstance().setStoryHandler(new StoryHandler());
                MainScreenController mainScreenController = new MainScreenController(
                        cameraAngleGroup,
                        null,
                        Playback.PlaybackType.PRODUCTION
                );
                NarrativeCraftMod.server.execute(mainScreenController::startSession);
                mainScreenController.setCurrentPreviewKeyframe(mainScreenController.getMainCamera());
            }
        }

        if(!pause){
            if(!minecraft.getSoundManager().isActive(MUSIC_INSTANCE)) {
                minecraft.getSoundManager().stop();
                minecraft.getSoundManager().play(MUSIC_INSTANCE);
            }
        }

        int n = storyFinished ? 5 : 3;
        if(pause) n = 5;
        int totalHeight = buttonHeight * n + gap * (n - 1);
        initialY = height / 2 - totalHeight / 2;
        if(narrativeCraftLogo.logoExists()) initialY += narrativeCraftLogo.getImageHeight() / 2 + gap;
        int startY = initialY;

        Component playBtnComponent;
        if(NarrativeCraftFile.getSave() == null && !pause) {
            playBtnComponent = Translation.message("screen.main_screen.play");
        } else {
            playBtnComponent = Translation.message("screen.main_screen.continue");
        }
        Button playButton = Button.builder(playBtnComponent, button -> {
            if(pause) {
                minecraft.setScreen(null);
            } else {
                playStory();
            }
        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        playButton.active = !NarrativeCraftMod.getInstance().getChapterManager().getChapters().isEmpty();
        this.addRenderableWidget(playButton);

        if(storyFinished && !pause) {
            startY += buttonHeight + gap;
            Button startNewGame = Button.builder(Translation.message("screen.main_screen.new_game"), button -> {
                ConfirmScreen confirmScreen = new ConfirmScreen(b -> {
                    if(b) {
                        NarrativeCraftFile.removeSave();
                        NarrativeCraftMod.getInstance().getNarrativeUserOptions().FINISHED_STORY = false;
                        NarrativeCraftFile.updateUserOptions();
                        playStory();
                    } else {
                        minecraft.setScreen(this);
                    }
                }, Component.literal(""), Translation.message("screen.main_screen.new_game.confirm"),
                        CommonComponents.GUI_YES, CommonComponents.GUI_CANCEL);
                minecraft.setScreen(confirmScreen);
            }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
            this.addRenderableWidget(startNewGame);
            startY += buttonHeight + gap;
            Button selectSceneButton = Button.builder(Translation.message("screen.main_screen.select_screen"), button -> {
                ChapterSelectorScreen screen = new ChapterSelectorScreen(this);
                minecraft.setScreen(screen);
            }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
            this.addRenderableWidget(selectSceneButton);
        }

        if(pause) {
            startY += buttonHeight + gap;
            Button loadLastSaveButton = Button.builder(Translation.message("screen.main_screen.pause.load_last_save"), button -> {
                minecraft.setScreen(null);
                new StoryHandler().start();
            }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
            this.addRenderableWidget(loadLastSaveButton);

            startY += buttonHeight + gap;
            Button skipCutsceneButton = Button.builder(Translation.message("screen.main_screen.pause.skip_cutscene"), button -> {
                minecraft.setScreen(null);
                CutscenePlayback cutscenePlayback = playerSession.getCutscenePlayback();
                if(cutscenePlayback != null) {
                    NarrativeCraftMod.server.execute(cutscenePlayback::skip);
                }
            }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
            skipCutsceneButton.active = playerSession.getCutscenePlayback() != null;
            this.addRenderableWidget(skipCutsceneButton);
        }

        startY += buttonHeight + gap;
        Button optionsButton = Button.builder(Translation.message("screen.main_screen.options"), button -> {
            MainScreenOptionsScreen screen = new MainScreenOptionsScreen(this);
            minecraft.setScreen(screen);
        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(optionsButton);

        if(pause) {
            startY += buttonHeight + gap;
            Button quitButton = Button.builder(Translation.message("screen.main_screen.pause.leave"), button -> {
                NarrativeCraftMod.getInstance().getStoryHandler().stop(true);
                MainScreen mainScreen = new MainScreen(false, false);
                minecraft.setScreen(mainScreen);
            }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
            this.addRenderableWidget(quitButton);
        } else {
            startY += buttonHeight + gap;
            Button quitButton = Button.builder(Translation.message("screen.main_screen.quit"), button -> {
                Utils.disconnectPlayer(minecraft);
            }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
            this.addRenderableWidget(quitButton);
        }

        devButton = Button.builder(Component.literal("Dev Environment"), button -> {
            ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(minecraft.player.getUUID());
            serverPlayer.setGameMode(GameType.CREATIVE);
            serverPlayer.sendSystemMessage(Translation.message("global.dev_env"));
            this.onClose();
        }).bounds(width - buttonWidth - 10,  buttonHeight, buttonWidth, buttonHeight).build();

        if(finishedStory) {
            FinishedStoryScreen screen = new FinishedStoryScreen();
            minecraft.setScreen(screen);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if(narrativeCraftLogo.logoExists()) {
            narrativeCraftLogo.render(guiGraphics, initialX, initialY - narrativeCraftLogo.getImageHeight() - gap - 5);
        }

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(pause) super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        else {
            if(NarrativeCraftFile.getMainScreenBackgroundFile() == null) {
                guiGraphics.blit(
                        RenderType::guiTextured,
                        BACKGROUND_IMAGE,
                        0, 0,
                        0, 0,
                        guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                        guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                        ARGB.colorFromFloat(1, 1, 1, 1)
                );
            }
        }
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public boolean isPauseScreen() {
        return pause;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == InputConstants.KEY_LCONTROL && !pause) {
            showDevBtnCount++;
            if(showDevBtnCount == 5) {
                this.addRenderableWidget(devButton);
            }
        }
        if(keyCode == InputConstants.KEY_ESCAPE && pause) {
            this.onClose();
            MainScreen.wasPaused = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
