package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.LeverBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.AttachFace
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

// we dont store the face levers are calced on the spot and are pretty small.
// why do special store it then if handles essence? cause redstone blocks are also handle through that for redstone keys, so we need the face
// even tho the code wont work for those rooms since we randomly select a route when entering.
// eventually i have to add logic to check for those specific rooms and prob split those rooms into 2 when storing depending on direction.
// for now i will pull the bloom ai logic when entering trap of "what room, i dont see a room?"
fun LeverBlockFaceOffsets(pos: BlockPos): Triple<Double, Double, Double> {

    val level = mc.level
    val state = level?.getBlockState(pos)
    val attachFace = state?.getValue(LeverBlock.FACE)
    val facing = state?.getValue(LeverBlock.FACING)

    return when (attachFace) {
        AttachFace.FLOOR -> Triple(0.5, 0.1, 0.5)
        AttachFace.CEILING -> Triple( 0.5, 0.9, 0.5)
        AttachFace.WALL -> when (facing) {
            Direction.NORTH -> Triple(0.5, 0.5, 0.9)
            Direction.SOUTH -> Triple(0.5, 0.5, 0.1)
            Direction.EAST  -> Triple(0.1, 0.5, 0.5)
            Direction.WEST  -> Triple(0.9, 0.5, 0.5)
            else -> Triple(0.5, 0.5, 0.5)
        }
        else -> Triple(0.5, 0.5, 0.5)
    }
}

// tldr skull blocks for secrets are never on anything but the ground so we only need to handle the face direction and then we can handle redstone blocks as an "else" blockClass type
fun SkullBlockFaceOffsets(face: String): Triple<Double, Double, Double> {
    return when (face) {
        "NORTH" -> Triple(0.5, 0.25, 0.25)
        "SOUTH" -> Triple(0.5, 0.25, 0.75)
        "EAST"  -> Triple(0.75, 0.25, 0.5)
        "WEST"  -> Triple(0.25, 0.25, 0.5)
        "UP"    -> Triple(0.5, 0.5, 0.5)
        "DOWN"  -> Triple(0.5, 0.0, 0.5)
        else    -> Triple(0.5, 0.5, 0.5)
    }
}

fun ChestBlockFaceOffsets(face: String): Triple<Double, Double, Double> {
    return when (face) {
        "NORTH" -> Triple(0.5, 0.4375, 0.9375)
        "SOUTH" -> Triple(0.5, 0.4375, 0.0625)
        "EAST"  -> Triple(0.9375, 0.4375, 0.5)
        "WEST"  -> Triple(0.0625, 0.4375, 0.5)
        "UP"    -> Triple(0.5, 0.875, 0.5)
        "DOWN"  -> Triple(0.5, 0.0, 0.5)
        else    -> Triple(0.5, 0.5, 0.5)
    }
}