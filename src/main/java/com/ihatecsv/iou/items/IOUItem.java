package com.ihatecsv.iou.items;

import com.ihatecsv.iou.IOUComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;

import java.util.List;

public class IOUItem extends Item {
    public IOUItem(Settings settings) { super(settings); }

    @Override
    public Text getName(ItemStack stack) {
        if (stack.contains(IOUComponents.ORIGINAL_STACK)) {
            return stack.get(IOUComponents.ORIGINAL_STACK).getName();
        }
        return super.getName(stack);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack,
                              TooltipContext ctx,
                              List<Text> tooltip,
                              TooltipType type) {

        if (!stack.contains(IOUComponents.ORIGINAL_STACK)) return;

        String who = stack.getOrDefault(IOUComponents.OWED_BY, "");
        if (!who.isEmpty()) {
            tooltip.add(Text.literal("IOU from " + who).formatted(Formatting.GRAY));
        }
    }

    @Override
    public boolean onClicked(ItemStack iouStack, ItemStack cursor, Slot slot,
                             ClickType click, PlayerEntity player, StackReference cursorRef) {

        if (click != ClickType.RIGHT || cursor.isEmpty())          return false;
        if (!iouStack.contains(IOUComponents.ORIGINAL_STACK))           return false;

        ItemStack original = iouStack.get(IOUComponents.ORIGINAL_STACK);
        if (original.getItem() != cursor.getItem())                return false;

        int repay = Math.min(cursor.getCount(), iouStack.getCount());
        iouStack.decrement(repay);
        if (iouStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        slot.markDirty();
        return true;
    }
}
