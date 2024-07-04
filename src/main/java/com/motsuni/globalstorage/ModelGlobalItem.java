package com.motsuni.globalstorage;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelGlobalItem implements Serializable {
    protected ItemStack interfaceItemStack;
    protected ItemStack originalItemStack;

    protected int amount;

    public ModelGlobalItem(@NotNull ItemStack itemStack) {
        this(itemStack, itemStack.getAmount());
    }

    public ModelGlobalItem(@NotNull ItemStack itemStack, int amount) {
        this.originalItemStack = itemStack.clone();

        this.interfaceItemStack = itemStack.clone();
        this.interfaceItemStack.setAmount(1);

        this.amount = amount;

        ItemMeta meta = this.interfaceItemStack.getItemMeta();
        if (meta != null) {
            if (meta.getLore() != null) {
                meta.getLore().add("保管数: " + this.amount);
            } else {
                List<String> lore = new ArrayList<>();
                lore.add("保管数: " + this.amount);

                meta.setLore(lore);
            }
        }

    }

    public Material getType() {
        return this.originalItemStack.getType();
    }

    public int getMaxStackSize() {
        return this.originalItemStack.getMaxStackSize();
    }

    /**
     * インベントリに表示するためのアイテムを取得する
     * @return ItemStack
     */
    public ItemStack getInterfaceItemStack() {
        return this.interfaceItemStack;
    }

    public void updateLore(@NotNull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {

            List<String> currentLore = null;
            ItemMeta originalMeta = this.originalItemStack.getItemMeta();
            if (originalMeta != null) {
                currentLore = originalMeta.getLore();
            }

            if (currentLore != null) {
                currentLore.add("保管数: " + this.amount);
                meta.setLore(currentLore);

            } else {
                List<String> lore = new ArrayList<>();
                lore.add("保管数: " + this.amount);

                meta.setLore(lore);
            }

            itemStack.setItemMeta(meta);
        }

        this.interfaceItemStack = itemStack;
    }

    /**
     * アイテムがスタック可能かどうか
     * @param itemStack ItemStack
     * @return boolean
     */
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return this.originalItemStack.isSimilar(itemStack);
    }

    public boolean isSimilarInterfaceItemStack(@NotNull ItemStack interfaceItemStack) {
        return this.interfaceItemStack.isSimilar(interfaceItemStack);
    }

    /**
     * このアイテムの保管されている総数を上書きする
     * @param amount int
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * このアイテムの個数を増やす
     * @param amount int
     */
    public void addAmount(int amount) {
        this.amount += amount;
    }

    /**
     * このアイテムの保管されている総数を取得する
     * @return int
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * アイテムを1つ取り出す
     * @return int
     */
    @NotNull
    public ItemStack pullOneItemStack() {
        return this.pullItemStack(1);
    }

    /**
     * アイテムを1スタック取り出す
     * @return ItemStack
     */
    @NotNull
    public ItemStack pullStackedItemStack() {
        return this.pullItemStack(this.interfaceItemStack.getMaxStackSize());
    }

    /**
     * アイテムを指定した数だけ取り出す
     * @param amount int
     * @return ItemStack
     */
    @NotNull
    public ItemStack pullItemStack(int amount) {

        if (this.interfaceItemStack.getMaxStackSize() < amount) {
            throw new IllegalArgumentException("requested amount is larger than max stack size");
        }

        if (amount > this.amount) {
            amount = this.amount;
        }

        ItemStack item = this.originalItemStack.clone();
        item.setAmount(amount);

        this.addAmount(-amount);


        return item;
    }
}
