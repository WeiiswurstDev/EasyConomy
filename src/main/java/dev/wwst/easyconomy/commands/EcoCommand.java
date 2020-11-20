package dev.wwst.easyconomy.commands;

import com.google.common.io.Files;
import dev.wwst.easyconomy.EasyConomyProvider;
import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EcoCommand implements CommandExecutor {

    private final EasyConomyProvider eco;
    private final MessageTranslator msg;

    public EcoCommand(EasyConomyProvider ecp) {
        this.eco = ecp;
        msg = MessageTranslator.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        label = label.toLowerCase();
        if(label.equals("eco") && args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aEasyConomy by Weiiswurst#0016 Version @version@\n&7Binary Storage option by &bGeolykt"));
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
                } else if(args.length == 1 && args[0].equalsIgnoreCase("backup")) {
                    sender.sendMessage(msg.getMessage("backup.starting"));
                    eco.getStorage().save();

                    File backupFolder = new File("plugins"+File.separator+"EasyConomy"+File.separator+"backups");
                    backupFolder.mkdir();

                    File backupFile = new File(backupFolder,
                            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) +
                                    '.' +
                                    Files.getFileExtension(eco.getStorage().getStorageFile().getPath())
                    );

                    try {
                        Files.copy(eco.getStorage().getStorageFile(), backupFile);
                    } catch (IOException e) {
                        sender.sendMessage(msg.getMessage("backup.error"));
                        return true;
                    }

                    sender.sendMessage(msg.getMessage("backup.finished"));
                    return true;
                }
                if(args.length != 3) {
                    sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/eco " + args[0] + " <playerName> <amount>"));
                    return true;
                }
            } else if(args.length != 2) {
                sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/givemoney|takemoney|setmoney <playerName> <amount>"));
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
        if(!p.isOnline() && !p.hasPlayedBefore()) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,target));
            return true;
        }
        final double amount;
        try {
            amount = Double.parseDouble(amountStr);
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
