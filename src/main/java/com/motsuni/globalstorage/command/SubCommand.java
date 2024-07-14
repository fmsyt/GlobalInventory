package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class SubCommand {

    protected GlobalInventoryManager manager;
    protected CommandSender commandSender;

    protected String key;
    protected String[] args;

    public SubCommand(GlobalInventoryManager manager, CommandSender commandSender, String @NotNull [] args) {

        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        this.manager = manager;
        this.commandSender = commandSender;
        this.key = args[0];
        this.args = newArgs;
    }

    public boolean invoke() {
        return false;
    }
}
