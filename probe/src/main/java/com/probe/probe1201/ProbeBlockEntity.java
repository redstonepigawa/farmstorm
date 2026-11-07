package com.probe.probe1201;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 1.20.1 的存档签名：saveAdditional(CompoundTag) / load(CompoundTag)，都没有 HolderLookup.Provider。 */
public class ProbeBlockEntity extends BlockEntity {
    private ItemStack held = ItemStack.EMPTY;
    private int progress;

    public ProbeBlockEntity(BlockPos pos, BlockState state) {
        super(ProbeMod.PROBE_BE.get(), pos, state);
    }

    public ItemStack held() {
        return this.held;
    }

    public int progress() {
        return this.progress;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Held", this.held.save(new CompoundTag()));
        tag.putInt("Progress", this.progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.held = ItemStack.of(tag.getCompound("Held"));
        this.progress = tag.getInt("Progress");
    }
}
