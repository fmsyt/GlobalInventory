package com.motsuni.globalstorage;

import com.motsuni.globalstorage.command.Command;
import com.motsuni.globalstorage.itemstack.ItemStackEmpty;
import com.motsuni.globalstorage.itemstack.NavigatorManager;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
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

    public static final int MAX_INVENTORY_SLOT_SIZE_WITH_NAVIGATION = 54;
    public static final int MAX_INVENTORY_SLOT_SIZE = MAX_INVENTORY_SLOT_SIZE_WITH_NAVIGATION - 9;

    protected Plugin plugin;
    protected PluginManager manager;

    protected List<ModelGlobalItem> globalItems;
    protected List<Inventory> inventories;

    protected NavigatorManager navigatorManager;

    protected Map<UUID, Integer> openedInventoryMap = new HashMap<>();

    public GlobalInventoryManager(@NotNull JavaPlugin plugin) {
        this.navigatorManager = new NavigatorManager();

        this.inventories = new ArrayList<>();
        this.globalItems = new ArrayList<>();

        Server server = getServer();

        this.plugin = plugin;
        this.manager = server.getPluginManager();

        PluginCommand gi = plugin.getCommand("globalstorage");
        if (gi != null) {
            gi.setExecutor(new Command(this));
        }
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
            Inventory inventory = this.createInventory();
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

        server.broadcastMessage("保存しました");
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

    public void setNavigationSlots(@NotNull Inventory inventory) {

        int inventoryIndex = this.inventories.indexOf(inventory);

        ItemStack previous = this.navigatorManager.getPrevious().getItemStack(inventoryIndex);
        ItemStack next = this.navigatorManager.getNext().getItemStack(inventoryIndex);
        ItemStack empty = ItemStackEmpty.getInstance().getItemStack();

        for (int i = 0; i < 9; i++) {
            inventory.setItem(GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE + i, empty.clone());
        }

        if (inventoryIndex > 0) {
            inventory.setItem(GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE, previous);
        }

        if (inventoryIndex < this.inventories.size() - 1) {
            inventory.setItem(GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE + 8, next);
        }
    }

    public void organizeInventory() {

        for (Inventory inventory: this.inventories) {
            inventory.clear();
            this.setNavigationSlots(inventory);
        }

        for (ModelGlobalItem globalItem : this.globalItems) {

            if (globalItem.getAmount() == 0) {
                continue;
            }

            ItemStack itemStack = globalItem.getInterfaceItemStack();

            Inventory inventory = this.inventories.stream()
                    .filter(inv -> inv.firstEmpty() != -1)
                    .findFirst()
                    .orElse(null);

            if (inventory == null) {
                inventory = this.createInventory();
                this.setNavigationSlots(inventory);

                this.inventories.add(inventory);
            }

            inventory.addItem(itemStack);
        }
    }

    public Inventory createInventory() {
        int currentPages = this.inventories.size() + 1;

        String inventoryName = String.format("GlobalStorage -%d-", currentPages);
        return getServer().createInventory(null, GlobalInventoryManager.MAX_INVENTORY_SLOT_SIZE_WITH_NAVIGATION, inventoryName);
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

        for (int i = 0; i < MAX_INVENTORY_SLOT_SIZE; i++) {
            ItemStack itemStack = inventory.getItem(i);
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

    public void openInventory(Player player) {
        this.openInventory(player, 0);
    }

    public void openInventory(@NotNull Player player, int inventoryIndex) {
        UUID uuid = player.getUniqueId();
        player.sendMessage(String.format("UUID: %s: Index: %d", uuid.toString(), inventoryIndex));

        this.openedInventoryMap.put(player.getUniqueId(), inventoryIndex);
        this.preOpenInventory(this.inventories.get(inventoryIndex));
        player.openInventory(this.inventories.get(inventoryIndex));
    }

    public void openNextInventory(@NotNull Player player) {
        Integer currentIndex = this.openedInventoryMap.get(player.getUniqueId());
        if (currentIndex == null) {
            currentIndex = 0;
        }

        int nextIndex = currentIndex + 1;
        if (nextIndex >= this.inventories.size()) {
            return;
        }

        this.openInventory(player, nextIndex);
    }

    public void openPreviousInventory(@NotNull Player player) {
        Integer currentIndex = this.openedInventoryMap.get(player.getUniqueId());
        if (currentIndex == null) {
            currentIndex = this.inventories.size() - 1;
        }

        int previousIndex = currentIndex - 1;
        if (previousIndex < 0) {
            return;
        }

        this.openInventory(player, previousIndex);
    }

    public void closeInventory(@NotNull Player player) {
        this.openedInventoryMap.remove(player);
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

    public int getInventoryLength() {
        return this.inventories.size();
    }

    public void removeAllItems() {
        this.globalItems.clear();
    }
}
