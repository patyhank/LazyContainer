package dev.konjactw.kcore.lazycontainer.mixin;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {

    @Inject(method = "getItems", at = @At("HEAD"), require = 0)
    private void lazycontainer$ensureGetItems(CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        ((LazyContainerAccess) this).lazycontainer$ensure();
    }

    @Inject(method = "getContents", at = @At("HEAD"), require = 0)
    private void lazycontainer$ensureGetContents(CallbackInfoReturnable<List<ItemStack>> cir) {
        ((LazyContainerAccess) this).lazycontainer$ensure();
    }

    @Inject(method = "setItems", at = @At("HEAD"), require = 0)
    private void lazycontainer$clearSetItems(NonNullList<ItemStack> items, CallbackInfo ci) {
        ((LazyContainerAccess) this).lazycontainer$clearRaw();
    }

    @Redirect(
            method = "loadFromTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/ContainerHelper;loadAllItems(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/core/NonNullList;)V"
            ),
            require = 0
    )
    private void lazycontainer$redirectLoad(ValueInput input, NonNullList<ItemStack> itemStacks) {
        ((LazyContainerAccess) this).lazycontainer$load(input, itemStacks);
    }

    @Redirect(
            method = "saveAdditional(Lnet/minecraft/world/level/storage/ValueOutput;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/ContainerHelper;saveAllItems(Lnet/minecraft/world/level/storage/ValueOutput;Lnet/minecraft/core/NonNullList;Z)V"
            ),
            require = 1
    )
    private void lazycontainer$redirectSaveNoEmpty(
            ValueOutput output,
            NonNullList<ItemStack> itemStacks,
            boolean alsoWhenEmpty
    ) {
        ((LazyContainerAccess) this).lazycontainer$saveNoEmpty(output, itemStacks);
    }
}