package com.github.whitehooder.LoreAnimations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class LoreAnimations extends JavaPlugin implements Listener {
    private static File animationFolder;
    private static File gifFolder;
    
    public int fps = 20;
    public double gif_speed = 0.5;
    
    public Set<Inventory> invlist = new HashSet<Inventory>();
    public HashMap<Material, Integer> framecounter = new HashMap<Material, Integer>();
    public HashMap<Material, ArrayList<ArrayList<String>>> animation = new HashMap<Material, ArrayList<ArrayList<String>>>();

    @Override
    public void onEnable() {
        animationFolder = new File(getDataFolder(), "animations");
        gifFolder = new File(getDataFolder(), "gifs");
        saveDefaultConfig();
        if (!gifFolder.exists()) {
        	gifFolder.mkdir();
        }
        if (!animationFolder.exists()) {
        	animationFolder.mkdir();
        }
        fps = getConfig().getInt("fps");
        gif_speed = getConfig().getDouble("gif-speed");
        for (Player p : getServer().getOnlinePlayers()) {
            invlist.add(p.getInventory());
        }
        for (File file : gifFolder.listFiles()) {
        	new LoreAnimator(file, this);
        }
        for (File file : animationFolder.listFiles()) {
            if (file.getName().endsWith(".txt")) {
                String string = file.getName().replaceFirst("\\.txt$", "");
                Material mat;
                if (getConfig().isString(string)) {
                    mat = Material.matchMaterial(getConfig().getString(string));
                    string = getConfig().getString(string);
                } else {
                	mat = Material.matchMaterial(string);
                }
                if (mat != null) {
                    framecounter.put(mat, 0);
                    InputStream iostream = null;
                    InputStreamReader ioreader = null;
                    BufferedReader bfreader = null;
                    try {
                        iostream = new FileInputStream(file);
                        ioreader = new InputStreamReader(iostream, "UTF-8");
                        bfreader = new BufferedReader(ioreader);
                        String line;
                        try {
                            ArrayList<String> frame = new ArrayList<String>();
                            ArrayList<ArrayList<String>> frames = new ArrayList<ArrayList<String>>();
                            while ((line = bfreader.readLine()) != null) {
                                if (line.equals("==FRAME==")) {
                                    frames.add((new ArrayList<String>(frame)));
                                    frame.clear();
                                } else if (line.equals("==COPY==")) {
                                	frame.add("==COPY==");
                                	frames.add((new ArrayList<String>(frame)));
                                	frame.clear();
                                } else {
                                    frame.add(colorize(line));
                                }
                            }
                            animation.put(mat, frames);
                            getLogger().info("Matched animation " + string + " with material " + mat);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        bfreader.close();
                        ioreader.close();
                        iostream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (iostream != null) {
                            try {
                                iostream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (ioreader != null) {
                            try {
                                ioreader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (bfreader != null) {
                            try {
                                bfreader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    getLogger().severe("Unknown material: " + string);
                }
            }
        }

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (Inventory inv : invlist) {
                    for (ItemStack item : inv.getContents()) {
                        if (item != null) {
                            if (animation.containsKey(item.getType())) {
                                ItemMeta meta = item.getItemMeta();
                                if (animation.get(item.getType()).size() > 0) {
                                	if (animation.get(item.getType()).get(framecounter.get(item.getType())).contains("==COPY==")) {
                                		framecounter.put(item.getType(), framecounter.get(item.getType()) + 1);
                                		if (framecounter.get(item.getType()) > (animation.get(item.getType()).size() - 1)) {
                                            framecounter.put(item.getType(), 0);
                                        }
                                    } else {
                                    	meta.setLore(animation.get(item.getType()).get(framecounter.get(item.getType())));
                                        framecounter.put(item.getType(), framecounter.get(item.getType()) + 1);
                                        item.setItemMeta(meta);
                                        if (framecounter.get(item.getType()) > (animation.get(item.getType()).size() - 1)) {
                                            framecounter.put(item.getType(), 0);
                                        }
                                    }
                                } else {
                                	meta.setLore(new ArrayList<String>());
                                    item.setItemMeta(meta);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, Math.round(20/fps));
        getServer().getPluginManager().registerEvents(this, this);
    }

    public String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s) + ChatColor.RESET;
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        invlist.add(event.getInventory());
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        invlist.remove(event.getInventory());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!invlist.contains(e.getPlayer().getInventory())) {
            invlist.add(e.getPlayer().getInventory());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        invlist.remove(e.getPlayer().getInventory());
    }
}