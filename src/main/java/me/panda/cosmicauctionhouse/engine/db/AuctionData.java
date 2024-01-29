package me.panda.cosmicauctionhouse.engine.db;

import me.panda.cosmicauctionhouse.engine.bp.Auction;
import me.panda.cosmicauctionhouse.engine.utils.ItemUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionData {

    private static final Logger logger = LoggerFactory.getLogger(AuctionData.class);

    private Connection connection;
    private final JavaPlugin plugin;

    public AuctionData(JavaPlugin plugin, String dbName) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/" + dbName);
            createTable();
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

    private void createTable() {
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

    public List<Auction> getAuctions() {
        List<Auction> auctions = new ArrayList<>();

        String selectSQL = "SELECT * FROM Auctions";

        try (PreparedStatement statement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String itemToAuctionData = resultSet.getString("itemToAuction");
                int price = resultSet.getInt("price");
                String sellerName = resultSet.getString("seller");
                boolean bought = resultSet.getBoolean("bought");
                String buyerName = resultSet.getString("buyer");

                ItemStack itemToAuction = ItemUtils.deserializeItemStack(itemToAuctionData);

                OfflinePlayer seller = plugin.getServer().getPlayerExact(sellerName);
                OfflinePlayer buyer = (buyerName != null) ? plugin.getServer().getPlayerExact(buyerName) : null;

                long timeLeft = resultSet.getLong("timeLeft");

                Auction auction = new Auction(itemToAuction, price, seller, timeLeft);
                auction.setBuyer(buyer);
                auction.setBought(bought);
                auctions.add(auction);
                auction.setTimeLeft(timeLeft);

            }

        } catch (SQLException e) {
            logger.error("Error retrieving auctions from the database", e);
        }

        return auctions;
    }

    public void saveAuctions(List<Auction> auctions) {
        // Clear existing data
        String deleteSQL = "DELETE FROM Auctions";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL)) {
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Problem deleting existing auctions", e);
        }

        // Insert new data
        String insertSQL = "INSERT INTO Auctions (itemToAuction, price, seller, bought, buyer, timeLeft) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
            for (Auction auction : auctions) {
                insertStatement.setString(1, ItemUtils.serializeItemStack(auction.getAuctionedItem()));
                insertStatement.setInt(2, auction.getPrice());
                insertStatement.setString(3, auction.getSeller().getName());
                insertStatement.setBoolean(4, auction.isBought());
                insertStatement.setString(5, (auction.getBuyer() != null) ? auction.getBuyer().getName() : null);
                insertStatement.setLong(6, auction.getTimeLeft()); // Add the remaining time

                insertStatement.addBatch();
            }

            insertStatement.executeBatch();

        } catch (SQLException e) {
            logger.warn("Problem saving existing auctions", e);
        }
    }
}
