package io.github.polymeta.wondertrade.util;

import io.github.polymeta.wondertrade.WonderTrade;
import io.github.polymeta.wondertrade.configuration.BaseConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiUtil
{
    public static void placeButton(BaseConfig.ButtonConfig input, Container container, RegistryAccess access)
    {
        if(input == null)
        {
            return;
        }
        var stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(input.item)));
        if(input.customName != null && !input.customName.isBlank())
        {
            stack.set(DataComponents.CUSTOM_NAME, TextUtil.styledText(input.customName, access));
        }
        container.setItem(input.position, stack);
    }

    public static void checkAndPlaceBorders(Container container)
    {
        if(!WonderTrade.config.gui.generateBorders)
        {
            return;
        }
        var redBorder = new ItemStack(Items.RED_STAINED_GLASS_PANE);
        var blackBorder = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        var whiteBorder = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
        redBorder.set(DataComponents.CUSTOM_NAME, Component.empty());
        blackBorder.set(DataComponents.CUSTOM_NAME, Component.empty());
        whiteBorder.set(DataComponents.CUSTOM_NAME, Component.empty());

        for(int i = 0; i < 9; i++)
        {
            container.setItem(i, redBorder);
        }
        for(int i = 9; i < 18; i++)
        {
            container.setItem(i, blackBorder);

        }
        for(int i = 18; i < 27; i++)
        {
            container.setItem(i, whiteBorder);
        }
    }
}
