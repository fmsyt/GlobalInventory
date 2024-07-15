package com.motsuni.globalstorage.itemstack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackEmpty {
    protected static ItemStackEmpty instance = null;
    protected ItemStack itemStack;

    public ItemStackEmpty() {
        this.itemStack = new ItemStack(Material.BARRIER, 1);

        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Empty");
        }

        this.itemStack.setItemMeta(meta);
    }

    public static ItemStackEmpty getInstance() {
        if (instance == null) {
            instance = new ItemStackEmpty();
        }

        return instance;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public boolean isSimilar(ItemStack itemStack) {
        return this.itemStack.isSimilar(itemStack);
    }
}
