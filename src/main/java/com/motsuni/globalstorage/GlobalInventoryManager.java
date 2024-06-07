package com.motsuni.globalstorage;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

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


public class GlobalInventoryManager {

    protected Plugin plugin;
    protected PluginManager manager;

    protected List<Inventory> inventories;

    public GlobalInventoryManager(Plugin plugin, PluginManager manager) {
        this.inventories = new ArrayList<>();

        this.plugin = plugin;
        this.manager = manager;
    }



    public void init() {
        Server server = Bukkit.getServer();
        server.broadcastMessage("読み込み中... (未実装)");

        this.load();
        for (Inventory inventory: this.inventories) {
            this.manager.registerEvents(new StorageListener(inventory, this), this.plugin);
            this.manager.registerEvents(new PlayerInteractListener(inventory), this.plugin);
        }
    }

    protected void load() {
        Server server = Bukkit.getServer();
        server.broadcastMessage("読み込み中...");

        // find inventory files
        Path path = Paths.get("plugins/GlobalStorage");

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            return;
        }

        try {
            Files.walk(path)
                .filter(Files::isRegularFile)
                .forEach(this::loadInventory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadInventory(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            String base64 = lines.get(0);
            Inventory inventory = base64ToInventory(base64);
            this.inventories.add(inventory);
        } catch (FileNotFoundException e) {
            // nothing to do
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void save() {
        Server server = Bukkit.getServer();
        server.broadcastMessage("保存中... (未実装)");
        // https://qiita.com/rushuu_r/items/677bf24db821838a7569#%E3%82%A4%E3%83%B3%E3%83%99%E3%83%B3%E3%83%88%E3%83%AA%E4%BD%9C%E6%88%90
        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/util/io/BukkitObjectOutputStream.html

        for (Inventory inventory : this.inventories) {
            this.save(inventory);
        }

    }

    public void save(Inventory inventory) {

        int index = this.inventories.indexOf(inventory);
        if (index == -1) {
            System.err.println("Inventory not found");
            return;
        }

        Path path = Paths.get(String.format("plugins/GlobalStorage/inventory.%d.txt", index));

        try {
            Files.createDirectories(path.getParent());
        } catch (FileAlreadyExistsException e) {
            // nothing to do
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.createFile(path);
        } catch (IOException e) {
            // e.printStackTrace();
        }

        List<String> line = Collections.singletonList(inventoryToBase64(inventory));
        try {
            Files.write(path, line, StandardCharsets.UTF_8);
            System.out.println("Saved inventory to " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String inventoryToBase64(Inventory inventory) {

        if (inventory == null) {
            return null;
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(byteArrayOutputStream);

            outputStream.writeInt(inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                outputStream.writeObject(item);
            }

            outputStream.close();

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Inventory base64ToInventory(String base64) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Global Inventory");

        if (base64 == null) {
            return inventory;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream inputStream = new BukkitObjectInputStream(byteArrayInputStream);

            int size = inputStream.readInt();
            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) inputStream.readObject();
                inventory.setItem(i, item);
            }

            inputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return inventory;
    }
}
