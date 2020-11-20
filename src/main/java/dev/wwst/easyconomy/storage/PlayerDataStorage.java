package dev.wwst.easyconomy.storage;

import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Weiiswurst
 */
public interface PlayerDataStorage {

    double getPlayerData(OfflinePlayer p);

    double getPlayerData(UUID player);

    List<UUID> getAllData();

    /*
     ** Saves the current FileConfiguration to the file on the disk
     */
    void save();

    void write(UUID key, double value);

    void reload();

    Map<UUID, Double> getBaltop();
    
    boolean has(UUID key);

    File getStorageFile();
}
