package io.github.polymeta.wondertrade.gui;

import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.polymeta.wondertrade.WonderTrade;
import io.github.polymeta.wondertrade.util.GuiUtil;
import io.github.polymeta.wondertrade.util.TradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class ConfirmationGui implements MenuProvider
{
    private final Container container = new SimpleContainer(9 * 3);
    private final ServerPlayer serverPlayer;
    private final Pokemon pokemon;

    public ConfirmationGui(ServerPlayer serverPlayer, Pokemon pokemon)
    {
        this.serverPlayer = serverPlayer;
        this.pokemon = pokemon;
        setupContainer();
    }

    private void setupContainer()
    {
        GuiUtil.checkAndPlaceBorders(this.container);
        GuiUtil.placeButton(WonderTrade.config.gui.denyButton, this.container, this.serverPlayer.registryAccess());
        GuiUtil.placeButton(WonderTrade.config.gui.confirmationButton, this.container, this.serverPlayer.registryAccess());
        container.setItem(13, PokemonItem.from(this.pokemon));
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
                if(WonderTrade.config.gui.denyButton != null && slot == WonderTrade.config.gui.denyButton.position)
                {
                    player.openMenu(new TradePartyGui((ServerPlayer) player));
                }
                else if(WonderTrade.config.gui.confirmationButton != null && slot == WonderTrade.config.gui.confirmationButton.position)
                {
                    TradeUtil.doWonderTrade((ServerPlayer) player, pokemon);
                    player.closeContainer();
                }
            }

            @Override
            public @NotNull ItemStack quickMoveStack(Player player, int i)
            {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player)
            {
                return true;
            }
        };
    }

    @Override
    public @NotNull Component getDisplayName()
    {
        return WonderTrade.config.gui.confirmationWindowTitle(this.serverPlayer.registryAccess());
    }
}
