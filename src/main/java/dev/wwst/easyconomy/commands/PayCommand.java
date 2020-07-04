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

public class PayCommand implements CommandExecutor {


    private final Economy eco;
    private final MessageTranslator msg;

    public PayCommand() {
        eco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        msg = MessageTranslator.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(msg.getMessage("general.playerOnly",true));
            return true;
        }
        String permission = Configuration.get().getString("permissions.pay","");
        if(!"".equals(permission) && !sender.hasPermission(permission)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noperm",true,permission));
            return true;
        }
        if(args.length != 2) {
            sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/pay <target> <amount>"));
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            sender.sendMessage(msg.getMessageAndReplace("general.notAnumber",true,args[1]));
            return true;
        }
        amount = Math.abs(amount);
        Player p = (Player) sender;
        if(!eco.has(p,amount)) {
            sender.sendMessage(msg.getMessageAndReplace("general.insufficientFunds",true,eco.format(amount-eco.getBalance(p))));
            return true;
        }
        if(args[0].equalsIgnoreCase(p.getName())) {
            sender.sendMessage(msg.getMessage("pay.self",true));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if(!eco.hasAccount(target)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,args[0]));
            return true;
        }
        eco.withdrawPlayer(p,amount);
        eco.depositPlayer(target,amount);
        p.sendMessage(msg.getMessageAndReplace("pay.you",true,target.getName(),eco.format(amount),eco.format(eco.getBalance(p))));
        if(target.getPlayer() != null) {
            target.getPlayer().sendMessage(msg.getMessageAndReplace("pay.target",true,p.getName(),eco.format(amount),eco.format(eco.getBalance(target))));
        }
        return true;
    }
}