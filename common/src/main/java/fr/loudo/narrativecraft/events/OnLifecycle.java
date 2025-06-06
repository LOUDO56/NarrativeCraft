package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.ChapterManager;
import net.minecraft.server.MinecraftServer;

public class OnLifecycle {

    public static void execute(MinecraftServer server) {
        if(server != null) {
            NarrativeCraftFile.init(server);
            NarrativeCraftMod.server = server;
            NarrativeCraftMod.getInstance().getChapterManager().init();
            NarrativeCraftMod.getInstance().getCharacterManager().init();
        }
    }

}
