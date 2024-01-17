package me.panda.cosmicauctionhouse.engine.gui;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import me.panda.cosmicauctionhouse.engine.AuctionHouse;
import me.panda.cosmicauctionhouse.engine.bp.Auction;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouseCommand implements CommandExecutor {


    private final CosmicAuctionHouse plugin;

    public AuctionHouseCommand(CosmicAuctionHouse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("ah") && sender instanceof Player) {
            Player player = (Player) sender;

            // Create the GUI
            openAuctionGUI(player);

            return true;
        }
        return false;
    }

    private void openAuctionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9 * 3, "Auction House");

        // Get the list of auctions
        AuctionHouse auctionHouse = plugin.getAuctionHouse();

        List<Auction> auctions = auctionHouse.getAuctions();

        // Add auction items to the GUI
        for (Auction auction : auctions) {
            ItemStack auctionItem = auction.getAuctionedItem().clone();
            ItemMeta itemMeta = auctionItem.getItemMeta();

            // Add price and seller to the lore
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add("Price: " + auction.getPrice());
            lore.add("Seller: " + auction.getSeller().getName());

            itemMeta.setLore(lore);
            auctionItem.setItemMeta(itemMeta);

            gui.addItem(auctionItem);
        }

        player.openInventory(gui);
    }


}
