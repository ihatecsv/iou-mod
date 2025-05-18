package com.ihatecsv.iou.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record IOUDropPayload(boolean strict) implements CustomPayload {

    public static final Id<IOUDropPayload> ID =
            new Id<>(Identifier.of("iou", "drop_iou"));

    public static final PacketCodec<RegistryByteBuf, IOUDropPayload> CODEC =
            PacketCodecs.BOOL
                    .xmap(IOUDropPayload::new, IOUDropPayload::strict)
                    .cast();

    static {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }

    @Override
    public Id<IOUDropPayload> getId() {
        return ID;
    }
}
