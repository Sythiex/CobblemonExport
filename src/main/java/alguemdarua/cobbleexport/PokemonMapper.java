package alguemdarua.cobbleexport;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PokemonMapper {
    private static final List<StatLabel> STAT_ORDER = List.of(
        new StatLabel(Stats.HP, "HP"),
        new StatLabel(Stats.ATTACK, "Atk"),
        new StatLabel(Stats.DEFENCE, "Def"),
        new StatLabel(Stats.SPECIAL_ATTACK, "SpA"),
        new StatLabel(Stats.SPECIAL_DEFENCE, "SpD"),
        new StatLabel(Stats.SPEED, "Spe")
    );

    private static final List<String> KNOWN_SECOND_WORDS = knownSecondWords();
    private static final Map<String, String> MOVE_NAME_OVERRIDES = moveNameOverrides();

    private PokemonMapper() {
    }

    public static String toShowdown(Pokemon pokemon) {
        List<String> lines = new ArrayList<>();

        lines.add(buildFirstLine(pokemon));
        lines.add("Ability: " + formatName(pokemon.getAbility().getName()));
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
            lines.add("- " + formatMoveName(move.getName()));
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

    private static String formatMoveName(String internalName) {
        String name = internalName.toLowerCase(Locale.ROOT);
        String override = MOVE_NAME_OVERRIDES.get(name);
        if (override != null) {
            return override;
        }

        for (String word : KNOWN_SECOND_WORDS) {
            if (name.endsWith(word) && name.length() > word.length()) {
                String first = name.substring(0, name.length() - word.length());
                return capitalize(first) + " " + capitalize(word);
            }
        }

        return capitalize(name);
    }

    private static String capitalize(String word) {
        if (word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
    }

    private static List<String> knownSecondWords() {
        List<String> words = new ArrayList<>(List.of(
            "throw", "smash", "gun", "sight", "claw", "fang", "punch", "kick", "beam",
            "bolt", "blast", "ball", "wave", "pulse", "dance", "song", "tail", "whip",
            "seed", "leaf", "storm", "wind", "rain", "snow", "hail", "quake", "slide",
            "fall", "dive", "fly", "rush", "charge", "force", "power", "guard", "shield",
            "edge", "stone", "rock", "slam", "strike", "attack", "defense", "speed",
            "trick", "swap", "room", "terrain", "field", "trap", "web", "spin", "turn",
            "screech", "cry", "roar", "growl", "bite", "breath", "fire", "flame",
            "thunder", "shock", "spark", "freeze", "chill", "ice", "mist", "fog",
            "cloud", "shadow", "light", "flash", "glow", "ray", "burst", "bomb", "shot",
            "arrow", "blade", "sword", "cut", "slash", "chop", "split", "break", "crush",
            "press", "squeeze", "grip", "hold", "lock", "block", "wall", "screen", "coat",
            "veil", "cloak", "wrap", "bind", "dew", "focus", "first", "rage", "noise",
            "sound"
        ));
        words.sort(Comparator.comparingInt(String::length).reversed());
        return Collections.unmodifiableList(words);
    }

    private static Map<String, String> moveNameOverrides() {
        Map<String, String> names = new HashMap<>();
        names.put("rockthrow", "Rock Throw");
        names.put("rocksmash", "Rock Smash");
        names.put("watergun", "Water Gun");
        names.put("foresight", "Foresight");
        names.put("thunderbolt", "Thunderbolt");
        names.put("thunderpunch", "Thunder Punch");
        names.put("thundershock", "Thunder Shock");
        names.put("thunderwave", "Thunder Wave");
        names.put("firepunch", "Fire Punch");
        names.put("firespin", "Fire Spin");
        names.put("fireblast", "Fire Blast");
        names.put("flamethrower", "Flamethrower");
        names.put("icebeam", "Ice Beam");
        names.put("icepunch", "Ice Punch");
        names.put("iceshard", "Ice Shard");
        names.put("solarbeam", "Solar Beam");
        names.put("solarblade", "Solar Blade");
        names.put("shadowball", "Shadow Ball");
        names.put("shadowclaw", "Shadow Claw");
        names.put("shadowpunch", "Shadow Punch");
        names.put("shadowsneak", "Shadow Sneak");
        names.put("darkpulse", "Dark Pulse");
        names.put("aurasphere", "Aura Sphere");
        names.put("focusblast", "Focus Blast");
        names.put("focuspunch", "Focus Punch");
        names.put("energyball", "Energy Ball");
        names.put("seedbomb", "Seed Bomb");
        names.put("leafblade", "Leaf Blade");
        names.put("leafstorm", "Leaf Storm");
        names.put("moonblast", "Moonblast");
        names.put("moonlight", "Moonlight");
        names.put("earthquake", "Earthquake");
        names.put("earthpower", "Earth Power");
        names.put("stoneedge", "Stone Edge");
        names.put("rockslide", "Rock Slide");
        names.put("rocktomb", "Rock Tomb");
        names.put("stealthrock", "Stealth Rock");
        names.put("ironhead", "Iron Head");
        names.put("irontail", "Iron Tail");
        names.put("irondefense", "Iron Defense");
        names.put("flashcannon", "Flash Cannon");
        names.put("steelwing", "Steel Wing");
        names.put("metalclaw", "Metal Claw");
        names.put("closecombat", "Close Combat");
        names.put("crosschop", "Cross Chop");
        names.put("crosspoison", "Cross Poison");
        names.put("dragonpulse", "Dragon Pulse");
        names.put("dragonclaw", "Dragon Claw");
        names.put("dragondance", "Dragon Dance");
        names.put("dragonbreath", "Dragon Breath");
        names.put("dragonrush", "Dragon Rush");
        names.put("dragontail", "Dragon Tail");
        names.put("outrage", "Outrage");
        names.put("dracometeor", "Draco Meteor");
        names.put("sludgebomb", "Sludge Bomb");
        names.put("sludgewave", "Sludge Wave");
        names.put("poisonjab", "Poison Jab");
        names.put("psychic", "Psychic");
        names.put("psyshock", "Psyshock");
        names.put("psywave", "Psywave");
        names.put("zenheadbutt", "Zen Headbutt");
        names.put("futuresight", "Future Sight");
        names.put("calmmind", "Calm Mind");
        names.put("nastyplot", "Nasty Plot");
        names.put("swordsdance", "Swords Dance");
        names.put("bulkup", "Bulk Up");
        names.put("shellsmash", "Shell Smash");
        names.put("quiverdance", "Quiver Dance");
        names.put("willowisp", "Will-O-Wisp");
        names.put("toxicspikes", "Toxic Spikes");
        names.put("stickyweb", "Sticky Web");
        names.put("rapidspin", "Rapid Spin");
        names.put("uturn", "U-turn");
        names.put("voltswitch", "Volt Switch");
        names.put("flipturn", "Flip Turn");
        names.put("trickroom", "Trick Room");
        names.put("tailwind", "Tailwind");
        names.put("bravebird", "Brave Bird");
        names.put("flareblitz", "Flare Blitz");
        names.put("wildcharge", "Wild Charge");
        names.put("headsmash", "Head Smash");
        names.put("doubleedge", "Double-Edge");
        names.put("bodyslam", "Body Slam");
        names.put("bodypress", "Body Press");
        names.put("heavyslam", "Heavy Slam");
        names.put("heatwave", "Heat Wave");
        names.put("airslash", "Air Slash");
        names.put("bugbuzz", "Bug Buzz");
        names.put("signalbeam", "Signal Beam");
        names.put("megahorn", "Megahorn");
        names.put("xscissor", "X-Scissor");
        names.put("nightslash", "Night Slash");
        names.put("nightshade", "Night Shade");
        names.put("suckerpunch", "Sucker Punch");
        names.put("knockoff", "Knock Off");
        names.put("foulplay", "Foul Play");
        names.put("scald", "Scald");
        names.put("surf", "Surf");
        names.put("hydropump", "Hydro Pump");
        names.put("aquajet", "Aqua Jet");
        names.put("aquatail", "Aqua Tail");
        names.put("waterfall", "Waterfall");
        names.put("liquidation", "Liquidation");
        names.put("muddywater", "Muddy Water");
        names.put("highhorsepower", "High Horsepower");
        names.put("superpower", "Superpower");
        names.put("drainpunch", "Drain Punch");
        names.put("machpunch", "Mach Punch");
        names.put("bulletpunch", "Bullet Punch");
        names.put("vacuumwave", "Vacuum Wave");
        names.put("extremespeed", "Extreme Speed");
        names.put("quickattack", "Quick Attack");
        names.put("protect", "Protect");
        names.put("detect", "Detect");
        names.put("substitute", "Substitute");
        names.put("recover", "Recover");
        names.put("roost", "Roost");
        names.put("synthesis", "Synthesis");
        names.put("toxic", "Toxic");
        names.put("spore", "Spore");
        names.put("sleeppowder", "Sleep Powder");
        names.put("stunspore", "Stun Spore");
        names.put("leechseed", "Leech Seed");
        names.put("gigadrain", "Giga Drain");
        names.put("megadrain", "Mega Drain");
        names.put("grassknot", "Grass Knot");
        names.put("powerwhip", "Power Whip");
        names.put("woodhammer", "Wood Hammer");
        names.put("hornleech", "Horn Leech");
        names.put("playrough", "Play Rough");
        names.put("dazzlinggleam", "Dazzling Gleam");
        names.put("drainingkiss", "Draining Kiss");
        names.put("mysticalfire", "Mystical Fire");
        names.put("fierydance", "Fiery Dance");
        names.put("lavaplume", "Lava Plume");
        names.put("overheat", "Overheat");
        names.put("blueflare", "Blue Flare");
        names.put("sacredfire", "Sacred Fire");
        names.put("vcreate", "V-create");
        names.put("thunderclap", "Thunderclap");
        names.put("voltabsorb", "Volt Absorb");
        names.put("waterpulse", "Water Pulse");
        names.put("darkestlariat", "Darkest Lariat");
        names.put("spiritshackle", "Spirit Shackle");
        names.put("poltergeist", "Poltergeist");
        names.put("phantomforce", "Phantom Force");
        names.put("hex", "Hex");
        names.put("hypnosis", "Hypnosis");
        names.put("dreameater", "Dream Eater");
        names.put("trickorttreat", "Trick-or-Treat");
        names.put("stoneaxe", "Stone Axe");
        names.put("headlongrush", "Headlong Rush");
        names.put("wavecrash", "Wave Crash");
        names.put("bitterblade", "Bitter Blade");
        names.put("ragingbolt", "Raging Bolt");
        names.put("psyblade", "Psyblade");
        names.put("makeitrain", "Make It Rain");
        names.put("bloodmoon", "Blood Moon");
        names.put("ivycudgel", "Ivy Cudgel");
        names.put("matchagotcha", "Matcha Gotcha");
        names.put("syrupbomb", "Syrup Bomb");
        names.put("electrodrift", "Electro Drift");
        names.put("icespinner", "Ice Spinner");
        names.put("trailblaze", "Trailblaze");
        names.put("ragefist", "Rage Fist");
        names.put("lastrespects", "Last Respects");
        names.put("populationbomb", "Population Bomb");
        names.put("tidalwave", "Tidal Wave");
        names.put("lifedew", "Life Dew");
        names.put("laserfocus", "Laser Focus");
        names.put("mefirst", "Me First");
        return Collections.unmodifiableMap(names);
    }

    private record StatLabel(Stat stat, String label) {
    }
}
