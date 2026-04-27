package alguemdarua.cobbleexport;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PokemonMapper {
    private static final List<StatLabel> STAT_ORDER = List.of(
        new StatLabel(Stats.HP, "HP"),
        new StatLabel(Stats.ATTACK, "Atk"),
        new StatLabel(Stats.DEFENCE, "Def"),
        new StatLabel(Stats.SPECIAL_ATTACK, "SpA"),
        new StatLabel(Stats.SPECIAL_DEFENCE, "SpD"),
        new StatLabel(Stats.SPEED, "Spe")
    );

    private PokemonMapper() {
    }

    public static String toShowdown(Pokemon pokemon) {
        List<String> lines = new ArrayList<>();

        lines.add(buildFirstLine(pokemon));
        lines.add("Ability: " + cleanDisplayName(pokemon.getAbility().getDisplayName(), formatName(pokemon.getAbility().getName())));
        lines.add("Tera Type: " + formatName(pokemon.getTeraType().getName()));

        if (pokemon.getLevel() != 100) {
            lines.add("Level: " + pokemon.getLevel());
        }

        if (pokemon.getShiny()) {
            lines.add("Shiny: Yes");
        }

        List<String> evParts = new ArrayList<>();
        for (StatLabel statLabel : STAT_ORDER) {
            Integer value = pokemon.getEvs().get(statLabel.stat());
            evParts.add((value == null ? 0 : value) + " " + statLabel.label());
        }
        lines.add("EVs: " + String.join(" / ", evParts));

        String natureName = pokemon.getEffectiveNature().getName().getPath();
        lines.add(formatName(natureName) + " Nature");

        List<String> ivParts = new ArrayList<>();
        for (StatLabel statLabel : STAT_ORDER) {
            int value = pokemon.getIvs().getEffectiveBattleIV(statLabel.stat());
            ivParts.add(value + " " + statLabel.label());
        }
        lines.add("IVs: " + String.join(" / ", ivParts));

        for (Move move : pokemon.getMoveSet().getMoves()) {
            lines.add("- " + cleanDisplayName(move.getDisplayName().getString(), formatName(move.getName())));
        }

        return String.join("\n", lines);
    }

    private static String buildFirstLine(Pokemon pokemon) {
        String speciesName = formatName(pokemon.getSpecies().getName().toLowerCase(Locale.ROOT));
        String nickname = pokemon.getNickname() == null ? null : pokemon.getNickname().getString();

        StringBuilder builder = new StringBuilder();
        if (nickname != null && !nickname.equals(speciesName)) {
            builder.append(nickname).append(" (").append(speciesName).append(")");
        } else {
            builder.append(speciesName);
        }

        if (pokemon.getGender() == Gender.MALE) {
            builder.append(" (M)");
        } else if (pokemon.getGender() == Gender.FEMALE) {
            builder.append(" (F)");
        }

        String item = getHeldItemName(pokemon);
        if (item != null) {
            builder.append(" @ ").append(formatName(item));
        }

        return builder.toString();
    }

    private static String getHeldItemName(Pokemon pokemon) {
        ItemStack stack = pokemon.heldItem();
        if (stack.isEmpty()) {
            return null;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static String formatName(String name) {
        String[] words = name.split("[_-]");
        List<String> formatted = new ArrayList<>(words.length);
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.add(capitalize(word));
            }
        }
        return String.join(" ", formatted);
    }

    private static String capitalize(String word) {
        if (word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
    }

    private static String cleanDisplayName(String displayName, String fallback) {
        String trimmed = displayName == null ? "" : displayName.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private record StatLabel(Stat stat, String label) {
    }
}
