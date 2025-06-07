package fr.loudo.narrativecraft.mixin;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow @Final private GameProfile profile;

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        UUID playerUUID = this.profile.getId();
        List<CharacterStory> characters = getRelevantCharacters();

        for (CharacterStory character : characters) {
            if (character.getEntity() == null || character.getCharacterSkinController().getCurrentSkin() == null) continue;
            if (!(character.getEntity() instanceof Player)) continue;
            if (!playerUUID.equals(character.getEntity().getUUID())) continue;

            PlayerSkin.Model model = character.getModel();
            ResourceLocation skinLocation = ResourceLocation.fromNamespaceAndPath(
                    NarrativeCraftMod.MOD_ID,
                    "character/" + Utils.getSnakeCase(character.getName()) + "/" + Utils.getSnakeCase(character.getCharacterSkinController().getCurrentSkin().getName())
            );

            PlayerSkin playerSkin = new PlayerSkin(
                    skinLocation,
                    null,
                    null,
                    null,
                    model == null ? PlayerSkin.Model.WIDE : model,
                    true
            );

            cir.setReturnValue(playerSkin);
        }
    }

    private List<CharacterStory> getRelevantCharacters() {
        NarrativeCraftMod mod = NarrativeCraftMod.getInstance();
        StoryHandler storyHandler = mod.getStoryHandler();
        if (storyHandler != null) {
            return storyHandler.getCurrentCharacters();
        } else {
            return mod.getPlaybackHandler()
                    .getPlaybacks()
                    .stream()
                    .map(Playback::getCharacter)
                    .toList();
        }
    }


}
