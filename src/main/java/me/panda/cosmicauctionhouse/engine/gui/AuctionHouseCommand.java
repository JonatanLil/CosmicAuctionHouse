package me.panda.cosmicauctionhouse.engine.gui;

import com.sun.tools.javac.jvm.Items;
import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import me.panda.cosmicauctionhouse.engine.AuctionHouse;
import me.panda.cosmicauctionhouse.engine.bp.Auction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AuctionHouseCommand implements CommandExecutor {


    private final CosmicAuctionHouse plugin;
    private AuctionHouse auctionHouse;

    public AuctionHouseCommand(CosmicAuctionHouse plugin) {
        this.plugin = plugin;
        auctionHouse = plugin.getAuctionHouse();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            System.out.println("Only players can use this command");
            return false;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("ah")) {
            if (args.length == 3 && args[0].equals("sell")) {
                int price = 0;
                int amount = 1;
                ItemStack item;

                try {
                    price = Integer.parseInt(args[1]);
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("/ah sell <price> <amount>");
                    return true;
                }

                item = player.getItemInHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage("You need to hold an item in your main hand!");
                    return true;
                }

                // Check if the player has enough items in hand
                int heldAmount = item.getAmount();
                if (heldAmount < amount) {
                    player.sendMessage("You don't have enough items in your hand.");
                    return true;
                }

                // Create a single auction with multiple items
                ItemStack auctionItem = item.clone();
                auctionItem.setAmount(amount);

                Auction auction = new Auction(auctionItem, price, player, -1); //IF initialTime is less then 0 then 24 hours
                // Add the auction to your AuctionHouse or process it accordingly
                auctionHouse.addAuction(auction);

                // Remove the sold items from the player's hand
                item.setAmount(heldAmount - amount);

                player.sendMessage("Auction created for " + amount + " item(s) at the price of " + price + " each.");
            } else if (args.length == 0) {
                openAuctionGUI(player);
            }
            return true;
        }
        return false;
    }

    private void openAuctionGUI(Player player) {
        // Create a map to store auctions by material
        Map<Material, List<Auction>> auctionsByMaterial = new HashMap<>();

        // Get the list of auctions
        List<Auction> auctions = auctionHouse.getAuctions();

        if (auctions == null || auctions.isEmpty()) {
            player.sendMessage("There are no auctions available at the moment.");
            return;
        }

        // Organize auctions by material
        for (Auction auction : auctions) {
            if (auction.getAuctionedItem() == null || auction.getAuctionedItem().getType() == Material.AIR) {
                // Skip auctions with invalid items
                continue;
            }

            Material material = auction.getAuctionedItem().getType();
            auctionsByMaterial.computeIfAbsent(material, k -> new ArrayList<>()).add(auction);
        }

        // Create the main GUI
        Inventory mainGUI = Bukkit.createInventory(null, 9 * 3, "Cosmic AuctionHouse");

        // Add categories to the main GUI
        for (Material material : auctionsByMaterial.keySet()) {
            ItemStack categoryItem = new ItemStack(material);
            ItemMeta categoryMeta = categoryItem.getItemMeta();

            if (categoryMeta == null) {
                continue;
            }

            categoryMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD +  StringUtils.capitalize(material.name().toLowerCase()));
            categoryItem.setItemMeta(categoryMeta);

            mainGUI.addItem(categoryItem);
        }

        // Open the main GUI
        player.openInventory(mainGUI);

        // Implement the InventoryClickListener for the main GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(mainGUI)) {
                    event.setCancelled(true); // Cancel the click event

                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        // Open sub-GUI for the selected category
                        Material selectedMaterial = event.getCurrentItem().getType();
                        openSubAuctionGUI(player, selectedMaterial);
                    }
                }
            }
        }, plugin);


    }


    private void openSubAuctionGUI(Player player, Material material) {
        Inventory subGUI = Bukkit.createInventory(null, 9 * 3, "Auction House - " + StringUtils.capitalize(material.name().toLowerCase()));

        // Get auctions for the selected material
        List<Auction> materialAuctions = auctionHouse.getAuctionsByMaterial(material);

        if (materialAuctions == null || materialAuctions.isEmpty()) {
            player.sendMessage("There are no auctions available for " + StringUtils.capitalize(material.name().toLowerCase()) + " at the moment.");
            return;
        }

        // Add auction items to the sub-GUI
        for (Auction auction : materialAuctions) {
            ItemStack displayItem = auction.getDisplayItem();
            subGUI.addItem(displayItem);
        }

        // Implement the InventoryClickListener for the sub-GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(subGUI)) {
                    event.setCancelled(true); // Cancel the click event

                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        // Handle item purchase
                        handlePurchase(player, event.getCurrentItem(), materialAuctions);
                    }
                }
            }
        }, plugin);

        // Open the sub-GUI
        player.openInventory(subGUI);
    }

    private void handlePurchase(Player player, ItemStack clickedItem, List<Auction> materialAuctions) {

        // Print details for debugging
        System.out.println("Clicked Item: " + clickedItem);
        System.out.println("Material Auctions Before Filtering: " + materialAuctions);



        Auction selectedAuction = null;
        for (Auction auction : materialAuctions) {
            if (Objects.equals(auction.getDisplayItem(), clickedItem)) {
                selectedAuction = auction;
                break; // No need to continue checking once a match is found
            }
        }

        // Print details for debugging
        System.out.println("Material Auctions After Filtering: " + materialAuctions);
        System.out.println("THE SELECTED AUCTION: " + selectedAuction);

        if (selectedAuction != null && !selectedAuction.isBought() && !selectedAuction.isExpired()) {
            // Mark the auction as bought
            selectedAuction.setBought(true);
            // Set the buyer
            selectedAuction.setBuyer(player);

            // Notify the player
            player.sendMessage(ChatColor.GREEN + "You bought " + ChatColor.YELLOW + selectedAuction.getAuctionedItem().getType() +
                    ChatColor.GREEN + " for " + ChatColor.YELLOW + selectedAuction.getPrice());

            // Implement any additional logic here (e.g., deducting money, handling items, etc.)

            // Add the purchased item to the player's inventory
            player.getInventory().addItem(selectedAuction.getAuctionedItem());
            // ADD HOOK INTO VAULT
            // Remove the item from the sub-GUI
            clickedItem.setAmount(0);
            // Remove the item from the auction list
            auctionHouse.removeAuction(selectedAuction);
        } else {
            player.sendMessage(ChatColor.RED + "This item is no longer available for purchase.");
        }
    }


}
