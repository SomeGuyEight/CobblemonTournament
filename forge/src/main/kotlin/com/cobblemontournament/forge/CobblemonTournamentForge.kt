package com.cobblemontournament.forge

import com.cobblemontournament.common.*
import com.cobblemontournament.forge.config.TournamentConfigForge
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import com.sg8.api.modimplementation.PlatformModImplementation

@Mod(MOD_ID)
@Suppress(("unused"))
object CobblemonTournamentForge : PlatformModImplementation(common = CobblemonTournament) {

    private val commonSpec: ForgeConfigSpec =
        ForgeConfigSpec.Builder().configure { TournamentConfigForge.initialize(it) }.value

    init {
        initialize()
    }

    // no platform-specific initialization needed at this time
    override fun initialize() = initializeCommon()

    override fun initializeConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonSpec)
    }

    // no platform-specific events to register at this time
    override fun registerEvents() { }

}
