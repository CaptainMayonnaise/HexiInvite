package com.hexicraft.invite;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * @author Ollie
 * @version 1.0
 */
public class ListFile extends ArrayList<String> {

    private JavaPlugin plugin;
    private File dataFolder;
    private File file;

    ListFile(JavaPlugin plugin, File dataFolder, String filename) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;
        this.file = new File(dataFolder, filename);
    }

    public boolean loadFile() {
        try {
            if (file.exists()) {
                addAll(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean add(String e) {
        super.add(e);
        saveFile(e);
        return true;
    }

    private void saveFile(String line) {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            System.err.println("Failed to create directory.");
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            out.println(line);
        }catch (IOException e) {
            plugin.getLogger().severe("Failed to save file: " + e.getMessage());
        }
    }
}
