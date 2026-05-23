package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.AABB

object HandleStartingPoint {
    fun AutoRoutes.renderStartingPoint(room: Room, event: RenderEvent.Extract) {

        AutoRouteManager.getAuthoringStartingPoint()?.let { node ->
            val world = node.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y + 1.1, world.z.toDouble(),
                world.x + 1.0, world.y + 1.15, world.z + 1.0
            )
            event.drawStyledBox(aabb, authoringNodesColor, if (startingPointRenderFilled) 0 else 1, renderNodesThroughWalls)
        }

        AutoRouteManager.currentRoute?.let { route ->
            val world = route.startingPoint.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y + 1.1, world.z.toDouble(),
                world.x + 1.0, world.y + 1.15, world.z + 1.0
            )
            event.drawStyledBox(aabb, startingPointNodeColor, if (startingPointRenderFilled) 0 else 1, renderNodesThroughWalls)
        }
    }

    fun checkAndActivate(room: Room): Boolean {

        val currentRoute = AutoRouteManager.currentRoute ?: return false

        val worldPos = currentRoute.startingPoint.toWorldPos(room)
        val aabb = AABB(
            worldPos.x.toDouble(), worldPos.y.toDouble(), worldPos.z.toDouble(),
            worldPos.x + 1.0, worldPos.y + 2.0, worldPos.z + 1.0
        )
        val player = Minecraft.getInstance().player ?: return false
        return aabb.contains(player.x, player.y, player.z)
    }
}