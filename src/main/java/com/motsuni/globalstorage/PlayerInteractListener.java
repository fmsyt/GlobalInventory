package com.motsuni.globalstorage;

import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;


public class PlayerInteractListener implements Listener {

    protected GlobalInventoryManager manager;

    public PlayerInteractListener(GlobalInventoryManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (block.getType().asBlockType() != BlockType.OXIDIZED_CUT_COPPER) {
            return;
        }

        Player player = event.getPlayer();
        player.chat("GlobalInventory Opened");

        player.openInventory(this.manager.inventories.getFirst());
    }

}
