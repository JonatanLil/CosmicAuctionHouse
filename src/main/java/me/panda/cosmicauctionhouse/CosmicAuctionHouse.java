package me.panda.cosmicauctionhouse;

import me.panda.cosmicauctionhouse.engine.AuctionHouse;
import me.panda.cosmicauctionhouse.engine.events.listener.AuctionPurchaseListener;
import me.panda.cosmicauctionhouse.engine.gui.AuctionHouseCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CosmicAuctionHouse extends JavaPlugin {

    public static JavaPlugin INSTANCE;
    private AuctionHouse auctionHouse;
    private static Economy econ = null;


    @Override
    public void onEnable() {
        INSTANCE = this;
        // Create data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        logLoadedPlugins();

        if (!setupEconomy() ) {
            getLogger().severe(" - Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            auctionHouse = new AuctionHouse();
        } catch (Exception e) {
            this.getSLF4JLogger().warn("Problem starting the ah!");
        }

        Objects.requireNonNull(getCommand("ah")).setExecutor(new AuctionHouseCommand(this));

        getServer().getPluginManager().registerEvents(new AuctionPurchaseListener(this), this);

        // Config stuff
        saveDefaultConfig();
        reloadConfig();
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

    public void logLoadedPlugins() {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Plugin plugin : pluginManager.getPlugins()) {
            getLogger().info("Loaded plugin: " + plugin.getName());
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No economy provider found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        econ = rsp.getProvider();
        getLogger().info("Economy provider found.");
        return true;
    }


    public static Economy getEconomy() {
        return econ;
    }

}
