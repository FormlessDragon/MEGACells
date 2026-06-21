package com.gripe.megacells.misc;

import ae2.api.crafting.IPatternDetails;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import com.gripe.megacells.definition.MEGAItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class DecompressionPattern implements IPatternDetails {
    private static final String TAG_PATTERN = "MEGADecompressionPattern";
    private static final String TAG_FROM = "From";
    private static final String TAG_TO = "To";

    private final AEItemKey definition;
    private final ItemStack from;
    private final ItemStack to;

    public DecompressionPattern(ItemStack from, ItemStack to) {
        this.from = from.copy();
        this.to = to.copy();

        ItemStack definition = new ItemStack(MEGAItems.SKY_STEEL_INGOT.asItem());
        definition.setTagCompound(encode(this.from, this.to));
        this.definition = AEItemKey.of(definition);
    }

    private static NBTTagCompound encode(ItemStack from, ItemStack to) {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound pattern = new NBTTagCompound();
        pattern.setTag(TAG_FROM, from.writeToNBT(new NBTTagCompound()));
        pattern.setTag(TAG_TO, to.writeToNBT(new NBTTagCompound()));
        root.setTag(TAG_PATTERN, pattern);
        return root;
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return new IInput[]{new Input(from)};
    }

    @Override
    public List<GenericStack> getOutputs() {
        return Collections.singletonList(new GenericStack(AEItemKey.of(to), to.getCount()));
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() && ((DecompressionPattern) o).definition.equals(definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    private record Input(ItemStack stack) implements IInput {
            private Input(ItemStack stack) {
                this.stack = stack.copy();
            }

            @Override
            public GenericStack[] possibleInputs() {
                return new GenericStack[]{new GenericStack(AEItemKey.of(stack), stack.getCount())};
            }

            @Override
            public long getMultiplier() {
                return 1;
            }

            @Override
            public boolean isValid(AEKey input, World level) {
                return input.matches(possibleInputs()[0]);
            }

            @Override
            public AEKey getRemainingKey(AEKey template) {
                return null;
            }
        }
}
