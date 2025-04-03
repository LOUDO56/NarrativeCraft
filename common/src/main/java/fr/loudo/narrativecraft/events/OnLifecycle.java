package fr.loudo.narrativecraft.events;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import net.minecraft.server.MinecraftServer;

public class OnLifecycle {

    public static void execute(MinecraftServer server) {
        NarrativeCraftFile.init(server);
        NarrativeCraftMod.server = server;
    }

}
