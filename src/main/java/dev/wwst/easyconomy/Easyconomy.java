package dev.wwst.easyconomy;

import dev.wwst.easyconomy.commands.BalanceCommand;
import dev.wwst.easyconomy.commands.EcoCommand;
import dev.wwst.easyconomy.commands.PayCommand;
import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import dev.wwst.easyconomy.utils.Metrics;
import dev.wwst.easyconomy.utils.PlayerDataStorage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Easyconomy extends JavaPlugin {

    private final List<PlayerDataStorage> toSave = new ArrayList<>();

    private static Easyconomy INSTANCE;

    public static final String PLUGIN_NAME = "EasyConomy";

    @Override
    public void onEnable() {
        INSTANCE = this;
        getDataFolder().mkdirs();
        Configuration.setup();
        new MessageTranslator(Configuration.get().getString("language"));

        PluginManager pm = Bukkit.getPluginManager();

        if(!pm.isPluginEnabled("Vault")) {
            getLogger().severe("!!! VAULT IS NOT INSTALLED !!!");
            getLogger().severe("!!! THE VAULT PLUGIN IS NEEDED FOR THIS PLUGIN !!!");
            pm.disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            Bukkit.getServicesManager().register(Economy.class, new EasyConomyProvider(),this, ServicePriority.Normal);
        } else {
            getLogger().severe("!!! YOU ALREADY HAVE AN ECONOMY PLUGIN !!!");
            getLogger().severe(String.format("!!! REMOVE OR DISABLE THE ECONOMY OF %s !!!",rsp.getProvider().getName()));
            pm.disablePlugin(this);
            return;
        }

        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("eco").setExecutor(new EcoCommand());
        getCommand("pay").setExecutor(new PayCommand());

        Metrics metrics = new Metrics(this, 7962);
    }

    @Override
    public void onDisable() {
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

}
