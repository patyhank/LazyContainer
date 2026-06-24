package dev.konjactw.kcore.lazycontainer.mixin;

import dev.konjactw.kcore.lazycontainer.LazyContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin extends BlockEntity implements LazyContainerAccess, Container {

    public BaseContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Shadow
    protected abstract NonNullList<ItemStack> getItems();

    @Unique
    public boolean lazycontainer$pending;

    @Unique
    public Tag lazycontainer$raw;

    @Override
    @Unique
    public void lazycontainer$clearRaw() {
        this.lazycontainer$pending = false;
        this.lazycontainer$raw = null;
    }

    @Override
    @Unique
    public void lazycontainer$load(ValueInput input, NonNullList<ItemStack> items) {
        if (input instanceof TagValueInput tagInput) {
            this.lazycontainer$raw = tagInput.input.get("Items");
            this.lazycontainer$pending = true;
            LazyContainer.onStash();
            return;
        }

        ContainerHelper.loadAllItems(input, items);
        this.lazycontainer$pending = false;
        this.lazycontainer$raw = null;
        LazyContainer.onEagerLoad();
    }

    @Override
    @Unique
    public void lazycontainer$ensure() {
        if (!this.lazycontainer$pending) {
            return;
        }

        this.lazycontainer$pending = false;

        Tag raw = this.lazycontainer$raw;
        if (raw != null) {
            try {
                CompoundTag tmp = new CompoundTag();
                tmp.put("Items", raw);

                ValueInput vi = TagValueInput.createGlobal(ProblemReporter.DISCARDING, tmp);
                ContainerHelper.loadAllItems(vi, this.getItems());

                this.lazycontainer$raw = null;
                LazyContainer.onEnsure();
            } catch (Throwable t) {
                this.lazycontainer$pending = true;
                throw t;
            }
        } else {
            this.lazycontainer$raw = null;
        }
    }

    @Override
    @Unique
    public void lazycontainer$save(ValueOutput output, NonNullList<ItemStack> items) {
        if (this.lazycontainer$trySaveRaw(output, true)) {
            return;
        }

        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    @Unique
    public void lazycontainer$saveNoEmpty(ValueOutput output, NonNullList<ItemStack> items) {
        if (this.lazycontainer$trySaveRaw(output, false)) {
            return;
        }

        ContainerHelper.saveAllItems(output, items, false);
    }

    @Unique
    private boolean lazycontainer$trySaveRaw(ValueOutput output, boolean allowEmpty) {
        if (!this.lazycontainer$pending) {
            return false;
        }

        Tag raw = this.lazycontainer$raw;

        boolean canWriteRaw = raw instanceof ListTag
                && !(!allowEmpty && ((ListTag) raw).isEmpty());

        if (canWriteRaw && output instanceof TagValueOutput tagOutput) {
            CompoundTag out = tagOutput.buildResult();

            if (LazyContainer.shadow()) {
                Tag eager = this.lazycontainer$eagerItems(raw, allowEmpty);

                if (!Objects.equals(eager, raw)) {
                    if (this.lazycontainer$sameItems(raw, eager)) {
                        LazyContainer.onBenignReorder(
                                String.valueOf(this.getBlockPos()),
                                String.valueOf(raw),
                                String.valueOf(eager)
                        );
                    } else {
                        LazyContainer.onShadowMismatch();
                        LazyContainer.dumpMismatch(
                                String.valueOf(this.getBlockPos()),
                                String.valueOf(raw),
                                eager == null ? "<discard>" : String.valueOf(eager)
                        );

                        System.err.println("[LazyContainer] SHADOW mismatch @ " + this.getBlockPos()
                                + " — writing eager (safe). rawType=" + raw.getClass().getSimpleName());

                        if (eager != null) {
                            out.put("Items", eager);
                        } else {
                            out.remove("Items");
                        }

                        return true;
                    }
                }
            }

            out.put("Items", raw);
            LazyContainer.onRawSave();
            return true;
        }

        this.lazycontainer$ensure();
        return false;
    }

    @Unique
    private Tag lazycontainer$eagerItems(Tag raw, boolean allowEmpty) {
        CompoundTag reIn = new CompoundTag();
        reIn.put("Items", raw);



        NonNullList<ItemStack> tmp = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        ContainerHelper.loadAllItems(
                TagValueInput.createGlobal(ProblemReporter.DISCARDING, reIn),
                tmp
        );

        TagValueOutput eagerOut = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING,
                MinecraftServer.getServer().registryAccess()
        );

        ContainerHelper.saveAllItems(eagerOut, tmp, allowEmpty);

        return eagerOut.buildResult().get("Items");
    }

    @Unique
    private boolean lazycontainer$sameItems(Tag rawTag, Tag eagerTag) {
        if (!(rawTag instanceof ListTag rawList) || !(eagerTag instanceof ListTag eagerList)) {
            return false;
        }

        int n = rawList.size();
        if (n != eagerList.size()) {
            return false;
        }

        boolean[] used = new boolean[n];

        for (Tag rawItem : rawList) {
            boolean found = false;

            for (int j = 0; j < n; j++) {
                if (!used[j] && rawItem.equals(eagerList.get(j))) {
                    used[j] = true;
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }
}