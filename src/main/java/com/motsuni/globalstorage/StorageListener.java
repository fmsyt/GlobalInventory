package com.motsuni.globalstorage;

import com.motsuni.globalstorage.utils.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

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

//        UUID playerUUID = event.getPlayer().getUniqueId();
//        String playerName = event.getPlayer().getName();
//
//        System.out.println("Global Inventory Opened: UUID=" + playerUUID + " Name=" + playerName);
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

        InventoryAction action = event.getAction();

        event.setCancelled(true);

        // InventoryAction action = event.getAction();
        // System.out.println("Inventory Clicked: " + action.name());

        // 今スロットにあるアイテム
        ItemStack clickedItemStack = event.getCurrentItem();
        if (clickedItemStack == null) {
            return;
        }

        int pickedAmount = clickedItemStack.getAmount();
        if (pickedAmount == 0) {
            return;
        }

        // 前のページを指すアイテムがクリックされた場合、次のページを表示する
        if (this.manager.navigatorManager.getPrevious().hasSimilar(clickedItemStack)) {
            Player player = (Player) event.getWhoClicked();
//            player.sendMessage("前のページを表示します");

            this.manager.openPreviousInventory(player);
            return;
        }

        // 次のページを指すアイテムがクリックされた場合、次のページを表示する
        if (this.manager.navigatorManager.getNext().hasSimilar(clickedItemStack)) {
            Player player = (Player) event.getWhoClicked();
//            player.sendMessage("次のページを表示します");
            this.manager.openNextInventory(player);
            return;
        }

        // Logger.info(String.format("Current Item: %s: amount=%d", clickedItemStack.getType().name(), clickedItemStack.getAmount()));


        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }


        if (clickedInventory instanceof PlayerInventory) {
            // クリックされたインベントリがプレイヤーのインベントリの場合、GlobalInventoryにアイテムを追加する
            // PlayerInventory -> GlobalInventory

            this.moveItemToGlobalInventory(globalInventory, clickedItemStack);
            clickedItemStack.setAmount(0);

        } else { // !(clickedInventory instanceof PlayerInventory)

            // クリックされたインベントリがGlobalInventoryの場合、プレイヤーのインベントリにアイテムを追加する
            // GlobalInventory -> PlayerInventory

            PlayerInventory playerInventory = event.getWhoClicked().getInventory();
            ItemStack itemStack = this.moveToPlayerInventory(playerInventory, clickedItemStack);

            playerInventory.addItem(itemStack);
        }

        this.manager.updateInventory(globalInventory);
    }


    public void moveItemToGlobalInventory(@NotNull Inventory to, @NotNull ItemStack itemStack) {
        this.manager.addItemAmount(itemStack);
    }

    @NotNull
    public ItemStack moveToPlayerInventory(@NotNull PlayerInventory to, @NotNull ItemStack interfaceItemStack) {

        Logger.info(String.format("Move to Player Inventory: %s: amount=%d: lore=%s", interfaceItemStack.getType().name(), interfaceItemStack.getAmount(), interfaceItemStack.getItemMeta().getLore()));

        ModelGlobalItem globalItem = this.manager.getGlobalItemFromInterfaceItemStack(interfaceItemStack);
        if (globalItem == null) {
            Logger.error("Item Not Found in Global Inventory");
            return new ItemStack(Material.AIR, 0);
        }

        int incomingAmount = Math.min(globalItem.getMaxStackSize(), globalItem.getAmount());

        int currentAmountOnGlobal = globalItem.getAmount();
        if (currentAmountOnGlobal == 0) {
            Logger.error("Item Not Found in Global Inventory");
            return new ItemStack(Material.AIR, 0);
        }

        int first = to.firstEmpty();
        if (first == -1) {
            Logger.error("Player Inventory is Full");
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
            // System.err.println("Item Cannot be Moved");
            Logger.error("Item Cannot be Moved");
            return new ItemStack(Material.AIR, 0);
        }

        ItemStack item = globalItem.pullItemStack(moveAmount);
        Logger.info(String.format("Item Moved: %s: amount=%d: left=%d", globalItem.getType().name(), moveAmount, amountLeft));

        return item;
    }



    @EventHandler
    public void onStorageClosed(@NotNull InventoryCloseEvent event) {

        // 閉じられたインベントリが管理対象のインベントリでない場合、何もしない
        Inventory inventory = event.getInventory();
        if (this.manager.isManagedInventory(inventory)) {
            return;
        }

        Player player = (Player) event.getPlayer();

//        Logger.info(String.format("Global Inventory Closed: UUID=%s Name=%s", player.getUniqueId(), player.getName()));

        this.manager.closeInventory(player);
        this.manager.save();
    }
}
