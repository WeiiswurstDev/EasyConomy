package dev.wwst.easyconomy.commands;

import com.google.common.io.Files;
import dev.wwst.easyconomy.EasyConomyProvider;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EcoCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final EasyConomyProvider eco;
    private final MessageTranslator msg;
    private final String[] postBackupCommand;

    /**
     * Creates a new EcoCommand instance which basically provides an interface with the economy.
     * @param pluginInstance The plugin that it should link to, used to locate files that should be backed up as well as allowing economy manipulation to work.
     *                       It is also used to start async tasks.
     * @param backupCommand The shell command that should be run after the files have been moved to the backup location. Run in the backup folder. Nullable.
     */
    public EcoCommand(Easyconomy pluginInstance, Collection<String> backupCommand) {
        this.eco = pluginInstance.getEcp();
        this.msg = MessageTranslator.getInstance();
        this.plugin = pluginInstance;
        if (backupCommand == null || backupCommand.size() == 0) {
            this.postBackupCommand = null;
        } else {
            this.postBackupCommand = backupCommand.toArray(new String[0]);
        }
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

                    if (postBackupCommand == null) {
                        sender.sendMessage(msg.getMessage("backup.finished"));
                    } else {
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            ProcessBuilder procBuilder = new ProcessBuilder(postBackupCommand).directory(backupFolder).inheritIO();
                            try {
                                Process proc = procBuilder.start();
                                if (!proc.waitFor(15, TimeUnit.SECONDS)) {
                                    sender.sendMessage(msg.getMessage("backup.timeout", true));
                                    proc.destroy();
                                }
                            } catch (IOException e) {
                                e.printStackTrace(); // these are chronic issues and need to be patched by whoever reads the log
                                Easyconomy.getInstance().getLogger().warning("If you are getting a permission denied error, then you may want to chmod the file.");
                            } catch (InterruptedException e) {
                                e.printStackTrace(); // these are issues induced by other plugins or the server software
                            }
                            sender.sendMessage(msg.getMessage("backup.finished"));
                        });
                    }
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
