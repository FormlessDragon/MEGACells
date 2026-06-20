package com.gripe.megacells.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import ae2.api.networking.GridServices;
import ae2.api.stacks.AEItemKey;

import com.gripe.megacells.item.cell.BulkCellItem;

public class CompressionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionService.class);
    private static final CompressionChain EMPTY = new CompressionChain(List.of());

    private static final List<CompressionChain> chains = new ArrayList<>();
    private static final Map<AEItemKey, CompressionChain> cachedChains = new WeakHashMap<>();
    private static final Map<Item, Item> overrideVariants = new HashMap<>();
    private static final Set<Item> blacklistedItems = new HashSet<>();

    public static void init() {
        GridServices.register(DecompressionService.class, DecompressionService.class);
        loadRecipes();
    }

    @NotNull
    public static CompressionChain getChain(@Nullable AEItemKey item) {
        if (item == null) {
            return EMPTY;
        }

        CompressionChain cached = cachedChains.get(item);

        if (cached != null) {
            return cached;
        }

        for (CompressionChain chain : chains) {
            if (chain.containsVariant(item)) {
                for (int j = 0; j < chain.size(); j++) {
                    cachedChains.put(AEItemKey.of(chain.getItem(j)), chain);
                }

                return chain;
            }
        }

        cachedChains.put(item, EMPTY);
        return EMPTY;
    }

    public static void syncToClient(SyncCompressionChainsPacket packet) {
        chains.clear();
        cachedChains.clear();
        chains.addAll(packet.chains());
    }

    public static void replaceClientChains(List<CompressionChain> syncedChains) {
        chains.clear();
        cachedChains.clear();
        chains.addAll(syncedChains);
    }

    public static void registerOverride(Item smaller, Item larger) {
        overrideVariants.put(smaller, larger);
        cachedChains.clear();
    }

    public static void blacklist(Item item) {
        blacklistedItems.add(item);
        cachedChains.clear();
    }

    public static void loadRecipes() {
        chains.clear();
        cachedChains.clear();

        List<IRecipe> compressed = new ArrayList<>();
        List<IRecipe> decompressed = new ArrayList<>();
        List<CompressionOverride> overrides = new ArrayList<>();

        for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            if (recipe == null || recipe.getRecipeOutput().isEmpty()) {
                continue;
            }

            if (isCompressionRecipe(recipe)) {
                compressed.add(recipe);
            } else if (isDecompressionRecipe(recipe)) {
                decompressed.add(recipe);
            }
        }

        compressed.removeIf(recipe -> isIrreversible(recipe, decompressed, overrides));
        decompressed.removeIf(recipe -> isIrreversible(recipe, compressed, overrides));

        int initialOverrideCount = overrides.size();

        Comparator<IRecipe> ingredientSize =
                Comparator.comparingInt(r -> firstIngredient(r).getMatchingStacks().length);
        compressed.sort(ingredientSize);
        decompressed.sort(ingredientSize);

        while (!compressed.isEmpty()) {
            IRecipe recipe = compressed.remove(0);
            ItemStack base = recipe.getRecipeOutput().copy();
            removeByOutput(decompressed, base);
            chains.add(generateChain(base, compressed, decompressed, overrides));
        }

        LOGGER.info(
                "Initialised bulk compression. Gathered {} compression chains, with {} overrides.",
                chains.size(),
                initialOverrideCount - overrides.size());
    }

    private static CompressionChain generateChain(
            ItemStack baseVariant,
            List<IRecipe> compressed,
            List<IRecipe> decompressed,
            List<CompressionOverride> overrides) {
        List<ItemStack> lowerList = new ArrayList<>();
        lowerList.add(baseVariant);

        List<Integer> stackHashes = new ArrayList<>();
        stackHashes.add(stackHash(baseVariant));

        for (ItemStack lower = getNextVariant(baseVariant, decompressed, overrides, false); lower != null; ) {
            ItemStack stack = lower;

            if (stackHashes.contains(stackHash(stack))) {
                if (stack.getCount() != 1) {
                    LOGGER.warn(
                            "Duplicate lower compression variant detected: {}. Check any recipe involving this item for problems.",
                            stack);
                }

                break;
            }

            lowerList.add(stack);
            removeByOutput(compressed, stack);
            lower = getNextVariant(stack, decompressed, overrides, false);
        }

        List<ItemStack> variantList = new ArrayList<>();

        for (int i = lowerList.size(); i > 0; i--) {
            variantList.add(CompressionChain.copyWithCount(
                    lowerList.get(i - 1),
                    lowerList.get(i % lowerList.size()).getCount()));
        }

        for (ItemStack higher = getNextVariant(baseVariant, compressed, overrides, true); higher != null; ) {
            if (stackHashes.contains(stackHash(higher))) {
                if (higher.getCount() != 1) {
                    LOGGER.warn(
                            "Duplicate higher compression variant detected: {}. Check any recipe involving this item for problems.",
                            higher);
                }

                break;
            }

            ItemStack stack = higher;
            variantList.add(stack);
            removeByOutput(decompressed, stack);
            higher = getNextVariant(stack, compressed, overrides, true);
        }

        CompressionChain chain = new CompressionChain(variantList);
        LOGGER.debug("Gathered bulk compression chain: {}", chain);
        return chain;
    }

    @Nullable
    private static ItemStack getNextVariant(
            ItemStack item, List<IRecipe> recipes, List<CompressionOverride> overrides, boolean compressed) {
        for (Iterator<CompressionOverride> it = overrides.iterator(); it.hasNext(); ) {
            CompressionOverride override = it.next();

            if (compressed && sameItemAndTag(override.smaller(), item)) {
                it.remove();
                return override.larger();
            }

            if (!compressed && sameItemAndTag(override.larger(), item)) {
                it.remove();
                return override.smaller();
            }
        }

        for (Iterator<IRecipe> it = recipes.iterator(); it.hasNext(); ) {
            IRecipe recipe = it.next();

            for (ItemStack input : firstIngredient(recipe).getMatchingStacks()) {
                if (sameItemAndTag(item, input)) {
                    it.remove();
                    ItemStack output = recipe.getRecipeOutput().copy();
                    return CompressionChain.copyWithCount(
                            output,
                            compressed ? ingredientCount(recipe) : output.getCount());
                }
            }
        }

        return null;
    }

    private static boolean isDecompressionRecipe(IRecipe recipe) {
        return ingredientCount(recipe) == 1;
    }

    private static boolean isCompressionRecipe(IRecipe recipe) {
        if (recipe.getRecipeOutput().getCount() != 1) {
            return false;
        }

        List<net.minecraft.item.crafting.Ingredient> ingredients = nonEmptyIngredients(recipe);

        if (ingredients.isEmpty()) {
            return false;
        }

        if (ingredients.size() == 1) {
            return true;
        }

        ItemStack[] first = ingredients.get(0).getMatchingStacks();

        for (int i = 1; i < ingredients.size(); i++) {
            ItemStack[] stacks = ingredients.get(i).getMatchingStacks();

            if (stacks.length != first.length) {
                return false;
            }

            for (int j = 0; j < stacks.length; j++) {
                if (!sameItemAndTag(stacks[j], first[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isIrreversible(
            IRecipe recipe, List<IRecipe> candidates, List<CompressionOverride> overrides) {
        if (overrideRecipe(recipe, overrides)) {
            return false;
        }

        ItemStack[] testInput = firstIngredient(recipe).getMatchingStacks();
        Item testOutput = recipe.getRecipeOutput().getItem();

        for (IRecipe candidate : candidates) {
            ItemStack[] input = firstIngredient(candidate).getMatchingStacks();
            Item output = candidate.getRecipeOutput().getItem();

            boolean compressible = false;
            boolean decompressible = false;

            for (ItemStack i : input) {
                if (i.getItem() == testOutput && !isBlacklisted(i)) {
                    compressible = true;
                    break;
                }
            }

            for (ItemStack i : testInput) {
                if (i.getItem() == output && !isBlacklisted(i)) {
                    decompressible = true;
                    break;
                }
            }

            boolean sameQuantity = candidate.getRecipeOutput().getCount() == ingredientCount(recipe)
                    && recipe.getRecipeOutput().getCount() == ingredientCount(candidate);

            if (compressible && decompressible && sameQuantity) {
                return false;
            }
        }

        return true;
    }

    private static boolean overrideRecipe(IRecipe recipe, List<CompressionOverride> overrides) {
        ItemStack output = recipe.getRecipeOutput();

        if (isBlacklisted(output)) {
            return false;
        }

        List<net.minecraft.item.crafting.Ingredient> ingredients = nonEmptyIngredients(recipe);

        for (ItemStack input : ingredients.get(0).getMatchingStacks()) {
            Item overridden = overrideVariants.get(input.getItem());

            if (overridden == null || overridden != output.getItem()) {
                continue;
            }

            boolean decompressed = isDecompressionRecipe(recipe);
            ItemStack larger = (decompressed ? input : output).copy();
            ItemStack smaller = decompressed
                    ? output.copy()
                    : CompressionChain.copyWithCount(input, ingredients.size());

            CompressionOverride override = new CompressionOverride(larger, smaller);
            LOGGER.debug("Found bulk compression override: {}", override);
            overrides.add(override);

            return true;
        }

        return false;
    }

    private static boolean isBlacklisted(ItemStack stack) {
        return stack.getItem() == Items.AIR || blacklistedItems.contains(stack.getItem());
    }

    static String variantString(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String s = id != null ? id.toString() : String.valueOf(stack.getItem());

        if (stack.hasTagCompound()) {
            s += "(*)";
        }

        return s;
    }

    private static void removeByOutput(List<IRecipe> recipes, ItemStack stack) {
        recipes.removeIf(recipe -> sameItemAndTag(stack, recipe.getRecipeOutput()));
    }

    private static net.minecraft.item.crafting.Ingredient firstIngredient(IRecipe recipe) {
        return nonEmptyIngredients(recipe).get(0);
    }

    private static List<net.minecraft.item.crafting.Ingredient> nonEmptyIngredients(IRecipe recipe) {
        List<net.minecraft.item.crafting.Ingredient> ingredients = new ArrayList<>();

        for (net.minecraft.item.crafting.Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient != null && ingredient != net.minecraft.item.crafting.Ingredient.EMPTY) {
                ItemStack[] stacks = ingredient.getMatchingStacks();

                if (stacks.length > 0) {
                    ingredients.add(ingredient);
                }
            }
        }

        return ingredients;
    }

    private static int ingredientCount(IRecipe recipe) {
        return nonEmptyIngredients(recipe).size();
    }

    private static boolean sameItemAndTag(ItemStack a, ItemStack b) {
        return BulkCellItem.sameItemAndTag(a, b);
    }

    private static int stackHash(ItemStack stack) {
        int hash = Item.getIdFromItem(stack.getItem());
        hash = 31 * hash + stack.getMetadata();
        hash = 31 * hash + (stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0);
        return hash;
    }
}
