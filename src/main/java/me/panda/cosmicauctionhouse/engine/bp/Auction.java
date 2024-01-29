package me.panda.cosmicauctionhouse.engine.bp;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Auction {

    private ItemStack itemToAuction;
    private int price;
    private OfflinePlayer seller;
    private boolean bought;
    private OfflinePlayer buyer;
    private boolean expired;
    private long timeLeft;
    private ItemStack displayItem;


    public Auction(ItemStack itemToAuction, int price, OfflinePlayer seller, long initialTime) {
        this.itemToAuction = itemToAuction;
        this.price = price;
        this.seller = seller;
        this.expired = false;

        startExpirationTimer(initialTime);
    }

    public ItemStack getAuctionedItem() {
        return  itemToAuction;
    }

    public int getPrice() {
        return price;
    }

    public OfflinePlayer getSeller() {
        return seller;
    }

    public OfflinePlayer getBuyer() {
        return buyer;
    }

    public boolean isBought() {
        return bought;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setBuyer(OfflinePlayer player) {
        buyer = player;
    }

    public void setBought(Boolean bought) {
        this.bought = bought;
    }

    public void setTimeLeft(long timeLeft) {
        this.startExpirationTimer(timeLeft);
    }

    private void startExpirationTimer(long initialTime) {
        this.timeLeft = (initialTime > 0) ? initialTime : 24 * 60 * 60; // Use initialTime if available
        long period = 20L; // Update every 20 ticks (1 second)

        new BukkitRunnable() {

            @Override
            public void run() {
                // Check if the auction is not already bought
                if (!bought) {
                    if (timeLeft <= 0) {
                        // Set the expired flag to true
                        expired = true;
                        // Cancel the task since the auction has expired
                        this.cancel();
                    } else {
                        // Update the time left
                        timeLeft--;
                        // Update the lore dynamically
                        updateDisplayItemLore(timeLeft);
                    }
                } else {
                    // Auction was bought, cancel the task
                    this.cancel();
                }
            }
        }.runTaskTimer(CosmicAuctionHouse.INSTANCE, 0L, period);
    }

    public String getSellerName() {
        return (seller != null) ? seller.getName() : "Unknown Seller";
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    private void updateDisplayItemLore(long timeLeft) {
        ItemStack displayItem = getDisplayItem();
        ItemMeta itemMeta = displayItem.getItemMeta();

        // Remove previous countdown lore
        List<String> lore = new ArrayList<>();
        for (String line : itemMeta.getLore()) {
            if (!line.contains("Time left:")) {
                lore.add(line);
            }
        }

        // Add updated countdown lore
        lore.add(ChatColor.GOLD + "Time left: " + ChatColor.YELLOW + formatTime(timeLeft));

        // Update the lore
        itemMeta.setLore(lore);
        displayItem.setItemMeta(itemMeta);
    }

    public ItemStack getDisplayItem() {
        if (this.displayItem != null) {
            return this.displayItem;
        }

        this.displayItem = itemToAuction.clone();
        ItemMeta itemMeta = displayItem.getItemMeta();

        // Add price and seller to the lore
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add("");
        lore.add(ChatColor.WHITE + "--------------------------------");
        lore.add(ChatColor.YELLOW + "Click item to buy!");
        lore.add(ChatColor.GOLD + "Price: " + ChatColor.YELLOW + getPrice());
        //lore.add(ChatColor.GOLD + "Time left: " + ChatColor.YELLOW + formatTime(24 * 60 * 60)); // Initial value
        lore.add(ChatColor.GOLD + "Seller: " + ChatColor.YELLOW + getSellerName());
        lore.add(ChatColor.WHITE + "--------------------------------");
        lore.add(ChatColor.GOLD + "Time left: " + ChatColor.YELLOW + getTimeLeft()); // Initial value


        itemMeta.setLore(lore);
        displayItem.setItemMeta(itemMeta);

        return this.displayItem;
    }


}
