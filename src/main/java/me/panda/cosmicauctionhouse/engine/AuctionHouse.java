package me.panda.cosmicauctionhouse.engine;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import me.panda.cosmicauctionhouse.engine.bp.Auction;
import me.panda.cosmicauctionhouse.engine.db.AuctionData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouse {

    private JavaPlugin plugin = CosmicAuctionHouse.INSTANCE;
    private AuctionData db;
    private static List<Auction> auctions;

    public AuctionHouse() {
        try {
            db = new AuctionData(plugin, "AuctionHouse");
        } catch (Exception e) {
            plugin.getSLF4JLogger().warn("Problem initiating the database");
        }

        try {
            auctions = db.getAuctions();
        } catch (Exception e) {
            plugin.getSLF4JLogger().warn("Problem retrieving auctions");
        }
    }

    public static List<Auction> getAuctions() {
        return auctions;
    }

    public static void addAuction(Auction auction) {
        auctions.add(auction);
    }

    public static void removeAuction(Auction auction) {
        auctions.remove(auction);
    }

}
