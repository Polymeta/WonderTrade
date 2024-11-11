package io.github.polymeta.wondertrade.util;

import io.github.polymeta.wondertrade.WonderTrade;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TextUtil
{
    public static Component styledText(String input, ServerPlayer player, TagResolver... tags)
    {
        return styledText(input, player.registryAccess(), tags);
    }

    public static Component styledText(String input, RegistryAccess registryAccess, TagResolver... tags)
    {
        return styledText(WonderTrade.miniMessage, input, registryAccess, tags);
    }

    public static Component styledText(MiniMessage instance, String input, RegistryAccess registryAccess, TagResolver... tags)
    {
        var text = instance.deserialize(input, tags);
        return Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text), registryAccess);
    }
}
