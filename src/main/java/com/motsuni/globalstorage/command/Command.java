package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import com.motsuni.globalstorage.ModelGlobalItem;
import com.motsuni.globalstorage.itemstack.ItemStackKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor, TabExecutor {

    protected GlobalInventoryManager manager;

    public Command(GlobalInventoryManager manager) {
        this.manager = manager;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {

        // usage: /globalstorage <give | inventory <open [page] | save | sort [asc | desc]> >

        boolean isOperator = commandSender.isOp();

        if (args.length == 1) {
            List<String> common = new ArrayList<>(Arrays.asList("help", "give", "inventory", "pull"));
            if (isOperator) {
                common.add("manage");
            }
            return common;
        }

        if (args.length == 2) {
            if (args[0].equals("inventory")) {
                return Arrays.asList("open", "save", "sort");
            }

            if (args[0].equals("manage") && isOperator) {
                return List.of("backup", "config");
            }
        }

        if (args.length == 3) {
            if (args[0].equals("inventory") && args[1].equals("sort")) {
                return Arrays.asList("asc", "desc");
            }

            if (args[0].equals("manage") && args[1].equals("config") && isOperator) {
                return Arrays.asList("max_pull_amount", "backup_interval");
            }
        }

        return null;
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
            case "manage":
                return manage(commandSender, newArgs);
            case "pull":
                return pull(commandSender, newArgs);
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
        player.sendMessage("/globalstorage inventory open [<ページ番号>]: インベントリを開く");
        player.sendMessage("/globalstorage inventory save: インベントリを保存する");
        player.sendMessage("/globalstorage inventory sort [asc|desc]: インベントリをソートする");
        player.sendMessage("/globalstorage pull <管理番号> [<個数>]: アイテムを引き出す");
        // player.sendMessage("/globalstorage inventory clear: インベントリの中身をすべて削除する");

        if (commandSender.isOp()) {
            player.sendMessage("/globalstorage manage backup: インベントリをバックアップする");
            player.sendMessage("/globalstorage manage config <max_pull_amount|backup_interval> [value]: 設定を変更する");
        }

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

    private boolean manage(CommandSender commandSender, String[] args) {
        if (!commandSender.isOp()) {
            return false;
        }
        return new CommandManage(manager, commandSender, args).invoke();
    }

    private boolean pull(CommandSender commandSender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        // TODO: ほかにどのClassが対応しているか知らないからPlayerに限定
        if (!(commandSender instanceof Player)) {
            return true;
        }

        ModelGlobalItem item = manager.getGlobalItemFromIndex(Integer.parseInt(args[0]));
        if (item == null) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                player.sendMessage("アイテムが見つかりません");
            }

            return true;
        }

        int amount = item.getMaxStackSize();
        if (args.length > 1) {
            String arg = args[1];
            if (arg.matches("[0-9]+")) {
                amount = Integer.parseInt(arg);
            } else {
                // ほかの入力を受け付けるならこの辺で処理
            }
        }

        int configMaxPullAmount = this.manager.getConfig().getMaxPullAmount();
        if (amount > configMaxPullAmount) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                player.sendMessage(String.format("引き出し可能な最大数は %d 個です", configMaxPullAmount));
            }

            return true;
        }

        int maxStackSize = item.getMaxStackSize();

        // TODO: 足元にアイテムを置くまでの暫定対応
        while (amount > maxStackSize) {
            ItemStack itemStack = item.pullItemStack(maxStackSize);

            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            }

            amount -= maxStackSize;
        }

        ItemStack itemStack = item.pullItemStack(amount);

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }

        this.manager.organizeInventory();

        return true;
    }
}
