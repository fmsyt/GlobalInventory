package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            case "sort":
                return sort(this.args.length > 0 ? this.args[0] : null);
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

        if (index < 0) {
            player.sendMessage("ページ番号は1以上で指定してください");
            return false;
        }

        if (manager.getInventoryLength() <= index) {
            String message = String.format("Index is out of range: %d", index);
            player.sendMessage(message);
            return true;
        }

        manager.openInventory(player, index);

        return true;
    }

    public boolean clear() {

        return false;

//        if (commandSender instanceof Player) {
//            Player player = (Player) commandSender;
//            player.sendMessage("GlobalInventory Cleared");
//        }
//
//        manager.removeAllItems();
//        return true;
    }

    public boolean save() {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.sendMessage("GlobalInventory Saved");
        }

        manager.save();
        return true;
    }

    public boolean sort(@Nullable String order) {
        if (order == null) {
            manager.sortByType();
            if (commandSender instanceof Player) {
                commandSender.sendMessage("ソートしました");
            }

            return true;
        }

        String _order = order.toLowerCase();
        if (!(_order.equals("asc") || _order.equals("desc"))) {
            return false;
        }

        manager.sortByName(_order);
        if (commandSender instanceof Player) {
            commandSender.sendMessage("ソートしました");
        }

        return true;
    }
}
