package com.motsuni.globalstorage;

import com.motsuni.globalstorage.command.Command;
import com.motsuni.globalstorage.config.PluginConfig;
import com.motsuni.globalstorage.itemstack.ItemStackEmpty;
import com.motsuni.globalstorage.itemstack.NavigatorManager;
import com.motsuni.globalstorage.utils.Logger;
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
import org.jetbrains.annotations.Contract;
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
    protected PluginConfig config;

    protected List<ModelGlobalItem> globalItems;
    protected List<Inventory> inventories;

    protected NavigatorManager navigatorManager;

    protected Map<UUID, Integer> openedInventoryMap = new HashMap<>();

    private int autoIncrementIndex = 0;

    public GlobalInventoryManager(@NotNull JavaPlugin plugin) {
        this.navigatorManager = new NavigatorManager();

        this.inventories = new ArrayList<>();
        this.globalItems = new ArrayList<>();

        Server server = getServer();

        this.plugin = plugin;
        this.manager = server.getPluginManager();

        this.config = new PluginConfig(plugin);

        PluginCommand gi = plugin.getCommand("globalstorage");
        if (gi != null) {
            gi.setExecutor(new Command(this));
        }
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public PluginConfig getConfig() {
        return this.config;
    }

    public void init() {
        this.load();
        this.manager.registerEvents(new StorageListener(this), this.plugin);
        this.manager.registerEvents(new PlayerInteractListener(this), this.plugin);
    }

    protected void load() {
        Logger.info("loading...");

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

        this.organizeInventory();
    }


    public void save() {
        // https://qiita.com/rushuu_r/items/677bf24db821838a7569#%E3%82%A4%E3%83%B3%E3%83%99%E3%83%B3%E3%83%88%E3%83%AA%E4%BD%9C%E6%88%90
        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/util/io/BukkitObjectOutputStream.html

        this.removeNoItemInGlobalItems();
        this.saveGlobalItems();
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

            try {
                inventory.addItem(itemStack);
            } catch (Exception e) {
                String itemName = itemStack.getType().name();
                Logger.error("Failed to add item: " + itemName);
            }
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
            Logger.error("Failed to save global items: " + e.getMessage());
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
            Logger.error("Failed to save global items: " + e.getMessage());
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
            Logger.error("Failed to load global items: " + e.getMessage());
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
                    item.setIndex(this.nextIndex());
                    this.globalItems.add(item);
                }

                inputStream.close();

            } catch (IOException | ClassNotFoundException e) {
                Logger.error("Failed to load global items: " + e.getMessage());
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
            this.globalItems.add(this.createModelGlobalItem(itemStack));
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

            this.globalItems.add(this.createModelGlobalItem(newItem));
            Logger.info(String.format("Set item: %s: amount=%d", newItem.getType().name(), newItem.getAmount()));

            return;
        }

        this.globalItems = this.globalItems.stream()
                .peek(item -> {
                    if (!item.isSimilar(itemStack)) {
                        return;
                    }
                    item.setAmount(amount);
                    Logger.info(String.format("Set item: %s: amount=%d", item.getType().name(), item.getAmount()));

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

            ModelGlobalItem modelGlobalItem = this.createModelGlobalItem(newItem);

            this.globalItems.add(modelGlobalItem);

            Inventory inventory = this.inventories.get(0);
            inventory.addItem(modelGlobalItem.getInterfaceItemStack());

            Logger.info(String.format("Added item: %s: amount=%d", newItem.getType().name(), newItem.getAmount()));

            return;
        }

        this.globalItems = this.globalItems.stream()
                .peek(item -> {
                    if (!item.isSimilar(itemStack)) {
                        return;
                    }
                    item.addAmount(amount);
                    Logger.info(String.format("Added item: %s: amount=%d", item.getType().name(), item.getAmount()));

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
                .filter(item -> item != null && item.getInterfaceItemStack().equals(itemStack))
                .findFirst()
                .orElse(null);

        return sameItem;
    }

    @Nullable
    public ModelGlobalItem getGlobalItemFromIndex(int index) {
        return this.globalItems.stream()
                .filter(item -> item != null && item.getIndex() == index)
                .findFirst()
                .orElse(null);
    }

    public int getInventoryLength() {
        return this.inventories.size();
    }

    public void removeAllItems() {
        this.globalItems.clear();
    }


    public void sortByName() {
        this.sortByName("asc");
    }

    /**
     * アイテムを名前でソートする
     * @param order String asc or desc
     */
    public void sortByName(String order) {
        this.globalItems.sort((a, b) -> {
            if (order.equals("asc")) {
                return a.getType().name().compareTo(b.getType().name());
            }

            return b.getType().name().compareTo(a.getType().name());
        });
        this.organizeInventory();
    }

    public void sortByType() {
        this.sortByType("asc");
    }

    /**
     * アイテムをキーでソートする
     * @param order String asc or desc
     */
    public void sortByType(String order) {
        this.globalItems.sort((a, b) -> {
            if (order.equals("asc")) {
                return a.getType().compareTo(b.getType());
            }

            return b.getType().compareTo(a.getType());
        });
        this.organizeInventory();
    }

    /**
     * バックアップを作成する
     * @return バックアップファイル名
     */
    public @Nullable String backup() {
        Path path = Paths.get("plugins/GlobalStorage/items.txt");
        Date date = new Date();

        // items_YYYYMMDD_HHMMSS.txt
        String backupFileName = String.format("plugins/GlobalStorage/items_%tY%<tm%<td_%<tH%<tM%<tS.txt", date);
        Path backupPath = Paths.get(backupFileName);

        try {
            // allow override
            Files.copy(path, backupPath);
            Logger.info("Backup global items: " + backupFileName);
        } catch (IOException e) {
            Logger.error("Failed to backup global items: " + e.getMessage());
            return null;
        }

        return backupFileName;
    }

    @Contract("_ -> new")
    private @NotNull ModelGlobalItem createModelGlobalItem(@NotNull ItemStack itemStack) {
        return new ModelGlobalItem(itemStack, itemStack.getAmount(), this.nextIndex());
    }

    private int nextIndex() {
        return ++this.autoIncrementIndex;
    }
}
