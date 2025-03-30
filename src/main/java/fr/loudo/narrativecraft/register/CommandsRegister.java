package fr.loudo.narrativecraft.register;

import fr.loudo.narrativecraft.NarrativeCraft;
import fr.loudo.narrativecraft.animations.commands.AnimationCommand;
import fr.loudo.narrativecraft.scenes.commands.SceneCommand;
import fr.loudo.narrativecraft.story.commands.ChapterCommand;
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
    }

}
