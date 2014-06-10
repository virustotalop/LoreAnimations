package com.github.whitehooder.LoreAnimations;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class LoreAnimations extends JavaPlugin implements Listener {

    private static File animationFolder;
    private static File gifFolder;
    
    public int fps = 20;
    public double gifSpeed = 0.5;

    public HashMap<Material, Integer> frameCounter = new HashMap<Material, Integer>();
    public HashMap<Material, ArrayList<ArrayList<String>>> animation = new HashMap<Material, ArrayList<ArrayList<String>>>();

    public void initialiseFolders() {

        animationFolder = new File(getDataFolder(), "animations");
        gifFolder = new File(getDataFolder(), "gifs");
        animationFolder.mkdirs();
        gifFolder.mkdirs();
    }

    public void initialiseConfig() {

        this.saveDefaultConfig();
        fps = getConfig().getInt("fps");
        gifSpeed = getConfig().getDouble("gif-speed");
    }

    public void initialiseAnimations() {

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
                    frameCounter.put(mat, 0);
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
    }

    public void convertGifs() {
        for (File file : gifFolder.listFiles()) {
        	boolean exists = false;
        	for (File txt : animationFolder.listFiles()) {
        		if (txt.getName().equalsIgnoreCase(file.getName().replaceAll("\\.[Gg][Ii][Ff]$", ".txt"))) {
        			exists = true;
        			break;
        		}	
        	}
        	if (!exists) {
        		new LoreAnimator(file, this);
        	}
        }
    }

    @Override
    public void onEnable() {

        initialiseFolders();
        initialiseConfig();
        convertGifs();
        initialiseAnimations();

        this.getServer().getScheduler().runTaskTimer(this, new DisplayRunnable(this), 0L, Math.round(20/fps));
    }

    public String colorize(String s) {

        return ChatColor.translateAlternateColorCodes('&', s) + ChatColor.RESET;
    }
}