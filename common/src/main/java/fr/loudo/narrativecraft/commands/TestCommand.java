package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.math.Transformation;
import fr.loudo.narrativecraft.events.OnHudRender;
import fr.loudo.narrativecraft.mixin.fields.DisplayFields;
import fr.loudo.narrativecraft.mixin.fields.ItemDisplayFields;
import fr.loudo.narrativecraft.screens.keyframes.KeyframeOptionScreen;
import fr.loudo.narrativecraft.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestCommand {

    static double t = 0;
    static ScheduledExecutorService scheduler;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("test")
                .then(Commands.literal("screen")
                        .then(Commands.literal("changeSecondValue")
                                .executes(TestCommand::openScreenChangeSecond)
                        )
                        .then(Commands.literal("testScreen")
                                .executes(TestCommand::openTestScreen)
                        )
                )
                .then(Commands.literal("keyframeDisplay")
                        .executes(TestCommand::createKeyFrameItem)
                )
                .then(Commands.literal("sound")
                        .then(Commands.literal("playFadeOut")
                                .executes(TestCommand::playFadeOut)
                        )
                )
                .then(Commands.literal("dialog")
                        .executes(TestCommand::dialogText)
                )
        );
    }

    private static int dialogText(CommandContext<CommandSourceStack> context) {

        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, context.getSource().getLevel());
        armorStand.snapTo(context.getSource().getPosition());
        context.getSource().getLevel().addFreshEntity(armorStand);
        OnHudRender.entityPos = armorStand.position();

        return Command.SINGLE_SUCCESS;
    }

    private static int playFadeOut(CommandContext<CommandSourceStack> context) {

        long startTime = System.currentTimeMillis();
        long endTime = 20000L;
        ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("custom.distant_memory");
        SoundEvent sound = SoundEvent.createVariableRangeEvent(resourceLocation);
        context.getSource().sendSuccess(() -> Component.literal("Played sound, current volume to 0.5F!"), false);
        SimpleSoundInstance simpleSoundInstance = new SimpleSoundInstance(sound.location(), SoundSource.MASTER, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.NONE, (double)0.0F, (double)0.0F, (double)0.0F, true);
        Minecraft.getInstance().getSoundManager().play(simpleSoundInstance);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            t = Math.min((double) elapsedTime / endTime, 1.0);
            float newVolume = (float) MathUtils.lerp(1, 0, t);
            Minecraft.getInstance().getSoundManager().setVolume(simpleSoundInstance, newVolume);
            context.getSource().sendSuccess(() -> Component.literal(String.format("New volume set to: %.2f", newVolume)), false);
            if(t >= 1.0) {
                scheduler.shutdown();
                Minecraft.getInstance().getSoundManager().stop(simpleSoundInstance);
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 50, TimeUnit.MILLISECONDS);

        return Command.SINGLE_SUCCESS;

    }

    private static int openTestScreen(CommandContext<CommandSourceStack> context) {

        KeyframeOptionScreen keyframeOptionScreen = new KeyframeOptionScreen(null, context.getSource().getPlayer());
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> client.setScreen(keyframeOptionScreen));

        return Command.SINGLE_SUCCESS;
    }

    private static int createKeyFrameItem(CommandContext<CommandSourceStack> context) {

        ServerPlayer player = context.getSource().getPlayer();

        Display.ItemDisplay itemDisplay = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, player.level());
        ((DisplayFields)itemDisplay).callSetBillboardConstraints(Display.BillboardConstraints.CENTER);
        Transformation transformation = new Transformation(
                new Vector3f(0f, 0f, 0f),
                new Quaternionf(0f, 0f, 0f, 1f),
                new Vector3f(0.5f, 0.5f, 0.5f),
                new Quaternionf(0f, 0f, 0f, 1f)
        );
        ((ItemDisplayFields)itemDisplay).callSetItemStack(new ItemStack(Items.ENDER_EYE));
        ((DisplayFields)itemDisplay).callSetBillboardConstraints(Display.BillboardConstraints.CENTER);
        ((DisplayFields)itemDisplay).callSetTransformation(transformation);
        itemDisplay.snapTo(player.position());
        player.level().addFreshEntity(itemDisplay);
        context.getSource().getPlayer().sendSystemMessage(Component.literal("Spawned"));
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {
            player.connection.send(new ClientboundRemoveEntitiesPacket(itemDisplay.getId()));
            context.getSource().getPlayer().sendSystemMessage(Component.literal("removed"));
        }, 2, TimeUnit.SECONDS);

        return Command.SINGLE_SUCCESS;
    }

    private static int openScreenChangeSecond(CommandContext<CommandSourceStack> context) {
        return Command.SINGLE_SUCCESS;
    }
}
