package com.cobblemontournament.common.commands.nodes.builder

import com.cobblemontournament.common.util.CommandUtil
import com.cobblemontournament.common.commands.nodes.NodeKeys.ACTIVE
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER
import com.cobblemontournament.common.commands.nodes.NodeKeys.BUILDER_NAME
import com.cobblemontournament.common.commands.nodes.NodeKeys.INFO
import com.cobblemontournament.common.commands.nodes.NodeKeys.TOURNAMENT
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object ActiveBuilderInfoNode
{
    /**
     * [TOURNAMENT] - [BUILDER] - [ACTIVE] - [BUILDER_NAME] - [INFO]
     *
     *      literal     [TOURNAMENT]    ->
     *      literal     [BUILDER]       ->
     *      literal     [ACTIVE]        ->
     *      argument    [BUILDER_NAME] , StringType ->
     *      literal     [INFO]          ->
     *      _
     */
    @JvmStatic
    fun getInfo(
        literal: LiteralArgumentBuilder<CommandSourceStack>
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return inner(literal = literal, argument = null)
    }

    /**
     * [TOURNAMENT] - [BUILDER] - [ACTIVE] - [BUILDER_NAME] - [INFO]
     *
     *      literal     [TOURNAMENT]    ->
     *      literal     [BUILDER]       ->
     *      literal     [ACTIVE]        ->
     *      argument    [BUILDER_NAME] , StringType ->
     *      literal     [INFO]          ->
     *      _
     */
    @JvmStatic
    fun getInfo(
        argument: RequiredArgumentBuilder<CommandSourceStack,*>
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return inner( literal = null, argument = argument)
    }

    @JvmStatic
    private fun inner(
        literal: LiteralArgumentBuilder<CommandSourceStack>? = null,
        argument: RequiredArgumentBuilder<CommandSourceStack,*>? = null
    ): LiteralArgumentBuilder<CommandSourceStack>
    {
        val builder = literal?: argument
        return ActiveBuilderNode.node(
            Commands.literal(INFO)
                .executes { ctx ->
                    CommandUtil.displayNoArgument(
                        player = ctx.source.player,
                        nodeKey = INFO
                    )
                }
                .then(builder)
        )
    }

}