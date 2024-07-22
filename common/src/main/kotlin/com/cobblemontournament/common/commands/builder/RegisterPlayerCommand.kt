package com.cobblemontournament.common.commands.builder

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemontournament.common.api.TournamentStoreManager
import com.cobblemontournament.common.commands.ExecutableCommand
import com.cobblemontournament.common.commands.nodes.ExecutionNode
import com.cobblemontournament.common.commands.nodes.builder.ActiveBuilderPlayersNode
import com.cobblemontournament.common.util.CommandUtil
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER_NAME
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_ENTITY
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_SEED
import com.cobblemontournament.common.commands.nodes.NodeKeys.REGISTER
import com.cobblemontournament.common.commands.nodes.NodeKeys.SEED
import com.cobblemontournament.common.commands.nodes.NodeKeys.TOURNAMENT
import com.cobblemontournament.common.player.properties.PlayerProperties
import com.cobblemontournament.common.util.ChatUtil
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import org.slf4j.helpers.Util

/**
 * [TOURNAMENT] - [BUILDER] - [BUILDER_NAME] - [PLAYER]
 *
 * [PLAYER_ENTITY] - * arguments -> [registerPlayer]
 *
 *      literal     [TOURNAMENT]        ->
 *      literal     [BUILDER]           ->
 *      argument    [BUILDER_NAME] , StringType ->
 *      literal     [PLAYER]            ->
 *      literal     [REGISTER]          ->
 *      argument    [PLAYER_ENTITY] , EntityType ->
 *      * arguments
 *      method      [registerPlayer]
 *
 *      * - optional
 */
object RegisterPlayerCommand : ExecutableCommand {
    override val executionNode get() = ExecutionNode { registerPlayer(ctx = it) }

    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(ActiveBuilderPlayersNode
            .nest(Commands
                .literal(REGISTER)
                .then(Commands
                    .argument(PLAYER_ENTITY, EntityArgument.player())
                    .executes(this.executionNode.node)
                    .then( Commands.literal( SEED )
                        .then( Commands.argument( PLAYER_SEED, IntegerArgumentType.integer( -1 ) )
                            .executes( this.executionNode.node )
                        )
                    )
                )
            )
        )
    }

    @JvmStatic
    fun registerPlayer(ctx: CommandContext<CommandSourceStack>): Int {
        val (nodeEntries, tournamentBuilder) = CommandUtil
            .getNodesAndTournamentBuilder(
                ctx = ctx,
                storeID = TournamentStoreManager.ACTIVE_STORE_ID,
                )

        var registeredPlayer: ServerPlayer? = null
        var playerProperties: PlayerProperties? = null
        for (entry in nodeEntries) {
            when (entry.key) {
                PLAYER_ENTITY -> {
                    registeredPlayer = EntityArgument.getPlayer(ctx, entry.key)
                    val success = tournamentBuilder?.addPlayer(
                        playerID = registeredPlayer.uuid,
                        playerName = registeredPlayer.name.string,
                        actorType = ActorType.PLAYER,
                        )
                    if (success == true) {
                        playerProperties = tournamentBuilder.getPlayer(playerID = registeredPlayer.uuid)
                    }
                }
                PLAYER_SEED -> {
                    if (playerProperties != null) {
                        val seed = Integer.parseInt(entry.value)
                        playerProperties.seed = seed
                        playerProperties.originalSeed = seed
                    }
                }
            }
        }

        var success = 0
        val text: MutableComponent = when {
            tournamentBuilder == null -> {
                CommandUtil.failedCommand(reason = "Tournament Builder was null")
            }
            registeredPlayer == null -> {
                CommandUtil.failedCommand(reason = "Server Player was null")
            }
            playerProperties == null -> {
                CommandUtil.failedCommand(reason = "Player already registered OR registration failed inside builder")
            }
            else -> {
                success = Command.SINGLE_SUCCESS
                CommandUtil.successfulCommand(
                    text = "REGISTERED ${registeredPlayer.name.string} with Tournament Builder \"${tournamentBuilder.name}\"",
                    )
            }
        }

        ctx.source.player?.let { player ->
            if (registeredPlayer != null && registeredPlayer != player && tournamentBuilder != null) {
                ChatUtil.displayInPlayerChat(
                    player = registeredPlayer,
                    text = "You were successfully REGISTERED with Tournament Builder \"${tournamentBuilder.name}\"!",
                    )
            }
            player.displayClientMessage(text ,false)
        } ?: Util.report(text.string)

        return success
    }
}
