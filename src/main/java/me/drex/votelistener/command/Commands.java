package me.drex.votelistener.command;

import com.mojang.brigadier.CommandDispatcher;
import me.drex.votelistener.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class Commands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            literal("votelistener").requires(Permissions.require("votelistener.reload", 2)).then(
                literal("reload").executes(context -> {
                    if (ConfigManager.loadConfig()) {
                        context.getSource().sendSuccess(() -> Component.literal("Config reloaded successfully!"), false);
                    } else {
                        context.getSource().sendFailure(Component.literal("Failed to reload config, check console for further information!"));
                    }
                    return 1;
                })
            )
        );
    }

}
