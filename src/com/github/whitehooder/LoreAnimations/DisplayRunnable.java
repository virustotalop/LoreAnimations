package com.github.whitehooder.LoreAnimations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class DisplayRunnable implements Runnable {

    public LoreAnimations instance;

    public DisplayRunnable(LoreAnimations instance) {

        this.instance = instance;
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory invent = player.getOpenInventory().getBottomInventory();
            if ((invent != null)
                    && (invent.getType().equals(InventoryType.CHEST)
                    || invent.getType().equals(InventoryType.PLAYER)
                    || invent.getType().equals(InventoryType.MERCHANT))) {
                for (ItemStack item : invent.getContents()) {
                    if (item != null) {
                        if (instance.animation.containsKey(item.getType())) {
                            ItemMeta meta = item.getItemMeta();
                            if (instance.animation.get(item.getType()).size() > 0) {
                                if (instance.animation.get(item.getType()).get(instance.frameCounter.get(item.getType())).contains("==COPY==")) {
                                    instance.frameCounter.put(item.getType(), instance.frameCounter.get(item.getType()) + 1);
                                    if (instance.frameCounter.get(item.getType()) > (instance.animation.get(item.getType()).size() - 1)) {
                                        instance.frameCounter.put(item.getType(), 0);
                                    }
                                } else {
                                    meta.setLore(instance.animation.get(item.getType()).get(instance.frameCounter.get(item.getType())));
                                    instance.frameCounter.put(item.getType(), instance.frameCounter.get(item.getType()) + 1);
                                    item.setItemMeta(meta);
                                    if (instance.frameCounter.get(item.getType()) > (instance.animation.get(item.getType()).size() - 1)) {
                                        instance.frameCounter.put(item.getType(), 0);
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
    }
}
