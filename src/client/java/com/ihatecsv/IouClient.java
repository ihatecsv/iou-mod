package com.ihatecsv;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class IouClient implements ClientModInitializer {
    private static KeyBinding DROP_KEY;

    @Override
    public void onInitializeClient() {
        DROP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.iou.drop",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.iou"
        ));

        BuiltinItemRendererRegistry.INSTANCE.register(Iou.IOU_ITEM, new IouItemRenderer());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (DROP_KEY.wasPressed()) {
                long handle = client.getWindow().getHandle();
                boolean ctrl = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
                if (!ctrl) continue;

                boolean shift = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)
                        || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);

                IouClientNetworking.sendDropPacket(shift);
            }
        });
    }
}
