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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.awt.*;

import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> spawn_command = literal("spawn").executes(ModCommands::execSpawnCommand);
        dispatcher.register(spawn_command);

        LiteralArgumentBuilder<CommandSourceStack> set_spawn_command = literal("setspawn").executes(ModCommands::execSetSpawnCommand).requires((source) -> source.hasPermission(3));
        dispatcher.register(set_spawn_command);
    }

    private static int execSpawnCommand(CommandContext<CommandSourceStack> context)
    {
        CommandSourceStack source = context.getSource();
        if( source.isPlayer() )
        {
            ServerPlayer player = source.getPlayer();

            // Get spawn position.
            MinecraftServer current_server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel overworld = current_server.getLevel(Level.OVERWORLD);
            BlockPos spawn_pos = overworld.getSharedSpawnPos();
            float spawn_angle = overworld.getSharedSpawnAngle();

            // Make sure it's not already occupied by a block.
            while (overworld.getBlockState(spawn_pos).isSuffocating(overworld, spawn_pos))
            {
                spawn_pos = spawn_pos.above(2);
            }

            // Teleport the player.
            if( player.getCommandSenderWorld().dimension() != Level.OVERWORLD )
            {
                player.changeDimension(overworld);
            }
            player.teleportTo( overworld, spawn_pos.getX(), spawn_pos.getY(), spawn_pos.getZ(), spawn_angle, 0 );

            MutableComponent message = Component.literal("Teleported to spawn.");
            source.sendSystemMessage(message);
        }

        return 1;
    }

    private static int execSetSpawnCommand(CommandContext<CommandSourceStack> context)
    {
        CommandSourceStack source = context.getSource();
        if( source.isPlayer() )
        {
            ServerPlayer player = source.getPlayer();

            if( player.getCommandSenderWorld().dimension() == Level.OVERWORLD ) {
                // Get player location
                BlockPos player_pos = player.getOnPos();
                float player_rot_y = player.getYRot();

                // Set spawn pos
                MinecraftServer current_server = ServerLifecycleHooks.getCurrentServer();
                ServerLevel level = current_server.getLevel(player.getCommandSenderWorld().dimension());
                level.setDefaultSpawnPos(player_pos, player_rot_y);

                MutableComponent message = Component.literal("Spawn set to (" + player_pos.getX() + ", " + player_pos.getY() + ", " + player_pos.getZ() + ").");
                source.sendSystemMessage(message);
            }
            else
            {
                MutableComponent message = Component.literal("You must be in the Overworld to use /setspawn. Sorry!");
                source.sendSystemMessage(message);
            }
        }

        return 1;
    }
}