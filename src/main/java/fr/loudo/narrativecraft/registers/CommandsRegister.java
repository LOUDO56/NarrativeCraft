package fr.loudo.narrativecraft.registers;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.commands.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NarrativeCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandsRegister {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        ChapterCommand.register(event.getDispatcher());
        SceneCommand.register(event.getDispatcher());
        AnimationCommand.register(event.getDispatcher());
        CharacterCommand.register(event.getDispatcher());
        RecordCommand.register(event.getDispatcher());
        SessionCommand.register(event.getDispatcher());
    }

}
