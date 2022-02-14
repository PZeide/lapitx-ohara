package com.zeide.lapitxohara.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.level.ServerWorldProperties;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LookupCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lookup")
                .executes(LookupCommand::lookupAll)
        );
    }

    private static int lookupAll(CommandContext<ServerCommandSource> context) {
        HashMap<ServerPlayerEntity, Integer> playerEntityCount = new HashMap<>();

        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            // Only look for living entities
            for (Entity entity : world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), EntityPredicates.VALID_LIVING_ENTITY)) {
                if (entity.isPlayer())
                    continue;

                Collection<ServerPlayerEntity> trackingPlayers = PlayerLookup.tracking(entity);
                for (ServerPlayerEntity player : trackingPlayers) {
                    playerEntityCount.put(player, playerEntityCount.getOrDefault(player, 0) + 1);
                }
            }
        }

        LiteralText result = new LiteralText("");
        result.append(new LiteralText("Données d'analyse:").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)));
        for (Map.Entry<ServerPlayerEntity, Integer> lookupEntry : playerEntityCount.entrySet()) {
            result.append(Text.of("\n"));

            ServerPlayerEntity player = lookupEntry.getKey();
            int entityCount = lookupEntry.getValue();

            ServerWorld world = player.getWorld();
            String worldName = ((ServerWorldProperties) world.getLevelProperties()).getLevelName();
            long worldEntitiesCount = world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), EntityPredicates.VALID_LIVING_ENTITY).stream()
                    .filter(e -> !e.isPlayer())
                    .count();
            double playerEntityPercentage = ((double) entityCount / worldEntitiesCount) * 100;

            result.append(player.getDisplayName().copy().setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            result.append(new LiteralText(" charge ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            result.append(new LiteralText(String.valueOf(entityCount)).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
            result.append(new LiteralText(String.format(" entités (%.2f%% total du monde) dans ", playerEntityPercentage)).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            result.append(new LiteralText(worldName).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
        }

        context.getSource().sendFeedback(result, false);
        return 1;
    }
}
