package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.storage.PlayerDataStorage;
import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class BaltopCommand implements CommandExecutor {

    private final MessageTranslator msg;
    private final PlayerDataStorage pds;
    private final Economy eco;

    public BaltopCommand() {
        msg = MessageTranslator.getInstance();
        pds = Easyconomy.getInstance().getEcp().getStorage();
        eco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String permission = Configuration.get().getString("permissions.baltop","");
        if(!"".equals(permission) && !sender.hasPermission(permission)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noPerms", true, permission));
            return true;
        }

        if(pds.getBaltop() == null || pds.getBaltop().size() == 0) {
            sender.sendMessage(msg.getMessage("baltop.none", true));
            return true;
        }

        StringBuilder message = new StringBuilder(msg.getMessage("baltop.start",true));
        message.append("\n"); // newline
        for(Map.Entry<UUID, Double> entry : pds.getBaltop().entrySet()) {
            if(Bukkit.getOfflinePlayer(entry.getKey()).getName() == null) {
                message.append("Invalid entry: ").append(entry.getKey().toString());
            } else {
                message.append(msg.getMessageAndReplace("baltop.value", false, eco.format(entry.getValue()), Bukkit.getOfflinePlayer(entry.getKey()).getName()));
                message.append("\n");
            }
        }

        sender.sendMessage(message.toString());

        return true;
    }
}