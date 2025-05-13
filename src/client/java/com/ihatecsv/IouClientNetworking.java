package com.ihatecsv;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public final class IouClientNetworking {
    public static void sendDropPacket(boolean strict) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(strict);
        ClientPlayNetworking.send(IouNetworking.DROP_PACKET, buf);
    }
}