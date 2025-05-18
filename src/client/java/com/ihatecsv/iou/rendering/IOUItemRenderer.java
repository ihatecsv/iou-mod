package com.ihatecsv.iou.rendering;

import com.ihatecsv.iou.IOUComponents;
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

import java.util.EnumMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class IOUItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private record TransformData(Vector3f translate, Vector3f rotation, Vector3f scale) {}
    private record ModeConfig(TransformData paper, TransformData overlay, boolean renderOverlay) {}

    private static final TransformData DEFAULT =
            new TransformData(new Vector3f(), new Vector3f(), new Vector3f(1, 1, 1));
    private static final ModeConfig DEFAULT_CFG = new ModeConfig(DEFAULT, DEFAULT, false);

    private static final Map<ModelTransformationMode, ModeConfig> CONFIG = new EnumMap<>(ModelTransformationMode.class);

    static {
        CONFIG.put(ModelTransformationMode.GUI,
                new ModeConfig(
                        new TransformData(new Vector3f(.5f,.5f,0),  new Vector3f(), new Vector3f(1,1,1)),
                        new TransformData(new Vector3f(.5f,.5f,.15f),new Vector3f(), new Vector3f(.4f,.4f,.4f)),
                        true));
        CONFIG.put(ModelTransformationMode.FIXED, CONFIG.get(ModelTransformationMode.GUI));
        CONFIG.put(ModelTransformationMode.GROUND, CONFIG.get(ModelTransformationMode.GUI));

        ModeConfig fp = new ModeConfig(
                new TransformData(new Vector3f(.5f,.5f,.5f), new Vector3f(), new Vector3f(1,1,1)),
                new TransformData(new Vector3f(.5f,.5f,.15f),new Vector3f(), new Vector3f(.4f,.4f,.4f)),
                false);
        CONFIG.put(ModelTransformationMode.FIRST_PERSON_LEFT_HAND,  fp);
        CONFIG.put(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, fp);
        CONFIG.put(ModelTransformationMode.THIRD_PERSON_LEFT_HAND,  fp);
        CONFIG.put(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, fp);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode,
                       MatrixStack matrices, VertexConsumerProvider vcp,
                       int light, int overlay) {

        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        ModeConfig cfg = CONFIG.getOrDefault(mode, DEFAULT_CFG);

        // paper
        matrices.push();
        apply(cfg.paper, matrices);
        renderer.renderItem(null, new ItemStack(Items.PAPER), mode, false, matrices, vcp,
                null, light, overlay, 0);
        matrices.pop();

        // item overlay
        if (cfg.renderOverlay && stack.contains(IOUComponents.ORIGINAL_STACK)) {
            ItemStack owed = stack.get(IOUComponents.ORIGINAL_STACK);
            matrices.push();
            apply(cfg.overlay, matrices);
            renderer.renderItem(null, owed, mode, false, matrices, vcp,
                    null, light, overlay, 0);
            matrices.pop();
        }
    }

    private static void apply(TransformData t, MatrixStack m) {
        m.translate(t.translate.x, t.translate.y, t.translate.z);
        m.multiply(new Quaternionf().rotateXYZ(
                (float) Math.toRadians(t.rotation.x),
                (float) Math.toRadians(t.rotation.y),
                (float) Math.toRadians(t.rotation.z)));
        m.scale(t.scale.x, t.scale.y, t.scale.z);
    }
}
