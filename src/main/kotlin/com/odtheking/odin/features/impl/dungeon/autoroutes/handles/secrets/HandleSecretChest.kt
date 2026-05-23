package com.odtheking.odin.features.impl.dungeon.autoroutes.handles.secrets

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.SecretPickupEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager.rotateFaceFromNorth
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
import com.odtheking.odin.utils.setCrouchState
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.AABB

object HandleSecretChest : HandleAction() {

    private var currentStep: RouteStep.SecretChest? = null

    fun execute(
        step: RouteStep.SecretChest,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Chest Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)

        baseExecute(room, module, coord, onSuccess, onFail)
    }

    init {
        on<SecretPickupEvent.Interact> {
            if (!isExecuting || !attemptedAction) return@on
            val step = currentStep ?: return@on
            val room = currentRoom ?: return@on
            val expectedPos = step.target.toWorldPos(room)
            if (blockPos == expectedPos) {
                onSuccess()
            }

        }
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return
        val module = currentModule ?: return

        if (now - delayStartTime >= 5000L) {
            modMessage("Chest timed out, skipping")
            onSuccess()
        }

        val deltaTime = now - lastFrameTimestamp
        lastFrameTimestamp = now

        val coord = step.target.toWorldPos(room)
        val worldFace = rotateFaceFromNorth(step.face, room.rotation)
        val faceOffset = getFaceOffset(worldFace, coord)

        val (tx, ty, tz) = getFinalTargetCoords(coord, faceOffset)
        val player = mc.player ?: return
        if (!holdItem("DUNGEONBREAKER")) return
        if (player.pose != Pose.CROUCHING) setCrouchState(true)

        val deadzone = rotateToward(tx, ty, tz, module, deltaTime)

        if (deadzone && amILookingAtTargetBlock(coord) && player.pose == Pose.CROUCHING) {
            if (!attemptedAction) {
                attemptedAction = true
                actionAttemptTime = now
                rightClick()
            }
        }
    }

    fun AutoRoutes.renderSecretChest(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.SecretChest>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(aabb, chestNodeColor, if (chestNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.SecretChest>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (chestNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}