package com.cobblemontournament.common.commands.testing.player

import com.cobblemontournament.common.api.TournamentStoreManager
import com.cobblemontournament.common.commands.builder.UnregisterPlayerCommand.unregisterPlayer
import com.cobblemontournament.common.commands.nodes.builder.ActivePlayersBuilderNode
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER_NAME
import com.cobblemontournament.common.commands.nodes.NodeKeys.EXECUTE
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_ENTITY
import com.cobblemontournament.common.commands.nodes.NodeKeys.TOURNAMENT
import com.cobblemontournament.common.commands.nodes.NodeKeys.UNREGISTER
import com.cobblemontournament.common.commands.nodes.NodeKeys.FAKE
import com.cobblemontournament.common.tournamentbuilder.TournamentBuilder
import com.cobblemontournament.common.util.ChatUtil
import com.cobblemontournament.common.util.CommandUtil
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.CommandSelection

object UnregisterFakePlayerCommand
{
    /**
     * [TOURNAMENT] -> [BUILDER] -> [BUILDER_NAME] -> [PLAYER] -> [PLAYER_ENTITY]
     *
     * -> [UNREGISTER]-[FAKE] -> [unregisterFakePlayer]
     *
     *      literal     [TOURNAMENT]            ->
     *      literal     [BUILDER]               ->
     *      argument    [BUILDER_NAME] , StringType ->
     *      literal     [PLAYER]                ->
     *      argument    [PLAYER_ENTITY] , EntityType ->
     *      literal     [UNREGISTER]-[FAKE]     ->
     *      method      [unregisterFakePlayer]
     */
    @JvmStatic
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registry: CommandBuildContext,
        selection: CommandSelection)
    {
        dispatcher.register(
            ActivePlayersBuilderNode.player(
                Commands.literal("$UNREGISTER-$FAKE")
                    .then(Commands.literal(EXECUTE)
                        .executes { ctx -> unregisterPlayer( ctx) }
                    )))
    }

    @JvmStatic
    fun unregisterFakePlayer(
        ctx : CommandContext<CommandSourceStack>
    ): Int
    {
        var tournamentBuilder   : TournamentBuilder? = null
        var removed             : Boolean?           = null

        val nodeEntries = CommandUtil.getNodeEntries( ctx.nodes, ctx.input)
        for (entry in nodeEntries) {
            when (entry.key) {
                BUILDER_NAME -> {
                    val ( builder, _ ) = TournamentStoreManager.getTournamentBuilderByName( entry.value)
                    tournamentBuilder = builder
                }
                PLAYER_ENTITY -> removed = tournamentBuilder?.removePlayerByName(entry.value)
            }
        }

        var success = 0
        var text = ""
        var color = ChatUtil.yellow
        if ( tournamentBuilder == null) {
            text = "Failed to UNREGISTER Fake Player b/c Tournament Builder was null"
        } else if (removed == null || removed == false) {
            text = "Failed to UNREGISTER Fake Player with ${tournamentBuilder.name} " +
                    "b/c IDK..."
        } else {
            text = "Successfully UNREGISTERED Fake Player " +
                    "from Tournament Builder: \"${tournamentBuilder.name}\"."
            color = ChatUtil.green
            success = Command.SINGLE_SUCCESS
        }

        val player = ctx.source.player
        if (player != null) {
            ChatUtil.displayInPlayerChat( player, text = text, color = color)
        }
        return success
    }

}