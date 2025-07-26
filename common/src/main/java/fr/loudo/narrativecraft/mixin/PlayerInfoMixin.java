package fr.loudo.narrativecraft.mixin;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scenes.cameraAngle.CameraAngleController;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow @Final private GameProfile profile;

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    public void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        try {
            List<CharacterStory> characters = getRelevantCharacters();

            for (CharacterStory character : characters) {
                if(character.getCharacterSkinController() == null) continue;
                if (character.getEntity() == null || character.getCharacterSkinController().getCurrentSkin() == null) continue;
                if (!(character.getEntity() instanceof FakePlayer)) continue;
                if (!this.profile.getName().equals(character.getName())) continue;

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
        } catch (Exception ignored) {}
    }

    private List<CharacterStory> getRelevantCharacters() {
        PlayerSession playerSession = NarrativeCraftMod.getInstance().getPlayerSession();
        NarrativeCraftMod mod = NarrativeCraftMod.getInstance();
        StoryHandler storyHandler = mod.getStoryHandler();
        if (storyHandler != null && storyHandler.isRunning()) {
            return storyHandler.getCurrentCharacters();
        } else if (playerSession != null && playerSession.getKeyframeControllerBase() instanceof CameraAngleController cameraAngleController) {
            return cameraAngleController.getCharacters();
        } else {
            return mod.getPlaybackHandler()
                    .getPlaybacks()
                    .stream()
                    .map(Playback::getCharacter)
                    .toList();
        }
    }


}
