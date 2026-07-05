package dev.tierforger.ccspatialprojector.util;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/**
 * Single owner for this mod's ItemStack custom data namespace.
 */
public final class StackData {
    private StackData() {}

    private static final String ROOT = CcSpatialProjector.MOD_ID;

    public static Optional<String> string(ItemStack stack, String key) {
        CompoundTag root = root(stack);
        return root.contains(key, Tag.TAG_STRING) ? Optional.of(root.getString(key)) : Optional.empty();
    }

    public static void putString(ItemStack stack, String key, String value) {
        CompoundTag root = root(stack);
        root.putString(key, value);
        save(stack, root);
    }

    public static Optional<CompoundTag> compound(ItemStack stack, String key) {
        CompoundTag root = root(stack);
        return root.contains(key, Tag.TAG_COMPOUND) ? Optional.of(root.getCompound(key).copy()) : Optional.empty();
    }

    public static void putCompound(ItemStack stack, String key, CompoundTag value) {
        CompoundTag root = root(stack);
        root.put(key, value.copy());
        save(stack, root);
    }

    public static void remove(ItemStack stack, String key) {
        CompoundTag root = root(stack);
        root.remove(key);
        save(stack, root);
    }

    private static CompoundTag root(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = customData.copyTag();
        return all.contains(ROOT, Tag.TAG_COMPOUND) ? all.getCompound(ROOT).copy() : new CompoundTag();
    }

    private static void save(ItemStack stack, CompoundTag root) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag all = customData.copyTag();
        if (root.isEmpty()) all.remove(ROOT);
        else all.put(ROOT, root);

        if (all.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
    }
}
