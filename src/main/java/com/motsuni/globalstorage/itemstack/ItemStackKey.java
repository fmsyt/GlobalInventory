package com.motsuni.globalstorage.itemstack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackKey {

    protected static ItemStackKey instance = null;
    protected ItemStack key;

    protected ItemStackKey() {
        this.key = new ItemStack(Material.STICK, 1);

        ItemMeta meta = this.key.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("GlobalInventory Key");
        }

        this.key.setItemMeta(meta);
    }

    public static ItemStackKey getInstance() {
        if (instance == null) {
            instance = new ItemStackKey();
        }

        return instance;
    }

    public ItemStack getKey() {
        return this.key;
    }

    public boolean isSimilar(ItemStack itemStack) {
        return this.key.isSimilar(itemStack);
    }
}
