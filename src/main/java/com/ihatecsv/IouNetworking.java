package com.ihatecsv;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IouNetworking {
    public static final Identifier DROP_PACKET = new Identifier(Iou.MOD_ID, "drop_iou");

    private static final long COOLDOWN_TICKS = 40L;

    private static final Map<UUID, Map<String, Long>> LAST_DROP = new ConcurrentHashMap<>();

    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(
                DROP_PACKET,
                (server, player, handler, buf, responseSender) -> {
                    boolean strict = buf.readBoolean();
                    server.execute(() -> handleDrop(player, strict));
                }
        );
    }

    private static void handleDrop(ServerPlayerEntity player, boolean strict) {
        ItemStack held = player.getMainHandStack();

        if (held.isEmpty() || held.getItem() == Iou.IOU_ITEM) {
            return;
        }

        long now = player.getWorld().getTime();
        String sig = stackSignature(held, strict);

        Map<String, Long> byStack = LAST_DROP.computeIfAbsent(
                player.getUuid(), id -> new ConcurrentHashMap<>());

        Long last = byStack.get(sig);
        if (last != null && now - last < COOLDOWN_TICKS) {
            return;
        }
        byStack.put(sig, now);

        ItemStack iou = new ItemStack(Iou.IOU_ITEM, held.getCount());
        NbtCompound tag = iou.getOrCreateNbt();
        tag.put("original_nbt", held.writeNbt(new NbtCompound()));
        tag.putString("owed_by", player.getName().getString());
        tag.putBoolean("strict", strict);

        World world = player.getWorld();
        Vec3d dir = player.getRotationVec(1.0F);

        double spawnX = player.getX() + dir.x * 0.5;
        double spawnY = player.getEyeY() - 0.3;
        double spawnZ = player.getZ() + dir.z * 0.5;

        ItemEntity entity = new ItemEntity(world, spawnX, spawnY, spawnZ, iou);

        float velocity = 0.3F;
        entity.setVelocity(dir.x * velocity,
                dir.y * velocity + 0.1F,
                dir.z * velocity);

        entity.setOwner(player.getUuid());
        entity.setThrower(player.getUuid());
        entity.setToDefaultPickupDelay();

        world.spawnEntity(entity);

        Iou.LOGGER.debug("Created {} IOU for {} ({}× {})",
                strict ? "strict" : "lenient",
                player.getName().getString(),
                held.getCount(),
                Registries.ITEM.getId(held.getItem()));
    }

    private static String stackSignature(ItemStack stack, boolean strict) {
        if (strict) {
            NbtCompound nbt = stack.writeNbt(new NbtCompound());
            return Registries.ITEM.getId(stack.getItem()) + "|" + nbt.toString();
        }
        return Registries.ITEM.getId(stack.getItem()) + "|" + stack.getCount();
    }
}
