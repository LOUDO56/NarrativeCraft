package fr.loudo.narrativecraft;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(NarrativeCraftMod.MOD_ID)
public class NarrativeCraftNeoForge {

    public NarrativeCraftNeoForge(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        NarrativeCraftMod.LOG.info("Hello NeoForge world!");
        CommonClass.init();


    }
}
