package com.zeide.lapitxohara.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zeide.lapitxohara.LapitxOharaMod;
import com.zeide.lapitxohara.config.SpawnPointData;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ForceSpawnPointCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("forcespawnpoint")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(ForceSpawnPointCommand::setWorldSpawnPoint)
        );
    }

    private static int setWorldSpawnPoint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        SpawnPointData spawnPointData = new SpawnPointData(
                player.getWorld().getRegistryKey(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch()
        );

        LapitxOharaMod.INSTANCE.getConfig().setSpawnPointData(spawnPointData);
        LapitxOharaMod.INSTANCE.getConfig().writeConfig();

        return 1;
    }
}
