package dev.wwst.easyconomy.storage;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.utils.Configuration;
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
public class YamlDataStorage implements PlayerDataStorage {

    private final File file;
    private FileConfiguration customFile;

    private final Easyconomy plugin;

    private Map<UUID, Double> balTop;
    private double smallestBalTop = Double.MAX_VALUE;

    /**
     ** Finds or generates the custom config file
     */
    public YamlDataStorage(String path, int baltopLength){
        plugin = (Easyconomy) Bukkit.getServer().getPluginManager().getPlugin(Easyconomy.PLUGIN_NAME);
        plugin.getLogger().log(Level.INFO, "Loading Storage: "+path);
        long timestamp = System.currentTimeMillis();

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
        timestamp = System.currentTimeMillis() - timestamp;
        plugin.getLogger().log(Level.INFO, "Loaded Storage: " + path + " within " + timestamp + "ms");
    }

    public FileConfiguration getConfig(){
        return customFile;
    }

    @Override
    public double getPlayerData(OfflinePlayer p) {
        return Doubles.tryParse(customFile.getString(p.getUniqueId().toString(),"0.0"));
    }

    @Override
    public double getPlayerData(UUID player) {
        return Doubles.tryParse(customFile.getString(player.toString(),"0.0"));
    }

    @Override
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
    @Override
    public void save() {
        long time = System.currentTimeMillis();
        Configuration.get().options().copyDefaults(true);
        try {
            customFile.save(file);
            Easyconomy.getInstance().getLogger().info(
                    "Storage file " + file.getName() + " saved within " + (System.currentTimeMillis() - time) + "ms.");
        } catch (IOException e) { e.printStackTrace();}
    }

    @Override
    public void write(UUID key, double value) {
        customFile.set(key.toString(), value+"");
        if(value > smallestBalTop) {
            System.out.println("Recalculating top balances (If you have a lot of accounts, this should happen very rarely)");
            balTop.put(key, value);
            recalcBaltop(balTop, Configuration.get().getInt("baltopPlayers"));
        }
        Easyconomy.getInstance().getLogger().info("Write to "+key.toString()+
                ": "+value+" and now saving. Value is "+customFile.get(key.toString()));
        save();
    }

    @Override
    public void reload() {
        long time = System.currentTimeMillis();
        customFile = YamlConfiguration.loadConfiguration(file);
        Easyconomy.getInstance().getLogger().info(
                "Storage file " + file.getName() + " loaded within " + (System.currentTimeMillis() - time) + "ms.");
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

    @Override
    public Map<UUID, Double> getBaltop() {
        return balTop;
    }

    @Override
    public boolean has(UUID key) {
        return getConfig().isSet(key.toString());
    }

    @Override
    public File getStorageFile() {
        return file;
    }
}