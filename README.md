# FrizzlenEco

A comprehensive economy plugin for Minecraft 1.21 Paper Spigot servers.

## Features

- **Multiple Currency Support**: Create and manage multiple currencies with different properties
- **Player Wallets**: Players can have accounts in different currencies
- **Admin Tools**: Powerful commands for server administrators to manage the economy
- **API**: Comprehensive API for other plugins to interact with the economy
- **Database Support**: Store economy data in SQLite or MySQL
- **Metrics**: Track economy statistics and transactions

## Commands

### Player Commands

- `/money [player] [currency]` - Check your balance or another player's balance
- `/balance [player] [currency]` - Alias for /money
- `/pay <player> <amount> [currency]` - Pay another player

### Admin Commands

- `/ecoadmin give <player> <amount> [currency]` - Give money to a player
- `/ecoadmin take <player> <amount> [currency]` - Take money from a player
- `/ecoadmin set <player> <amount> [currency]` - Set a player's balance
- `/ecoadmin reset <player> [currency]` - Reset a player's balance to initial value
- `/ecoadmin create <id> <name> <symbol> <initialBalance>` - Create a new currency
- `/ecoadmin reload` - Reload the plugin configuration

## Permissions

- `frizzleneco.admin` - Access to administrative commands
- `frizzleneco.admin.negative` - Allows setting negative balances
- `frizzleneco.balance` - Check your own balance
- `frizzleneco.balance.others` - Check other players' balances
- `frizzleneco.pay` - Pay other players

## Configuration

The plugin is highly configurable. See `config.yml` for database settings, general settings, and transaction settings.

### Currency Configuration

Currencies are configured in `currencies.yml`. Each currency has the following properties:

- `id` - Unique identifier for the currency
- `name` - Display name of the currency
- `symbol` - Currency symbol (e.g. $)
- `format` - Format string for displaying amounts (e.g. "%s%s")
- `decimalPlaces` - Number of decimal places to display
- `isDefault` - Whether this is the default currency
- `initialBalance` - Balance given to new accounts
- `minBalance` - Minimum allowed balance
- `maxBalance` - Maximum allowed balance
- `allowNegative` - Whether negative balances are allowed
- `isEnabled` - Whether this currency is enabled

## API for Developers

FrizzlenEco provides a comprehensive API for other plugins to interact with the economy. The main interface is `EconomyProvider`, which can be accessed through the Bukkit Services Manager:

```java
EconomyProvider economy = Bukkit.getServicesManager().getRegistration(EconomyProvider.class).getProvider();
```

The API provides methods for:

- Checking balances
- Depositing and withdrawing money
- Transferring money between accounts
- Creating accounts
- Getting currency information

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Start or restart your server
4. Configure the plugin in `plugins/FrizzlenEco/config.yml`

## Building from Source

1. Clone the repository
2. Run `mvn clean package`
3. The built JAR will be in the `target` folder

## License

This project is licensed under the MIT License - see the LICENSE file for details. 