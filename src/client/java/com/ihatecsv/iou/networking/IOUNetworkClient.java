package com.ihatecsv.iou.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class IOUNetworkClient {
    public static void sendDropPacket(boolean strict) {
        ClientPlayNetworking.send(new IOUDropPayload(strict));
    }
}