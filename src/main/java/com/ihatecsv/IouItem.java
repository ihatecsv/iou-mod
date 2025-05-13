package com.ihatecsv;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class IouItem extends Item {
    public IouItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.contains("original_nbt", NbtElement.COMPOUND_TYPE)) {
            ItemStack original = ItemStack.fromNbt(tag.getCompound("original_nbt"));
            return original.getName();
        }
        return super.getName(stack);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext ctx) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("original_nbt", NbtElement.COMPOUND_TYPE)) return;

        if (tag.contains("owed_by", NbtElement.STRING_TYPE)) {
            tooltip.add(Text.literal("IOU from " + tag.getString("owed_by")).formatted(Formatting.GRAY));
        }
    }

    @Override
    public boolean onClicked(ItemStack iouStack,
                             ItemStack cursor,
                             Slot slot,
                             ClickType clickType,
                             PlayerEntity player,
                             StackReference cursorRef) {

        if (clickType != ClickType.RIGHT || cursor.isEmpty()) return false;

        NbtCompound tag = iouStack.getNbt();
        if (tag == null || !tag.contains("original_nbt", NbtElement.COMPOUND_TYPE)) return false;

        ItemStack original = ItemStack.fromNbt(tag.getCompound("original_nbt"));
        boolean sameItem = original.getItem() == cursor.getItem();

        if (!sameItem) return false;

        int repay = Math.min(cursor.getCount(), iouStack.getCount());
        iouStack.decrement(repay);
        if (iouStack.isEmpty()) slot.setStack(ItemStack.EMPTY);

        slot.markDirty();

        return true;
    }
}
