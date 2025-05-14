package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.commands.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(NarrativeCraftMod.MOD_ID)
public class CommandsRegister {

    public CommandsRegister(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(CommandsRegister::register);
    }

    public static void register(RegisterCommandsEvent event) {
        PlayerSessionCommand.register(event.getDispatcher());
        RecordCommand.register(event.getDispatcher());
        TestCommand.register(event.getDispatcher());
    }

}
