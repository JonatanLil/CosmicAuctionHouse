package me.panda.cosmicauctionhouse.engine.gui;

import me.panda.cosmicauctionhouse.CosmicAuctionHouse;
import me.panda.cosmicauctionhouse.engine.AuctionHouse;
import me.panda.cosmicauctionhouse.engine.bp.Auction;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.panda.cosmicauctionhouse.CosmicAuctionHouse.getEconomy;

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
            System.out.println("ᴏɴʟʏ ᴘʟᴀʏᴇʀs ᴄᴀɴ ᴜsᴇ ᴛʜɪs ᴄᴏᴍᴍᴀɴᴅ!");
            return false;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("ah")) {
            if (args.length == 0) {
                openAuctionGUI(player);
                return true; // Return true here to indicate that the command was handled successfully
            } else if (args.length == 3 && args[0].equals("sell")) {
                int price;
                int amount;
                ItemStack item;

                try {
                    price = Integer.parseInt(args[1]);
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("/ᴀʜ sᴇʟʟ <ᴘʀɪᴄᴇ> <ᴀᴍᴏᴜɴᴛ>");
                    return true;
                }

                item = player.getItemInHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage("ʏᴏᴜ ɴᴇᴇᴅ ᴛᴏ ʜᴏʟᴅ ᴀɴ ɪᴛᴇᴍ ɪɴ ʏᴏᴜʀ ᴍᴀɪɴ ʜᴀɴᴅ!");
                    return true;
                }

                // Check if the player has enough items in hand
                int heldAmount = item.getAmount();
                if (heldAmount < amount) {
                    player.sendMessage("ʏᴏᴜ ᴅᴏɴ'ᴛ ʜᴀᴠᴇ ᴇɴᴏᴜɢʜ ɪᴛᴇᴍs ɪɴ ʏᴏᴜʀ ʜᴀɴᴅ.");
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

                player.sendMessage("ᴀᴜᴄᴛɪᴏɴ ᴄʀᴇᴀᴛᴇᴅ ғᴏʀ " + amount + " ɪᴛᴇᴍ(s) ᴀᴛ ᴛʜᴇ ᴘʀɪᴄᴇ ᴏғ " + price + " ᴇᴀᴄʜ.");
            } else {
                player.sendMessage("Invalid usage. Correct usage: /ᴀʜ sᴇʟʟ <ᴘʀɪᴄᴇ> <ᴀᴍᴏᴜɴᴛ>");
                return true;
            }
        }
        return false;
    }


    private void openAuctionGUI(Player player) {
        // Create a map to store auctions by material
        Map<Material, List<Auction>> auctionsByMaterial = new HashMap<>();

        // Get the list of auctions
        List<Auction> auctions = auctionHouse.getAuctions();

        /*
        if (auctions == null || auctions.isEmpty()) {
            player.sendMessage("ᴛʜᴇʀᴇ ᴀʀᴇ ɴᴏ ᴀᴜᴄᴛɪᴏɴs ᴀᴠᴀɪʟᴀʙʟᴇ ᴀᴛ ᴛʜᴇ ᴍᴏᴍᴇɴᴛ.");
            return;
        }
         */

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
        Inventory mainGUI = Bukkit.createInventory(null, 9 * 6, "ᴄᴏsᴍɪᴄ ᴀᴜᴄᴛɪᴏɴʜᴏᴜsᴇ");

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

        ItemStack nextPageItem = new ItemStack(Material.PAPER);
        ItemMeta nextPageMeta = nextPageItem.getItemMeta();
        nextPageMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ɴᴇxᴛ ᴘᴀɢᴇ ->");
        nextPageItem.setItemMeta(nextPageMeta);
        mainGUI.setItem(9 * 6 - 4, nextPageItem);

        ItemStack prevPageItem = new ItemStack(Material.PAPER);
        ItemMeta prevPageMeta = prevPageItem.getItemMeta();
        prevPageMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "<- ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ");
        prevPageItem.setItemMeta(prevPageMeta);
        mainGUI.setItem(9 * 6 - 6, prevPageItem);

        ItemStack historyAuctionItem = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta historyMeta = historyAuctionItem.getItemMeta();
        historyMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ᴀᴜᴄᴛɪᴏɴ ʜɪsᴛᴏʀʏ");
        List<String> historyMetaLore = new ArrayList<>(); // Initialize the list
        historyMetaLore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ʜᴇʀᴇ ᴛᴏ ᴠɪᴇᴡ ᴀʟʟ ʀᴇᴄᴇɴᴛʟʏ sᴏʟᴅ ᴀᴜᴄᴛɪᴏɴs.");
        historyMeta.setLore(historyMetaLore);
        historyAuctionItem.setItemMeta(historyMeta);
        mainGUI.setItem(9 * 6 - 7, historyAuctionItem);

        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "ɢᴜɪᴅᴇ");
        List<String> helpMetaLore = new ArrayList<>(); // Initialize the list
        helpMetaLore.add(ChatColor.GRAY + "ᴛʜɪs ɪs ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜsᴇ, ʜᴇʀᴇ ʏᴏᴜ ᴄᴀɴ");
        helpMetaLore.add(ChatColor.GRAY + "ʟɪsᴛ ɪᴛᴇᴍs ғᴏʀ sᴀʟᴇ ᴀɴᴅ ᴘᴜʀᴄʜᴀsᴇ ɪᴛᴇᴍs");
        helpMetaLore.add(ChatColor.GRAY + "ᴛʜᴀᴛ ᴏᴛʜᴇʀs ʜᴀᴠᴇ ʟɪsᴛᴇᴅ ғᴏʀ sᴀʟᴇ.");
        helpMetaLore.add("");
        helpMetaLore.add(ChatColor.GRAY + "ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ ɪs ᴀʟsᴏ ᴀ ɢʀᴇᴀᴛ ᴡᴀʏ ᴛᴏ ᴍᴀᴋᴇ");
        helpMetaLore.add(ChatColor.GRAY + "ᴍᴏɴᴇʏ ʙʏ sᴇʟʟɪɴɢ ғᴀʀᴍᴀʙʟᴇ ɪᴛᴇᴍs ᴏᴛʜᴇʀ");
        helpMetaLore.add(ChatColor.GRAY + "ᴘʟᴀʏᴇʀs ᴍᴀʏ ʙᴇ ɪɴᴛᴇʀᴇsᴛᴇᴅ ɪɴ ʙᴜʏɪɴɢ.");
        helpMetaLore.add("");
        helpMetaLore.add(ChatColor.GRAY + "ᴀʟʟ ᴀᴜᴄᴛɪᴏɴs ʟᴀsᴛ ғᴏʀ ᴀ ᴍᴀx ᴏғ " + ChatColor.YELLOW + "24 ʜᴏᴜʀs");
        helpMetaLore.add(ChatColor.GRAY + "ᴜɴsᴏʟᴅ ɪᴛᴇᴍs ᴄᴀɴ ʙᴇ found ɪɴ ʏᴏᴜʀ " + ChatColor.YELLOW + "ᴄᴏʟʟᴇᴄᴛɪᴏɴ ʙɪɴ" + ChatColor.GRAY + ".");
        helpMetaLore.add("");
        helpMetaLore.add(ChatColor.GRAY + "ʏᴏᴜ ᴄᴀɴ ᴀʟsᴏ ʟɪsᴛ ɪᴛᴇᴍs ᴜᴘ ғᴏʀ ᴀᴜᴄᴛɪᴏɴ");
        helpMetaLore.add(ChatColor.GRAY + "ᴡʜᴇʀᴇ ᴘʟᴀʏᴇʀs ᴄᴀɴ ᴏᴜᴛʙɪᴅ ᴇᴀᴄʜ ᴏᴛʜᴇʀ ᴛᴏ");
        helpMetaLore.add(ChatColor.GRAY + "ʙᴇ ᴛʜᴇ ʜɪɢʜᴇsᴛ ʙɪᴅᴅᴇʀ ᴜsɪɴɢ " + ChatColor.YELLOW + " /ʙɪᴅs" + ChatColor.GRAY + ".");
        helpMetaLore.add("");
        helpMetaLore.add(ChatColor.GRAY + "ᴛᴏ ʟɪsᴛ ᴀɴ ɪᴛᴇᴍ ᴏɴ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜsᴇ, ᴊᴜsᴛ ʜᴏʟᴅ");
        helpMetaLore.add(ChatColor.GRAY + "ᴛʜᴇ ɪᴛᴇᴍ ɪɴ ʏᴏᴜʀ ʜᴀɴᴅ ᴀɴᴅ ᴛʏᴘᴇ " + ChatColor.YELLOW + "/ᴀʜ sᴇʟʟ <ᴘʀɪᴄᴇ>");
        helpMetaLore.add("");
        helpMetaLore.add(ChatColor.GRAY + "ᴛᴏ ʟɪsᴛ ᴀɴ ɪᴛᴇᴍ ᴀs ᴀ ʙɪᴅ ᴀᴜᴄᴛɪᴏɴ, ᴜsᴇ");
        helpMetaLore.add(ChatColor.YELLOW + "/ᴀʜ sᴛᴀʀᴛʙɪᴅ <sᴛᴀʀᴛPʀɪᴄᴇ> <ᴍɪɴɪɴᴄʀᴇᴍᴇɴᴛ>");
        helpMetaLore.add("");
        helpMetaLore.add(ChatColor.GRAY + "ғᴏʀ ᴍᴏʀᴇ ɪɴғᴏʀᴍᴀᴛɪᴏɴ ᴜsᴇ /ᴀʜ ʜᴇʟᴘ");

        helpMeta.setLore(helpMetaLore);
        helpItem.setItemMeta(helpMeta);
        mainGUI.setItem(9 * 6 - 1, helpItem);

        ItemStack collectionItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta collectionMeta = collectionItem.getItemMeta();
        collectionMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "ᴄᴏʟʟᴇᴄᴛɪᴏɴ ʙɪɴ");
        List<String> collectionItemLore = new ArrayList<>(); // Initialize the list
        collectionItemLore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ʜᴇʀᴇ ᴛᴏ ᴠɪᴇᴡ ᴀɴᴅ ᴄᴏʟʟᴇᴄᴛ ᴀʟʟ ᴏғ ᴛʜᴇ");
        collectionItemLore.add(ChatColor.GRAY + "ɪᴛᴇᴍs ʏᴏᴜ ʜᴀᴠᴇ ᴄᴀɴᴄᴇʟʟᴇᴅ ᴏʀ ʜᴀᴠᴇ ᴇxᴘɪʀᴇᴅ.");
        collectionItemLore.add("");
        collectionItemLore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "0 ɪᴛᴇᴍ(s)");
        collectionMeta.setLore(collectionItemLore);
        collectionItem.setItemMeta(collectionMeta);
        mainGUI.setItem(9 * 6 - 8, collectionItem);

        ItemStack yourAuctionsItem = new ItemStack(Material.DIAMOND);
        ItemMeta yourAuctionsMeta = yourAuctionsItem.getItemMeta();
        yourAuctionsMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ʏᴏᴜʀ ᴀᴜᴄᴛɪᴏɴs");
        List<String> yourAuctionsMetaLore = new ArrayList<>(); // Initialize the list
        yourAuctionsMetaLore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ʜᴇʀᴇ ᴛᴏ ᴠɪᴇᴡ ᴀʟʟ ᴏғ ᴛʜᴇ ɪᴛᴇᴍs ʏᴏᴜ");
        yourAuctionsMetaLore.add(ChatColor.GRAY + "ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ sᴇʟʟɪɴɢ ᴏɴ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ.");
        yourAuctionsMetaLore.add("");
        yourAuctionsMetaLore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "0 ɪᴛᴇᴍ(s)");
        yourAuctionsMeta.setLore(yourAuctionsMetaLore);
        yourAuctionsItem.setItemMeta(yourAuctionsMeta);
        mainGUI.setItem(9 * 6 - 9, yourAuctionsItem);

        ItemStack refreshAuctionsItem = new ItemStack(Material.CHEST);
        ItemMeta refreshAuctionsItemMeta = refreshAuctionsItem.getItemMeta();
        refreshAuctionsItemMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ʀᴇғʀᴇsʜ ᴀᴜᴄᴛɪᴏɴs");
        List<String> refreshAuctionLore = new ArrayList<>(); // Initialize the list
        refreshAuctionLore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ʜᴇʀᴇ ᴛᴏ ᴠɪᴇᴡ ᴀʟʟ ᴏғ ᴛʜᴇ ɪᴛᴇᴍs ʏᴏᴜ");
        refreshAuctionLore.add(ChatColor.GRAY + "ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ sᴇʟʟɪɴɢ ᴏɴ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ.");
        refreshAuctionLore.add("");
        refreshAuctionLore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "0 ɪᴛᴇᴍ(s)");
        refreshAuctionsItemMeta.setLore(refreshAuctionLore);
        refreshAuctionsItem.setItemMeta(refreshAuctionsItemMeta);
        mainGUI.setItem(9 * 6 - 5, refreshAuctionsItem);

        ItemStack filterItem = new ItemStack(Material.ANVIL);
        ItemMeta filterItemMeta = filterItem.getItemMeta();
        filterItemMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ғɪʟᴛᴇʀ sʏsᴛᴇᴍ");
        List<String> filterItemLore = new ArrayList<>(); // Initialize the list
        filterItemLore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ʜᴇʀᴇ ᴛᴏ ᴠɪᴇᴡ ᴀʟʟ ᴏғ ᴛʜᴇ ɪᴛᴇᴍs ʏᴏᴜ");
        filterItemLore.add(ChatColor.GRAY + "ᴀʀᴇ ᴄᴜʀʀᴇɴᴛʟʏ sᴇʟʟɪɴɢ ᴏɴ ᴛʜᴇ ᴀᴜᴄᴛɪᴏɴ.");
        filterItemMeta.setLore(filterItemLore);
        filterItem.setItemMeta(filterItemMeta);
        mainGUI.setItem(9 * 6 - 3, filterItem);

        ItemStack categoryItem = new ItemStack(Material.CHEST_MINECART);
        ItemMeta categoryItemMeta = categoryItem.getItemMeta();
        categoryItemMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ᴄᴀᴛᴇɢᴏʀʏ ᴠɪᴇᴡ");
        List<String> categoryItemLore = new ArrayList<>(); // Initialize the list
        categoryItemLore.add(ChatColor.GRAY + "ᴄʟɪᴄᴋ ʜᴇʀᴇ ᴛᴏ ᴠɪᴇᴡ ᴀʟʟ ᴄᴜʀʀᴇɴᴛ");
        categoryItemLore.add(ChatColor.GRAY + "ᴀᴜᴄᴛɪᴏɴ ɪᴛᴇᴍ ᴄᴀᴛᴇɢᴏʀɪᴇs!");
        categoryItemMeta.setLore(categoryItemLore);
        categoryItem.setItemMeta(categoryItemMeta);
        mainGUI.setItem(9 * 6 - 2, categoryItem);

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
                        if (event.getCurrentItem().getType().equals(Material.PAPER)) {
                            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.BOLD + "ɴᴇxᴛ ᴘᴀɢᴇ ->")) {
                                // Open next page
                                // Implement logic to calculate next page auctions and open the sub-GUI
                            } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.BOLD + "<- ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ")) {
                                // Open previous page
                                // Implement logic to calculate previous page auctions and open the sub-GUI
                            }  else {
                                Material selectedMaterial = event.getCurrentItem().getType();
                                openSubAuctionGUI(player, selectedMaterial);
                            }
                        } else if (event.getCurrentItem().getType().equals(Material.KNOWLEDGE_BOOK) && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.BOLD + "ᴀᴜᴄᴛɪᴏɴ ʜɪsᴛᴏʀʏ")) {
                            openHistoryAuctions(player);
                        } else if (event.getCurrentItem().getType().equals(Material.DIAMOND) && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "" + ChatColor.BOLD + "ʏᴏᴜʀ ᴀᴜᴄᴛɪᴏɴs")) {
                            openYourAuctions(player);
                        } else {
                            Material selectedMaterial = event.getCurrentItem().getType();
                            openSubAuctionGUI(player, selectedMaterial);
                        }
                    }
                }
            }
        }, plugin);

    }

    private void openYourAuctions(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9*6, "auction house - your auctions");
        List<Auction> auctions = auctionHouse.getAuctions();
        List<Auction> playerAuctions = new ArrayList<>();
        for (Auction auction : auctions) {
            if (auction.getSeller().getUniqueId().equals(player.getUniqueId())) {
                playerAuctions.add(auction);
                inv.addItem(auction.getDisplayItem());
            }
        }
        player.openInventory(inv);
    }

    private void openSubAuctionGUI(Player player, Material material) {
        Inventory subGUI = Bukkit.createInventory(null, 9 * 3, "ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜsᴇ - " + StringUtils.capitalize(material.name().toLowerCase()));
        Map<Integer, Auction> slotAuctionMap = new HashMap<>();

        // Get auctions for the selected material
        List<Auction> materialAuctions = auctionHouse.getAuctionsByMaterial(material);

        if (materialAuctions == null || materialAuctions.isEmpty()) {
            player.sendMessage("ᴛʜᴇʀᴇ ᴀʀᴇ ɴᴏ ᴀᴜᴄᴛɪᴏɴ ᴀᴠᴀɪʟᴀʙʟᴇ ғᴏʀ " + StringUtils.capitalize(material.name().toLowerCase()) + " ᴀᴛ ᴛʜᴇ ᴍᴏᴍᴇɴᴛ.");
            return;
        }

        // Add auction items to the sub-GUI
        int i = 0;
        for (Auction auction : materialAuctions) {
            ItemStack displayItem = auction.getDisplayItem();
            subGUI.addItem(displayItem);
            slotAuctionMap.put(i, auction); // Store the auction information with the slot number
            i++;
        }

        //TESTING
        long period = 20L;

        new BukkitRunnable() {

            @Override
            public void run() {
                for (int slot : slotAuctionMap.keySet()) {
                    Auction auction = slotAuctionMap.get(slot);
                    if (!auction.isBought()) {
                        auction.updateDisplayItemLore(auction.getTimeLeft(), subGUI, slot);
                    }
                }
                //player.sendMessage("Updated Inventory");
            }
        }.runTaskTimer(CosmicAuctionHouse.INSTANCE, 0, period);


        // Implement the InventoryClickListener for the sub-GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(subGUI)) {
                    event.setCancelled(true); // Cancel the click event

                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        int clickedSlot = event.getRawSlot();
                        if (slotAuctionMap.containsKey(clickedSlot)) {
                            // Handle item purchase using the auction information
                            handlePurchase(player, event.getCurrentItem(), Collections.singletonList(slotAuctionMap.get(clickedSlot)));
                        }
                    }
                }
            }
        }, plugin);

        // Open the sub-GUI
        player.openInventory(subGUI);
    }

    private void handlePurchase(Player player, ItemStack clickedItem, List<Auction> materialAuctions) {

        Auction selectedAuction = null;
        for (Auction auction : materialAuctions) {
            if (Objects.equals(auction.getDisplayItem(), clickedItem)) {
                selectedAuction = auction;
                break; // No need to continue checking once a match is found
            }
        }

        if (selectedAuction != null && !selectedAuction.isBought() && !selectedAuction.isExpired()) {

            selectedAuction.setBought(true);

            selectedAuction.setBuyer(player);

            // Notify the player
            player.sendMessage(ChatColor.GREEN + "ʏᴏᴜ ʙᴏᴜɢʜᴛ " + ChatColor.YELLOW + selectedAuction.getAuctionedItem().getType() +
                    ChatColor.GREEN + " ғᴏʀ " + ChatColor.YELLOW + selectedAuction.getPrice());

            // Implement any additional logic here (e.g., deducting money, handling items, etc.)

            // Add the purchased item to the player's inventory
            player.getInventory().addItem(selectedAuction.getAuctionedItem());

            Economy econ = getEconomy();

            player.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player, selectedAuction.getPrice());
            if(r.transactionSuccess()) {
                player.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                player.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }


            // Remove the item from the sub-GUI
            clickedItem.setAmount(0);
            auctionHouse.removeAuction(selectedAuction);
            auctionHouse.addHistoryAuction(selectedAuction);

            //AuctionPurchaseEvent purchaseEvent = new AuctionPurchaseEvent(player, selectedAuction, selectedAuction.getSeller());
            //Bukkit.getPluginManager().callEvent(purchaseEvent);
        } else {
            player.sendMessage(ChatColor.RED + "ᴛʜɪs ɪᴛᴇᴍ ɪs ɴᴏ ʟᴏɴɢᴇʀ ᴀᴠᴀɪʟᴀʙʟᴇ ғᴏʀ ᴘᴜʀᴄʜᴀsᴇ.");
        }
    }

    private void openHistoryAuctions(Player player) {
        Inventory subGUI = Bukkit.createInventory(null, 9 * 6, "ᴀᴜᴄᴛɪᴏɴ ʜᴏᴜsᴇ - ᴀᴜᴄᴛɪᴏɴ ʜɪsᴛᴏʀʏ");
        int i = 0;
        for (Auction auction : auctionHouse.getHistoryAuctions()) {
            ItemStack itemStack = auction.getAuctionedItem().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            lore.add("");
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.GOLD + "ᴘʀɪᴄᴇ: " + ChatColor.YELLOW + auction.getPrice());
            lore.add(ChatColor.GOLD + "sᴇʟʟᴇʀ: " + ChatColor.YELLOW + auction.getSellerName());
            lore.add(ChatColor.GOLD + "ʙᴜʏᴇʀ: " + ChatColor.YELLOW + auction.getBuyer().getName());
            lore.add(ChatColor.WHITE + "--------------------------------");

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            subGUI.setItem(i, itemStack);
            i++;
        }


        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().equals(subGUI)) {
                    event.setCancelled(true); // Cancel the click event
                    /*
                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        openDetailedAuctionView(player);
                    }
                     */
                }
            }
        }, plugin);

        player.openInventory(subGUI);
    }

    //FOR LATER
    private void openDetailedAuctionView(Player player) {

    }

    private void openDetailProfile(Player player, Player playerToView) {
        // PLACEHOLDERs for faction, balance, ign etc
    }


}
