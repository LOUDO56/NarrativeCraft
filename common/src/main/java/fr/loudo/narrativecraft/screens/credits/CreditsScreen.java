package fr.loudo.narrativecraft.screens.credits;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.mixin.fields.WinScreenFields;
import fr.loudo.narrativecraft.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class CreditsScreen extends WinScreen {

    public static final ResourceLocation LOGO = ResourceLocation.withDefaultNamespace("textures/narrativecraft_logo.png");

    private static final ResourceLocation BACKGROUND_IMAGE = ResourceLocation.withDefaultNamespace("textures/narrativecraft_credits/background.png");
    private static final ResourceLocation MUSIC = ResourceLocation.withDefaultNamespace("narrativecraft_credits.music");

    public static final SimpleSoundInstance MUSIC_INSTANCE = SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(MUSIC), 1, 1);


    public CreditsScreen(boolean fromMainMenu, boolean showFinishScreen) {
        super(false, () -> {
            MainScreen mainScreen;
            if(showFinishScreen && !fromMainMenu) {
                mainScreen = new MainScreen(true, false);
            } else {
                mainScreen = new MainScreen(false, false);
            }
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(mainScreen));
        });
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.getSoundManager().stop(MUSIC_INSTANCE);
    }

    @Override
    protected void init() {
        super.init();
        if(!minecraft.getSoundManager().isActive(MUSIC_INSTANCE)) {
            minecraft.getSoundManager().stop();
            minecraft.getSoundManager().play(MUSIC_INSTANCE);
        }
        ((WinScreenFields)this).callAddCreditsLine(Component.literal("Tool Used").withStyle(ChatFormatting.GRAY), false);
        ((WinScreenFields)this).callAddCreditsLine(Component.literal("           ").append("Ink - Narrative Script Language by Inkle").withStyle(ChatFormatting.WHITE), false);
        ((WinScreenFields)this).callAddCreditsLine(Component.literal("           ").append("Blade-ink-java - Ink java adaptation by BladeCoder").withStyle(ChatFormatting.WHITE), false);
        ((WinScreenFields)this).callAddCreditsLine(Component.literal("           ").append("NarrativeCraft - Mod used to create this story by LOUDO").withStyle(ChatFormatting.WHITE), false);
        ((WinScreenFields)this).callAddCreditsLine(Component.literal("           "), false);

    }

    @Override
    public boolean keyPressed(int p_169469_, int p_169470_, int p_169471_) {
        if(p_169469_ == InputConstants.KEY_ESCAPE) {
            minecraft.setScreen(null);
            onClose();
        }
        return super.keyPressed(p_169469_, p_169470_, p_169471_);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int p_294762_, int p_295473_, float p_296441_) {
        if(Utils.resourceExists(BACKGROUND_IMAGE)) {
            guiGraphics.blit(
                    BACKGROUND_IMAGE,
                    0, 0,
                    0f, 0f,
                    guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                    guiGraphics.guiWidth(), guiGraphics.guiHeight()
            );
        } else {
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0xFF000000);
        }
    }
}

