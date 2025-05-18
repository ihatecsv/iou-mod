package com.ihatecsv.iou;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class IOUComponents {

    public static final ComponentType<Boolean> STRICT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("iou:strict"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .build());

    public static final ComponentType<String>  OWED_BY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("iou:owed_by"),
            ComponentType.<String>builder()
                    .codec(Codec.STRING)
                    .build());

    public static final ComponentType<ItemStack> ORIGINAL_STACK = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("iou:original_stack"),
            ComponentType.<ItemStack>builder()
                    .codec(ItemStack.CODEC)
                    .packetCodec(ItemStack.PACKET_CODEC)
                    .build());

    public static void init() {}

    private IOUComponents() {}
}
