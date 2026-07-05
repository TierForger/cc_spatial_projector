package dev.tierforger.ccspatialprojector.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public record ProjectorLocation(String dimension, int x, int y, int z) {
    public static ProjectorLocation of(Level level, BlockPos pos) {
        return new ProjectorLocation(level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos blockPos() {
        return new BlockPos(x, y, z);
    }

    public boolean matches(Level level, BlockPos pos) {
        return isSameDimension(level)
            && pos.getX() == x
            && pos.getY() == y
            && pos.getZ() == z;
    }

    public boolean isSameDimension(Level level) {
        return level.dimension().location().toString().equals(dimension);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }

    public static ProjectorLocation fromTag(CompoundTag tag) {
        return new ProjectorLocation(tag.getString("dimension"), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }
}
