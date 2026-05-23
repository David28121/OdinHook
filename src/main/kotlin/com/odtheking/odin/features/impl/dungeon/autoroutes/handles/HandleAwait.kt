package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.world.phys.AABB

object HandleAwait : HandleAction() {
    private var currentStep: RouteStep.AwaitNode? = null

    fun execute(
        step: RouteStep.AwaitNode,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        currentStep = step
        val coord = step.target.toWorldPos(room)
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return

        if (now - delayStartTime >= 5000L) {
            println("Await 5s timeout marking success")
            onSuccess()
        }

        val worldPos = step.target.toWorldPos(room)
        val aabb = AABB(
            worldPos.x.toDouble(), worldPos.y.toDouble(), worldPos.z.toDouble(),
            worldPos.x + 1.0, worldPos.y + 3.0, worldPos.z + 1.0
        )

        val player = mc.player ?: return

        if (aabb.contains(player.x, player.y, player.z)) onSuccess()
    }

    fun AutoRoutes.renderAwait(room: Room, event: RenderEvent.Extract) {
        val throughWalls = renderNodesThroughWalls

        //if node is in a currently edited route

        AutoRouteManager.currentRoute?.let { route ->
            route.steps.filterIsInstance<RouteStep.AwaitNode>().forEach { step ->
                val world = step.target.toWorldPos(room)
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 3.0, world.z + 1.0
                )
                event.drawStyledBox(aabb, awaitNodeColor, if (awaitNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.AwaitNode>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 3.0, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (awaitNodeRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }
}