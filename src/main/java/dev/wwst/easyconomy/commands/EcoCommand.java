package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EcoCommand implements CommandExecutor {

    private final Economy eco;
    private final MessageTranslator msg;

    public EcoCommand() {
        eco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        msg = MessageTranslator.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        label = label.toLowerCase();
        if(label.equals("eco") && args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aEasyConomy by Weiiswurst#0016 Version "+ Easyconomy.getInstance().getDescription().getVersion()));
            return true;
        } else {
            String permission = Configuration.get().getString("permissions.modify","");
            if("".equals(permission) && !sender.isOp() || !permission.equals("") && !sender.hasPermission(permission)) {
                sender.sendMessage(msg.getMessageAndReplace("general.noPerms",true,"".equals(permission)?"Operator permissions":permission));
                return true;
            }
            if(label.equals("eco")) {
                if(args.length == 1 && args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(msg.getMessage("eco.helpMessage",true));
                    return true;
                }
                if(args.length != 3) {
                    sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/eco " + args[0] + " <playerName> <amount>"));
                    return true;
                }
            } else if(args.length != 2) {
                sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/" +label+ " <playerName> <amount>"));
                return true;
            }

            if(label.equals("eco")) {
                return performOperation(sender,args[0],args[1],args[2]);
            } else {
                return performOperation(sender,label,args[0],args[1]);
            }
        }
    }

    private boolean performOperation(CommandSender sender, String operation, String target, String amountStr) {
        final OfflinePlayer p = Bukkit.getOfflinePlayer(target);
        if(!p.hasPlayedBefore() || !eco.hasAccount(p)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,target));
            return true;
        }
        final int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch(NumberFormatException e) {
            sender.sendMessage(msg.getMessageAndReplace("general.notAnumber",true,amountStr));
            return true;
        }
        final EconomyResponse res;
        switch (operation.toLowerCase()) {
            case "add": case "addmoney": case "give": case "givemoney":
                res = eco.depositPlayer(p,amount);
                break;
            case "remove": case "removemoney": case "take": case "takemoney":
                res = eco.withdrawPlayer(p,amount);
                break;
            case "set": case "setbalance": case "setmoney":
                eco.withdrawPlayer(p,eco.getBalance(p));
                res = eco.depositPlayer(p,amount);
                break;
            default:
                res = null;
                break;
        }
        if(res == null)
            sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/eco give|take|set <playerName> <amount>"));
        else
            sender.sendMessage(msg.getMessageAndReplace("eco.success",true,p.getName(),eco.format(res.amount),eco.format(res.balance)));
        return true;
    }
}
