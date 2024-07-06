package com.motsuni.globalstorage;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;


public class GlobalInventoryManager {

    public static final int MAX_INVENTORY_SLOT_SIZE = 54;

    protected Plugin plugin;
    protected PluginManager manager;

    protected List<ModelGlobalItem> globalItems;

    protected List<Inventory> inventories;

    public GlobalInventoryManager(Plugin plugin) {
        this.inventories = new ArrayList<>();
        this.globalItems = new ArrayList<>();

        Server server = getServer();

        this.plugin = plugin;
        this.manager = server.getPluginManager();
    }



    public void init() {
        this.load();
        this.manager.registerEvents(new StorageListener(this), this.plugin);
        this.manager.registerEvents(new PlayerInteractListener(this), this.plugin);
    }

    protected void load() {
        Server server = getServer();
        server.broadcastMessage("[GlobalStorage] loading... ");

        // find inventory files
        Path path = Paths.get("plugins/GlobalStorage");

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            return;
        }

        this.loadGlobalItems();

        int inventoryLength = (int) Math.floor((double) this.globalItems.size() / GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE) + 1;
        this.inventories = new ArrayList<>();

        for (int i = 0; i < inventoryLength; i++) {
            Inventory inventory = server.createInventory(null, GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE, "GlobalStorage");
            this.inventories.add(inventory);
        }

        System.out.println("[GlobalStorage] Inventory Length: " + inventoryLength);

