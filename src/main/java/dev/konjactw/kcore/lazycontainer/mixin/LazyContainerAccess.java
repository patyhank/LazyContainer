package dev.konjactw.kcore.lazycontainer.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface LazyContainerAccess {
    void lazycontainer$ensure();
    void lazycontainer$clearRaw();
    void lazycontainer$load(ValueInput input, NonNullList<ItemStack> items);
    void lazycontainer$save(ValueOutput output, NonNullList<ItemStack> items);
    void lazycontainer$saveNoEmpty(ValueOutput output, NonNullList<ItemStack> items);
}