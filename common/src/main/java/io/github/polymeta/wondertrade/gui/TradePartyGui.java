package io.github.polymeta.wondertrade.gui;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.polymeta.wondertrade.WonderTrade;
import io.github.polymeta.wondertrade.util.GuiUtil;
import io.github.polymeta.wondertrade.util.TextUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradePartyGui implements MenuProvider
{
    private final Container container = new SimpleContainer(9 * 3);
    private final ServerPlayer serverPlayer;

    public TradePartyGui(ServerPlayer serverPlayer)
    {
        this.serverPlayer = serverPlayer;
        setupContainer();
    }

    private void setupContainer()
    {
        GuiUtil.checkAndPlaceBorders(this.container);
        GuiUtil.placeButton(WonderTrade.config.gui.cancelButton, this.container, this.serverPlayer.registryAccess());

        var emptyBlock = new ItemStack(CobblemonItems.POKE_BALL);
        emptyBlock.set(DataComponents.CUSTOM_NAME, TextUtil.styledText("<red>empty...", this.serverPlayer));
        emptyBlock.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        emptyBlock.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);

        var pokemonStorage = Cobblemon.INSTANCE.getStorage().getParty(this.serverPlayer).toGappyList();
        for(int i = 0; i < 3; i++)
        {
            var poke = pokemonStorage.get(i);
            if(poke != null)
            {
                container.setItem(i + 10, PokemonItem.from(pokemonStorage.get(i)));
            }
            else
            {
                container.setItem(i + 10, emptyBlock);
            }
        }
        for(int i = 3; i < 6; i++)
        {
            var poke = pokemonStorage.get(i);
            if(poke != null)
            {
                container.setItem(i + 11, PokemonItem.from(pokemonStorage.get(i)));
            }
            else
            {
                container.setItem(i + 11, emptyBlock);
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player)
    {
        return new ChestMenu(MenuType.GENERIC_9x3, i, inventory, container, 3)
        {
            @Override
            public void clicked(int slot, int buttonNo, ClickType clickType, Player player)
            {
                PlayerPartyStore storage = Cobblemon.INSTANCE.getStorage().getParty((ServerPlayer) player);
                var pokemon = switch(slot)
                {
                    case 10 -> storage.get(0);
                    case 11 -> storage.get(1);
                    case 12 -> storage.get(2);
                    case 14 -> storage.get(3);
                    case 15 -> storage.get(4);
                    case 16 -> storage.get(5);
                    default -> null;
                };
                if(pokemon != null)
                {
                    openConfirmationGui(pokemon);
                }
                if(WonderTrade.config.gui.cancelButton != null && slot == WonderTrade.config.gui.cancelButton.position)
                {
                    player.closeContainer();
                }
            }

            @Override
            public boolean stillValid(Player player)
            {
                return true;
            }

            @Override
            public @NotNull ItemStack quickMoveStack(Player player, int i)
            {
                return ItemStack.EMPTY;
            }
        };
    }

    public void openConfirmationGui(Pokemon pokemon)
    {
        this.serverPlayer.openMenu(new ConfirmationGui(this.serverPlayer, pokemon));
    }

    @Override
    public @NotNull Component getDisplayName()
    {
        return WonderTrade.config.gui.mainWindowTitle(this.serverPlayer.registryAccess());
    }
}
