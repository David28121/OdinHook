package com.odtheking.odin.features.impl.dungeon.autoroutes

import com.odtheking.odin.utils.rotateAroundNorth
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.core.BlockPos

data class RouteNode(
    val x: Int,
    val y: Int,
    val z: Int
)

data class RoomRoute(
    val startingPoint: RouteNode,
    val steps: List<RouteStep> = emptyList()
)

sealed class RouteStep {
    data class Etherwarp(val target: RouteNode, val face: String = "TOP") : RouteStep()
    data class BreakBlock(val target: RouteNode, val face: String = "TOP") : RouteStep()
    data class Superboom(val target: RouteNode, val face: String = "TOP") : RouteStep()
    data class SecretChest(val target: RouteNode, val face: String = "TOP") : RouteStep()

    data class RotateTo(val target: RouteNode, val face: String = "TOP") : RouteStep()

    data class SecretSpecial(val target: RouteNode, val face: String = "TOP") : RouteStep()
    data class SecretLever(val target: RouteNode) : RouteStep()
    data class SecretItem(val target: RouteNode) : RouteStep()
    data class SecretBat(val target: RouteNode) : RouteStep()

    data class Movement(val target: RouteNode) : RouteStep()

    data class StopMovement(val target: RouteNode) : RouteStep()

    data class AwaitNode(val target: RouteNode) : RouteStep()
}

fun RouteNode.toWorldPos(room: Room): BlockPos {
    val relative = BlockPos(x, y, z)
    return relative.rotateAroundNorth(room.rotation).offset(room.clayPos.x, 0, room.clayPos.z)
}