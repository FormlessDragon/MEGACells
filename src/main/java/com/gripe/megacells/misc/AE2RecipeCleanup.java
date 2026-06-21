package com.gripe.megacells.misc;

import ae2.core.AppEng;
import ae2.core.definitions.AEBlocks;
import ae2.core.definitions.AEItems;
import ae2.recipes.AERecipeTypes;
import ae2.recipes.game.CraftingUnitTransformRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;

public final class AE2RecipeCleanup {
    private static final ResourceLocation AE2_4X_CRAFTING_RECIPE =
        AppEng.makeId("network/crafting/cpu_4x_crafting_accelerator");
    private static final ResourceLocation AE2_4X_TRANSFORM_RECIPE =
        AppEng.makeId("crafting_unit_upgrade/4x_crafting_accelerator");

    private AE2RecipeCleanup() {
    }

    public static void removeOriginal4xAcceleratorRecipes() {
        removeForgeRecipe(AE2_4X_CRAFTING_RECIPE);
        removeForgeRecipe(AE2_4X_TRANSFORM_RECIPE);

        var transformRecipe = new CraftingUnitTransformRecipe(
            AEBlocks.CRAFTING_UNIT.block(),
            AEBlocks.CRAFTING_ACCELERATOR_4X.block(),
            AEItems.CONCURRENT_PROCESSOR.asItem());
        if (!AERecipeTypes.CRAFTING_UNIT_TRANSFORM.remove(transformRecipe)) {
            throw new IllegalStateException("Missing AE2 transform recipe: " + AE2_4X_TRANSFORM_RECIPE);
        }
    }

    private static void removeForgeRecipe(ResourceLocation id) {
        if (!(ForgeRegistries.RECIPES instanceof IForgeRegistryModifiable<IRecipe> recipes)) {
            throw new IllegalStateException("Recipe registry is not modifiable");
        }

        if (!recipes.containsKey(id)) {
            throw new IllegalStateException("Missing AE2 recipe: " + id);
        }

        recipes.remove(id);
    }
}
