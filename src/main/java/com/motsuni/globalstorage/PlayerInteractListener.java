package com.motsuni.globalstorage;

import com.motsuni.globalstorage.itemstack.ItemStackKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class PlayerInteractListener implements Listener {

    protected GlobalInventoryManager manager;

    public PlayerInteractListener(GlobalInventoryManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {

         Action action = event.getAction();
         if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
             return;
         }

        ItemStack leftHand = event.getItem();
        if (!ItemStackKey.getInstance().isSimilar(leftHand)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        this.manager.openInventory(player);
    }

}
