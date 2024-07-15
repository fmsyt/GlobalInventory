package com.motsuni.globalstorage.itemstack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NavigatorPagerPrevious {

    protected ItemStack itemStack;
    protected Map<Integer, ItemStack> itemStackMap = new HashMap<>();

    public NavigatorPagerPrevious() {
        this.itemStack = new ItemStack(Material.COMMAND_BLOCK, 1);

        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta != null) {
//            meta.setDisplayName("前のページ");
        }

        this.itemStack.setItemMeta(meta);
    }

    public void updateMeta(@NotNull ItemStack itemStack) {
        ItemMeta meta = this.itemStack.getItemMeta();
        itemStack.setItemMeta(meta);
    }

    public ItemStack getOriginalItemStack() {
        ItemStack itemStack = this.itemStack;
        return itemStack;
    }

    public ItemStack getItemStack(int inventoryIndex) {
        if (!this.itemStackMap.containsKey(inventoryIndex)) {
            ItemStack itemStack = this.itemStack.clone();
            this.updateMeta(itemStack);
            this.itemStackMap.put(inventoryIndex, itemStack);
        }

        return this.itemStackMap.get(inventoryIndex);
    }

    public boolean hasSimilar(@NotNull ItemStack itemStack) {
        return this.itemStackMap.values().stream().anyMatch(itemStack::isSimilar);
    }

    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return this.itemStack.isSimilar(itemStack);
    }
}
