package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.mixin.accessors.KeyMappingAccessor
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager.rotateFaceFromNorth
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes.renderNodesThroughWalls
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes.startingPointNodeColor
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes.startingPointRenderFilled
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.setCrouchState
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.AABB
import kotlin.compareTo

object HandleDungeonbreaker : HandleAction() {

    private var currentStep: RouteStep.BreakBlock? = null

    fun execute(
        step: RouteStep.BreakBlock,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        currentStep = step
        val coord = step.target.toWorldPos(room)
        val level = mc.level

        if (level?.getBlockState(coord)?.isAir == true) {
            onSuccess()
            return
        }

        baseExecute(room, module, coord, onSuccess, onFail)
    }

    init {
        onSend<ServerboundPlayerActionPacket> {
            if (!isExecuting) return@onSend
            val step = currentStep ?: return@onSend
            val room = currentRoom ?: return@onSend

            val expectedPos = step.target.toWorldPos(room)

            if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                && pos == expectedPos) {

                println("Block Broken")

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
            modMessage("Dungeonbreaker failed due to timeout, preventing lockup")
            onFail()
        }

        val deltaTime = now - lastFrameTimestamp
        lastFrameTimestamp = now

        val coord = step.target.toWorldPos(room)
        val worldFace = rotateFaceFromNorth(step.face, room.rotation)
        val faceOffset = getFaceOffset(worldFace, coord)

        val (tx, ty, tz) = getFinalTargetCoords(coord, faceOffset)

        if (!holdItem("DUNGEONBREAKER")) return

        val deadzone = rotateToward(tx, ty, tz, module, deltaTime)

        if (deadzone && amILookingAtTargetBlock(coord)) {
            if (!attemptedAction) {
                attemptedAction = true
                actionAttemptTime = now
                val options = mc.options ?: return
                val key = (options.keyAttack as KeyMappingAccessor).key
                KeyMapping.set(key, true)
            }
        }
    }

    fun AutoRoutes.renderDungeonbreaker(room: Room, event: RenderEvent.Extract) {
        val throughWalls = renderNodesThroughWalls

        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.BreakBlock>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(aabb, dungeonbreakerNodeColor, if (dungeonbreakerNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.BreakBlock>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (dungeonbreakerNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}