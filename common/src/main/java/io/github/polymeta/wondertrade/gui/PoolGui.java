package io.github.polymeta.wondertrade.gui;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.item.PokemonItem;
import io.github.polymeta.wondertrade.WonderTrade;
import io.github.polymeta.wondertrade.util.GuiUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PoolGui implements MenuProvider
{
    private final SimpleContainer container = new SimpleContainer(9 * 6);
    private final ServerPlayer serverPlayer;
    private int pageNumber;
    private final int pageSize = 45;
    private final int totalPages = (int) Math.ceil((double) WonderTrade.pool.pokemon.size() / pageSize);

    public PoolGui(ServerPlayer serverPlayer, int pageNumber)
    {
        this.serverPlayer = serverPlayer;
        this.pageNumber = pageNumber;
        setupContainer();
    }

    private void setupContainer()
    {
        this.container.getItems().clear();
        var redBorder = new ItemStack(Items.RED_STAINED_GLASS_PANE);
        redBorder.set(DataComponents.CUSTOM_NAME, Component.empty());
        for(int i = 45; i < 54; i++)
        {
            this.container.getItems().set(i, redBorder);
        }
        var pageContent = WonderTrade.pool.pokemon.stream()
                                  .skip((long) this.pageNumber * this.pageSize)
                                  .limit(this.pageSize)
                                  .map(PokemonProperties.Companion::parse)
                                  .map(PokemonItem::from)
                                  .toList();

        for(int i = 0; i < pageContent.size(); i++)
        {
            this.container.getItems().set(i, pageContent.get(i));
        }

        if(this.pageNumber > 0)
        {
            GuiUtil.placeButton(WonderTrade.config.gui.prevPageButton, this.container, this.serverPlayer.registryAccess());
        }
        if(this.pageNumber < this.totalPages - 1)
        {
            GuiUtil.placeButton(WonderTrade.config.gui.nextPageButton, this.container, this.serverPlayer.registryAccess());
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player)
    {
        return new ChestMenu(MenuType.GENERIC_9x6, i, inventory, container, 6)
        {
            @Override
            public void clicked(int slot, int buttonNo, ClickType clickType, Player player)
            {
                if(WonderTrade.config.gui.prevPageButton != null && slot == WonderTrade.config.gui.prevPageButton.position
                           && pageNumber > 0)
                {
                    pageNumber--;
                    setupContainer();
                }
                if(WonderTrade.config.gui.nextPageButton != null && slot == WonderTrade.config.gui.nextPageButton.position
                           && pageNumber < totalPages - 1)
                {
                    pageNumber++;
                    setupContainer();
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

    @Override
    public @NotNull Component getDisplayName()
    {
        return WonderTrade.config.gui.poolWindowTitle(this.serverPlayer.registryAccess());
    }
}