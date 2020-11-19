package dev.wwst.easyconomy;

import dev.wwst.easyconomy.commands.BalanceCommand;
import dev.wwst.easyconomy.commands.BaltopCommand;
import dev.wwst.easyconomy.commands.EcoCommand;
import dev.wwst.easyconomy.commands.PayCommand;
import dev.wwst.easyconomy.events.JoinEvent;
import dev.wwst.easyconomy.storage.PlayerDataStorage;
import dev.wwst.easyconomy.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Weiiswurst
 */
public final class Easyconomy extends JavaPlugin {

    private final List<PlayerDataStorage> toSave = new ArrayList<>();

    private static Easyconomy INSTANCE;

    private EasyConomyProvider ecp = null;

    public static final String PLUGIN_NAME = "EasyConomy";

    private boolean isLoaded = false;

    @Override
    public void onLoad() {
        PluginManager pm = Bukkit.getPluginManager();
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException expected) {
            getLogger().severe("!!! VAULT IS NOT INSTALLED !!!");
            getLogger().severe("!!! THE VAULT PLUGIN IS NEEDED FOR THIS PLUGIN !!!");
            pm.disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            ecp = new EasyConomyProvider();
            Bukkit.getServicesManager().register(Economy.class, ecp,this, ServicePriority.Normal);
        } else {
            getLogger().severe("!!! YOU ALREADY HAVE AN ECONOMY PLUGIN !!!");
            getLogger().severe(String.format("!!! REMOVE OR DISABLE THE ECONOMY OF %s !!!",rsp.getProvider().getName()));
            pm.disablePlugin(this);
            return;
        }
        isLoaded = true;
    }

    @Override
    public void onEnable() {
        if (!isLoaded) {
            return;
        }
        INSTANCE = this;
        getDataFolder().mkdirs();
        Configuration.setup();
        new MessageTranslator(Configuration.get().getString("language"));

        PluginManager pm = Bukkit.getPluginManager();

        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("eco").setExecutor(new EcoCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("baltop").setExecutor(new BaltopCommand());

        pm.registerEvents(new JoinEvent(ecp.getStorage()),this);

        Metrics metrics = new Metrics(this, 7962);

        if(Configuration.get().getBoolean("update-checker")) {
            new UpdateChecker(this, 81034).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    getLogger().info("You are up to date!");
                } else {
                    getLogger().warning("!!! There is a new update available! Download at https://www.spigotmc.org/resources/easyconomy.81034/ !!!");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        if (!isLoaded) {
            return;
        }
        for(PlayerDataStorage pds : toSave) {
            pds.save();
        }
        getLogger().log(Level.INFO, "EasyConomy was disabled.");
    }


    public String getConfigFolderPath() {
        return "plugins//EasyConomy";
    }

    public static Easyconomy getInstance() {
        return INSTANCE;
    }

    public void addDataStorage(PlayerDataStorage pds) {
        toSave.add(pds);
    }

    public EasyConomyProvider getEcp() {
        return ecp;
    }
}
