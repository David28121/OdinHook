package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.mixin.accessors.KeyMappingAccessor
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.client.KeyMapping
import net.minecraft.world.phys.AABB

object HandleMovement : HandleAction() {
    var isMoving = false
        private set

    fun execute(step: RouteStep.Movement, room: Room, module: AutoRoutes, onSuccess: () -> Unit, onFail: () -> Unit) {

        modMessage("Movement Node")

        isMoving = true
        KeyMapping.set((mc.options.keyUp as KeyMappingAccessor).key, true)
        onSuccess()
    }

    fun update() {
        if (isMoving) {
            KeyMapping.set((mc.options.keyUp as KeyMappingAccessor).key, true)
        }
    }

    fun stopMoving() {
        isMoving = false
        KeyMapping.set((mc.options.keyUp as KeyMappingAccessor).key, false)
    }

    fun AutoRoutes.renderMovement(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.Movement>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(
                    aabb,
                    movementNodeColor,
                    if (movementRenderFilled) 0 else 1,
                    renderNodesThroughWalls
                )
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.Movement>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (movementRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}