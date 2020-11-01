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

import java.util.Objects;

@SuppressWarnings("deprecation")
public class BalanceCommand implements CommandExecutor {

    private final Economy economy;
    private final MessageTranslator messageTranslator;

    public BalanceCommand() {

        economy = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(Economy.class)).getProvider();

        messageTranslator = MessageTranslator.getInstance();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) {

            if(args.length != 1) {

                sender.sendMessage(messageTranslator.getMessageAndReplace("general.syntax",true,"/bal <playerName>"));

                return true;
            }

            sendBalanceOfOther(sender,args[0]);

            return true;
        }

        String permission = Configuration.get().getString("permissions.balance","");

        if(permission != null && !"".equals(permission) && !sender.hasPermission(permission)) {

            sender.sendMessage(messageTranslator.getMessageAndReplace("general.noPerms",true,permission));

            return true;
        }

        Player p = (Player) sender;

        if(args.length == 0) {

            p.sendMessage(messageTranslator.getMessageAndReplace("balance.ofSelf",true, economy.format(economy.getBalance(p))));

        } else if(args.length == 1) {
            String otherBalancePerm = Configuration.get().getString("permissions.othersBalance","");

            if(otherBalancePerm != null && !"".equals(otherBalancePerm) && !sender.hasPermission(otherBalancePerm)) {

                sender.sendMessage(messageTranslator.getMessageAndReplace("general.noPerms",true,otherBalancePerm));

                return true;
            }

            sendBalanceOfOther(sender,args[0]);

        } else {

            sender.sendMessage(messageTranslator.getMessageAndReplace("general.syntax",true,"/bal <playerName>"));

        }

        return true;

    }

    private void sendBalanceOfOther(CommandSender sender, String otherName) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(otherName);

        if(!p.hasPlayedBefore() || !economy.hasAccount(p)) {

            sender.sendMessage(messageTranslator.getMessageAndReplace("general.noAccount",true,otherName));

        } else {

            sender.sendMessage(messageTranslator.getMessageAndReplace("balance.ofOther", true, p.getName(), economy.format(economy.getBalance(p))));

        }
    }
}
