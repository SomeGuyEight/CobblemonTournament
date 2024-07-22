package com.cobblemontournament.common.commands.testing.player

import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemontournament.common.api.TournamentStoreManager
import com.cobblemontournament.common.api.storage.TournamentBuilderStore
import com.cobblemontournament.common.commands.nodes.builder.ActiveBuilderPlayersNode
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER_NAME
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_ENTITY
import com.cobblemontournament.common.commands.nodes.NodeKeys.PLAYER_SEED
import com.cobblemontournament.common.commands.nodes.NodeKeys.REGISTER
import com.cobblemontournament.common.commands.nodes.NodeKeys.TOURNAMENT
import com.cobblemontournament.common.commands.nodes.NodeKeys.FAKE
import com.cobblemontournament.common.player.properties.PlayerProperties
import com.cobblemontournament.common.tournamentbuilder.TournamentBuilder
import com.cobblemontournament.common.util.ChatUtil
import com.cobblemontournament.common.util.CommandUtil
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import java.util.*

object RegisterFakePlayerCommand {

    /**
     * [TOURNAMENT] -> [BUILDER] -> [BUILDER_NAME] -> [PLAYER] -> [PLAYER_ENTITY]
     *
     * -> [REGISTER]-[FAKE] -> [PLAYER_SEED]-> [registerFakePlayer]
     *
     *      literal     [TOURNAMENT]        ->
     *      literal     [BUILDER]           ->
     *      argument    [BUILDER_NAME] , StringType ->
     *      literal     [PLAYER]            ->
     *      argument    [PLAYER_ENTITY] , EntityType ->
     *      literal     [REGISTER]-[FAKE]   ->
     *      * argument  [PLAYER_SEED] , IntType ->
     *      method      [registerFakePlayer]
     *
     *      * - optional
     */
    fun register( dispatcher: CommandDispatcher<CommandSourceStack> ) {
        dispatcher
            .register(ActiveBuilderPlayersNode
                .nest(Commands
                    .literal("$REGISTER-$FAKE")
                    .executes { ctx ->
                        registerFakePlayer(ctx = ctx)
                    }
                    .then(Commands
                        .literal(PLAYER_SEED)
                        .then(Commands
                            .argument(PLAYER_SEED, IntegerArgumentType.integer(-1))
                            .executes { ctx ->
                                registerFakePlayer(ctx = ctx)
                            }
                        )
                    )
                )
            )
    }

    private fun registerFakePlayer(ctx: CommandContext<CommandSourceStack>): Int {
        var tournamentBuilder: TournamentBuilder? = null
        var playerProperties: PlayerProperties? = null

        val nodeEntries = CommandUtil.getNodeEntries( ctx )
        for (entry in nodeEntries) {
            when (entry.key) {
                BUILDER_NAME -> {
                    tournamentBuilder = TournamentStoreManager.getInstanceByName(
                        storeClass = TournamentBuilderStore::class.java,
                        name = entry.value,
                        storeID = TournamentStoreManager.ACTIVE_STORE_ID
                    ).first
                }
                PLAYER_ENTITY -> {
                    // TODO better method or is fine since it is just for testing ?
                    //  - temp for testing -> just take as string for now
                    if (tournamentBuilder == null) {
                        continue
                    }
                    playerProperties = PlayerProperties(
                        name = entry.value,
                        actorType = ActorType.NPC,
                        playerID = UUID.randomUUID(),
                        tournamentID = tournamentBuilder.uuid,
                        )
                }
                PLAYER_SEED -> playerProperties?.seed = Integer.parseInt(entry.value)
            }
        }

        var success = 0
        var color = ChatUtil.yellow
        val text: String
        if (tournamentBuilder == null) {
            text = "Failed to REGISTER Fake Player b/c Tournament Builder was null"
        } else if (playerProperties == null) {
            text = "Failed to REGISTER Fake Player with ${tournamentBuilder.name} b/c Player Properties were null"
        } else {
            text = "Successfully REGISTERED Fake Player \"${playerProperties.name}\" with Tournament Builder \"${tournamentBuilder.name}\"."
            color = ChatUtil.green
            success = Command.SINGLE_SUCCESS
        }

        val player = ctx.source.player
        if (player != null) {
            ChatUtil.displayInPlayerChat( player, text, color )
        }

        return success
    }
}
