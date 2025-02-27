/*
    Copyright (C) 2021 Maarten Magits

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.mmaarten.mmcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * created on 29/04/2021 by Mmaarten. Project: MmCommands
 */
@SuppressWarnings("unused")
public abstract class MmCommand {
    private final @NotNull List<MmCommand> subcommands;

    /**
     * Instantiates a new MmCommand.
     */
    public MmCommand() {
        this.subcommands = new ArrayList<>();
    }

    /**
     * Code to be ran when this MmCommand is called.
     *
     * @param sender  the sender
     * @param command the command
     * @param label   the label
     * @param args    the args
     */
    public abstract void onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args);

    /**
     * On tab complete list.
     *
     * @param sender  the sender
     * @param command the command
     * @param alias   the alias
     * @param args    the args
     * @return the list
     */
    public abstract @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args);

    /**
     * With sub command mm command.
     *
     * @param command the command
     * @return the mm command
     */
    public @NotNull MmCommand withSubCommand(@NotNull MmCommand command) {
        if (command.getClass().getAnnotation(MmCommandSignature.class) == null)
            throw new IllegalArgumentException(
                    "Class " + command.getClass().getName() + " must be annotated by a " + MmCommandSignature.class.getName() + " annotation"
            );
        MmCommandSignature sig = command.getClass().getAnnotation(MmCommandSignature.class);
        if (subCommandExists(command))
            throw new IllegalArgumentException("Cannot register " + sig.name() + ": Name already registered as subcommand");
        if (checkAliasNameConflict(command))
            throw new IllegalArgumentException("Cannot register " + sig.name() + ": Registers an alias that is already being used as name for another subcommand");
        if (checkAliasAliasConflict(command))
            throw new IllegalArgumentException("Cannot register " + sig.name() + ": Registers an alias that is already being used as alias for another subcommand");
        if (checkNameAliasConflict(command))
            throw new IllegalArgumentException("Cannot register " + sig.name() + ": Name is already being used as alias for another subcommand");
        if (sig.type() != MmCommandType.SUB_COMMAND)
            throw new IllegalArgumentException("Cannot register " + sig.name() + ": Command type must be " + MmCommandType.SUB_COMMAND);
        this.subcommands.add(command);
        return this;
    }

    /**
     * Help string.
     *
     * @return the string
     */
    public @Nullable String help() {
        return null;
    }

    private boolean subCommandExists(@NotNull MmCommand command) {
        return this.subcommands.stream().anyMatch(command1 ->
                command1.getClass().getAnnotation(MmCommandSignature.class).name().equals(command.getClass().getAnnotation(MmCommandSignature.class).name())
        );
    }

    private boolean checkAliasNameConflict(@NotNull MmCommand command) {
        String[] aliases = command.getClass().getAnnotation(MmCommandSignature.class).aliases();
        Set<String> existingNames = this.subcommands
                .stream()
                .map(subcommand -> subcommand.getClass().getAnnotation(MmCommandSignature.class).name())
                .collect(Collectors.toSet());
        return Arrays.stream(aliases).anyMatch(existingNames::contains);
    }

    private boolean checkAliasAliasConflict(@NotNull MmCommand command) {
        String[] aliases = command.getClass().getAnnotation(MmCommandSignature.class).aliases();
        Set<String> existingAliases = new HashSet<>();
        this.subcommands.forEach(subcommand ->
                existingAliases.addAll(
                        Arrays.asList(
                                subcommand.getClass().getAnnotation(MmCommandSignature.class).aliases()
                        )
                )
        );
        return Arrays.stream(aliases).anyMatch(existingAliases::contains);
    }

    private boolean checkNameAliasConflict(@NotNull MmCommand command) {
        String name = command.getClass().getAnnotation(MmCommandSignature.class).name();
        Set<String> existingAliases = new HashSet<>();
        this.subcommands.forEach(subcommand ->
                existingAliases.addAll(
                        Arrays.asList(
                                subcommand.getClass().getAnnotation(MmCommandSignature.class).aliases()
                        )
                )
        );
        return existingAliases.contains(name);
    }

    /**
     * Gets subcommands.
     *
     * @return the subcommands
     */
    public @NotNull Set<MmCommand> getSubcommands() {
        return new HashSet<>(this.subcommands);
    }
}
