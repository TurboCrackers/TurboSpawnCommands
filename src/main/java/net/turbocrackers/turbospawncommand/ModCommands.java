package net.turbocrackers.turbospawncommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.awt.*;

import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = literal("spawn").executes(ModCommands::execSpawnCommand);
        dispatcher.register(command);
    }

    private static int execSpawnCommand(CommandContext<CommandSourceStack> context)
    {
        CommandSourceStack source = context.getSource();
        MutableComponent message = Component.literal("Teleported to spawn.");
        source.sendSystemMessage(message);

        // Get spawn position
        MinecraftServer current_server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = current_server.getLevel(Level.OVERWORLD);
        BlockPos spawn_pos = level.getSharedSpawnPos();
        float spawn_angle = level.getSharedSpawnAngle();

        // Make sure it's not already occupied by a block.
        while (level.getBlockState(spawn_pos).isSuffocating(level, spawn_pos))
        {
            spawn_pos = spawn_pos.above(2);
        }

        // Teleport player
        if( source.isPlayer() )
        {
            source.getPlayer().teleportTo( level, spawn_pos.getX(), spawn_pos.getY(), spawn_pos.getZ(), spawn_angle, 0 );
        }

        return 1;
    }
}