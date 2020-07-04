package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class BalanceCommand implements CommandExecutor {

    private final Economy eco;
    private final MessageTranslator msg;

    public BalanceCommand() {
        eco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        msg = MessageTranslator.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            if(args.length != 1) {
                sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/bal <playerName>"));
                return true;
            }
            sendBalanceOfOther(sender,args[0]);
            return true;
        }

        String permission = Configuration.get().getString("permissions.balance","");
        if(!"".equals(permission) && !sender.hasPermission(permission)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noPerms",true,permission));
            return true;
        }

        Player p = (Player) sender;
        if(args.length == 0) {
            p.sendMessage(msg.getMessageAndReplace("balance.ofSelf",true,eco.format(eco.getBalance(p))));
        } else if(args.length == 1) {
            sendBalanceOfOther(sender,args[0]);
        } else {
            sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/bal <playerName>"));
        }
        return true;
    }

    private void sendBalanceOfOther(CommandSender sender, String otherName) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(otherName);
        if(!p.hasPlayedBefore() || !eco.hasAccount(p)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,otherName));
        } else {
            sender.sendMessage(msg.getMessageAndReplace("balance.ofOther", true, p.getName(), eco.format(eco.getBalance(p))));
        }
    }
}
