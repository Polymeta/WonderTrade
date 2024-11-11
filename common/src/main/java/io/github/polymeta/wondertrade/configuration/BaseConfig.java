package io.github.polymeta.wondertrade.configuration;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.polymeta.wondertrade.WonderTrade;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BaseConfig {
    public static Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public int poolSize = 5;
    public boolean cooldownEnabled = true;
    public int cooldown = 5;
    public List<String> blacklist = new ArrayList<>();
    public int poolMinLevel = 1;
    public int poolMaxLevel = 100;
    public boolean adjustNewPokemonToLevelRange = false;

    public MessageConfig messages = new MessageConfig();

    public static class MessageConfig {
        public String wonderTradeFeedback = "<gray>[<white>Wonder<red>Trade<gray>] <white>Are you sure you want to trade your" +
                " <aqua>lvl <level> <pokemon> (<species>)</aqua>?<wtconfirm><yellow> Click here to confirm!</wtconfirm>";
        public String cooldownFeedback = "<gray>[<white>Wonder<red>Trade<gray>] <red>You are on cooldown!";
        public String pokemonNotAllowed = "<gray>[<white>Wonder<red>Trade<gray>] <red>You cannot trade this pokemon!";
        public String successFeedback = "<gray>[<white>Wonder<red>Trade<gray>] <green>Successfully traded!";
        public String broadcastPokemonAdded = "<gray>[<white>Wonder<red>Trade<gray>]<white> <pokemon> (<species>) got added to the wondertrade pool!";
        public String broadcastShinyPokemonAdded = "<gray>[<white>Wonder<red>Trade<gray>]<yellow> Shiny <pokemon> (<species>) got added to the wondertrade pool!";


        public Component wonderTradeFeedback(Pokemon pokemon, int slot, RegistryAccess registryAccess) {
            var miniMessage = MiniMessage.builder()
                    .tags(TagResolver.builder()
                            .resolvers(TagResolver.standard())
                            .resolver(TagResolver.resolver("wtconfirm", Tag.styling(ClickEvent.runCommand("/wondertrade " + (slot + 1) + " --confirm"))))
                            .build())
                    .build();

            return TextUtil.styledText(miniMessage, this.wonderTradeFeedback, registryAccess,
                    Placeholder.unparsed("level", String.valueOf(pokemon.getLevel())),
                    Placeholder.unparsed("pokemon", pokemon.getDisplayName().getString()),
                    Placeholder.unparsed("species", pokemon.getSpecies().getName()));
        }

        public Component cooldownFeedback(RegistryAccess registryAccess) {
            return TextUtil.styledText(this.cooldownFeedback, registryAccess);
        }

        public Component successFeedback(RegistryAccess registryAccess) {
            return TextUtil.styledText(this.successFeedback, registryAccess);
        }

        public Component broadcastPokemon(Pokemon pokemon, RegistryAccess registryAccess) {
            var stringMessage = pokemon.getShiny() ? this.broadcastShinyPokemonAdded : broadcastPokemonAdded;
            if(stringMessage.isBlank() || stringMessage.isEmpty()) {
                return Component.empty();
            }
            return TextUtil.styledText(stringMessage, registryAccess,
                    Placeholder.unparsed("pokemon", pokemon.getDisplayName().getString()),
                    Placeholder.unparsed("species", pokemon.getSpecies().getName()));
        }

        public Component pokemonNotAllowed(RegistryAccess registryAccess) {
            return TextUtil.styledText(this.pokemonNotAllowed, registryAccess);
        }
    }
}
