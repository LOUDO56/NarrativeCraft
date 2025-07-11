package fr.loudo.narrativecraft.screens.mainScreen;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import org.lwjgl.glfw.GLFW;

public class MainScreen extends Screen {


    public static final ResourceLocation LOADING_LOGO = ResourceLocation.withDefaultNamespace("textures/narrativecraft_mainscreen/loading_logo.png");
    public static final ResourceLocation BACKGROUND_IMAGE = ResourceLocation.withDefaultNamespace("textures/narrativecraft_mainscreen/background.png");
    public static final ResourceLocation MUSIC = ResourceLocation.withDefaultNamespace("narrativecraft_mainscreen.music");

    private static final SimpleSoundInstance MUSIC_INSTANCE = new SimpleSoundInstance(SoundEvent.createVariableRangeEvent(MainScreen.MUSIC).location(), SoundSource.MASTER, 0.7f, 1, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true);

    private long startTime = 0;

    private final int buttonWidth = 100;
    private final int buttonHeight = 20;

    private final int initialX = 50;
    private final int gap = 10;

    public MainScreen() {
        super(Component.literal("Main screen"));
    }


    private void playStory() {
        this.onClose();
        StoryHandler storyHandler = new StoryHandler();
        NarrativeCraftMod.getInstance().setStoryHandler(storyHandler);
        storyHandler.start();
    }

    @Override
    public void tick() {
        super.tick();
        if(startTime == 0) return;
        if(System.currentTimeMillis() - startTime >= 5 * 1000L) {
            playStory();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.getSoundManager().stop(MUSIC_INSTANCE);
    }
    @Override
    protected void init() {

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
            minecraft.getSoundManager().setVolume(MUSIC_INSTANCE, 0.4f);
            startTime = System.currentTimeMillis();
            GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), 208897, 212995); // Hide mouse
            clearWidgets();
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

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(startTime > 0) {
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ARGB.colorFromFloat(1, 0, 0, 0));
            guiGraphics.blit(
                    RenderType::guiTextured,
                    MainScreen.LOADING_LOGO,
                    guiGraphics.guiWidth() - 30, guiGraphics.guiHeight() - 30,
                    0, 0,
                    12, 12,
                    12, 12,
                    0xFFFFFFFF
            );
        }
    }

    @Override
    protected void renderBlurredBackground() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
