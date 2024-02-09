package me.panda.cosmicauctionhouse.engine.events.listener;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import me.panda.cosmicauctionhouse.engine.AuctionHouse;
import me.panda.cosmicauctionhouse.engine.bp.Auction;
import me.panda.cosmicauctionhouse.engine.events.AuctionPurchaseEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class AuctionPurchaseListener implements Listener {

    private final CosmicAuctionHouse plugin;
    private AuctionHouse auctionHouse;

    public AuctionPurchaseListener(CosmicAuctionHouse plugin) {
        this.plugin = plugin;
        auctionHouse = plugin.getAuctionHouse();
    }

    @EventHandler
    public void onAuctionPurchase(AuctionPurchaseEvent event) {

        Auction auction = event.getAuction();

        if (!event.isCancelled()) {
            auction.setBuyer(event.getBuyer());
            auctionHouse.addHistoryAuction(auction);

            // Remove the item from the auction list
            auctionHouse.removeAuction(auction);
        }
    }

}
