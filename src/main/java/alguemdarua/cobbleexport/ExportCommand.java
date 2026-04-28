package alguemdarua.cobbleexport;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ExportCommand {
    private ExportCommand() {
    }

    public static void register(RegisterClientCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> root = Commands.literal("cobble_export")
            .then(Commands.literal("help").executes(ExportCommand::sendHelpMessage))
            .then(Commands.literal("party")
                .executes(ctx -> exportParty(ctx, false))
                .then(Commands.literal("new").executes(ctx -> exportParty(ctx, true))))
            .then(Commands.literal("box")
                .then(Commands.literal("all")
                    .executes(ctx -> exportAllBoxes(ctx, false))
                    .then(Commands.literal("new")
                        .executes(ctx -> exportAllBoxes(ctx, true))))
                .then(Commands.argument("boxNum", IntegerArgumentType.integer(1, 200))
                    .executes(ctx -> exportBox(ctx, IntegerArgumentType.getInteger(ctx, "boxNum"), false))
                    .then(Commands.literal("new")
                        .executes(ctx -> exportBox(ctx, IntegerArgumentType.getInteger(ctx, "boxNum"), true)))))
            .build();

        event.getDispatcher().getRoot().addChild(root);
    }

    private static int sendHelpMessage(CommandContext<CommandSourceStack> ctx) {
        MutableComponent message = Component.empty()
            .append(Component.literal("\n---------- [ ").withStyle(ChatFormatting.DARK_GRAY))
            .append(text("Cobblemon Export", ChatFormatting.AQUA, true))
            .append(Component.literal(" ] ----------\n").withStyle(ChatFormatting.DARK_GRAY))
            .append(text("\nHello! This mod was made by ", ChatFormatting.GRAY, false))
            .append(text("AlguemDaRua", ChatFormatting.GOLD, true))
            .append(text(".\nIt's a small mod I created to access Pokemon info without having to check them one by one. I hope you enjoy it!\nPlease leave feedback so I can make it better. Thanks!\n\n", ChatFormatting.GRAY, false).withStyle(ChatFormatting.ITALIC))
            .append(text("Available Commands:\n", ChatFormatting.WHITE, true))
            .append(commandText("/cobble_export party", "Overwrites the 'party_export.txt' file."))
            .append(Component.literal("\n"))
            .append(commandText("/cobble_export party new", "Creates a NEW file (e.g. 'party_export_1.txt')."))
            .append(Component.literal("\n"))
            .append(commandText("/cobble_export box <num>", "Exports a specific PC box to a text file."))
            .append(Component.literal("\n"))
            .append(commandText("/cobble_export box <num> new", "Exports a PC box to a NEW text file."))
            .append(Component.literal("\n"))
            .append(commandText("/cobble_export box all", "Overwrites the 'boxes_export.txt' file."))
            .append(Component.literal("\n"))
            .append(commandText("/cobble_export box all new", "Creates a NEW all-boxes file."))
            .append(text("\n\nFiles are saved in: ", ChatFormatting.WHITE, false))
            .append(text(".minecraft/cobblemon_exports/", ChatFormatting.GREEN, false))
            .append(Component.literal("\n----------------------------------------").withStyle(ChatFormatting.DARK_GRAY));

        ctx.getSource().sendSuccess(() -> message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent text(String content, ChatFormatting color, boolean bold) {
        MutableComponent text = Component.literal(content).withStyle(color);
        return bold ? text.withStyle(ChatFormatting.BOLD) : text;
    }

    private static MutableComponent commandText(String command, String description) {
        return Component.literal(" > ")
            .withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(command).withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to type")))))
            .append(Component.literal("\n     " + description).withStyle(ChatFormatting.GRAY));
    }

    private static int exportParty(CommandContext<CommandSourceStack> ctx, boolean createNew) {
        List<String> exportData = extractPokemonFromList(CobblemonClient.INSTANCE.getStorage().getParty());

        if (exportData.isEmpty()) {
            sendError(ctx, "Your party is empty!");
            return 0;
        }

        saveShowdown(ctx.getSource(), "party_export", exportData, createNew);
        return Command.SINGLE_SUCCESS;
    }

    private static int exportBox(CommandContext<CommandSourceStack> ctx, int boxNum, boolean createNew) {
        List<ClientBox> boxes = getPcBoxesFromStorage();

        if (boxes == null) {
            sendError(ctx, "Could not find PC Storage. Please open your PC block once to load data.");
            return 0;
        }

        int index = boxNum - 1;
        if (index < 0 || index >= boxes.size()) {
            sendError(ctx, "Box " + boxNum + " does not exist (Max: " + boxes.size() + ").");
            return 0;
        }

        List<String> exportData = extractPokemonFromList(boxes.get(index));
        if (exportData.isEmpty()) {
            sendError(ctx, "Box " + boxNum + " is empty!");
            return 0;
        }

        saveShowdown(ctx.getSource(), "box_" + boxNum + "_export", exportData, createNew);
        return Command.SINGLE_SUCCESS;
    }

    private static int exportAllBoxes(CommandContext<CommandSourceStack> ctx, boolean createNew) {
        List<ClientBox> boxes = getPcBoxesFromStorage();

        if (boxes == null) {
            sendError(ctx, "Could not find PC Storage. Please open your PC block once to load data.");
            return 0;
        }

        List<String> exportData = new ArrayList<>();
        for (ClientBox box : boxes) {
            exportData.addAll(extractPokemonFromList(box));
        }

        if (exportData.isEmpty()) {
            sendError(ctx, "No Pokemon found in any loaded PC box!");
            return 0;
        }

        saveShowdown(ctx.getSource(), "boxes_export", exportData, createNew);
        return Command.SINGLE_SUCCESS;
    }

    private static List<String> extractPokemonFromList(Iterable<Pokemon> pokemonList) {
        List<String> data = new ArrayList<>();
        for (Pokemon pokemon : pokemonList) {
            if (pokemon != null) {
                data.add(PokemonMapper.toShowdown(pokemon));
            }
        }
        return data;
    }

    private static List<ClientBox> getPcBoxesFromStorage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }

        Map<UUID, ClientPC> pcStores = CobblemonClient.INSTANCE.getStorage().getPcStores();
        ClientPC pc = pcStores.get(minecraft.player.getUUID());
        if (pc == null) {
            pc = pcStores.values().stream().findFirst().orElse(null);
        }

        return pc == null ? null : pc.getBoxes();
    }

    private static void saveShowdown(CommandSourceStack source, String baseName, List<String> data, boolean createNew) {
        File exportDir = new File(Minecraft.getInstance().gameDirectory, "cobblemon_exports");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            sendError(source, "Failed to create export directory: " + exportDir.getAbsolutePath());
            return;
        }

        File file = createNew ? nextSnapshotFile(exportDir, baseName) : new File(exportDir, baseName + ".txt");

        try {
            Files.writeString(file.toPath(), String.join("\n\n", data), StandardCharsets.UTF_8);

            MutableComponent clickableText = Component.literal("[OPEN FILE]")
                .withStyle(style -> style
                    .withColor(ChatFormatting.YELLOW)
                    .withBold(true)
                    .withUnderlined(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open " + file.getName()))));

            MutableComponent message = Component.literal("[CobbleExport] Successfully saved to ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(file.getName()).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" "))
                .append(clickableText);

            source.sendSuccess(() -> message, false);
        } catch (IOException e) {
            sendError(source, "Failed to write file: " + e.getMessage());
        }
    }

    private static File nextSnapshotFile(File exportDir, String baseName) {
        int i = 1;
        File candidate = new File(exportDir, baseName + "_" + i + ".txt");
        while (candidate.exists()) {
            i++;
            candidate = new File(exportDir, baseName + "_" + i + ".txt");
        }
        return candidate;
    }

    private static void sendError(CommandContext<CommandSourceStack> ctx, String message) {
        sendError(ctx.getSource(), message);
    }

    private static void sendError(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal("[CobbleExport] " + message).withStyle(ChatFormatting.RED));
    }
}
