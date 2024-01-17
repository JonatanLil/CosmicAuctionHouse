package me.panda.cosmicauctionhouse.engine.bp;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Timer;
import java.util.TimerTask;

public class Auction {

    private ItemStack itemToAuction;
    private int price;
    private Player seller;
    private boolean bought;
    private Player buyer;
    private boolean expired;

    public Auction(ItemStack itemToAuction, int price, Player seller) {
        this.itemToAuction = itemToAuction;
        this.price = price;
        this.seller = seller;
        this.expired = false;
        startExpirationTimer();
    }

    public ItemStack getAuctionedItem() {
        return  itemToAuction;
    }

    public int getPrice() {
        return price;
    }

    public Player getSeller() {
        return seller;
    }

    public Player getBuyer() {
        return buyer;
    }

    public boolean isBought() {
        return bought;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setBuyer(Player player) {
        buyer = player;
    }

    public void setBought(Boolean bought) {
        this.bought = bought;
    }

    private void startExpirationTimer() {
        Timer timer = new Timer(true); // true indicates a daemon thread
        long durationInMillis = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Check if the auction is not already bought
                if (!bought) {
                    // Set the expired flag to true
                    expired = true;
                    // Additional logic can be added here, such as notifying players, etc.
                }
            }
        }, durationInMillis);
    }

}
