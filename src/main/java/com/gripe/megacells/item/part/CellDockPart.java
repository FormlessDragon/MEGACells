package com.gripe.megacells.item.part;

import ae2.api.implementations.blockentities.IChestOrDrive;
import ae2.api.inventories.InternalInventory;
import ae2.api.networking.GridFlags;
import ae2.api.networking.IGridNodeListener;
import ae2.api.parts.IPartCollisionHelper;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.api.storage.IStorageMounts;
import ae2.api.storage.IStorageProvider;
import ae2.api.storage.MEStorage;
import ae2.api.storage.StorageCells;
import ae2.api.storage.cells.CellState;
import ae2.api.storage.cells.StorageCell;
import ae2.container.ISubGui;
import ae2.helpers.IPriorityHost;
import ae2.items.parts.PartModels;
import ae2.me.storage.DriveWatcher;
import ae2.parts.AEBasePart;
import ae2.parts.PartModel;
import ae2.util.InteractionUtil;
import ae2.util.inv.AppEngCellInventory;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.InternalInventoryHost;
import ae2.util.inv.filter.IAEItemFilter;
import com.gripe.megacells.MEGACells;
import com.gripe.megacells.definition.MEGAItems;
import com.gripe.megacells.misc.MEGAGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CellDockPart extends AEBasePart
    implements InternalInventoryHost, IChestOrDrive, IStorageProvider, IPriorityHost {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellDockPart.class);

    @PartModels
    private static final IPartModel MODEL = new PartModel(MEGACells.makeId("part/cell_dock"));

    private final AppEngCellInventory cellInventory = new AppEngCellInventory(this, 1);
    private DriveWatcher cellWatcher;
    private boolean cached;
    private boolean wasOnline;
    private int priority;

    private Item clientCell = Items.AIR;
    private CellState clientCellState = CellState.ABSENT;
    private byte spin;

    public CellDockPart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode()
            .setIdlePowerUsage(0.5)
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .addService(IStorageProvider.class, this);
        cellInventory.setFilter(new Filter());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        cellInventory.readFromNBT(data, "cell");
        priority = data.getInteger("priority");
        spin = data.getByte("spin");
        cached = false;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        cellInventory.writeToNBT(data, "cell");
        data.setInteger("priority", priority);
        data.setByte("spin", spin);
    }

    @Override
    public boolean readFromStream(PacketBuffer data) {
        boolean changed = super.readFromStream(data);
        Item oldCell = clientCell;
        CellState oldState = clientCellState;
        byte oldSpin = spin;

        ResourceLocation cellId = data.readResourceLocation();
        Item item = ForgeRegistries.ITEMS.getValue(cellId);
        clientCell = item == null ? Items.AIR : item;
        clientCellState = data.readEnumValue(CellState.class);
        spin = data.readByte();

        return changed || oldCell != clientCell || oldState != clientCellState || oldSpin != spin;
    }

    @Override
    public void writeToStream(PacketBuffer data) {
        super.writeToStream(data);
        ResourceLocation cellId = getCell().isEmpty()
            ? ForgeRegistries.ITEMS.getKey(Items.AIR)
            : ForgeRegistries.ITEMS.getKey(getCell().getItem());
        data.writeResourceLocation(cellId);
        data.writeEnumValue(clientCellState = getCellStatus(0));
        data.writeByte(spin);
    }

    @Override
    public void readVisualStateFromNBT(NBTTagCompound data) {
        super.readVisualStateFromNBT(data);
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data.getString("cellId")));
        clientCell = item == null ? Items.AIR : item;

        String cellStatus = data.getString("cellStatus");

        try {
            clientCellState = CellState.valueOf(cellStatus);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Ignoring invalid cell dock visual state {}", cellStatus, e);
            clientCellState = CellState.ABSENT;
        }

        spin = data.getByte("spin");
    }

    @Override
    public void writeVisualStateToNBT(NBTTagCompound data) {
        super.writeVisualStateToNBT(data);
        ResourceLocation cellId = getCell().isEmpty()
            ? ForgeRegistries.ITEMS.getKey(Items.AIR)
            : ForgeRegistries.ITEMS.getKey(getCell().getItem());
        data.setString("cellId", cellId.toString());
        data.setString("cellStatus", getCellStatus(0).name());
        data.setByte("spin", spin);
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        boolean online = getMainNode().isOnline();

        if (online != wasOnline) {
            wasOnline = online;
            IStorageProvider.requestUpdate(getMainNode());
            recalculateDisplay();
        }
    }

    @Override
    public boolean onUseWithoutItem(EntityPlayer player, Vec3d pos) {
        if (!isClientSide()) {
            openMainGui(player);
        }

        return true;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, EntityPlayer player, EnumHand hand, Vec3d pos) {
        if (InteractionUtil.canWrenchRotate(player, heldItem, getTileEntity().getPos())) {
            if (!isClientSide()) {
                spin = (byte) ((spin + 1) % 4);
                getHost().markForSave();
                getHost().markForUpdate();
            }

            return true;
        }

        return super.onUseItemOn(heldItem, player, hand, pos);
    }

    @Override
    public void onPlacement(EntityPlayer player) {
        super.onPlacement(player);
        byte rotation = (byte) (MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 2.5D) & 3);

        if (getSide() == EnumFacing.UP || getSide() == EnumFacing.DOWN) {
            spin = rotation;
        }
    }

    @Override
    public void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        openMainGui(player);
    }

    @Override
    public ItemStack getMainContainerIcon() {
        return MEGAItems.CELL_DOCK.stack();
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        ItemStack cell = getCell();

        if (!cell.isEmpty()) {
            drops.add(cell);
            cellInventory.setItemDirect(0, ItemStack.EMPTY);
        }
    }

    public AppEngCellInventory getCellInventory() {
        return cellInventory;
    }

    private ItemStack getCell() {
        return cellInventory.getStackInSlot(0);
    }

    @Override
    public int getCellCount() {
        return 1;
    }

    @Override
    public boolean isCellBlinking(int slot) {
        return false;
    }

    @Override
    public Item getCellItem(int slot) {
        return slot == 0 ? getCell().getItem() : null;
    }

    @Override
    public MEStorage getCellInventory(int slot) {
        return slot == 0 && cellWatcher != null ? cellWatcher : null;
    }

    @Override
    public StorageCell getOriginalCellInventory(int slot) {
        return slot == 0 && cellWatcher != null ? cellWatcher.getCell() : null;
    }

    @Override
    public CellState getCellStatus(int slot) {
        if (isClientSide()) {
            return clientCellState;
        }

        updateState();
        return slot == 0 && cellWatcher != null ? cellWatcher.getStatus() : CellState.ABSENT;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        if (getMainNode().isOnline()) {
            updateState();

            if (cellWatcher != null) {
                storageMounts.mount(cellWatcher, priority);
            }
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        getHost().markForSave();
        getHost().markForUpdate();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        cached = false;
        updateState();
        IStorageProvider.requestUpdate(getMainNode());
    }

    private void updateState() {
        if (cached) {
            return;
        }

        cellWatcher = null;
        cellInventory.setHandler(0, null);
        double power = 0.5;

        ItemStack cellStack = getCell();
        if (!cellStack.isEmpty()) {
            StorageCell cell = StorageCells.getCellInventory(cellStack, this::onCellContentChanged);

            if (cell != null) {
                cellWatcher = new DriveWatcher(cell, cellStack, this::recalculateDisplay);
                cellInventory.setHandler(0, cell);
                power += cell.getIdleDrain();
            }
        }

        getMainNode().setIdlePowerUsage(power);
        cached = true;
    }

    private void onCellContentChanged() {
        getTileEntity().markDirty();
        getHost().markForUpdate();
    }

    private void recalculateDisplay() {
        CellState state = getCellStatus(0);

        if (clientCellState != state) {
            clientCellState = state;
            getHost().markForUpdate();
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int newValue) {
        priority = newValue;
        getHost().markForSave();
        cached = false;
        updateState();
        IStorageProvider.requestUpdate(getMainNode());
    }

    @Override
    public IPartModel getStaticModels() {
        return MODEL;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(3, 3, 12, 13, 13, 16);
        bch.addBox(5, 5, 11, 11, 11, 12);
    }

    public Item getClientCell() {
        return clientCell;
    }

    public byte getSpin() {
        return spin;
    }

    private void openMainGui(EntityPlayer player) {
        BlockPos pos = getTileEntity().getPos();
        player.openGui(
            MEGACells.instance,
            MEGAGuiHandler.CELL_DOCK,
            getLevel(),
            MEGAGuiHandler.encodeX(pos.getX(), getSide()),
            pos.getY(),
            pos.getZ());
    }

    private static class Filter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return StorageCells.isCellHandled(stack);
        }
    }
}
