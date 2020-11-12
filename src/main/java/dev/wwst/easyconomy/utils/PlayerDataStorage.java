package dev.wwst.easyconomy.utils;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import dev.wwst.easyconomy.Easyconomy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Weiiswurst
 */
public class PlayerDataStorage {

    private final File file;
    private FileConfiguration customFile;

    private final Easyconomy plugin;

    private Map<UUID, Double> balTop;
    private double smallestBalTop = Double.MAX_VALUE;

    /**
     ** Finds or generates the custom config file
     */
    public PlayerDataStorage(String path, int baltopLength){
        plugin = (Easyconomy) Bukkit.getServer().getPluginManager().getPlugin(Easyconomy.PLUGIN_NAME);
        plugin.getLogger().log(Level.INFO, "Loading Storage: "+path);

        File storageFolder = new File(plugin.getDataFolder()+"/storage");
        if(!storageFolder.exists()) storageFolder.mkdirs();

        file = new File(plugin.getDataFolder()+"/storage", path);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);
        plugin.addDataStorage(this);

        if(baltopLength > 0) {
            plugin.getLogger().info("Calculating top balances... (if you have thousands of accounts, this could take a few seconds)");
            Map<UUID, Double> notSorted = new HashMap<>();
            for(String key : customFile.getKeys(false)) {
                notSorted.put(UUID.fromString(key), Doubles.tryParse(customFile.getString(key)));
            }
            recalcBaltop(notSorted, baltopLength);
            plugin.getLogger().info(balTop.size()+" balances are now in the baltop.");
        } else {
            balTop = null;
        }
    }

    public FileConfiguration getConfig(){
        return customFile;
    }

    public double getPlayerData(OfflinePlayer p) {
        return Doubles.tryParse(customFile.getString(p.getUniqueId().toString(),"0.0"));
    }

    public double getPlayerData(UUID player) {
        return Doubles.tryParse(customFile.getString(player.toString(),"0.0"));
    }

    public List<UUID> getAllData() {
        List<UUID> toReturn = Lists.newArrayList();
        for(String key : customFile.getKeys(false)) {
            if(customFile.getBoolean(key)) toReturn.add(UUID.fromString(key));
        }
        return toReturn;
    }
    /*
     ** Saves the current FileConfiguration to the file on the disk
     */
    public void save() {
        Configuration.get().options().copyDefaults(true);
        try {
            customFile.save(file);
            Easyconomy.getInstance().getLogger().log(Level.INFO, "Storage file "+file.getName()+" saved.");
        } catch (IOException e) { e.printStackTrace();}
    }

    public void write(String path, double value) {
        customFile.set(path, value+"");
        if(value > smallestBalTop) {
            System.out.println("Recalculating top balances (If you have a lot of accounts, this should happen very rarely)");
            balTop.put(UUID.fromString(path), value);
            recalcBaltop(balTop, Configuration.get().getInt("baltopPlayers"));
        }
        Easyconomy.getInstance().getLogger().info("Write to "+path+": "+value+" and now saving. Value is "+customFile.get(path));
        save();
    }

    public void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    private void recalcBaltop(Map<UUID, Double> notSorted, int baltopLength) {
        balTop = notSorted.entrySet().stream()
                .sorted((c1, c2) -> -c1.getValue().compareTo(c2.getValue()))
                .peek(val->{
                    if(val.getValue() < smallestBalTop) smallestBalTop = val.getValue();})
                .limit(baltopLength)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
        if(balTop.size() < baltopLength) {
            smallestBalTop = Double.MIN_VALUE;
        }
    }

    public Map<UUID, Double> getBaltop() {
        return balTop;
    }

}
