package me.panda.cosmicauctionhouse;

import me.panda.cosmicauctionhouse.engine.AuctionHouse;
import me.panda.cosmicauctionhouse.engine.gui.AuctionHouseCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CosmicAuctionHouse extends JavaPlugin {

    public static JavaPlugin INSTANCE;
    private AuctionHouse auctionHouse;

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Create data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Config stuff
        saveDefaultConfig();
        reloadConfig();

        try {
            auctionHouse = new AuctionHouse();
        } catch (Exception e) {
            this.getSLF4JLogger().warn("Problem starting the ah!");
        }

        Objects.requireNonNull(getCommand("ah")).setExecutor(new AuctionHouseCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            auctionHouse.closeConnection();
        } catch (Exception e) {
            getSLF4JLogger().warn("Problem disabling the auction house");
        }
    }

    public AuctionHouse getAuctionHouse() {
        return auctionHouse;
    }
}
