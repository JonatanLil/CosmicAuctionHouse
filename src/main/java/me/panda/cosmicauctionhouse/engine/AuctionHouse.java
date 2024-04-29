package me.panda.cosmicauctionhouse.engine;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import me.panda.cosmicauctionhouse.engine.bp.Auction;
import me.panda.cosmicauctionhouse.engine.db.AuctionData;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionHouse {

    private JavaPlugin plugin = CosmicAuctionHouse.INSTANCE;
    private AuctionData db;
    private static List<Auction> auctions;
    private static List<Auction> historyList;

    public AuctionHouse() {
        try {
            db = new AuctionData(plugin, "AuctionHouse.db");
        } catch (Exception e) {
            plugin.getSLF4JLogger().warn("Problem initiating the database");
        }

        try {
            auctions = db.getAuctions();
            historyList = db.getHistoryAuctions();
        } catch (Exception e) {
            plugin.getSLF4JLogger().warn("Problem retrieving auctions");
        }
    }

    public List<Auction> getAuctions() {
        return auctions;
    }
    public List<Auction> getHistoryAuctions() {
        return historyList;
    }

    public AuctionData getDb() {
        return db;
    }

    public void closeConnection() {
        if (!auctions.isEmpty() || !historyList.isEmpty()) {
            //db.saveAuctions(auctions);
            //db.saveHistoryAuctions(historyList);
            db.closeConnection();
        }
        else {
            db.closeConnection();
            System.out.println("Closed ah without any auctions");
        }
    }

    public void addHistoryAuction(Auction auction) {
        db.saveHistoryAuction(auction);
        historyList.add(auction);
    }

    public void removeHistoryAuction(Auction auction) {
        historyList.remove(auction);
    }


    public void addAuction(Auction auction) {
        db.saveAuction(auction);
        auctions.add(auction);
    }

    public void removeAuction(Auction auction) {
        db.removeAuction(auction.getId());
        auctions.remove(auction);
    }

    public static List<Auction> getAuctionsByMaterial(Material material) {
        return auctions.stream()
                .filter(auction -> auction.getAuctionedItem().getType() == material)
                .collect(Collectors.toList());
    }
}
