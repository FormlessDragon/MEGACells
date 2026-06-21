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
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class CompressionChain {
    public static final long STACK_LIMIT = (long) Math.pow(2, 42);

    private final List<ItemStack> variants;
    private final Supplier<List<Pair<IPatternDetails, IPatternDetails>>> patterns = memoize(this::gatherPatterns);

    CompressionChain(List<ItemStack> variants) {
        this.variants = Collections.unmodifiableList(variants);
    }

    public static CompressionChain read(PacketBuffer buffer) throws IOException {
        int size = buffer.readVarInt();
        List<ItemStack> variants = new ObjectArrayList<>();

        for (int i = 0; i < size; i++) {
            variants.add(buffer.readItemStack());
        }

        return new CompressionChain(variants);
    }

    public static long clamp(BigInteger toClamp, long limit) {
        return toClamp.min(BigInteger.valueOf(limit)).longValue();
    }

    private static BigInteger bigCount(ItemStack stack) {
        return BigInteger.valueOf(stack.getCount());
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

    public BigInteger unitFactor(AEItemKey item) {
        if (item == null) {
            return BigInteger.ONE;
        }

        BigInteger potentialFactor = BigInteger.ONE;

        for (ItemStack variant : variants) {
            potentialFactor = potentialFactor.multiply(bigCount(variant));

            if (BulkCellItem.sameItemAndTag(item.getReadOnlyStack(), variant)) {
                return potentialFactor;
            }
        }

        return BigInteger.ONE;
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

    public Map<AEItemKey, Long> initStacks(BigInteger unitCount, int cutoff, AEItemKey fallback) {
        Map<AEItemKey, Long> stacks = new Object2LongLinkedOpenHashMap<>();

        if (!isEmpty()) {
            for (int i = 0; i < cutoff; i++) {
                BigInteger factor = bigCount(variants.get((i + 1) % variants.size()));
                stacks.put(AEItemKey.of(variants.get(i)), unitCount.remainder(factor).longValue());
                unitCount = unitCount.divide(factor);
            }

            stacks.put(AEItemKey.of(variants.get(cutoff)), clamp(unitCount, STACK_LIMIT));
        } else if (fallback != null) {
            stacks.put(fallback, clamp(unitCount, STACK_LIMIT));
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
