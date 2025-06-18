package fr.loudo.narrativecraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Utf8String;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Bypass Minecraft 16 characters name limit
@Mixin(Utf8String.class)
public abstract class Utf8StringMixin {

    @ModifyVariable(method = "write", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int modifyWriteMaxLength(int maxLength) {
        return getMaxTagNameLength(maxLength);
    }

    @ModifyVariable(method = "read", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int modifyReadMaxLength(int maxLength) {
        return getMaxTagNameLength(maxLength);
    }

    private static int getMaxTagNameLength(int maxLength) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft != null) {
            if(minecraft.isSingleplayer()) {
                return maxLength == 16 ? 64 : maxLength;
            } else {
                return maxLength;
            }
        }
        return maxLength;
    }
}