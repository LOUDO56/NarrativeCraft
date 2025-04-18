package fr.loudo.narrativecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.math.Transformation;
import fr.loudo.narrativecraft.mixin.fields.DisplayFields;
import fr.loudo.narrativecraft.mixin.fields.ItemDisplayFields;
import fr.loudo.narrativecraft.screens.CutsceneSettingsScreen;
import fr.loudo.narrativecraft.screens.KeyframeOptionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestCommand {

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
        );
    }

    private static int openTestScreen(CommandContext<CommandSourceStack> context) {

        KeyframeOptionScreen keyframeOptionScreen = new KeyframeOptionScreen(null);
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
        CutsceneSettingsScreen cutsceneSettingsScreen = new CutsceneSettingsScreen();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(cutsceneSettingsScreen));
        return Command.SINGLE_SUCCESS;
    }
}
