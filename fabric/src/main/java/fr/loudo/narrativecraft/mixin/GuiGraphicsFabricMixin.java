
package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.gui.ICustomGuiRender;
import fr.loudo.narrativecraft.gui.IGuiTextAccessor;
import fr.loudo.narrativecraft.gui.SkipArrow2dGui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsFabricMixin implements ICustomGuiRender {

    @Shadow @Final private GuiRenderState guiRenderState;

    @Shadow @Final private Matrix3x2fStack pose;

    @Shadow @Final private GuiGraphics.ScissorStack scissorStack;

    @Override
    public void drawnDialogSkip(float dialogWidth, float width, float height, float offsetX, int color) {
        this.guiRenderState.submitGuiElement(
                new SkipArrow2dGui(
                        RenderPipelines.GUI,
                        TextureSetup.noTexture(),
                        new Matrix3x2f(this.pose),
                        dialogWidth,
                        width,
                        height,
                        offsetX,
                        color,
                        this.scissorStack.peek()
                ));
    }

    @Override
    public void drawStringFloat(String text, Font font, float x, float y, int color, boolean drawShadow) {
        if (ARGB.alpha(color) != 0) {
            GuiTextRenderState guiTextRenderState = new GuiTextRenderState(
                    font,
                    Language.getInstance().getVisualOrder(FormattedText.of(text)),
                    new Matrix3x2f(this.pose),
                    (int) x,
                    (int) y,
                    color,
                    0,
                    drawShadow,
                    this.scissorStack.peek()
            );
            ((IGuiTextAccessor)(Object)guiTextRenderState).narrativecraft$setFloatX(x);
            ((IGuiTextAccessor)(Object)guiTextRenderState).narrativecraft$setFloatY(y);
            this.guiRenderState.submitText(guiTextRenderState);
        }
    }

}
