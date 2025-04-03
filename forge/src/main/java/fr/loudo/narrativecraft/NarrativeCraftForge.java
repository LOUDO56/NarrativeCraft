package fr.loudo.narrativecraft;

import net.minecraftforge.fml.common.Mod;

@Mod(NarrativeCraftMod.MOD_ID)
public class NarrativeCraftForge {

    public NarrativeCraftForge() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        NarrativeCraftMod.LOG.info("Hello Forge world!");
        CommonClass.init();

    }
}
