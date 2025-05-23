package fr.loudo.narrativecraft;

import fr.loudo.narrativecraft.registers.CommandsRegister;
import fr.loudo.narrativecraft.registers.EventsRegister;
import fr.loudo.narrativecraft.registers.ModKeysRegister;
import net.fabricmc.api.ModInitializer;

public class NarrativeCraftFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        CommandsRegister.register();
        EventsRegister.register();
        ModKeysRegister.register();

    }
}
