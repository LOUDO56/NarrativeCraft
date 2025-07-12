package fr.loudo.narrativecraft.screens.mainScreen;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleGroup;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.MainScreenController;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.GameType;

public class MainScreen extends Screen {

    public static final ResourceLocation LOADING_LOGO = ResourceLocation.withDefaultNamespace("textures/narrativecraft_mainscreen/loading_logo.png");
    public static final ResourceLocation BACKGROUND_IMAGE = ResourceLocation.withDefaultNamespace("textures/narrativecraft_mainscreen/background.png");
    public static final ResourceLocation MUSIC = ResourceLocation.withDefaultNamespace("narrativecraft_mainscreen.music");

    public static final SimpleSoundInstance MUSIC_INSTANCE = new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(MainScreen.MUSIC).location(), SoundSource.MASTER, 0.7f, 1, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true);

    private final int buttonWidth = 100;
    private final int buttonHeight = 20;

    private final int initialX = 50;
    private final int gap = 10;

    private int showDevBtnCount;
    private Button devButton;

    public MainScreen(ServerPlayer serverPlayer) {
        super(Component.literal("Main screen"));
    }

    private void playStory() {
        this.onClose();
        NarrativeCraftMod.getInstance().setStoryHandler(null);
        StoryHandler storyHandler = new StoryHandler();
        NarrativeCraftMod.server.execute(storyHandler::start);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.options.hideGui = false;
        minecraft.getSoundManager().stop(MUSIC_INSTANCE);
        PlayerSession playerSession = Utils.getSessionOrNull(minecraft.player.getUUID());
        NarrativeCraftMod.server.execute(() -> {
            playerSession.getKeyframeControllerBase().stopSession(false);
        });
    }

    @Override
    protected void init() {

        showDevBtnCount = 0;
        PlayerSession playerSession = Utils.getSessionOrNull(minecraft.player.getUUID());
        minecraft.options.hideGui = true;
        if(playerSession == null || playerSession.getKeyframeControllerBase() == null) {
            CameraAngleGroup cameraAngleGroup = NarrativeCraftFile.getMainScreenBackgroundFile();
            if(cameraAngleGroup != null) {
                playerSession = NarrativeCraftMod.getInstance().getPlayerSessionManager().setSession(minecraft.player, null, null);
                StoryHandler storyHandler = new StoryHandler();
                storyHandler.setPlayerSession(playerSession);
                NarrativeCraftMod.getInstance().setStoryHandler(storyHandler);
                MainScreenController mainScreenController = new MainScreenController(
                        cameraAngleGroup,
                        Utils.getServerPlayerByUUID(minecraft.player.getUUID()),
                        Playback.PlaybackType.PRODUCTION
                );
                NarrativeCraftMod.server.execute(mainScreenController::startSession);
                mainScreenController.setCurrentPreviewKeyframe(mainScreenController.getMainCamera());
            }
        }

        if(!minecraft.getSoundManager().isActive(MUSIC_INSTANCE)) {
            minecraft.getSoundManager().play(MUSIC_INSTANCE);
        }

        int startY = height / 2 - ((buttonHeight + gap) * 5) / 2;

        Component playBtnComponent;
        if(NarrativeCraftFile.getSave() == null) {
            playBtnComponent = Translation.message("screen.main_screen.play");
        } else {
            playBtnComponent = Translation.message("screen.main_screen.continue");
        }
        Button playButton = Button.builder(playBtnComponent, button -> {
            playStory();
        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(playButton);

        startY += buttonHeight + gap;
        Button selectSceneButton = Button.builder(Translation.message("screen.main_screen.select_screen"), button -> {

        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(selectSceneButton);

        startY += buttonHeight + gap;
        Button optionsButton = Button.builder(Translation.message("screen.main_screen.options"), button -> {

        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(optionsButton);

        startY += buttonHeight + gap;
        Button minecraftOptionsButton = Button.builder(Translation.message("screen.main_screen.minecraft_options"), button -> {
            OptionsScreen screen = new OptionsScreen(this, minecraft.options);
            minecraft.setScreen(screen);
        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(minecraftOptionsButton);

        startY += buttonHeight + gap;
        Button quitButton = Button.builder(Translation.message("screen.main_screen.quit"), button -> {
            Utils.disconnectPlayer(minecraft);
        }).bounds(initialX, startY, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(quitButton);

        devButton = Button.builder(Component.literal("Dev Environment"), button -> {
            ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(minecraft.player.getUUID());
            serverPlayer.setGameMode(GameType.CREATIVE);
            serverPlayer.sendSystemMessage(Translation.message("global.dev_env"));
            this.onClose();
        }).bounds(width - buttonWidth - 10,  buttonHeight, buttonWidth, buttonHeight).build();
    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == InputConstants.KEY_LCONTROL) {
            showDevBtnCount++;
            if(showDevBtnCount == 5) {
                this.addRenderableWidget(devButton);
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
