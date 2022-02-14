package com.zeide.lapitxohara.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow public ServerPlayerEntity player;
    @Shadow private int ticks;

    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "tick", at = @At("TAIL"))
    private void updatePlayerList(CallbackInfo info) {
        if (ticks % 20 == 0) {
            updateTaskList();
        }
    }

    private void updateTaskList() {
        int playerCount = server.getCurrentPlayerCount();
        float tps = 1000 / Math.max(server.getTickTime(), 50);

        LiteralText header = new LiteralText("");
        header.append(new LiteralText("Lapitx Mutus\n").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)));
        header.append(new LiteralText("Joueurs connectés: ").setStyle(Style.EMPTY.withColor(0x8c8c8c)));
        header.append(new LiteralText(String.valueOf(playerCount)).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));

        LiteralText footer = new LiteralText("");
        footer.append(new LiteralText("TPS: ").setStyle(Style.EMPTY.withColor(0x8c8c8c)));
        footer.append(new LiteralText(String.format("%.1f", tps)).setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
        footer.append("   ");
        footer.append(new LiteralText("Ping: ").setStyle(Style.EMPTY.withColor(0x8c8c8c)));
        footer.append(new LiteralText(player.pingMilliseconds + "ms").setStyle(Style.EMPTY.withColor(Formatting.GOLD)));

        PlayerListHeaderS2CPacket playerListPacket = new PlayerListHeaderS2CPacket(header, footer);
        sendPacket(playerListPacket);
    }
}
