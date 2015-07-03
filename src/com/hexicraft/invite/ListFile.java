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
    private Path path;

    ListFile(JavaPlugin plugin, Path path) {
        this.plugin = plugin;
        this.path = path;
    }

    public boolean loadFile() {
        try {
            File file = new File(path.toString());
            if (file.exists()) {
                addAll(Files.readAllLines(path, Charset.defaultCharset()));
            } else {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create file");
                }
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("An I/O error has occurred while enabling HexiInvite:");
            e.printStackTrace();
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
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path.toString(), true)))) {
            out.println(line);
        }catch (IOException e) {
            plugin.getLogger().severe("An I/O error has occurred while saving a HexiInvite file:");
            e.printStackTrace();
        }
    }
}
