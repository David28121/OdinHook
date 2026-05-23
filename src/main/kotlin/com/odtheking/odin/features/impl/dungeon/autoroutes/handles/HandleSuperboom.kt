package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager.rotateFaceFromNorth
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes.superboomNodeTimeout
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.leftClick
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.world.phys.AABB

object HandleSuperboom : HandleAction() {

    private var currentStep: RouteStep.Superboom? = null

    fun execute(
        step: RouteStep.Superboom,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Superboom Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)
        val level = mc.level

        if (level?.getBlockState(coord)?.isAir == true) {
            onSuccess()
            return
        }

        if (!holdItem("SUPERBOOM_TNT")) return
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    init {
        onReceive<ClientboundBlockUpdatePacket> {
            if (!isExecuting || !attemptedAction) return@onReceive
            val step = currentStep ?: return@onReceive
            val room = currentRoom ?: return@onReceive
            val expectedPos = step.target.toWorldPos(room)
            if (pos.distSqr(expectedPos) <= 9 && blockState.isAir) {
                onSuccess()
            }
        }
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return
        val module = currentModule ?: return

        if (now - delayStartTime >= superboomNodeTimeout) {
            modMessage("Superboom failed due to timeout, preventing lockup")
            onFail()
        }

        val deltaTime = now - lastFrameTimestamp
        lastFrameTimestamp = now

        val coord = step.target.toWorldPos(room)
        val worldFace = rotateFaceFromNorth(step.face, room.rotation)
        val faceOffset = getFaceOffset(worldFace, coord)

        val (tx, ty, tz) = getFinalTargetCoords(coord, faceOffset)
        val deadzone = rotateToward(tx, ty, tz, module, deltaTime)

        if (deadzone && amILookingAtTargetBlock(coord)) {
            if (!attemptedAction) {
                attemptedAction = true
                actionAttemptTime = now
                leftClick()
            }
        }
    }

    fun AutoRoutes.renderSuperboom(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.Superboom>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(aabb, superboomNodeColor, if (superboomNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.Superboom>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (superboomNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}