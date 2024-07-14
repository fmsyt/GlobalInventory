package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import com.motsuni.globalstorage.itemstack.ItemStackKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Command implements CommandExecutor {

    protected GlobalInventoryManager manager;

    public Command(GlobalInventoryManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {

        System.out.printf("Command: %s, Args: %s\n", label, Arrays.toString(args));

        if (!(commandSender instanceof Player)) {
            return false;
        }

        if (args.length == 0) {
            return false;
        }

        String key = args[0];
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (key) {
            case "help":
                return help(commandSender);
            case "give":
                return give(commandSender, newArgs);
            case "inventory":
                return inventory(commandSender, newArgs);
            default:
                return false;
        }
    }

    private boolean help(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            return true;
        }

        Player player = (Player) commandSender;
        player.sendMessage("GlobalStorage Help");
        player.sendMessage("/globalstorage give: インベントリを開くためのアイテムを受け取る");
        player.sendMessage("/globalstorage inventory open [page]: インベントリを開く");
        player.sendMessage("/globalstorage inventory save: インベントリを保存する");
        player.sendMessage("/globalstorage inventory clear: インベントリの中身をすべて削除する");

        return true;
    }

    private boolean give(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;

        ItemStack key = ItemStackKey.getInstance().getKey();
        player.getInventory().addItem(key);
        return true;
    }

    private boolean inventory(CommandSender commandSender, String[] args) {
        return new CommandInventory(manager, commandSender, args).invoke();
    }
}
