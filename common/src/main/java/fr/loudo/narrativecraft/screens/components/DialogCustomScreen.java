package fr.loudo.narrativecraft.screens.components;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.dialog.Dialog;
import fr.loudo.narrativecraft.narrative.dialog.DialogData;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.ScreenUtils;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class DialogCustomScreen extends Screen {

    private final Screen lastScreen;
    private FakePlayer fakePlayer;
    private Vec3 lastPos;
    private float lastXRot;
    private float lastYRot;

    private ScreenUtils.LabelBox paddingXBox;
    private ScreenUtils.LabelBox paddingYBox;
    private ScreenUtils.LabelBox scaleBox;
    private ScreenUtils.LabelBox letterSpacingBox;
    private ScreenUtils.LabelBox gapBox;
    private ScreenUtils.LabelBox maxWidthBox;
    private ScreenUtils.LabelBox bcColorBox;
    private ScreenUtils.LabelBox textColorBox;
    private ScreenUtils.LabelBox bobbingSpeed;
    private ScreenUtils.LabelBox bobbingStrength;
    private StringWidget errorWidget;

    public DialogCustomScreen(Screen lastScreen) {
        super(Component.literal("Character Custom Dialog Screen"));
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        LocalPlayer player = minecraft.player;
        ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(minecraft.player.getUUID());
        fakePlayer.remove(Entity.RemovalReason.KILLED);
        player.setPos(lastPos);
        player.setXRot(lastXRot);
        player.setYRot(lastYRot);
        player.setYHeadRot(lastYRot);
        serverPlayer.setGameMode(GameType.CREATIVE);
        serverPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID())));
        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(fakePlayer.getId()));
        NarrativeCraftMod.getInstance().setTestDialog(null);
        minecraft.setScreen(lastScreen);
    }

    @Override
    protected void init() {
        DialogData dialogData = DialogData.globalDialogData;
        if(fakePlayer == null) {
            LocalPlayer player = minecraft.player;
            Vec3 localPos = player.position();
            lastPos = localPos;
            lastXRot = player.getXRot();
            lastYRot = player.getYRot();
            ServerLevel serverLevel = Utils.getServerLevel();
            ServerPlayer serverPlayer = Utils.getServerPlayerByUUID(minecraft.player.getUUID());
            serverPlayer.setGameMode(GameType.SPECTATOR);
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            player.setDeltaMovement(0, 0, 0);
            player.setPos(localPos.x, localPos.y + player.getEyeHeight() + 5, localPos.z);
            player.setXRot(0);
            player.setYRot(0);
            player.setYHeadRot(0);
            fakePlayer = new FakePlayer(serverLevel, new GameProfile(UUID.randomUUID(), ""));
            fakePlayer.setXRot(0);
            fakePlayer.setYHeadRot(180);
            fakePlayer.setYRot(180);
            fakePlayer.setPos(localPos.x, localPos.y + player.getEyeHeight() + 4.2, localPos.z + 2);
            serverPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            serverLevel.addFreshEntity(fakePlayer);
            Dialog dialog = new Dialog(
                fakePlayer,
                "Lorem ipsum dolor sit amet consectetur adipiscing elit",
                dialogData
            );
            dialog.setUnSkippable(true);
            NarrativeCraftMod.getInstance().setTestDialog(dialog);
        }
        int gap = 5;
        int labelHeight = 20;
        int labelWidth = 50;
        int currentY;
        int startX = this.width - labelWidth - 120;

        errorWidget = ScreenUtils.text(Component.literal(""), minecraft.font, this.width / 2, 10);
        this.addRenderableWidget(errorWidget);
        currentY = this.height / 2 - ((labelHeight + gap) * 12) / 2;
        paddingXBox = new ScreenUtils.LabelBox(
                Component.literal("Padding X"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        paddingXBox.getEditBox().setValue(String.valueOf(dialogData.getPaddingX()));
        this.addRenderableWidget(paddingXBox.getStringWidget());
        this.addRenderableWidget(paddingXBox.getEditBox());
        currentY += paddingXBox.getEditBox().getHeight() + gap;

        paddingYBox = new ScreenUtils.LabelBox(
                Component.literal("Padding Y"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        paddingYBox.getEditBox().setValue(String.valueOf(dialogData.getPaddingY()));
        this.addRenderableWidget(paddingYBox.getStringWidget());
        this.addRenderableWidget(paddingYBox.getEditBox());
        currentY += paddingYBox.getEditBox().getHeight() + gap;

        scaleBox = new ScreenUtils.LabelBox(
                Component.literal("Scale"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        scaleBox.getEditBox().setValue(String.valueOf(dialogData.getScale()));
        this.addRenderableWidget(scaleBox.getStringWidget());
        this.addRenderableWidget(scaleBox.getEditBox());
        currentY += scaleBox.getEditBox().getHeight() + gap;

        letterSpacingBox = new ScreenUtils.LabelBox(
                Component.literal("Letter Spacing"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        letterSpacingBox.getEditBox().setValue(String.valueOf(dialogData.getLetterSpacing()));
        this.addRenderableWidget(letterSpacingBox.getStringWidget());
        this.addRenderableWidget(letterSpacingBox.getEditBox());
        currentY += letterSpacingBox.getEditBox().getHeight() + gap;

        gapBox = new ScreenUtils.LabelBox(
                Component.literal("Gap"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        gapBox.getEditBox().setValue(String.valueOf(dialogData.getGap()));
        this.addRenderableWidget(gapBox.getStringWidget());
        this.addRenderableWidget(gapBox.getEditBox());
        currentY += gapBox.getEditBox().getHeight() + gap;

        maxWidthBox = new ScreenUtils.LabelBox(
                Component.literal("Max Width"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        maxWidthBox.getEditBox().setValue(String.valueOf(dialogData.getMaxWidth()));
        this.addRenderableWidget(maxWidthBox.getStringWidget());
        this.addRenderableWidget(maxWidthBox.getEditBox());
        currentY += maxWidthBox.getEditBox().getHeight() + gap;

        bcColorBox = new ScreenUtils.LabelBox(
                Component.literal("Background Color"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        bcColorBox.getEditBox().setValue(Integer.toHexString(dialogData.getBackgroundColor()));
        this.addRenderableWidget(bcColorBox.getStringWidget());
        this.addRenderableWidget(bcColorBox.getEditBox());
        currentY += bcColorBox.getEditBox().getHeight() + gap;

        textColorBox = new ScreenUtils.LabelBox(
                Component.literal("Text Color"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        textColorBox.getEditBox().setValue(Integer.toHexString(dialogData.getTextColor()));
        this.addRenderableWidget(textColorBox.getStringWidget());
        this.addRenderableWidget(textColorBox.getEditBox());
        currentY += textColorBox.getEditBox().getHeight() + gap;

        bobbingSpeed = new ScreenUtils.LabelBox(
                Component.literal("Bobbing Speed"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        bobbingSpeed.getEditBox().setValue(String.valueOf(dialogData.getBobbingNoiseShakeSpeed()));
        this.addRenderableWidget(bobbingSpeed.getStringWidget());
        this.addRenderableWidget(bobbingSpeed.getEditBox());
        currentY += bobbingSpeed.getEditBox().getHeight() + gap;

        bobbingStrength = new ScreenUtils.LabelBox(
                Component.literal("Bobbing Strength"),
                minecraft.font,
                labelWidth,
                labelHeight,
                startX,
                currentY,
                ScreenUtils.Align.HORIZONTAL
        );
        bobbingStrength.getEditBox().setValue(String.valueOf(dialogData.getBobbingNoiseShakeStrength()));
        this.addRenderableWidget(bobbingStrength.getStringWidget());
        this.addRenderableWidget(bobbingStrength.getEditBox());
        currentY += bobbingStrength.getEditBox().getHeight() + gap;

        Button updateButton = Button.builder(Translation.message("screen.update.text"), button -> {
            updateValues();
        }).pos(startX, currentY).width(70).build();
        this.addRenderableWidget(updateButton);

        currentY += updateButton.getHeight() + gap;

        Button closeButton = Button.builder(CommonComponents.GUI_DONE, button -> {
            updateValues();
            onClose();
        }).pos(startX, currentY).width(70).build();
        this.addRenderableWidget(closeButton);

    }

    private void updateValues() {
        try {
            DialogData dialogData = DialogData.globalDialogData;
            dialogData.setPaddingX(Float.parseFloat(paddingXBox.getEditBox().getValue()));
            dialogData.setPaddingY(Float.parseFloat(paddingYBox.getEditBox().getValue()));
            dialogData.setScale(Float.parseFloat(scaleBox.getEditBox().getValue()));
            dialogData.setLetterSpacing(Float.parseFloat(letterSpacingBox.getEditBox().getValue()));
            dialogData.setGap(Float.parseFloat(gapBox.getEditBox().getValue()));
            dialogData.setMaxWidth(Integer.parseInt(maxWidthBox.getEditBox().getValue()));
            dialogData.setBackgroundColor(Integer.parseInt(bcColorBox.getEditBox().getValue(), 16));
            dialogData.setTextColor(Integer.parseInt(textColorBox.getEditBox().getValue(), 16));
            dialogData.setBobbingNoiseShakeSpeed(Float.parseFloat(bobbingSpeed.getEditBox().getValue()));
            dialogData.setBobbingNoiseShakeStrength(Float.parseFloat(bobbingStrength.getEditBox().getValue()));
            Dialog dialog = NarrativeCraftMod.getInstance().getTestDialog();
            dialog.setText(dialog.getText());
            dialog.setPaddingX(dialogData.getPaddingX());
            dialog.setPaddingY(dialogData.getPaddingY());
            dialog.setScale(dialogData.getScale());
            dialog.setLetterSpacing(dialogData.getLetterSpacing());
            dialog.setGap(dialogData.getGap());
            dialog.setMaxWidth(dialogData.getMaxWidth());
            dialog.setDialogBackgroundColor(dialogData.getBackgroundColor());
            dialog.setTextDialogColor(dialogData.getTextColor());
            dialog.getDialogEntityBobbing().setNoiseShakeSpeed(dialogData.getBobbingNoiseShakeSpeed());
            dialog.getDialogEntityBobbing().setNoiseShakeStrength(dialogData.getBobbingNoiseShakeStrength());
            dialog.reset();
            dialog.setUnSkippable(true);
            errorWidget.setMessage(Component.empty());
            NarrativeCraftFile.updateGlobalDialogValues(dialogData);
        } catch (NumberFormatException e) {
            Component message = Component.literal(e.getMessage()).withColor(0xF24949);
            errorWidget.setMessage(message);
            errorWidget.setPosition(this.width / 2 - minecraft.font.width(message) / 2, 10);
            errorWidget.setWidth(minecraft.font.width(message));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}
}
