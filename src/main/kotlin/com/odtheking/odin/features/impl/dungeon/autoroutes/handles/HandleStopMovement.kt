package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleMovement.stopMoving
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.world.phys.AABB

object HandleStopMovement: HandleAction() {
    private var currentStep: RouteStep.StopMovement? = null

    fun execute(
        step: RouteStep.StopMovement,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Stop Movement Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return

        if (now - delayStartTime >= 5000L) {
            modMessage("Stop Movement failed due to timeout, preventing lockup")
            stopMoving()
            onFail()
            return
        }

        val worldPos = step.target.toWorldPos(room)
        val aabb = AABB(
            worldPos.x.toDouble() - 0.25, worldPos.y.toDouble(), worldPos.z.toDouble() - 0.25,
            worldPos.x + 1.25, worldPos.y + 3.0, worldPos.z + 1.25
        )

        val player = mc.player ?: return

        if (aabb.contains(player.x, player.y, player.z)) {
            stopMoving()
            onSuccess()
        }
    }

    fun AutoRoutes.renderStopMovement(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.StopMovement>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble() - 0.25, world.y.toDouble(), world.z.toDouble() - 0.25,
                    world.x + 1.25, world.y + 3.0, world.z + 1.25
                )
                event.drawStyledBox(aabb, stopMovementNodeColor, if (stopMovementRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.StopMovement>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble() - 0.25, world.y.toDouble(), world.z.toDouble() - 0.25,
                world.x + 1.25, world.y + 3.0, world.z + 1.25
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (stopMovementRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}