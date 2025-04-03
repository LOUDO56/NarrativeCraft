package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftManager;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.animations.Animation;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import net.minecraft.server.level.ServerPlayer;

public class InkTagTranslators {

    public static void execute(String command) {
        String[] segment = command.split(" ");
        switch (segment[0]) {
            case "animation":
                playAnimation(segment[2]);
                break;
        }
    }

    //NOTE: only for testing
    private static void playAnimation(String animationName) {
        String[] splittedName = animationName.split("\\.");
        String finalName;
        if(splittedName.length > 1) {
            finalName = splittedName[2];
        } else {
            finalName = animationName;
        }
        Animation animation = NarrativeCraftFile.getAnimationFromFile(finalName);
        ServerPlayer serverPlayer = NarrativeCraftManager.server.getPlayerList().getPlayers().getFirst();
        Playback playback = new Playback(animation, serverPlayer.serverLevel());
        playback.start();
    }


}
