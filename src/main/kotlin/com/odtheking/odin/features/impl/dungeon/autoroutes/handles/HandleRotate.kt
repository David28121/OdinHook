package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager.rotateFaceFromNorth
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.world.phys.AABB

object HandleRotate  : HandleAction() {

    private var currentStep: RouteStep.RotateTo? = null

    fun execute(
        step: RouteStep.RotateTo,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Rotate Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return
        val module = currentModule ?: return

        if (now - delayStartTime >= 5000L) {
            modMessage("Rotate failed due to timeout, preventing lockup")
            onFail()
        }

        val deltaTime = now - lastFrameTimestamp
        lastFrameTimestamp = now

        val coord = step.target.toWorldPos(room)
        val worldFace = rotateFaceFromNorth(step.face, room.rotation)
        val faceOffset = getFaceOffset(worldFace, coord, true)
        val (tx, ty, tz) = getFinalTargetCoords(coord, faceOffset)
        val deadzone = rotateToward(tx, ty, tz, module, deltaTime)

        if (deadzone) {
            onSuccess()
        }
    }

    fun AutoRoutes.renderRotate(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.RotateTo>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(
                    aabb,
                    rotateNodeColor,
                    if (rotateRenderFilled) 0 else 1,
                    renderNodesThroughWalls
                )
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.RotateTo>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(
                aabb,
                authoringNodesColor,
                if (rotateRenderFilled) 0 else 1,
                renderNodesThroughWalls
            )
        }
    }
}
