package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandManage extends SubCommand{
    public CommandManage(GlobalInventoryManager manager, CommandSender commandSender, String @NotNull [] args) {
        super(manager, commandSender, args);
    }

    @Override
    public boolean invoke() {
        switch (this.key) {
            case "backup":
                return backup();
            default:
                return false;
        }
    }

    public boolean backup() {
        this.manager.backup();
        return true;
    }
}
