package me.panda.cosmicauctionhouse.engine.bp;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionItem {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private int id; // Added id variable
    private ItemStack itemToAuction;
    private int price;
    private OfflinePlayer seller;
    private boolean bought;
    private OfflinePlayer buyer;
    private boolean expired;
    private long timeLeft;
    private ItemStack displayItem;
    private boolean expiredTask;

    public AuctionItem(ItemStack itemToAuction, int price, OfflinePlayer seller, long initialTime) {
        this.itemToAuction = itemToAuction;
        this.price = price;
        this.seller = seller;
        this.expired = false;

        this.id = idGenerator.incrementAndGet();

        System.out.println("ID : " + id);

        startExpirationTimer(initialTime);
    }

    public AuctionItem(ItemStack itemToAuction, int price, OfflinePlayer seller) {
        this.itemToAuction = itemToAuction;
        this.price = price;
        this.seller = seller;
        this.expired = true;

        this.id = idGenerator.incrementAndGet();
        System.out.println("ID : " + id);
    }
    // Added set & get ID method
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public ItemStack getAuctionedItem() {
        return itemToAuction;
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
        if (!expiredTask) {
            expiredTask = true;
            new BukkitRunnable() {

                @Override
                public void run() {
                    // Check if the auction is not already bought
                    // System.out.println("RUNNABLE");
                    if (!bought && !expired) {
                        System.out.println("NOT BOUGHT");
                        if (timeLeft <= 0) {
                            // Set the expired flag to true
                            expired = true;
                            // System.out.println("EXPIRED");
                            // Cancel the task since the auction has expired
                            this.cancel();
                        } else {
                            // Update the time left
                            timeLeft--;
                        }
                    } else {
                        // Auction was bought/removed, cancel the task
                        this.cancel();
                    }
                }
            }.runTaskTimer(CosmicAuctionHouse.INSTANCE, 0L, period);
        }
    }

    public String getSellerName() {
        return (seller != null) ? seller.getName() : "ᴜɴᴋɴᴏᴡɴ sᴇʟʟᴇʀ";
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

    public void updateDisplayItemLore(long timeLeft, Inventory inventory, int slot) {
        // System.out.println("TESTING INSIDE UpdateDisplayItem method");
        ItemStack displayItem = getDisplayItem();
        ItemMeta itemMeta = displayItem.getItemMeta();
        itemMeta.lore().clear();
        // Remove previous countdown lore
        List<String> lore = new ArrayList<>();
        for (String line : itemMeta.getLore()) {
            if (!line.contains("ᴛɪᴍᴇ ʟᴇғᴛ:")) {
                lore.add(line);
            }
        }


        // Add updated countdown lore
        lore.add(ChatColor.GOLD + "ᴛɪᴍᴇ ʟᴇғᴛ: " + ChatColor.YELLOW + formatTime(timeLeft));

        // Update the lore
        itemMeta.setLore(lore);
        displayItem.setItemMeta(itemMeta);
        inventory.setItem(slot, displayItem);
    }

    public ItemStack getDisplayItem() {
        if (this.displayItem != null) {
            return this.displayItem;
        }
        // System.out.println("DisplayItem = null");

        this.displayItem = itemToAuction.clone();
        ItemMeta itemMeta = displayItem.getItemMeta();

        // Add price and seller to the lore
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.add("");
        lore.add(ChatColor.WHITE + "--------------------------------");
        lore.add(ChatColor.YELLOW + "ᴄʟɪᴄᴋ ɪᴛᴇᴍ ᴛᴏ ʙᴜʏ!");
        lore.add(ChatColor.GOLD + "ᴘʀɪᴄᴇ: " + ChatColor.YELLOW + getPrice());
        lore.add(ChatColor.GOLD + "sᴇʟʟᴇʀ: " + ChatColor.YELLOW + getSellerName());
        lore.add(ChatColor.WHITE + "--------------------------------");
        lore.add(ChatColor.GOLD + "ᴛɪᴍᴇ ʟᴇғᴛ: " + ChatColor.YELLOW + getTimeLeft()); // Initial value


        itemMeta.setLore(lore);
        displayItem.setItemMeta(itemMeta);

        return this.displayItem;
    }
}
