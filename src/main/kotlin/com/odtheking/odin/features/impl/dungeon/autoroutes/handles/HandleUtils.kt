package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType

fun SlabBlockFaceOffsets(face: String, state: BlockState): Triple<Double, Double, Double> {
    val half = state.getValue(SlabBlock.TYPE)
    return when (half) {
        SlabType.TOP -> when (face) {
            "NORTH" -> Triple(0.5, 0.75, 0.0)
            "SOUTH" -> Triple(0.5, 0.75, 1.0)
            "EAST" -> Triple(1.0, 0.75, 0.5)
            "WEST" -> Triple(0.0, 0.75, 0.5)
            "UP" -> Triple(0.5, 1.0, 0.5)
            "DOWN" -> Triple(0.5, 0.5, 0.5)
            else -> Triple(0.5, 0.5, 0.5)
        }
        SlabType.BOTTOM -> when (face) {
            "NORTH" -> Triple(0.5, 0.25, 0.0)
            "SOUTH" -> Triple(0.5, 0.25, 1.0)
            "EAST" -> Triple(1.0, 0.25, 0.5)
            "WEST" -> Triple(0.0, 0.25, 0.5)
            "UP" -> Triple(0.5, 0.5, 0.5)
            "DOWN" -> Triple(0.5, 0.0, 0.5)
            else -> Triple(0.5, 0.5, 0.5)
        }
        else -> when (face) {
            "NORTH" -> Triple(0.5, 0.5, 0.0)
            "SOUTH" -> Triple(0.5, 0.5, 1.0)
            "EAST" -> Triple(1.0, 0.5, 0.5)
            "WEST" -> Triple(0.0, 0.5, 0.5)
            "UP" -> Triple(0.5, 1.0, 0.5)
            "DOWN" -> Triple(0.5, 0.0, 0.5)
            else -> Triple(0.5, 0.5, 0.5)
        }
    }
}