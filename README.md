# WonderTrade
WonderTrader for Cobblemon working on both fabric and forge!

## Commands & Permissions

| Command                           | Permission                         |
|-----------------------------------|------------------------------------|
| `/wondertrade <slot> [--confirm]` | `wondertrade.command.trade.base`   |
| Bypass wondertrade cooldown       | `wondertrade.command.trade.bypass` |
| `/regenerate [<poolSize>]`        | `wondertrade.command.regenerate`   |

## Config explanation

- `poolSize` - How big the wondertrade pool is supposed to be. Can be overridden with the regenerate command if desired. Only used when pool is empty.
- `cooldownEnabled` - Whether to enable cool-downs on the wondertrade command
- `cooldown` - Cool-down in **MINUTES**. Only used if above value is set to `true`
