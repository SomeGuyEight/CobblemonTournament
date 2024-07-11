package com.cobblemontournament.common.commands.builder

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemontournament.common.commands.suggestions.ActorTypeSuggestionProvider
import com.cobblemontournament.common.commands.nodes.builder.ActivePlayersBuilderNode
import com.cobblemontournament.common.commands.suggestions.PlayerNameSuggestionProvider
import com.cobblemontournament.common.util.CommandUtil
import com.cobblemontournament.common.commands.nodes.NodeKeys.ACTOR_TYPE
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER_NAME
import com.cobblemontournament.common.commands.nodes.NodeKeys.NEW
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_ENTITY
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_NAME
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_SEED
import com.cobblemontournament.common.commands.nodes.NodeKeys.SEED
import com.cobblemontournament.common.commands.nodes.NodeKeys.TOURNAMENT
import com.cobblemontournament.common.commands.nodes.NodeKeys.UPDATE
import com.cobblemontournament.common.util.ChatUtil
import com.cobblemontournament.common.util.TournamentUtil
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.CommandSelection
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import org.slf4j.helpers.Util

object UpdatePlayerCommand
{
    /**
     * [TOURNAMENT] - [BUILDER] - [BUILDER_NAME] - [PLAYER]
     * [UPDATE] - [PLAYER_NAME] - * arguments -> [updatePlayer]
     *
     *      literal     [TOURNAMENT]        ->
     *      literal     [BUILDER]           ->
     *      argument    [BUILDER_NAME] , StringType ->
     *      literal     [PLAYER]            ->
     *      literal     [UPDATE]            ->
     *      argument    [PLAYER_NAME] , StringType ->
     *      * arguments
     *      method      [updatePlayer]
     *
     *      * - optional
     */
    @JvmStatic
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registry: CommandBuildContext,
        selection: CommandSelection )
    {
        dispatcher.register(
            ActivePlayersBuilderNode.player(
                Commands.literal(UPDATE)
                    .then(Commands.argument(PLAYER_NAME, StringArgumentType.string())
                        .suggests { ctx, builder ->
                            PlayerNameSuggestionProvider().getSuggestions(ctx,builder)
                        }
                        .then(Commands.literal(ACTOR_TYPE)
                            .then( Commands.argument("$NEW$ACTOR_TYPE",StringArgumentType.string())
                                .suggests(ActorTypeSuggestionProvider())
                                .executes { ctx -> updatePlayer( ctx = ctx) }
                            ))
                        .then(Commands.literal(SEED)
                            .then(Commands.argument("$NEW$PLAYER_SEED", IntegerArgumentType.integer(-1))
                                .executes { ctx -> updatePlayer( ctx = ctx) }
                            ))
                    ))
        )
    }

    @JvmStatic
    fun updatePlayer(
        ctx: CommandContext <CommandSourceStack>
    ): Int
    {
        var updatedPlayer   : ServerPlayer? = null
        var seed            : Int?          = null
        var actorType       : ActorType?    = null

        val ( nodeEntries, tournamentBuilder ) = CommandUtil.getNodesAndTournamentBuilder( ctx )
        for ( entry in nodeEntries ) {
            when ( entry.key ) {
                PLAYER_ENTITY -> updatedPlayer = EntityArgument.getPlayer( ctx, entry.key )
                "$NEW$PLAYER_SEED" -> seed = Integer.parseInt( entry.value )
                "$NEW$ACTOR_TYPE" -> actorType = TournamentUtil.getActorTypeOrNull( entry.value ) ?: continue
            }
        }

        var success = 0
        val text: MutableComponent
        if ( tournamentBuilder == null ) {
            text = CommandUtil.failedCommand(
                reason = "Tournament Builder was null" )
        } else if ( updatedPlayer == null ) {
            text = CommandUtil.failedCommand(
                reason = "Server Player was null" )
        } else if ( seed == null && actorType == null ) {
            text = CommandUtil.failedCommand(
                reason = "All properties to update were null" )
        } else {
            tournamentBuilder.updatePlayer( updatedPlayer.uuid, actorType, seed )
            text = CommandUtil.successfulCommand(
                action = "UPDATED ${updatedPlayer.name} properties in Tournament Builder \"${tournamentBuilder.name}\"" )
            success = Command.SINGLE_SUCCESS
        }

        val player = ctx.source.player
        if ( player != null ) {
            player.displayClientMessage( text ,false )
            if ( updatedPlayer != null && updatedPlayer != player && tournamentBuilder != null ) {
                ChatUtil.displayInPlayerChat(
                    player = updatedPlayer,
                    text   = "Your properties have been UPDATED in Tournament Builder \"${tournamentBuilder.name}\"!",
                    color  = ChatUtil.white)
            }
        } else {
            Util.report( text.string )
        }
        return success
    }

}