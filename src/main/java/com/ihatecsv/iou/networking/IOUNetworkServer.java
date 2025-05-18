package com.ihatecsv.iou.networking;

import com.ihatecsv.iou.IOU;
import com.ihatecsv.iou.IOUComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IOUNetworkServer {
    private static final long COOLDOWN_TICKS = 40L;

    private static final Map<UUID, Map<String, Long>> LAST_DROP = new ConcurrentHashMap<>();

    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(
                IOUDropPayload.ID,
                (payload, context) -> handleDrop(context.player(), payload.strict())
        );
    }

    private static void handleDrop(ServerPlayerEntity player, boolean strict) {
        ItemStack held = player.getMainHandStack();

        if (held.isEmpty() || held.getItem() == IOU.IOU_ITEM) {
            return;
        }

        long now = player.getWorld().getTime();
        String sig = stackSignature(held, strict, player);

        Map<String, Long> byStack = LAST_DROP.computeIfAbsent(
                player.getUuid(), id -> new ConcurrentHashMap<>());

        Long last = byStack.get(sig);
        if (last != null && now - last < COOLDOWN_TICKS) {
            return;
        }
        byStack.put(sig, now);

        ItemStack iou = new ItemStack(IOU.IOU_ITEM, held.getCount());
        iou.set(IOUComponents.ORIGINAL_STACK, held.copy());
        iou.set(IOUComponents.OWED_BY, player.getName().getString());
        iou.set(IOUComponents.STRICT, strict);

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

        entity.setThrower(player);
        entity.setToDefaultPickupDelay();

        world.spawnEntity(entity);

        IOU.LOGGER.debug("Created {} IOU for {} ({}× {})",
                strict ? "strict" : "lenient",
                player.getName().getString(),
                held.getCount(),
                Registries.ITEM.getId(held.getItem()));
    }

    private static String stackSignature(ItemStack stack, boolean strict, ServerPlayerEntity player) {
        if (strict) {
            return Registries.ITEM.getId(stack.getItem()) + "|" +
                    stack.encode(Objects.requireNonNull(player.getServer()).getRegistryManager()).toString();
        }
        return Registries.ITEM.getId(stack.getItem()) + "|" + stack.getCount();
    }
}
