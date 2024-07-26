package com.cobblemontournament.common.commands.nodes.nested

import com.cobblemontournament.common.commands.LiteralArgumentBuilder
import com.cobblemontournament.common.commands.nodes.ExecutionNode
import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * [nodeKey] will be a [LiteralArgumentBuilder] to enter a command tree
 */
open class RootNestedNode(nodeKey: String) : NestedNode(nodeKey) {

    override fun tryGetParentNode(): NestedNode? = null

    override fun nest(
        builder: ArgumentBuilder<CommandSourceStack, *>,
        execution: ExecutionNode?
    ): LiteralArgumentBuilder {
        return Commands
            .literal(nodeKey)
            .executes((execution ?: this.executionNode).action)
            .then(builder)
    }

}