        this.organizeInventory();
    }


    public void save() {
        Server server = getServer();
        server.broadcastMessage("保存中... ");
        // https://qiita.com/rushuu_r/items/677bf24db821838a7569#%E3%82%A4%E3%83%B3%E3%83%99%E3%83%B3%E3%83%88%E3%83%AA%E4%BD%9C%E6%88%90
        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/util/io/BukkitObjectOutputStream.html

        this.removeNoItemInGlobalItems();
        this.saveGlobalItems();
    }

    public void preOpenInventory(int inventoryIndex) {
        Inventory inventory = this.inventories.get(inventoryIndex);
        this.preOpenInventory(inventory);
    }

    public void preOpenInventory(Inventory inventory) {
        this.updateInventory(inventory);
    }

    public void updateInventory(Inventory inventory) {
        if (this.isManagedInventory(inventory)) {
            return;
        }

        this.removeNoItemInInventory(inventory);

        this.organizeInventory();
        this.updateRoleInInventory(inventory);
    }

    public void organizeInventory() {

        for (Inventory inventory: this.inventories) {
            inventory.clear();
        }

        for (int i = 0; i < this.globalItems.size(); i++) {

            ModelGlobalItem globalItem = this.globalItems.get(i);
            if (globalItem.getAmount() == 0) {
                continue;
            }

            ItemStack itemStack = globalItem.getInterfaceItemStack();

            int storageIndex = (int) Math.floor((double) i / GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE);

            Inventory inventory = null;
            if (storageIndex <= this.inventories.size()) {
                inventory = this.inventories.get(storageIndex);

            } else {
                inventory = getServer().createInventory(null, GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE, "GlobalStorage");
                this.inventories.add(inventory);

                System.out.println("[GlobalStorage] Added new inventory: " + storageIndex);
            }


            inventory.addItem(itemStack);
        }
    }

    public void removeNoItemInGlobalItems() {
        this.globalItems = this.globalItems.stream()
                .filter(item -> item.getAmount() > 0)
                .collect(Collectors.toList());
    }

    public void removeNoItemInInventory(Inventory inventory) {
        if (this.isManagedInventory(inventory)) {
            return;
        }

        for (ItemStack itemStack: inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }

            ModelGlobalItem globalItem = this.getGlobalItemFromInterfaceItemStack(itemStack);
            if (globalItem == null || globalItem.getAmount() == 0) {
                inventory.remove(itemStack);
            }
        }
    }

    public void updateRoleInInventory(Inventory inventory) {
        if (this.isManagedInventory(inventory)) {
            return;
        }

        for (ItemStack itemStack: inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }

            ModelGlobalItem globalItem = this.getGlobalItemFromInterfaceItemStack(itemStack);
            if (globalItem == null) {
                continue;
            }

            globalItem.updateLore(itemStack);
        }
    }



    public void saveGlobalItems() {
        Path path = Paths.get("plugins/GlobalStorage/items.txt");

        try {
            Files.createDirectories(path.getParent());
        } catch (FileAlreadyExistsException e) {
            // nothing to do
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(byteArrayOutputStream);

            outputStream.writeInt(this.globalItems.size());

            for (ModelGlobalItem item: this.globalItems) {
                outputStream.writeObject(item);
            }

            outputStream.close();

            String base64 = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            Files.write(path, Collections.singletonList(base64), StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGlobalItems() {
        Path path = Paths.get("plugins/GlobalStorage/items.txt");

        List<String> lines = null;

        try {
            lines = Files.readAllLines(path);
            if (lines.isEmpty()) {
                return;
            }
        } catch (FileNotFoundException e) {
            // nothing to do
            return;

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lines == null) {
            return;
        }

        for (String line: lines) {
            try {
                byte[] bytes = Base64.getDecoder().decode(line);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                BukkitObjectInputStream inputStream = new BukkitObjectInputStream(byteArrayInputStream);

                int size = inputStream.readInt();
                for (int i = 0; i < size; i++) {
                    ModelGlobalItem item = (ModelGlobalItem) inputStream.readObject();
                    this.globalItems.add(item);
                }

                inputStream.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isManagedInventory(@NotNull Inventory inventory) {
        return !this.inventories.contains(inventory);
    }


    public void addItem(@NotNull ItemStack itemStack) {
        ModelGlobalItem globalItem = this.globalItems.stream()
                .filter(item -> item != null && item.isSimilar(itemStack))
                .findFirst()
                .orElse(null);

        if (globalItem == null) {
            this.globalItems.add(new ModelGlobalItem(itemStack, itemStack.getAmount()));
            return;
        }

        globalItem.addAmount(itemStack.getAmount());
    }

    public void removeItem(@NotNull ItemStack itemStack) {
        this.globalItems.remove(itemStack);
    }

    public int getItemAmount(@NotNull ItemStack itemStack) {
        int amount = 0;
        for (ModelGlobalItem item: this.globalItems) {
            if (!item.isSimilar(itemStack)) {
                continue;
            }

            amount += item.getAmount();
        }

        return amount;
    }


    public void setItemAmount(@NotNull ItemStack itemStack, int amount) {

        ModelGlobalItem sameItem = this.globalItems.stream()
                .filter(item -> item != null && item.isSimilar(itemStack))
                .findFirst()
                .orElse(null);

        if (sameItem == null) {
            ItemStack newItem = itemStack.clone();
            newItem.setAmount(amount);

            this.globalItems.add(new ModelGlobalItem(newItem));
            System.out.println("[GlobalStorage] Set item: " + newItem.getType().name() + ", Amount: " + newItem.getAmount());

            return;
        }

        this.globalItems = this.globalItems.stream()
                .peek(item -> {
                    if (!item.isSimilar(itemStack)) {
                        return;
                    }
                    item.setAmount(amount);
                    System.out.println("[GlobalStorage] Set item: " + item.getType().name() + ", Amount: " + item.getAmount());
                }).collect(Collectors.toList());

    }


    public void addItemAmount(@NotNull ItemStack itemStack) {
        this.addItemAmount(itemStack, itemStack.getAmount());
    }

    public void addItemAmount(@NotNull ItemStack itemStack, int amount) {

        ModelGlobalItem sameItem = this.globalItems.stream()
                .filter(item -> item != null && item.isSimilar(itemStack))
                .findFirst()
                .orElse(null);

        if (sameItem == null) {
            ItemStack newItem = itemStack.clone();
            newItem.setAmount(amount);

            ModelGlobalItem modelGlobalItem = new ModelGlobalItem(newItem);

            this.globalItems.add(modelGlobalItem);

            Inventory inventory = this.inventories.get(0);
            inventory.addItem(modelGlobalItem.getInterfaceItemStack());

            System.out.println("[GlobalStorage] Added item: " + newItem.getType().name() + ", Amount: " + newItem.getAmount());

            return;
        }

        this.globalItems = this.globalItems.stream()
                .peek(item -> {
                    if (!item.isSimilar(itemStack)) {
                        return;
                    }
                    item.addAmount(amount);
                    System.out.println("[GlobalStorage] Added item: " + item.getType().name() + ", Amount: " + item.getAmount());
                }).collect(Collectors.toList());

    }

    @Nullable
    public ModelGlobalItem getGlobalItem(@NotNull ItemStack itemStack) {
        return this.globalItems.stream()
                .filter(item -> item != null && item.isSimilar(itemStack))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public ItemStack getInterfaceItemStack(@NotNull ItemStack itemStack) {
        ModelGlobalItem sameItem = this.getGlobalItem(itemStack);
        if (sameItem == null) {
            return null;
        }

        return sameItem.getInterfaceItemStack();
    }

    @Nullable
    public ModelGlobalItem getGlobalItemFromInterfaceItemStack(@NotNull ItemStack itemStack) {
        ModelGlobalItem sameItem = this.globalItems.stream()
                .filter(item -> item != null && item.isSimilarInterfaceItemStack(itemStack))
                .findFirst()
                .orElse(null);

        return sameItem;
    }
}
