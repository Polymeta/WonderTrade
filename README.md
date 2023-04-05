# WonderTrade
WonderTrader for Cobblemon working on both fabric and forge!

## Commands & Permissions

| Command                                         | Permission                         |
|-------------------------------------------------|------------------------------------|
| `/wondertrade <slot> [--confirm]` OR `/wt`      | `wondertrade.command.trade.base`   |
| Bypass wondertrade cooldown                     | `wondertrade.command.trade.bypass` |
| `/regenerate [<poolSize>]` OR `/regeneratepool` | `wondertrade.command.regenerate`   |

## Config explanation

### General

- `poolSize` - How big the wondertrade pool is supposed to be. Can be overridden with the regenerate command if desired. Only used when pool is empty.
- `cooldownEnabled` - Whether to enable cool-downs on the wondertrade command
- `cooldown` - Cool-down in **MINUTES**. Only used if above value is set to `true`
- `blacklist` - A list of Pokémon properties that can not be wondertraded, an example entry would be "cobblemon:charmander", but you can even get more complex as we use the pokemon properties under the hood. Refer to the wondertrade pool for more examples!

### Message Config

As a preface, this plugin uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) to parse these messages.
It's a powerful api allowing for various formatting options for you as user.
Refer to the default messages to see what placeholders are allowed where.

- `wonderTradeFeedback` - The confirmation question that gets sent to the player when they do /wondertrade without confirmation
  - be sure to leave the `<wtconfirm>` tag in as everything in that allows the player to click it to confirm the trade
- `cooldownFeedback` - message that gets sent when the player is on cooldown
- `pokemonNotAllowed` - message that gets sent when a player attempts to trade a forbidden pokemon
- `successFeedback` - message that get sent on successful trade
- `broadcastPokemonAdded` and `broadcastShinyPokemonAdded` - broadcast messages that gets sent to all players when Pokémon get added to the trade pool
  - Don't want to broadcast normal Pokémon or at all? - Just set the specific broadcast message to something blank like "", which then turns broadcasting for this off
