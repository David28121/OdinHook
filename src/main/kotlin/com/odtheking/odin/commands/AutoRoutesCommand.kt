package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.render.Etherwarp.getEtherPos
import com.odtheking.odin.utils.modMessage
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult

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

    literal("undo").runs {
        AutoRouteManager.undoLastStep()
        modMessage("Undid last step")
    }

    literal("startingNode").runs {
        val player = mc.player?: return@runs
        val pos = BlockPos(player.blockX, player.blockY - 1, player.blockZ)
        AutoRouteManager.setAuthoringStartingPoint(pos)
        modMessage("Starting Node placed: ${pos.x}, ${pos.y}, ${pos.z} ")
    }

    literal("etherwarp").runs {

        if (AutoRouteManager.getAuthoringStartingPoint() == null) {
            modMessage("Set a starting point first")
            return@runs
        }

        val player = mc.player?: return@runs
        val level = mc.level?: return@runs

        val etherPos = getEtherPos(
            player.oldPosition(),
            63.0,
            etherWarp = true
        )

        if (!etherPos.succeeded) {modMessage("Invalid Etherwarp block") ; return@runs}

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
        modMessage("Etherwarp Node placed: ${etherPos.pos?.x}, ${etherPos.pos?.y}, ${etherPos.pos?.z}, Face: ${hit.direction.name} ")
    }

    literal("dungeonbreaker").runs {

        if (AutoRouteManager.getAuthoringStartingPoint() == null) {
            modMessage("Set a starting point first")
            return@runs
        }

        val player = mc.player ?: return@runs
        val level = mc.level ?: return@runs

        val hit = level.clip(
            ClipContext(
                player.getEyePosition(1f),
                player.getEyePosition(1f).add(player.lookAngle.scale(5.0)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            )
        )

        if (hit.type == HitResult.Type.MISS) {
            modMessage("Not looking at a block")
            return@runs
        }

        AutoRouteManager.addStepWithFace(hit.blockPos, hit.direction.name, RouteStep::BreakBlock)
        modMessage("Dungeonbreaker Node placed: ${hit.blockPos?.x}, ${hit.blockPos?.y}, ${hit.blockPos?.z}, Face: ${hit.direction.name} ")
    }
}