package com.odtheking.odin.features.impl.dungeon.autoroutes.handles.secrets

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.SecretPickupEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleAction
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.rightClick
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.world.phys.AABB

object HandleSecretBat : HandleAction() {

    private var currentStep: RouteStep.SecretBat? = null

    fun execute(
        step: RouteStep.SecretBat,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Bat Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    init {
        on<SecretPickupEvent.Bat> {
            if (!isExecuting || !attemptedAction) return@on
            onSuccess()
        }
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return
        val module = currentModule ?: return

        if (now - delayStartTime >= 5000L) {
            modMessage("Bat timed out, skipping")
            onSuccess()
        }

        val deltaTime = now - lastFrameTimestamp
        lastFrameTimestamp = now

        val coord = step.target.toWorldPos(room)
        val faceOffset = getFaceOffset(pos = coord) // you hype the floor so no face defaults to TOP anyways

        val (tx, ty, tz) = getFinalTargetCoords(coord, faceOffset)
        if (!holdItem("HYPERION")) {modMessage("No hype found") ; return} // for now you have to have a hype will add more methods later :p
        val deadzone = rotateToward(tx, ty, tz, module, deltaTime)

        if (deadzone && amILookingAtTargetBlock(coord)) {
            if (!attemptedAction) {
                modMessage("Attempted to Kill Bat,, $deadzone")
                attemptedAction = true
                actionAttemptTime = now
                rightClick()
            }
        }
    }

    fun AutoRoutes.renderSecretBat(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.SecretBat>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(aabb, batNodeColor, if (batNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.SecretBat>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (batNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}