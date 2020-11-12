package dev.wwst.easyconomy.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

/**
 * @author Weiiswurst
 */
public interface PlayerDataStorage {

    public double getPlayerData(OfflinePlayer p);

    public double getPlayerData(UUID player);

    public List<UUID> getAllData();

    /*
     ** Saves the current FileConfiguration to the file on the disk
     */
    public void save();

    public void write(UUID key, double value);

    public void reload();

    public Map<UUID, Double> getBaltop();
    
    public boolean has(UUID key);

}
