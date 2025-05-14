package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.commands.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandsRegister {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        PlayerSessionCommand.register(event.getDispatcher());
        TestCommand.register(event.getDispatcher());
    }

}
