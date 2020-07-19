package dev.wwst.easyconomy.events;

import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.utils.Configuration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    private final Economy eco;

    public JoinEvent(Economy eco) {
        this.eco = eco;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(Easyconomy.getInstance(), () -> {
            if(!eco.hasAccount(e.getPlayer())) {
                final String cmd = "eco give "+e.getPlayer().getName()+" "+Configuration.get().getInt("startingBalance");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }
        }, 25);
    }

}
