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
import java.util.stream.Collectors;

/**
 * Handles administrative economy commands
 */
public class AdminCommands implements CommandExecutor, TabCompleter {
    private final FrizzlenEco plugin;
    private final EconomyManager economyManager;
    
    /**
     * Creates a new AdminCommands instance
     * @param plugin the FrizzlenEco plugin instance
     */
    public AdminCommands(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        
        // Register commands
        plugin.getCommand("ecoadmin").setExecutor(this);
        plugin.getCommand("ecoadmin").setTabCompleter(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzleneco.admin")) {
            MessageUtil.sendError(sender, "You don't have permission to use this command");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGiveCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "take":
                return handleTakeCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "set":
                return handleSetCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "reset":
                return handleResetCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "create":
                return handleCreateCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "reload":
                return handleReloadCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "help":
            default:
                showHelp(sender);
                return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("frizzleneco.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete for subcommands
            String partialCommand = args[0].toLowerCase();
            List<String> subCommands = Arrays.asList("give", "take", "set", "reset", "create", "reload", "help");
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partialCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("give") || subCommand.equals("take") || subCommand.equals("set") || subCommand.equals("reset")) {
                if (args.length == 2) {
                    // Tab complete for player names
                    String partialName = args[1].toLowerCase();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(partialName)) {
                            completions.add(player.getName());
                        }
                    }
                } else if (args.length == 3 && !subCommand.equals("reset")) {
                    // Tab complete for amount (except reset)
                    completions.add("10");
                    completions.add("100");
                    completions.add("1000");
                } else if ((args.length == 4 && !subCommand.equals("reset")) || (args.length == 3 && subCommand.equals("reset"))) {
                    // Tab complete for currency
                    String partialCurrency = args[args.length - 1].toLowerCase();
                    for (Currency currency : economyManager.getCurrencies()) {
                        if (currency.getId().toLowerCase().startsWith(partialCurrency)) {
                            completions.add(currency.getId());
                        }
                    }
                }
            } else if (subCommand.equals("create")) {
                if (args.length == 2) {
                    // Tab complete for currency ID
                    completions.add("newcurrency");
                } else if (args.length == 3) {
                    // Tab complete for currency name
                    completions.add("NewCurrency");
                } else if (args.length == 4) {
                    // Tab complete for currency symbol
                    completions.add("$");
                    completions.add("€");
                    completions.add("£");
                } else if (args.length == 5) {
                    // Tab complete for initial balance
                    completions.add("100");
                    completions.add("500");
                    completions.add("1000");
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
        MessageUtil.sendInfo(sender, "=== Economy Admin Commands ===");
        MessageUtil.sendInfo(sender, "/ecoadmin give <player> <amount> [currency] - Give money to a player");
        MessageUtil.sendInfo(sender, "/ecoadmin take <player> <amount> [currency] - Take money from a player");
        MessageUtil.sendInfo(sender, "/ecoadmin set <player> <amount> [currency] - Set a player's balance");
        MessageUtil.sendInfo(sender, "/ecoadmin reset <player> [currency] - Reset a player's balance to initial value");
        MessageUtil.sendInfo(sender, "/ecoadmin create <id> <name> <symbol> <initialBalance> - Create a new currency");
        MessageUtil.sendInfo(sender, "/ecoadmin reload - Reload the plugin configuration");
    }
    
    /**
     * Handles the give command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(sender, "Usage: /ecoadmin give <player> <amount> [currency]");
            return true;
        }
        
        // Find target player
        String playerName = args[0];
        OfflinePlayer target = Bukkit.getPlayerExact(playerName);
        
        if (target == null) {
            target = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .findFirst().orElse(null);
        }
        
        if (target == null) {
            MessageUtil.sendError(sender, "Player not found: " + playerName);
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
        
        // Ensure player has an account
        UUID targetUuid = target.getUniqueId();
        if (!economyManager.hasAccount(targetUuid, currency)) {
            economyManager.createAccount(targetUuid, target.getName(), currency);
        }
        
        // Give money to player
        boolean success = economyManager.deposit(targetUuid, amount, currency);
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Gave " + currency.format(amount) + " to " + target.getName());
            
            // Notify player if online
            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null && targetPlayer.isOnline()) {
                MessageUtil.sendSuccess(targetPlayer, "You received " + currency.format(amount) + " from an admin");
            }
        } else {
            MessageUtil.sendError(sender, "Failed to give money to player. Please try again.");
        }
        
        return true;
    }
    
    /**
     * Handles the take command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleTakeCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(sender, "Usage: /ecoadmin take <player> <amount> [currency]");
            return true;
        }
        
        // Find target player
        String playerName = args[0];
        OfflinePlayer target = Bukkit.getPlayerExact(playerName);
        
        if (target == null) {
            target = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .findFirst().orElse(null);
        }
        
        if (target == null) {
            MessageUtil.sendError(sender, "Player not found: " + playerName);
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
        
        // Check if player has an account
        UUID targetUuid = target.getUniqueId();
        if (!economyManager.hasAccount(targetUuid, currency)) {
            MessageUtil.sendError(sender, target.getName() + " doesn't have an account for " + currency.getName());
            return true;
        }
        
        // Take money from player
        boolean success = economyManager.withdraw(targetUuid, amount, currency);
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Took " + currency.format(amount) + " from " + target.getName());
            
            // Notify player if online
            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null && targetPlayer.isOnline()) {
                MessageUtil.sendWarning(targetPlayer, "An admin took " + currency.format(amount) + " from your account");
            }
        } else {
            MessageUtil.sendError(sender, "Failed to take money from player. They may not have enough funds.");
        }
        
        return true;
    }
    
    /**
     * Handles the set command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(sender, "Usage: /ecoadmin set <player> <amount> [currency]");
            return true;
        }
        
        // Find target player
        String playerName = args[0];
        OfflinePlayer target = Bukkit.getPlayerExact(playerName);
        
        if (target == null) {
            target = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .findFirst().orElse(null);
        }
        
        if (target == null) {
            MessageUtil.sendError(sender, "Player not found: " + playerName);
            return true;
        }
        
        // Parse amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
            if (amount.compareTo(BigDecimal.ZERO) < 0 && !sender.hasPermission("frizzleneco.admin.negative")) {
                MessageUtil.sendError(sender, "Amount cannot be negative without the frizzleneco.admin.negative permission");
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
        
        // Ensure player has an account
        UUID targetUuid = target.getUniqueId();
        if (!economyManager.hasAccount(targetUuid, currency)) {
            economyManager.createAccount(targetUuid, target.getName(), currency);
        }
        
        // Get current balance
        BigDecimal currentBalance = economyManager.getBalance(targetUuid, currency);
        
        // Set player's balance
        boolean success;
        if (amount.compareTo(currentBalance) > 0) {
            // Need to deposit
            success = economyManager.deposit(targetUuid, amount.subtract(currentBalance), currency);
        } else if (amount.compareTo(currentBalance) < 0) {
            // Need to withdraw
            success = economyManager.withdraw(targetUuid, currentBalance.subtract(amount), currency);
        } else {
            // No change needed
            success = true;
        }
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Set " + target.getName() + "'s balance to " + currency.format(amount));
            
            // Notify player if online
            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null && targetPlayer.isOnline()) {
                MessageUtil.sendWarning(targetPlayer, "An admin set your balance to " + currency.format(amount));
            }
        } else {
            MessageUtil.sendError(sender, "Failed to set player's balance. Please try again.");
        }
        
        return true;
    }
    
    /**
     * Handles the reset command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleResetCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtil.sendError(sender, "Usage: /ecoadmin reset <player> [currency]");
            return true;
        }
        
        // Find target player
        String playerName = args[0];
        OfflinePlayer target = Bukkit.getPlayerExact(playerName);
        
        if (target == null) {
            target = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .findFirst().orElse(null);
        }
        
        if (target == null) {
            MessageUtil.sendError(sender, "Player not found: " + playerName);
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
        
        // Reset player's account
        UUID targetUuid = target.getUniqueId();
        boolean success = economyManager.createAccount(targetUuid, target.getName(), currency);
        
        if (success) {
            MessageUtil.sendSuccess(sender, "Reset " + target.getName() + "'s balance to " + 
                    currency.format(currency.getInitialBalance()));
            
            // Notify player if online
            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null && targetPlayer.isOnline()) {
                MessageUtil.sendWarning(targetPlayer, "An admin reset your balance to " + 
                        currency.format(currency.getInitialBalance()));
            }
        } else {
            MessageUtil.sendError(sender, "Failed to reset player's balance. Please try again.");
        }
        
        return true;
    }
    
    /**
     * Handles the create command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            MessageUtil.sendError(sender, "Usage: /ecoadmin create <id> <name> <symbol> <initialBalance>");
            return true;
        }
        
        String id = args[0];
        String name = args[1];
        String symbol = args[2];
        
        // Parse initial balance
        BigDecimal initialBalance;
        try {
            initialBalance = new BigDecimal(args[3]);
            if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
                MessageUtil.sendError(sender, "Initial balance cannot be negative");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(sender, "Invalid initial balance: " + args[3]);
            return true;
        }
        
        // Check if currency already exists
        if (economyManager.getCurrency(id) != null) {
            MessageUtil.sendError(sender, "Currency with ID '" + id + "' already exists");
            return true;
        }
        
        // Create new currency
        Currency currency = Currency.builder()
                .id(id)
                .name(name)
                .symbol(symbol)
                .initialBalance(initialBalance)
                .build();
        
        // Save currency to config
        plugin.getConfigManager().saveCurrency(currency);
        
        // Reload economy manager to load the new currency
        economyManager.initialize();
        
        MessageUtil.sendSuccess(sender, "Created new currency: " + name + " (" + symbol + ")");
        
        return true;
    }
    
    /**
     * Handles the reload command
     * @param sender the command sender
     * @param args the command arguments
     * @return true if the command was handled
     */
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        // Save all data first
        economyManager.shutdown();
        
        // Reload config
        plugin.getConfigManager().loadConfigs();
        
        // Reinitialize economy
        economyManager.initialize();
        
        MessageUtil.sendSuccess(sender, "FrizzlenEco configuration reloaded");
        
        return true;
    }
} 