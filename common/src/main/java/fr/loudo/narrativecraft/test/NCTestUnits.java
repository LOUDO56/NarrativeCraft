package fr.loudo.narrativecraft.test;

import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scenes.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scenes.animations.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.recordings.MovementData;
import fr.loudo.narrativecraft.narrative.recordings.actions.ActionsData;
import fr.loudo.narrativecraft.narrative.recordings.playback.Playback;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

// TODO: Complete

public class NCTestUnits {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final CharacterStory characterStory = new CharacterStory("test");

    private static final String TESTING = "Testing %s...";
    private static final String PASSED = "§aPassed! ✔";
    private static final String FAILED = "§cTest unit failed! ✖ %s";

    public static void test() {
        Chapter chapter = new Chapter(0, "test", "test");
        Scene scene = new Scene("test", "test", chapter);
        check(testCreateChapterFolder(chapter), "Impossible to create chapter folder.");
        check(testCreateSceneFolder(scene), "Impossible to create scene folder.");
        check(testCreateCharacterFolder(characterStory), "Impossible to create a character folder.");
    }

    private static void check(boolean isPassed, String failMessage) {
        if(!isPassed) {
            throw new RuntimeException(String.format(FAILED, failMessage));
        } else {
            sendPassedMessage();
        }
    }

    private static void sendPassedMessage() {
        minecraft.player.displayClientMessage(Component.literal(PASSED), false);
    }

    private static void sendTestingMessage(String label) {
        minecraft.player.displayClientMessage(Component.literal(String.format(TESTING, label)), false);
    }

    private static boolean testCreateSceneFolder(Scene scene) {
        sendTestingMessage("Testing creating scene folder");
        return NarrativeCraftFile.createSceneFolder(scene);
    }

    private static boolean testCreateChapterFolder(Chapter chapter) {
        sendTestingMessage("Testing creating chapter folder");
        return NarrativeCraftFile.createChapterDirectory(chapter);
    }

    private static boolean testCreateCharacterFolder(CharacterStory characterStory) {
        sendTestingMessage("Testing creating character folder");
        return NarrativeCraftFile.createCharacterFile(characterStory);
    }

    private static boolean testPlayAnimation(Scene scene, boolean loop) {
        Animation animation = new Animation("test", "test");
        FakePlayer fakePlayer = FakePlayer.createRandom();
        animation.getActionsData().add(new ActionsData(fakePlayer, 0));
        Vec3 position = minecraft.player.position();
        for (int i = 0; i < 40; i++) {
            animation.getActionsData().getFirst().getMovementData().add(new MovementData(
                    position.x,
                    position.y,
                    position.z,
                    0,
                    0,
                    0,
                    true
            ));
            position.add(0.5, 0, 0);
        }
        Playback playback = new Playback(
            animation,
            Utils.getServerLevel(),
            characterStory,
            Playback.PlaybackType.DEVELOPMENT,
            loop
        );
        playback.start();
        return true;
    }
}
