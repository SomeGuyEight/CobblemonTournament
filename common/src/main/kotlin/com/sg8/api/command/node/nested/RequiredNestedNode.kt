package com.sg8.api.command.node.nested

import com.sg8.api.command.LiteralArgumentBuilder
import com.sg8.api.command.node.ExecutionNode
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

open class RequiredNestedNode<T : ArgumentType<*>>(
    nodeKey: String,
    private val parentNode: NestedNode,
    private val argumentType: T,
    private val suggestionProvider: SuggestionProvider<CommandSourceStack>? = null,
) : NestedNode(nodeKey) {

    override fun tryGetParentNode(): NestedNode = parentNode

    override fun nest(
        builder: ArgumentBuilder<CommandSourceStack, *>,
        execution: ExecutionNode?,
    ): LiteralArgumentBuilder {
        return if (suggestionProvider != null) {
            parentNode
                .nest(Commands
                    .argument(nodeKey, argumentType)
                    .suggests(suggestionProvider)
                    .executes((execution ?: this.executionNode).handler)
                    .then(builder)
                )
        } else {
            parentNode
                .nest(Commands
                    .argument(nodeKey, StringArgumentType.string())
                    .executes((execution ?: this.executionNode).handler)
                    .then(builder)
                )
        }
    }

}