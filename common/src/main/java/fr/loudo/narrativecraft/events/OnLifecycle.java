package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import net.minecraft.server.MinecraftServer;

public class OnLifecycle {

    public static void execute(MinecraftServer server) {
        if(server != null) {
            NarrativeCraftMod.server = server;
            NarrativeCraftFile.init(server);
            NarrativeCraftMod.getInstance().getCharacterManager().init();
            NarrativeCraftMod.getInstance().getChapterManager().init();
        }
    }

}
