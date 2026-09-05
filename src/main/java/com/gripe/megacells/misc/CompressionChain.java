package com.gripe.megacells.misc;

import ae2.api.crafting.IPatternDetails;
import ae2.api.stacks.AEItemKey;
import com.gripe.megacells.item.cell.BulkCellItem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class CompressionChain {
    public static final long STACK_LIMIT = (long) Math.pow(2, 42);

    private final List<ItemStack> variants;
    private final UnitAmount[] unitFactors;
    private final UnitAmount oneFactor = new UnitAmount().init(1);
    private final Supplier<List<Pair<IPatternDetails, IPatternDetails>>> patterns = memoize(this::gatherPatterns);

    CompressionChain(List<ItemStack> variants) {
        this.variants = Collections.unmodifiableList(variants);
        this.unitFactors = new UnitAmount[variants.size()];

        UnitAmount factor = new UnitAmount().init(1);
        for (int i = 0; i < variants.size(); i++) {
            factor.multiply(variants.get(i).getCount());
            unitFactors[i] = new UnitAmount().init(factor);
        }
    }

    public static CompressionChain read(PacketBuffer buffer) throws IOException {
        int size = buffer.readVarInt();
        List<ItemStack> variants = new ObjectArrayList<>();

        for (int i = 0; i < size; i++) {
            variants.add(buffer.readItemStack());
        }

        return new CompressionChain(variants);
    }

    static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;

            @Override
            public T get() {
                if (value == null) {
                    value = supplier.get();
                }

                return value;
            }
        };
    }

    public void write(PacketBuffer buffer) {
        buffer.writeVarInt(variants.size());

        for (ItemStack variant : variants) {
            buffer.writeItemStack(variant);
        }
    }

    public boolean isEmpty() {
        return variants.isEmpty();
    }

    public boolean containsVariant(AEItemKey item) {
        for (ItemStack variant : variants) {
            if (BulkCellItem.sameItemAndTag(item.getReadOnlyStack(), variant)) {
                return true;
            }
        }

        return false;
    }

    public ItemStack getItem(int index) {
        return variants.get(index).copy();
    }

    public UnitAmount unitFactor(AEItemKey item) {
        if (item == null) {
            return oneFactor;
        }

        for (int i = 0; i < variants.size(); i++) {
            if (BulkCellItem.sameItemAndTag(item.getReadOnlyStack(), variants.get(i))) {
                return unitFactors[i];
            }
        }

        return oneFactor;
    }

    public int size() {
        return variants.size();
    }

    public List<IPatternDetails> getDecompressionPatterns(int cutoff) {
        if (isEmpty()) {
            return Collections.emptyList();
        }

        List<IPatternDetails> decompressionPatterns = new ObjectArrayList<>();
        List<Pair<IPatternDetails, IPatternDetails>> availablePatterns = patterns.get();

        for (int i = 0; i < variants.subList(0, cutoff).size(); i++) {
            decompressionPatterns.add(availablePatterns.get(i).right());
        }

        for (int i = cutoff; i < variants.size() - 1; i++) {
            decompressionPatterns.add(availablePatterns.get(i).left());
        }

        return Collections.unmodifiableList(decompressionPatterns);
    }

    private List<Pair<IPatternDetails, IPatternDetails>> gatherPatterns() {
        List<Pair<IPatternDetails, IPatternDetails>> gatheredPatterns = new ObjectArrayList<>();

        for (int i = 0; i < variants.size() - 1; i++) {
            ItemStack smaller = copyWithCount(variants.get(i), variants.get(i + 1).getCount());
            ItemStack larger = copyWithCount(variants.get(i + 1), 1);

            IPatternDetails compression = new DecompressionPattern(smaller, larger);
            IPatternDetails decompression = new DecompressionPattern(larger, smaller);

            gatheredPatterns.add(Pair.of(compression, decompression));
        }

        return gatheredPatterns;
    }

    public Map<AEItemKey, Long> initStacks(UnitAmount unitCount, int cutoff, AEItemKey fallback) {
        Map<AEItemKey, Long> stacks = new Object2LongLinkedOpenHashMap<>();

        if (!isEmpty()) {
            UnitAmount remaining = new UnitAmount().init(unitCount);

            for (int i = 0; i < cutoff; i++) {
                long factor = variants.get((i + 1) % variants.size()).getCount();
                stacks.put(AEItemKey.of(variants.get(i)), remaining.remainderToLong(factor, factor - 1));
                remaining.divide(factor);
            }

            stacks.put(AEItemKey.of(variants.get(cutoff)), remaining.toLongSaturated(STACK_LIMIT));
        } else if (fallback != null) {
            stacks.put(fallback, unitCount.toLongSaturated(STACK_LIMIT));
        }

        return stacks;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() && ((CompressionChain) o).variants.equals(variants);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(variants);
    }

    @Override
    public String toString() {
        java.util.Iterator<ItemStack> it = variants.iterator();

        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (; ; ) {
            ItemStack stack = it.next();
            sb.append(stack.getCount());
            sb.append("x -> ");
            sb.append(CompressionService.variantString(stack));

            if (!it.hasNext()) {
                return sb.append(']').toString();
            }

            sb.append(", ");
        }
    }
}
