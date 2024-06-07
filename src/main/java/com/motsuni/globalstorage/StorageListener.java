package com.motsuni.globalstorage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class StorageListener implements Listener {

    protected GlobalInventoryManager manager;
    protected Inventory inventory;

    public StorageListener(Inventory inventory, GlobalInventoryManager manager) {
        this.inventory = inventory;
        this.manager = manager;
    }

    @EventHandler
    public void onStorageClosed(InventoryCloseEvent event) {

        if (event.getInventory() != this.inventory) {
            return;
        }

        event.getPlayer().sendMessage("Global Inventory Closed");
        this.manager.save(this.inventory);
    }
}
