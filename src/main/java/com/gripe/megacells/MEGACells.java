package com.gripe.megacells;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import com.gripe.megacells.definition.MEGABlocks;
import com.gripe.megacells.definition.MEGAItems;
import com.gripe.megacells.item.cell.BulkCellItem;
import com.gripe.megacells.misc.CompressionService;
import com.gripe.megacells.misc.LavaTransformLogic;
import com.gripe.megacells.misc.MEGAGuiHandler;
import com.gripe.megacells.misc.SyncCompressionChainsPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        dependencies = "required-after:ae2")
@EventBusSubscriber(modid = Tags.MOD_ID)
public final class MEGACells {
    public static final String MODID = Tags.MOD_ID;

    @Mod.Instance(MODID)
    public static MEGACells instance;

    public static ResourceLocation makeId(String path) {
        return new ResourceLocation(MODID, path);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SyncCompressionChainsPacket.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, MEGAGuiHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(CompressionService.class);
        MinecraftForge.EVENT_BUS.register(LavaTransformLogic.class);
        CompressionService.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        BulkCellItem.registerHandler();
        MEGAItems.initUpgrades();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        CompressionService.loadRecipes();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        MEGABlocks.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        MEGAItems.registerItems(event.getRegistry());
        MEGABlocks.registerItems(event.getRegistry());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        MEGABlocks.registerModels();
        MEGAItems.registerModels();
    }
}
