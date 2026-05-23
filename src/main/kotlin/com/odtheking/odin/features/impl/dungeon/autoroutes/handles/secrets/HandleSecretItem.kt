package com.odtheking.odin.features.impl.dungeon.autoroutes.handles.secrets

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.SecretPickupEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes.itemNodeTimeout
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleAction
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.world.phys.AABB

object HandleSecretItem : HandleAction() {
    private var currentStep: RouteStep.SecretItem? = null

    fun execute(
        step: RouteStep.SecretItem,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Item Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    init {
        on<SecretPickupEvent.Item> {
            if (!isExecuting || !attemptedAction) return@on
            onSuccess()
        }
    }

    fun tick(now: Long) {
        if (!isExecuting) return

        if (now - delayStartTime >= itemNodeTimeout) {
            modMessage("Item timed out, skipping")
            onSuccess()
        }
    }

    fun AutoRoutes.renderSecretItem(room: Room, event: RenderEvent.Extract) {
        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.SecretItem>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 3.0, world.z + 1.0
                )
                event.drawStyledBox(aabb, itemNodeColor, if (itemNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.SecretItem>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 3.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (itemNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}