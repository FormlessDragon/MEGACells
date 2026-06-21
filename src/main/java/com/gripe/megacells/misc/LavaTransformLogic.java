package com.gripe.megacells.misc;

import ae2.recipes.AERecipeTypes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public final class LavaTransformLogic {
    private static final Set<Item> lavaCache = new HashSet<>();

    private LavaTransformLogic() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            lavaCache.clear();
        }
    }

    public static boolean canTransformInLava(EntityItem entity) {
        return getLavaTransformableItems().contains(entity.getItem().getItem());
    }

    public static boolean allIngredientsPresent(EntityItem entity) {
        var x = entity.posX;
        var y = entity.posY;
        var z = entity.posZ;
        var level = entity.world;

        var items = new HashSet<Item>();
        for (var nearby : level.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1))) {
            if (!nearby.isDead && !nearby.getItem().isEmpty()) {
                items.add(nearby.getItem().getItem());
            }
        }

        for (var recipe : AERecipeTypes.TRANSFORM.getRecipes()) {
            if (!recipe.getCircumstance().isFluid(FluidRegistry.LAVA)) {
                continue;
            }

            var allPresent = true;
            for (var ingredient : recipe.getIngredients()) {
                var present = false;
                for (var stack : ingredient.getMatchingStacks()) {
                    if (items.contains(stack.getItem())) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    allPresent = false;
                    break;
                }
            }

            if (allPresent) {
                return true;
            }
        }

        return false;
    }

    private static Set<Item> getLavaTransformableItems() {
        if (lavaCache.isEmpty()) {
            for (var recipe : AERecipeTypes.TRANSFORM.getRecipes()) {
                if (!recipe.getCircumstance().isFluid(FluidRegistry.LAVA)) {
                    continue;
                }

                for (var ingredient : recipe.getIngredients()) {
                    for (var stack : ingredient.getMatchingStacks()) {
                        lavaCache.add(stack.getItem());
                    }
                }
            }
        }

        return lavaCache;
    }
}
