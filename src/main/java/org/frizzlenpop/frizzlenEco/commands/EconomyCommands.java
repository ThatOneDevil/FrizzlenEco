package org.frizzlenpop.frizzlenEco.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.Currency;
import org.frizzlenpop.frizzlenEco.economy.EconomyManager;
import org.frizzlenpop.frizzlenEco.util.MessageUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles basic economy commands
 */
public class EconomyCommands implements CommandExecutor, TabCompleter {
    private final FrizzlenEco plugin;
    private final EconomyManager economyManager;
    
    /**
     * Creates a new EconomyCommands instance
     * @param plugin the FrizzlenEco plugin instance
     */
    public EconomyCommands(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        
        // Register commands
        plugin.getCommand("money").setExecutor(this);
        plugin.getCommand("money").setTabCompleter(this);
        plugin.getCommand("balance").setExecutor(this);
        plugin.getCommand("balance").setTabCompleter(this);
        plugin.getCommand("pay").setExecutor(this);
        plugin.getCommand("pay").setTabCompleter(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("money") || command.getName().equalsIgnoreCase("balance")) {
            return handleBalanceCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("pay")) {
            return handlePayCommand(sender, args);
        }
        
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (command.getName().equalsIgnoreCase("money") || command.getName().equalsIgnoreCase("balance")) {
            if (args.length == 1) {
                // Tab complete for player names
                String partialName = args[0].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partialName)) {
                        completions.add(player.getName());
                    }
                }
            } else if (args.length == 2) {
                // Tab complete for currency
                String partialCurrency = args[1].toLowerCase();
                for (Currency currency : economyManager.getCurrencies()) {
                    if (currency.getId().toLowerCase().startsWith(partialCurrency)) {
                        completions.add(currency.getId());
                    }
                }
            }
        } else if (command.getName().equalsIgnoreCase("pay")) {
            if (args.length == 1) {
                // Tab complete for player names
                String partialName = args[0].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partialName) && !player.getName().equals(sender.getName())) {
                        completions.add(player.getName());
                    }
                }
            } else if (args.length == 3) {
                // Tab complete for currency
                String partialCurrency = args[2].toLowerCase();
                for (Currency currency : economyManager.getCurrencies()) {
                    if (currency.getId().toLowerCase().startsWith(partialCurrency)) {
                        completions.add(currency.getId());
                    }
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Handles the balance command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleBalanceCommand(CommandSender sender, String[] args) {
        // /balance [player] [currency]
        OfflinePlayer target;
        Currency currency = economyManager.getDefaultCurrency();
        
        if (args.length == 0) {
            // Check own balance
            if (!(sender instanceof Player)) {
                MessageUtil.sendError(sender, "Console must specify a player");
                return true;
            }
            
            target = (Player) sender;
        } else {
            // Check another player's balance
            String playerName = args[0];
            target = Bukkit.getPlayerExact(playerName);
            
            if (target == null) {
                target = Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                        .findFirst().orElse(null);
            }
            
            if (target == null) {
                MessageUtil.sendError(sender, "Player not found: " + playerName);
                return true;
            }
            
            // Check for specified currency
            if (args.length >= 2) {
                String currencyId = args[1];
                currency = economyManager.getCurrency(currencyId);
                
                if (currency == null) {
                    MessageUtil.sendError(sender, "Currency not found: " + currencyId);
                    return true;
                }
            }
        }
        
        // Get the balance
        BigDecimal balance = economyManager.getBalance(target.getUniqueId(), currency);
        
        // Send balance message
        if (sender.equals(target)) {
            MessageUtil.sendInfo(sender, "Your balance: " + currency.format(balance));
        } else {
            MessageUtil.sendInfo(sender, target.getName() + "'s balance: " + currency.format(balance));
        }
        
        return true;
    }
    
    /**
     * Handles the pay command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handlePayCommand(CommandSender sender, String[] args) {
        // /pay <player> <amount> [currency]
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "Only players can use this command");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendError(sender, "Usage: /pay <player> <amount> [currency]");
            return true;
        }
        
        Player from = (Player) sender;
        String targetName = args[0];
        
        // Find target player
        OfflinePlayer to = Bukkit.getPlayerExact(targetName);
        if (to == null) {
            to = Arrays.stream(Bukkit.getOfflinePlayers())
                 .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(targetName))
                 .findFirst().orElse(null);
            
            if (to == null) {
                MessageUtil.sendError(sender, "Player not found: " + targetName);
                return true;
            }
        }
        
        // Cannot pay yourself
        if (from.getUniqueId().equals(to.getUniqueId())) {
            MessageUtil.sendError(sender, "You cannot pay yourself");
            return true;
        }
        
        // Parse amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                MessageUtil.sendError(sender, "Amount must be positive");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(sender, "Invalid amount: " + args[1]);
            return true;
        }
        
        // Get currency
        Currency currency = economyManager.getDefaultCurrency();
        if (args.length >= 3) {
            String currencyId = args[2];
            currency = economyManager.getCurrency(currencyId);
            
            if (currency == null) {
                MessageUtil.sendError(sender, "Currency not found: " + currencyId);
                return true;
            }
        }
        
        // Check if sender has enough money
        UUID fromUuid = from.getUniqueId();
        if (!economyManager.has(fromUuid, amount, currency)) {
            MessageUtil.sendError(sender, "You don't have enough " + currency.getName() + 
                    ". Required: " + currency.format(amount));
            return true;
        }
        
        // Perform transfer
        boolean success = economyManager.transfer(fromUuid, to.getUniqueId(), amount, currency);
        if (success) {
            MessageUtil.sendSuccess(sender, "You paid " + to.getName() + " " + 
                    currency.format(amount));
            
            // Notify receiver if online
            Player toPlayer = to.getPlayer();
            if (toPlayer != null && toPlayer.isOnline()) {
                MessageUtil.sendSuccess(toPlayer, "You received " + currency.format(amount) + 
                        " from " + from.getName());
            }
        } else {
            MessageUtil.sendError(sender, "Failed to transfer funds. Please try again.");
        }
        
        return true;
    }
} 