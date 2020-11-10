package dev.wwst.easyconomy;

import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.PlayerDataStorage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Weiiswurst
 */
public class EasyConomyProvider implements Economy {

    private final PlayerDataStorage pds;
    private final Logger logger;

    private final String currencyFormatSingular,
            currencyFormatPlural;

    public EasyConomyProvider() {
        pds = new PlayerDataStorage("balances.yml", Configuration.get().getInt("baltopPlayers"));
        if(Configuration.get().getBoolean("enable-logging",true))
            logger = Easyconomy.getInstance().getLogger();
        else
            logger = null;

        currencyFormatSingular = ChatColor.translateAlternateColorCodes('&',Configuration.get().getString("names.currencyFormatSingular", "%s Dollar"));
        currencyFormatPlural = ChatColor.translateAlternateColorCodes('&',Configuration.get().getString("names.currencyFormatPlural","%s Dollars"));
    }

    public PlayerDataStorage getStorage() {
        return pds;
    }

    /**
     * Checks if economy method is enabled.
     *
     * @return Success or Failure
     */
    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("EasyConomy");
    }

    /**
     * Gets name of economy method
     *
     * @return Name of Economy Method
     */
    @Override
    public String getName() {
        return "EasyConomy";
    }

    /**
     * Returns true if the given implementation supports banks.
     *
     * @return true if the implementation supports banks
     */
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    /**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     *
     * @return number of digits after the decimal point kept
     */
    @Override
    public int fractionalDigits() {
        return -1;
    }

    /**
     * Format amount into a human readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.
     *
     * @param amount to format
     * @return Human readable string describing amount
     */
    @Override
    public String format(double amount) {
        int decimalsShown = Configuration.get().getInt("decimalsShown");
        if(decimalsShown >= 0) {
            amount = new BigDecimal(amount).setScale(decimalsShown, RoundingMode.DOWN).doubleValue();
        }
        if(amount != 1) return String.format(currencyFormatPlural,amount);
        else return String.format(currencyFormatSingular,amount);
    }

    /**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (plural)
     */
    @Override
    public String currencyNamePlural() {
        return Configuration.get().getString("names.currencyNamePlural","Dollars");
    }

    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (singular)
     */
    @Override
    public String currencyNameSingular() {
        return Configuration.get().getString("names.currencyNameSingular","Dollar");
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player to check
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        //logger.info(pds.getConfig().isSet(player.getUniqueId().toString())+"");
        return true;
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName),worldName);
    }

    /**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player    to check in the world
     * @param worldName world-specific account
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Gets balance of a player
     *
     * @param player of the player
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player) {
        return pds.getPlayerData(player);
    }

    /**
     * @param playerName
     * @param world
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    /**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player to check
     * @param world  name of the world
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to check
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use @{link {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName,amount);
    }

    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to check
     * @param worldName to check with
     * @param amount    to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player,amount);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName),amount);
    }

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        final double oldBalance = getBalance(player);
        // BigDecimal for less approximations when dealing with doubles (due to how floating point values are handled in
        // Java, there will always be approximations) ( https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html )
        final double newBalance = BigDecimal.valueOf(oldBalance).subtract(BigDecimal.valueOf(amount)).doubleValue();
        if(logger != null)
            logger.info("[TRANSFER] "+player.getUniqueId()+" "+format(-amount));
        pds.write(player.getUniqueId().toString(),newBalance);
        //logger.info("New bal"+getBalance(player)+" old bal "+oldBalance);
        return new EconomyResponse(Math.abs(amount),newBalance,EconomyResponse.ResponseType.SUCCESS,"");
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName),worldName,amount);
    }

    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to withdraw from
     * @param worldName - name of the world
     * @param amount    Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player,amount);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return withdrawPlayer(playerName,-amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player,-amount);
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName,worldName,-amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to deposit to
     * @param worldName name of the world
     * @param amount    Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player,worldName,-amount);
    }

    /**
     * @param name
     * @param player
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0,getBalance(player), EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     *
     * @param name   of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0,getBalance(player), EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Deletes a bank account with the specified name.
     *
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Returns the amount the bank has
     *
     * @param name of the account
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name   of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Check if the player is a member of the bank account
     *
     * @param name   of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0,0, EconomyResponse.ResponseType.NOT_IMPLEMENTED,"No bank support");
    }

    /**
     * Gets the list of banks
     *
     * @return the List of Banks
     */
    @Override
    public List<String> getBanks() {
        return new ArrayList<>();
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Attempts to create a player account for the given player
     *
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName),worldName);
    }

    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     *
     * @param player    OfflinePlayer
     * @param worldName String name of the world
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }
}
