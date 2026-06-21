package com.gripe.megacells.client;

import ae2.api.client.StorageCellModels;
import ae2.block.networking.EnergyCellBlockItem;
import ae2.core.AppEng;
import ae2.core.registries.CraftingUnitClientRegistry;
import ae2.items.storage.BasicStorageCell;
import ae2.items.tools.powered.PortableCellItem;
import com.gripe.megacells.MEGACells;
import com.gripe.megacells.Tags;
import com.gripe.megacells.block.MEGACraftingUnitDefinition;
import com.gripe.megacells.definition.MEGABlocks;
import com.gripe.megacells.definition.MEGAItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class MEGACellsClient {
    private MEGACellsClient() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerEnergyCellFillLevel();
        registerStorageCellModels();
    }

    @SubscribeEvent
    public static void registerCraftingUnitTextures(TextureStitchEvent.Pre event) {
        Set<ResourceLocation> textures = new LinkedHashSet<>();

        for (var definition : MEGACraftingUnitDefinition.values()) {
            var visualDefinition = definition.getVisualDefinition();
            addTexture(textures, visualDefinition.ringCornerTexture());
            addTexture(textures, visualDefinition.ringSideHorTexture());
            addTexture(textures, visualDefinition.ringSideVerTexture());
            addTexture(textures, visualDefinition.baseTexture());
            addTexture(textures, visualDefinition.lightTexture());
            addTexture(textures, visualDefinition.monitorBaseTexture());
            addTexture(textures, visualDefinition.monitorLightDarkTexture());
            addTexture(textures, visualDefinition.monitorLightMediumTexture());
            addTexture(textures, visualDefinition.monitorLightBrightTexture());
        }

        for (ResourceLocation texture : textures) {
            event.getMap().registerSprite(texture);
        }
    }

    @SubscribeEvent
    public static void registerCraftingFormedModels(ModelBakeEvent event) {
        var registry = CraftingUnitClientRegistry.getInstance();
        registry.initBuiltins();

        for (var definition : MEGACraftingUnitDefinition.values()) {
            IBakedModel model = registry.createFormedModel(
                definition,
                DefaultVertexFormats.BLOCK,
                ModelLoader.defaultTextureGetter());
            registerFormedModelVariants(event, definition, model);
        }
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        List<Item> standardCells = new ArrayList<>();
        List<Item> portableCells = new ArrayList<>();

        for (var cell : MEGAItems.getTieredCells()) {
            (cell.portable() ? portableCells : standardCells).add(cell.item().asItem());
        }

        standardCells.add(MEGAItems.BULK_ITEM_CELL.asItem());

        IItemColor standardCellColor = BasicStorageCell::getColor;
        IItemColor portableCellColor = PortableCellItem::getColor;

        event.getItemColors().registerItemColorHandler(standardCellColor, standardCells.toArray(new Item[0]));
        event.getItemColors().registerItemColorHandler(portableCellColor, portableCells.toArray(new Item[0]));
    }

    private static void registerEnergyCellFillLevel() {
        MEGABlocks.MEGA_ENERGY_CELL.asItem()
                                   .addPropertyOverride(AppEng.makeId("fill_level"), (stack, world, entity) -> {
                                       var energyCell = (EnergyCellBlockItem) MEGABlocks.MEGA_ENERGY_CELL.asItem();
                                       double maxPower = energyCell.getAEMaxPower(stack);

                                       if (maxPower <= 0) {
                                           return 0;
                                       }

                                       return (float) (energyCell.getAECurrentPower(stack) / maxPower);
                                   });
    }

    private static void registerStorageCellModels() {
        String modelPrefix = "block/drive/cells/";

        for (var cell : MEGAItems.getTieredCells()) {
            StorageCellModels.registerModel(
                cell.item().asItem(),
                MEGACells.makeId(modelPrefix + cell.tier().namePrefix() + "_" + cell.modelType().driveModelSuffix() + "_cell"));
        }

        StorageCellModels.registerModel(
            MEGAItems.BULK_ITEM_CELL.asItem(),
            MEGACells.makeId(modelPrefix + MEGAItems.BULK_ITEM_CELL.id().getPath()));
    }

    private static void registerFormedModelVariants(
        ModelBakeEvent event,
        MEGACraftingUnitDefinition definition,
        IBakedModel model) {
        String fullPath = definition.getVisualDefinition().formedModel().getPath();
        String blockstatePath = fullPath.startsWith("block/") ? fullPath.substring("block/".length()) : fullPath;
        String blockPath = definition.id().getPath();

        registerFormedModelVariant(event, fullPath, "formed=true,powered=false", model);
        registerFormedModelVariant(event, fullPath, "formed=true,powered=true", model);
        registerFormedModelVariant(event, blockstatePath, "formed=true,powered=false", model);
        registerFormedModelVariant(event, blockstatePath, "formed=true,powered=true", model);
        registerFormedModelVariant(event, blockPath, "formed=true,powered=false", model);
        registerFormedModelVariant(event, blockPath, "formed=true,powered=true", model);
    }

    private static void registerFormedModelVariant(ModelBakeEvent event, String path, String variant, IBakedModel model) {
        event.getModelRegistry().putObject(new ModelResourceLocation(MEGACells.makeId(path), variant), model);
    }

    private static void addTexture(Set<ResourceLocation> textures, ResourceLocation texture) {
        if (texture != null) {
            textures.add(texture);
        }
    }

    public static void initScreens() {
        // Registered from the 1.12 GUI handler once menu migration is complete.
    }

    public static Minecraft minecraft() {
        return Minecraft.getMinecraft();
    }
}
