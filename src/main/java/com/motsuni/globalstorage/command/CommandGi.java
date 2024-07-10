package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.itemstack.ItemStackKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandGi implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;
        if (!player.hasPermission("globalstorage.command.gi")) {
            return false;
        }

        ItemStack key = ItemStackKey.getInstance().getKey();
        player.getInventory().addItem(key);

        return true;
    }
}
