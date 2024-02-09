package me.panda.cosmicauctionhouse.engine.events;

import me.panda.cosmicauctionhouse.engine.bp.Auction;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AuctionPurchaseEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final OfflinePlayer buyer;
    private final Auction auction;
    private final OfflinePlayer seller;
    private boolean cancelled;

    public AuctionPurchaseEvent(OfflinePlayer buyer, Auction auction, OfflinePlayer seller) {
        this.buyer = buyer;
        this.auction = auction;
        this.seller = seller;
    }


    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public OfflinePlayer getBuyer() {
        return buyer;
    }

    public Auction getAuction() {
        return auction;
    }

    public OfflinePlayer getSeller() {
        return seller;
    }

}
