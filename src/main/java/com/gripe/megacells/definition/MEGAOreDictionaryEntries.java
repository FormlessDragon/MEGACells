package com.gripe.megacells.definition;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Declares the ore dictionary names MEGACells contributes so recipe resources and runtime
 * registrations stay aligned.
 */
public final class MEGAOreDictionaryEntries {
    private static final Map<String, String> ENTRY_IDS = Map.of(
            "ingotSkySteel", "megacells:sky_steel_ingot",
            "ingotSkyBronze", "megacells:sky_bronze_ingot");

    private MEGAOreDictionaryEntries() {
    }

    public static Map<String, String> entryIds() {
        return ENTRY_IDS;
    }

    public static void registerAll() {
        for (var entry : registrations()) {
            var item = entry.itemSupplier().get();
            if (item == null) {
                throw new IllegalStateException(
                        "Missing item for ore dictionary entry " + entry.oreName() + " -> " + entry.itemId());
            }
            OreDictionary.registerOre(entry.oreName(), item);
        }
    }

    private static List<Entry> registrations() {
        return List.of(
                new Entry("ingotSkySteel", ENTRY_IDS.get("ingotSkySteel"), MEGAItems.SKY_STEEL_INGOT::asItem),
                new Entry("ingotSkyBronze", ENTRY_IDS.get("ingotSkyBronze"), MEGAItems.SKY_BRONZE_INGOT::asItem));
    }

    private record Entry(String oreName, String itemId, Supplier<Item> itemSupplier) {
    }
}
