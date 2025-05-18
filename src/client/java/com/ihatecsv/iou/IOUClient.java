package com.ihatecsv.iou;

import com.ihatecsv.iou.networking.IOUNetworkClient;
import com.ihatecsv.iou.rendering.IOUItemRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class IOUClient implements ClientModInitializer {
    private static KeyBinding DROP_KEY;

    @Override
    public void onInitializeClient() {
        DROP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.iou.drop",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.iou"
        ));

        BuiltinItemRendererRegistry.INSTANCE.register(IOU.IOU_ITEM, new IOUItemRenderer());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (DROP_KEY.wasPressed()) {
                long handle = client.getWindow().getHandle();
                boolean ctrl = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
                if (!ctrl) continue;

                boolean shift = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)
                        || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);

                IOUNetworkClient.sendDropPacket(shift);
            }
        });

        ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
            if (stack.isEmpty()) return;

            RegistryWrapper.WrapperLookup lookup = ctx.getRegistryLookup();
            if (lookup == null) lookup = BuiltinRegistries.createWrapperLookup();

            NbtElement tag = stack.encode(lookup);
            if (tag == null || "{}".equals(tag.toString())) return;

            Text snbt = NbtHelper.toPrettyPrintedText(tag);
            lines.add(snbt.copy().formatted(Formatting.DARK_GRAY));
        });
    }
}
