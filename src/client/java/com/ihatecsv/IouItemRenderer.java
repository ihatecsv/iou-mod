package com.ihatecsv;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.EnumMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class IouItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private record TransformData(Vector3f translate, Vector3f rotation, Vector3f scale) {
    }

    private record ModeConfig(TransformData paperTransform, TransformData overlayTransform, boolean renderOverlay) {
    }

    private static final TransformData DEFAULT_TRANSFORM =
            new TransformData(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
    private static final ModeConfig DEFAULT_CONFIG =
            new ModeConfig(DEFAULT_TRANSFORM, DEFAULT_TRANSFORM, false);

    private static final Map<ModelTransformationMode, ModeConfig> CONFIG = new EnumMap<>(ModelTransformationMode.class);

    static {
        CONFIG.put(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND,
                new ModeConfig(
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f)),
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.15f), new Vector3f(0f, 0f, 0f), new Vector3f(0.4f, 0.4f, 0.4f)),
                        false
                )
        );
        CONFIG.put(ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                new ModeConfig(
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f)),
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.15f), new Vector3f(0f, 0f, 0f), new Vector3f(0.4f, 0.4f, 0.4f)),
                        false
                )
        );
        CONFIG.put(ModelTransformationMode.GROUND,
                new ModeConfig(
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f)),
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0f, 0f, 0f), new Vector3f(0.4f, 0.4f, 0.4f)),
                        false
                )
        );
        CONFIG.put(ModelTransformationMode.FIXED,
                new ModeConfig(
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f)),
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0f, 0f, 0f), new Vector3f(0.4f, 0.4f, 0.4f)),
                        true
                )
        );
        CONFIG.put(ModelTransformationMode.GUI,
                new ModeConfig(
                        new TransformData(new Vector3f(0.5f, 0.5f, 0f), new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f)),
                        new TransformData(new Vector3f(0.5f, 0.5f, 0.15f), new Vector3f(0f, 0f, 0f), new Vector3f(0.4f, 0.4f, 0.4f)),
                        true
                )
        );
    }

    @Override
    public void render(ItemStack stack,
                       ModelTransformationMode mode,
                       MatrixStack matrices,
                       VertexConsumerProvider vcp,
                       int light,
                       int overlay) {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        ModeConfig cfg = CONFIG.getOrDefault(mode, DEFAULT_CONFIG);

        // Paper
        matrices.push();
        applyTransform(matrices, cfg.paperTransform);
        renderer.renderItem(
                null,
                new ItemStack(Items.PAPER),
                mode,
                false,
                matrices,
                vcp,
                null,
                light,
                overlay,
                0
        );
        matrices.pop();

        // Overlay
        if (cfg.renderOverlay) {
            NbtCompound tag = stack.getNbt();
            if (tag != null && tag.contains("original_nbt", NbtElement.COMPOUND_TYPE)) {
                ItemStack owed = ItemStack.fromNbt(tag.getCompound("original_nbt"));
                matrices.push();
                applyTransform(matrices, cfg.overlayTransform);
                renderer.renderItem(
                        null,
                        owed,
                        mode,
                        false,
                        matrices,
                        vcp,
                        null,
                        light,
                        overlay,
                        0
                );
                matrices.pop();
            }
        }
    }

    private void applyTransform(MatrixStack matrices, TransformData t) {
        matrices.translate(t.translate.x, t.translate.y, t.translate.z);

        float xRad = (float) Math.toRadians(t.rotation.x);
        float yRad = (float) Math.toRadians(t.rotation.y);
        float zRad = (float) Math.toRadians(t.rotation.z);

        Quaternionf rotationQuat = new Quaternionf().rotateXYZ(xRad, yRad, zRad);

        matrices.multiply(rotationQuat);

        matrices.scale(t.scale.x, t.scale.y, t.scale.z);
    }
}
