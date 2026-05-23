package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.SlabType

fun SlabBlockFaceOffsets(face: String, pos: BlockPos): Triple<Double, Double, Double> {
    val level = mc.level
    val state = level?.getBlockState(pos)
    val half = state?.getValue(SlabBlock.TYPE)
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