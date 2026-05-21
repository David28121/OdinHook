package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.render.Etherwarp.getEtherPos
import com.odtheking.odin.utils.modMessage
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext

val autoRoutesCommand = Commodore("ar", "autoroutes") {
    runs {
        modMessage("Run \"ar/autoroutes help\" for info on each node")
        modMessage("Run \"ar/autoroutes (type)\" to place a node of that type")
    }

    literal("help").runs{
        modMessage("saveRoute: Finishes the route and saves to config")
        modMessage("startingNode: Places a node at your feet, which when walked on plays back a route")
        modMessage("etherwarp: Places a node where you are looking if it is a valid etherwarp")
    }

    literal("saveRoute").runs {
        AutoRouteManager.saveCurrentRoute()
        modMessage("Route Saved")
    }

    literal("startingNode").runs {
        val player = mc.player?: return@runs
        val pos = BlockPos(player.blockX, player.blockY - 1, player.blockZ)
        AutoRouteManager.setAuthoringStartingPoint(pos)
        modMessage("Starting Node placed: ${pos.x}, ${pos.y}, ${pos.z} ")
    }

    literal("etherwarp").runs {

        val player = mc.player?: return@runs
        val level = mc.level?: return@runs

        val etherPos = getEtherPos(
            player.oldPosition(),
            63.0,
            etherWarp = true
        )

        if (!etherPos.succeeded) modMessage("Invalid Etherwarp block")

        val hit = level.clip(
            ClipContext(
                player.getEyePosition(1f),
                player.getEyePosition(1f).add(player.lookAngle.scale(63.0)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            )
        )

        AutoRouteManager.addStepWithFace(etherPos.pos, hit.direction.name, RouteStep::Etherwarp)
        modMessage("Etherwarp Node placed: ${etherPos.pos?.x}, ${etherPos.pos?.y}, ${etherPos.pos?.z} ")
    }
}