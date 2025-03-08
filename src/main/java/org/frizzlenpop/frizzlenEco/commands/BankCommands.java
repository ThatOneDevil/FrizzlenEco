package org.frizzlenpop.frizzlenEco.commands;

import org.bukkit.Bukkit;
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

/**
 * Handles bank-related commands
 */
public class BankCommands implements CommandExecutor, TabCompleter {
    private final FrizzlenEco plugin;
    private final EconomyManager economyManager;
    
    /**
     * Creates a new BankCommands instance
     * @param plugin the FrizzlenEco plugin instance
     */
    public BankCommands(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        
        // Register commands
        plugin.getCommand("bank").setExecutor(this);
        plugin.getCommand("bank").setTabCompleter(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "balance":
                return handleBalanceCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "deposit":
                return handleDepositCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "withdraw":
                return handleWithdrawCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "transfer":
                return handleTransferCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "list":
                return handleListCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "help":
            default:
                showHelp(sender);
                return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete for subcommands
            String partialCommand = args[0].toLowerCase();
            List<String> subCommands = Arrays.asList("balance", "deposit", "withdraw", "transfer", "list", "help");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partialCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("balance") || subCommand.equals("list")) {
                // No additional tab completions
            } else if (subCommand.equals("deposit") || subCommand.equals("withdraw")) {
                if (args.length == 2) {
                    // Tab complete for amount
                    completions.add("10");
                    completions.add("100");
                    completions.add("1000");
                } else if (args.length == 3) {
                    // Tab complete for currency
                    String partialCurrency = args[2].toLowerCase();
                    for (Currency currency : economyManager.getCurrencies()) {
                        if (currency.getId().toLowerCase().startsWith(partialCurrency)) {
                            completions.add(currency.getId());
                        }
                    }
                }
            } else if (subCommand.equals("transfer")) {
                if (args.length == 2) {
                    // Tab complete for player names
                    String partialName = args[1].toLowerCase();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(partialName) && !player.getName().equals(sender.getName())) {
                            completions.add(player.getName());
                        }
                    }
                } else if (args.length == 3) {
                    // Tab complete for amount
                    completions.add("10");
                    completions.add("100");
                    completions.add("1000");
                } else if (args.length == 4) {
                    // Tab complete for currency
                    String partialCurrency = args[3].toLowerCase();
                    for (Currency currency : economyManager.getCurrencies()) {
                        if (currency.getId().toLowerCase().startsWith(partialCurrency)) {
                            completions.add(currency.getId());
                        }
                    }
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Shows the help message
     * @param sender the command sender
     */
    private void showHelp(CommandSender sender) {
        MessageUtil.sendInfo(sender, "=== Bank Commands ===");
        MessageUtil.sendInfo(sender, "/bank balance [currency] - Check your bank balance");
        MessageUtil.sendInfo(sender, "/bank deposit <amount> [currency] - Deposit money into your bank");
        MessageUtil.sendInfo(sender, "/bank withdraw <amount> [currency] - Withdraw money from your bank");
        MessageUtil.sendInfo(sender, "/bank transfer <player> <amount> [currency] - Transfer money from your bank to another player's bank");
        MessageUtil.sendInfo(sender, "/bank list - List all your bank accounts");
    }
    
    /**
     * Handles the balance command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleBalanceCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "Only players can use this command");
            return true;
        }
        
        Player player = (Player) sender;
        Currency currency = economyManager.getDefaultCurrency();
        
        // Check for specified currency
        if (args.length >= 1) {
            String currencyId = args[0];
            currency = economyManager.getCurrency(currencyId);
            
            if (currency == null) {
                MessageUtil.sendError(sender, "Currency not found: " + currencyId);
                return true;
            }
        }
        
        // Get the balance
        BigDecimal balance = economyManager.getBalance(player.getUniqueId(), currency);
        
        // Send balance message
        MessageUtil.sendInfo(sender, "Your bank balance: " + currency.format(balance));
        
        return true;
    }
    
    /**
     * Handles the deposit command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleDepositCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "Only players can use this command");
            return true;
        }
        
        if (args.length < 1) {
            MessageUtil.sendError(sender, "Usage: /bank deposit <amount> [currency]");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Parse amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[0]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                MessageUtil.sendError(sender, "Amount must be positive");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(sender, "Invalid amount: " + args[0]);
            return true;
        }
        
        // Get currency
        Currency currency = economyManager.getDefaultCurrency();
        if (args.length >= 2) {
            String currencyId = args[1];
            currency = economyManager.getCurrency(currencyId);
            
            if (currency == null) {
                MessageUtil.sendError(sender, "Currency not found: " + currencyId);
                return true;
            }
        }

        BigDecimal balance = economyManager.getBalance(player.getUniqueId(), currency);
        if (amount > balance){
            MessageUtil.sendError(sender, "Insufficient funds!");
            return true;
        }
        
        // Perform deposit
        UUID playerUuid = player.getUniqueId();
        boolean success = economyManager.deposit(playerUuid, amount, currency);
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Deposited " + currency.format(amount) + " into your bank account");
        } else {
            MessageUtil.sendError(sender, "Failed to deposit funds. Please try again.");
        }
        
        return true;
    }
    
    /**
     * Handles the withdraw command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleWithdrawCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "Only players can use this command");
            return true;
        }
        
        if (args.length < 1) {
            MessageUtil.sendError(sender, "Usage: /bank withdraw <amount> [currency]");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Parse amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[0]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                MessageUtil.sendError(sender, "Amount must be positive");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(sender, "Invalid amount: " + args[0]);
            return true;
        }
        
        // Get currency
        Currency currency = economyManager.getDefaultCurrency();
        if (args.length >= 2) {
            String currencyId = args[1];
            currency = economyManager.getCurrency(currencyId);
            
            if (currency == null) {
                MessageUtil.sendError(sender, "Currency not found: " + currencyId);
                return true;
            }
        }
        
        // Check if player has enough money
        UUID playerUuid = player.getUniqueId();
        if (economyManager.getBalance(playerUuid, currency).compareTo(amount) < 0) {
            MessageUtil.sendError(sender, "You don't have enough " + currency.getName() + 
                    " in your bank account. Required: " + currency.format(amount));
            return true;
        }
        
        // Perform withdrawal
        boolean success = economyManager.withdraw(playerUuid, amount, currency);
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Withdrew " + currency.format(amount) + " from your bank account");
        } else {
            MessageUtil.sendError(sender, "Failed to withdraw funds. Please try again.");
        }
        
        return true;
    }
    
    /**
     * Handles the transfer command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleTransferCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "Only players can use this command");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendError(sender, "Usage: /bank transfer <player> <amount> [currency]");
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
        
        // Cannot transfer to yourself
        if (from.getUniqueId().equals(to.getUniqueId())) {
            MessageUtil.sendError(sender, "You cannot transfer to yourself");
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
        if (economyManager.getBalance(fromUuid, currency).compareTo(amount) < 0) {
            MessageUtil.sendError(sender, "You don't have enough " + currency.getName() + 
                    " in your bank account. Required: " + currency.format(amount));
            return true;
        }
        
        // Perform transfer
        boolean success = economyManager.transfer(fromUuid, to.getUniqueId(), amount, currency);
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Transferred " + currency.format(amount) + " to " + 
                    to.getName() + "'s bank account");
            
            // Notify receiver if online
            Player toPlayer = to.getPlayer();
            if (toPlayer != null && toPlayer.isOnline()) {
                MessageUtil.sendSuccess(toPlayer, "You received " + currency.format(amount) + 
                        " in your bank account from " + from.getName());
            }
        } else {
            MessageUtil.sendError(sender, "Failed to transfer funds. Please try again.");
        }
        
        return true;
    }
    
    /**
     * Handles the list command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleListCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendError(sender, "Only players can use this command");
            return true;
        }
        
        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();
        
        // Get all player accounts
        MessageUtil.sendInfo(sender, "=== Your Bank Accounts ===");
        
        boolean hasAccounts = false;
        for (Currency currency : economyManager.getCurrencies()) {
            if (economyManager.hasAccount(playerUuid, currency)) {
                BigDecimal balance = economyManager.getBalance(playerUuid, currency);
                MessageUtil.sendInfo(sender, currency.getName() + ": " + currency.format(balance));
                hasAccounts = true;
            }
        }
        
        if (!hasAccounts) {
            MessageUtil.sendInfo(sender, "You don't have any bank accounts");
        }
        
        return true;
    }
} 
