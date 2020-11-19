package dev.wwst.easyconomy.events;

import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.storage.PlayerDataStorage;
import dev.wwst.easyconomy.utils.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    private final PlayerDataStorage pds;

    public JoinEvent(PlayerDataStorage pds) {
        this.pds = pds;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(Easyconomy.getInstance(), () -> {
            if(!pds.has(e.getPlayer().getUniqueId())) {
                final String cmd = "eco give "+e.getPlayer().getName()+" "+Configuration.get().getInt("startingBalance");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }
        }, 25);
    }

}
