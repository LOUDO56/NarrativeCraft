package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.gui.IGuiTextAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


// Same class as GuiTextRenderState, but has float as x and y value.
// It's needed for me, to make dialog effects such as waving or shaking no wacky and smooth
@Mixin(value = GuiTextRenderState.class)
public class GuiTextRenderStateMixin implements IGuiTextAccessor {
    private float xFloat;
    private float yFloat;

    @Redirect(method = "ensurePrepared", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;"))
    private Font.PreparedText narrativecraft$ensurePrepared(Font instance, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, int backgroundColor) {
        float finalX = (xFloat > 0) ? xFloat : x;
        float finalY = (yFloat > 0) ? yFloat : y;
        return instance.prepareText(text, finalX, finalY, color, dropShadow, backgroundColor);
    }

    @Override
    public void narrativecraft$setFloatX(float floatX) {
        xFloat = floatX;
    }

    @Override
    public void narrativecraft$setFloatY(float floatY) {
        yFloat = floatY;
    }
}
