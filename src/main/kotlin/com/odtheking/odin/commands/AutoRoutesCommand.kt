package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.render.Etherwarp.getEtherPos
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

val autoRoutesCommand = Commodore("ar", "autoroutes") {
    runs {
        modMessage("Run \"ar/autoroutes help (type)\" for info on each node")
        modMessage("Run \"ar/autoroutes (type)\" to place a node of that type")
    }

    literal("help").runs{
        modMessage("saveRoute: Finishes the route and saves to config")
        modMessage("undo: Undoes the last placed node")
        modMessage("clearRoom: Right now clears every route saved for the room")
        modMessage("startingNode: Places a node at your feet, which when walked on plays back a route")
        modMessage("etherwarp: Places a node at the block you are looking at if it is a valid etherwarp.")
        modMessage("dungeonbreaker: Places a node at the block you are looking at.")
        modMessage("superboom: Places a node at the block you are looking at.")
        modMessage("await: Places a node where you are standing.")
        modMessage("movement: Places a node where you are standing.")
        modMessage("stopMovement: Places a node where you are standing.")
        modMessage("rotate: Places a node at the block you are looking at.")
    }

    literal("saveRoute").runs {
        AutoRouteManager.saveCurrentRoute()
        modMessage("Route Saved")
    }

    literal("undo").runs {
        AutoRouteManager.undoLastStep()
        modMessage("Undid last step")
    }

    literal("clearRoom").runs {
        val roomName = DungeonUtils.currentRoomName
        if (roomName == "Unknown") {
            modMessage("Not in a room")
            return@runs
        }
        AutoRouteManager.clearRoomRoutes(roomName)
        modMessage("Cleared all routes for: $roomName")
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
        val hit = lookAtBlockInRange(5.0)?: return@runs
        AutoRouteManager.addStepWithFace(hit.blockPos, hit.direction.name, RouteStep::BreakBlock)
        modMessage("Dungeonbreaker Node placed: ${hit.blockPos?.x}, ${hit.blockPos?.y}, ${hit.blockPos?.z}, Face: ${hit.direction.name} ")
    }

    literal("await").runs {
        val pos = basicNodeBs()?: return@runs
        AutoRouteManager.addStepNoFace(pos, RouteStep::AwaitNode)
        modMessage("Await Node placed: ${pos.x}, ${pos.y}, ${pos.z} ")
    }

    literal("movement").runs {
        val pos = basicNodeBs()?: return@runs
        AutoRouteManager.addStepNoFace(pos, RouteStep::Movement)
        modMessage("Movement Node placed: ${pos.x}, ${pos.y}, ${pos.z} ")
    }

    literal("stopMovement").runs {
        val pos = basicNodeBs()?: return@runs
        AutoRouteManager.addStepNoFace(pos, RouteStep::StopMovement)
        modMessage("Stop Movement Node placed: ${pos.x}, ${pos.y}, ${pos.z} ")
    }

    literal("superboom").runs {
        val hit = lookAtBlockInRange(5.0)?: return@runs
        AutoRouteManager.addStepWithFace(hit.blockPos, hit.direction.name, RouteStep::Superboom)
        modMessage("Superboom Node placed: ${hit.blockPos?.x}, ${hit.blockPos?.y}, ${hit.blockPos?.z}, Face: ${hit.direction.name} ")
    }

    literal("rotate").runs {
        val hit = lookAtBlockInRange(100.0)?: return@runs
        AutoRouteManager.addStepWithFace(hit.blockPos, hit.direction.name, RouteStep::RotateTo)
        modMessage("Rotate Node placed: ${hit.blockPos?.x}, ${hit.blockPos?.y}, ${hit.blockPos?.z}, Face: ${hit.direction.name} ")
    }


}

//incredible naming ik ill fix this shit up more later and believe me it is better than the original code by a mile...

fun basicNodeBs(): BlockPos? {
    if (AutoRouteManager.getAuthoringStartingPoint() == null) {
        modMessage("Set a starting point first")
        return null
    }
    val player = mc.player ?: return null
    return BlockPos(player.blockX, player.blockY - 1, player.blockZ)
}

fun lookAtBlockInRange(scale: Double): BlockHitResult? {
    if (AutoRouteManager.getAuthoringStartingPoint() == null) {
        modMessage("Set a starting point first")
        return null
    }

    val player = mc.player ?: return null
    val level = mc.level ?: return null

    val hit = level.clip(
        ClipContext(
            player.getEyePosition(1f),
            player.getEyePosition(1f).add(player.lookAngle.scale(scale)),
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        )
    )

    if (hit.type == HitResult.Type.MISS) {
        modMessage("Not looking at a block")
        return null
    }

    return hit
}