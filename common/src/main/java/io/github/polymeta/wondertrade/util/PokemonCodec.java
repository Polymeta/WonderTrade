package io.github.polymeta.wondertrade.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import java.util.UUID;

/**
 * Pool entries use Cobblemon's own codec. A property string cannot round-trip a
 * regional form: applying properties recomputes aspects from species features
 * instead of restoring them, so the pool reset forms back to base (issue #8).
 */
public final class PokemonCodec {

    private PokemonCodec() {}

    public static JsonElement encode(Pokemon pokemon) {
        return Pokemon.getCODEC().encodeStart(JsonOps.INSTANCE, pokemon).getOrThrow();
    }

    /** Strings are pool entries written before the codec; parsed the old way. */
    public static Pokemon decode(JsonElement entry) {
        if (entry.isJsonPrimitive()) {
            return PokemonProperties.Companion.parse(entry.getAsString()).create();
        }
        var pokemon = Pokemon.getCODEC().parse(JsonOps.INSTANCE, entry).getOrThrow();
        // The codec restores the depositor's UUID, which must not reach a second party.
        pokemon.setUuid(UUID.randomUUID());
        return pokemon;
    }
}
