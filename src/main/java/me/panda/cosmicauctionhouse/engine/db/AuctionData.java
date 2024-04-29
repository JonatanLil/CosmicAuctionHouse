package me.panda.cosmicauctionhouse.engine.db;

import me.panda.cosmicauctionhouse.engine.bp.Auction;
import me.panda.cosmicauctionhouse.engine.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionData {

    private static final Logger logger = LoggerFactory.getLogger(AuctionData.class);

    private Connection connection;
    private final JavaPlugin plugin;

    public AuctionData(JavaPlugin plugin, String dbName) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/" + dbName);
            createAuctionsTable();
            createHistoryAuctionsTable();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Problem regarding data table or connection to db", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing the connection", e);
        }
    }

    private void createAuctionsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Auctions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "itemToAuction TEXT, "
                + "price INTEGER, "
                + "seller VARCHAR(255), "
                + "bought BOOLEAN, "
                + "buyer VARCHAR(255),"
                + "timeLeft INTEGER"
                + ")";

        try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error creating table", e);
        }
    }

    private void createHistoryAuctionsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS HistoryAuctions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "itemToAuction TEXT, "
                + "price INTEGER, "
                + "seller VARCHAR(255), "
                + "buyer VARCHAR(255)"
                + ")";

        try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error creating table", e);
        }
    }

    public List<Auction> getHistoryAuctions() {
        List<Auction> historyAuctions = new ArrayList<>();

        String selectSQL = "SELECT * FROM HistoryAuctions";

        try (PreparedStatement statement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = statement.executeQuery()) {
            System.out.println("DEBUGGING HISTORY AUCTIONS: FETCHING");
            while (resultSet.next()) {
                int id = resultSet.getInt("id"); // Get the ID from the database
                String itemToAuctionData = resultSet.getString("itemToAuction");
                int price = resultSet.getInt("price");
                String sellerName = resultSet.getString("seller");
                String buyerName = resultSet.getString("buyer");

                ItemStack itemToAuction = ItemUtils.deserializeItemStack(itemToAuctionData);
                OfflinePlayer seller = Bukkit.getOfflinePlayerIfCached(sellerName);
                OfflinePlayer buyer = (buyerName != null) ? Bukkit.getOfflinePlayerIfCached(buyerName) : null;

                Auction auction = new Auction(itemToAuction, price, seller);
                auction.setBuyer(buyer);

                // Set the ID of the history auction retrieved from the database
                auction.setId(id);

                historyAuctions.add(auction);


                System.out.println(auction);
            }

        } catch (SQLException e) {
            logger.error("Error retrieving history auctions from the database", e);
        }

        return historyAuctions;
    }


    public List<Auction> getAuctions() {
        List<Auction> auctions = new ArrayList<>();

        String selectSQL = "SELECT * FROM Auctions";

        try (PreparedStatement statement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = statement.executeQuery()) {
            System.out.println("DEBUGGING REGULAR AUCTIONS: FETCHING");
            while (resultSet.next()) {
                int id = resultSet.getInt("id"); // Get the ID from the database
                String itemToAuctionData = resultSet.getString("itemToAuction");
                int price = resultSet.getInt("price");
                String sellerName = resultSet.getString("seller");
                boolean bought = resultSet.getBoolean("bought");
                String buyerName = resultSet.getString("buyer");

                ItemStack itemToAuction = ItemUtils.deserializeItemStack(itemToAuctionData);

                //OfflinePlayer seller = plugin.getServer().getPlayerExact(sellerName);
                OfflinePlayer seller = Bukkit.getOfflinePlayerIfCached(sellerName);
                OfflinePlayer buyer = (buyerName != null) ? Bukkit.getOfflinePlayerIfCached(buyerName) : null;

                long timeLeft = resultSet.getLong("timeLeft");

                Auction auction = new Auction(itemToAuction, price, seller, timeLeft);
                auction.setBuyer(buyer);
                auction.setBought(bought);
                auction.setTimeLeft(timeLeft);

                // Set the ID of the auction
                auction.setId(id);

                auctions.add(auction);

                System.out.println(auction);
            }

        } catch (SQLException e) {
            logger.error("Error retrieving auctions from the database", e);
        }

        return auctions;
    }

    /* OLD
    public void saveHistoryAuctions(List<Auction> auctions) {
        String insertSQL = "INSERT INTO HistoryAuctions (id, itemToAuction, price, seller, buyer) VALUES (?, ?, ?, ?, ?) \n" +
                "ON CONFLICT(id) DO UPDATE SET itemToAuction=excluded.itemToAuction, price=excluded.price, seller=excluded.seller, buyer=excluded.buyer";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("DEBUGGING HISTORY AUCTIONS: SAVING");
            for (Auction auction : auctions) {
                System.out.println(auction);
                insertStatement.setInt(1, auction.getId()); // Set the ID
                insertStatement.setString(2, ItemUtils.serializeItemStack(auction.getAuctionedItem()));
                insertStatement.setInt(3, auction.getPrice());
                insertStatement.setString(4, auction.getSeller().getName());
                insertStatement.setString(5, (auction.getBuyer() != null) ? auction.getBuyer().getName() : null);

                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.warn("Problem saving the history auctions", e);
        }
    }


    public void saveAuctions(List<Auction> auctions) {
        String insertSQL = "INSERT INTO Auctions (id, itemToAuction, price, seller, bought, buyer, timeleft) VALUES (?, ?, ?, ?, ?, ?, ?) \n" +
                "ON CONFLICT(id) DO UPDATE SET itemToAuction=excluded.itemToAuction, price=excluded.price, seller=excluded.seller, bought=excluded.bought, buyer=excluded.buyer, timeleft=excluded.timeleft";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("DEBUGGING REGULAR AUCTIONS: SAVING");
            for (Auction auction : auctions) {
                System.out.println(auction.toString());
                insertStatement.setInt(1, auction.getId()); // Set the ID
                insertStatement.setString(2, ItemUtils.serializeItemStack(auction.getAuctionedItem()));
                insertStatement.setInt(3, auction.getPrice());
                insertStatement.setString(4, auction.getSeller().getName());
                insertStatement.setBoolean(5, auction.isBought());
                insertStatement.setString(6, (auction.getBuyer() != null) ? auction.getBuyer().getName() : null);
                insertStatement.setLong(7, auction.getTimeLeft());

                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            logger.warn("Problem saving existing auctions", e);
        }
    }
     */

    public void removeAuction(int auctionId) {
        String deleteSQL = "DELETE FROM Auctions WHERE id = ?";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL)) {
            deleteStatement.setInt(1, auctionId);
            int rowsAffected = deleteStatement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No auction with ID " + auctionId + " found in the database.");
            } else {
                System.out.println("Auction with ID " + auctionId + " removed from the database.");
            }
        } catch (SQLException e) {
            logger.warn("Problem removing auction", e);
        }
    }

    public void saveAuction(Auction auction) {
        String insertSQL = "INSERT INTO Auctions (id, itemToAuction, price, seller, bought, buyer, timeleft) VALUES (?, ?, ?, ?, ?, ?, ?) \n" +
                "ON CONFLICT(id) DO UPDATE SET itemToAuction=excluded.itemToAuction, price=excluded.price, seller=excluded.seller, bought=excluded.bought, buyer=excluded.buyer, timeleft=excluded.timeleft";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setInt(1, auction.getId()); // Set the ID
            insertStatement.setString(2, ItemUtils.serializeItemStack(auction.getAuctionedItem()));
            insertStatement.setInt(3, auction.getPrice());
            insertStatement.setString(4, auction.getSeller().getName());
            insertStatement.setBoolean(5, auction.isBought());
            insertStatement.setString(6, (auction.getBuyer() != null) ? auction.getBuyer().getName() : null);
            insertStatement.setLong(7, auction.getTimeLeft());

            insertStatement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Problem saving auction", e);
        }
    }

    public void saveHistoryAuction(Auction auction) {
        String insertSQL = "INSERT INTO HistoryAuctions (id, itemToAuction, price, seller, buyer) VALUES (?, ?, ?, ?, ?) \n" +
                "ON CONFLICT(id) DO UPDATE SET itemToAuction=excluded.itemToAuction, price=excluded.price, seller=excluded.seller, buyer=excluded.buyer";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setInt(1, auction.getId()); // Set the ID
            insertStatement.setString(2, ItemUtils.serializeItemStack(auction.getAuctionedItem()));
            insertStatement.setInt(3, auction.getPrice());
            insertStatement.setString(4, auction.getSeller().getName());
            insertStatement.setString(5, (auction.getBuyer() != null) ? auction.getBuyer().getName() : null);

            insertStatement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Problem saving the history auction", e);
        }
    }


}
