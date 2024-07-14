package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandInventory extends SubCommand {

    public CommandInventory(GlobalInventoryManager manager, CommandSender commandSender, String @NotNull [] args) {
        super(manager, commandSender, args);
    }

    @Override
    public boolean invoke() {
        switch (this.key) {
            case "open":
                return open();
            case "clear":
                return clear();
            case "save":
                return save();
            default:
                return false;
        }
    }

    public boolean open() {

        if (!(commandSender instanceof Player)) {
            System.out.println("CommandSender is not Player");
            return true;
        }

        int index = 0;
        if (this.args.length > 0) {

            boolean isNumber = args[0].matches("[0-9]+");
            if (!isNumber) {
                return false;
            }

            index = Integer.parseInt(args[0]) - 1;
        }

        Player player = (Player) commandSender;

        if (manager.getInventoryLength() <= index) {
            String message = String.format("Index is out of range: %d", index);
            player.chat(message);
            return true;
        }

        manager.openInventory(player, index);

        return true;
    }

    public boolean clear() {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.chat("GlobalInventory Cleared");
        }

        manager.removeAllItems();
        return true;
    }

    public boolean save() {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.chat("GlobalInventory Saved");
        }

        manager.save();
        return true;
    }
}
