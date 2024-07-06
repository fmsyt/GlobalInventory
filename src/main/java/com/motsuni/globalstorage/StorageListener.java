package com.motsuni.globalstorage;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageListener implements Listener {

    protected GlobalInventoryManager manager;


    public StorageListener(GlobalInventoryManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onOpened(@NotNull InventoryOpenEvent event) {
        if (this.manager.isManagedInventory(event.getInventory())) {
            return;
        }

        event.getPlayer().sendMessage("Global Inventory Opened");
    }

    /**
     * インベントリがクリックされたときの処理
     * この後に各種イベントが発生する
     *
     * @apiNote <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/package-summary.html">Reference</a>
     * @apiNote <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/InventoryClickEvent.html">Reference: InventoryClickEvent</a>
     */
    @EventHandler
    public void onClicked(@NotNull InventoryClickEvent event) {

        Inventory globalInventory = event.getInventory();

        if (this.manager.isManagedInventory(globalInventory)) {
            return;
        }

        event.setCancelled(true);

        InventoryAction action = event.getAction();
        System.out.println("Inventory Clicked: " + action.name());

        // 今スロットにあるアイテム
        ItemStack clickedItemStack = event.getCurrentItem();
        if (clickedItemStack == null) {
            return;
        }

        if (clickedItemStack.getType() == Material.AIR) {
            // クリックされたスロットにアイテムがない場合、何もしない
            return;
        }

        int pickedAmount = clickedItemStack.getAmount();
        if (pickedAmount == 0) {
            return;
        }

        System.out.printf("Current Item: %s: amount=%d%n", clickedItemStack.getType().name(), clickedItemStack.getAmount());

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        Inventory from = null;
        Inventory to = null;

        if (clickedInventory instanceof PlayerInventory) {
            // クリックされたインベントリがプレイヤーのインベントリの場合、GlobalInventoryにアイテムを追加する
            // PlayerInventory -> GlobalInventory

            this.moveItemToGlobalInventory(globalInventory, clickedItemStack);
            clickedItemStack.setAmount(0);

            System.out.println("Item Moved to Global Inventory");

        } else { // !(clickedInventory instanceof PlayerInventory)

            // クリックされたインベントリがGlobalInventoryの場合、プレイヤーのインベントリにアイテムを追加する
            // GlobalInventory -> PlayerInventory

            PlayerInventory playerInventory = event.getWhoClicked().getInventory();
            ItemStack itemStack = this.moveToPlayerInventory(playerInventory, clickedItemStack);

            playerInventory.addItem(itemStack);

            System.out.println("Item Moved to Player Inventory");
        }

        this.manager.updateInventory(globalInventory);
    }


    public void moveItemToGlobalInventory(@NotNull Inventory to, @NotNull ItemStack itemStack) {
        this.manager.addItemAmount(itemStack);
    }

    @NotNull
    public ItemStack moveToPlayerInventory(@NotNull PlayerInventory to, @NotNull ItemStack interfaceItemStack) {

        System.out.println("Move to Player Inventory: " + interfaceItemStack.getType().name() + ": amount=" + interfaceItemStack.getAmount() + " lore=" + interfaceItemStack.getItemMeta().getLore());

        ModelGlobalItem globalItem = this.manager.getGlobalItemFromInterfaceItemStack(interfaceItemStack);
        if (globalItem == null) {
            System.err.println("Item Not Found in Global Inventory");
            return new ItemStack(Material.AIR, 0);
        }

        int incomingAmount = Math.min(globalItem.getMaxStackSize(), globalItem.getAmount());

        int currentAmountOnGlobal = globalItem.getAmount();
        if (currentAmountOnGlobal == 0) {
            System.err.println("Item Not Found in Global Inventory");
            return new ItemStack(Material.AIR, 0);
        }

        int first = to.firstEmpty();
        if (first == -1) {
            System.err.println("Player Inventory is Full");
            return new ItemStack(Material.AIR, 0);
        }

        ItemStack sameItemInPlayerInventory = to.getItem(first);

        int moveAmount = 0;
        int havingAmount = 0;

        int amountLeft = 0;

        if (sameItemInPlayerInventory != null) {
            havingAmount = sameItemInPlayerInventory.getAmount();
        }

        int maxStackSize = globalItem.getMaxStackSize();

        if (havingAmount + incomingAmount > maxStackSize) {
            moveAmount = maxStackSize - havingAmount;
        } else {
            moveAmount = incomingAmount;
        }

        if (moveAmount == 0) {
            System.err.println("Item Cannot be Moved");
            return new ItemStack(Material.AIR, 0);
        }

        ItemStack item = globalItem.pullItemStack(moveAmount);
        System.out.printf("Item Moved: %s: amount=%d%n: left=%d", globalItem.getType().name(), moveAmount, amountLeft);

        return item;
    }



    @EventHandler
    public void onStorageClosed(@NotNull InventoryCloseEvent event) {

        // 閉じられたインベントリが管理対象のインベントリでない場合、何もしない
        Inventory inventory = event.getInventory();
        if (this.manager.isManagedInventory(inventory)) {
            return;
        }

        event.getPlayer().sendMessage("Global Inventory Closed");
        this.manager.save();
    }
}
