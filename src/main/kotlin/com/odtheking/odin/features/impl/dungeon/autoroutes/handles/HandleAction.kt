package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.utils.blockDeadzoneToAngle
import com.odtheking.odin.utils.getAnglesToTarget
import com.odtheking.odin.utils.getSlotForSkyblockId
import com.odtheking.odin.utils.getStepSize
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt
import kotlin.random.Random

abstract class HandleAction {
    protected var lastFrameTimestamp = System.currentTimeMillis()
    protected var delayStartTime = 0L
    protected var isExecuting = false
    protected var attemptedAction = false
    protected var actionAttemptTime = 0L
    protected val TIMEOUT_MS = 2000L

    protected var xVary = 0f
    protected var yVary = 0f
    protected var zVary = 0f

    protected var successCallback: (() -> Unit)? = null
    protected var failCallback: (() -> Unit)? = null


    protected fun baseExecute(
        room: Room,
        module: AutoRoutes,
        targetCoord: BlockPos,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        isExecuting = true
        delayStartTime = System.currentTimeMillis()
        lastFrameTimestamp = System.currentTimeMillis()
        currentRoom = room
        currentModule = module
        successCallback = onSuccess
        failCallback = onFail

        xVary = (Random.nextFloat() * 0.1f - 0.05f)
        yVary = (Random.nextFloat() * 0.1f - 0.05f)
        zVary = (Random.nextFloat() * 0.1f - 0.05f)
    }

    protected var currentRoom: Room? = null
    protected var currentModule: AutoRoutes? = null

    protected fun rotateToward(
        tx: Double, ty: Double, tz: Double,
        module: AutoRoutes,
        deltaTime: Long
    ): Boolean {
        // returns true if within deadzone
        val (deltaYaw, deltaPitch) = getAnglesToTarget(tx, ty, tz)
        val player = mc.player ?: return false
        val partialTick = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
        val eyePos = player.getEyePosition(partialTick)
        val distance = eyePos.distanceTo(Vec3(tx, ty, tz)).toFloat()
        val deadzoneAngle = blockDeadzoneToAngle(0.1f, distance)

        val stepSize = getStepSize()
        val elapsed = deltaTime / 1000f
        val vary = (Random.nextFloat() * 2f - 1f) * module.rotationVary
        val rotationThisFrame = (module.rotationSpeed + vary) * elapsed
        val totalDelta = sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch)

        if (totalDelta <= deadzoneAngle) return true

        val yawRatio = deltaYaw / totalDelta
        val pitchRatio = deltaPitch / totalDelta
        val stepsThisFrame = (rotationThisFrame / stepSize).toInt().coerceAtLeast(1)
        var snappedTotal = stepsThisFrame * stepSize

        if (snappedTotal >= totalDelta) {
            if (totalDelta / stepSize > 1) snappedTotal = stepSize * (totalDelta / stepSize).toInt()
            else return false
        }

        if (!attemptedAction) {
            player.xRot += (snappedTotal * pitchRatio)
            player.yRot += (snappedTotal * yawRatio)
        }
        return false
    }

    protected fun getFinalTargetCoords(pos: BlockPos, offset: Triple<Double, Double, Double>): Vec3 {
        val tx = pos.x + offset.first + xVary
        val ty = pos.y + offset.second + yVary
        val tz = pos.z + offset.third + zVary
        return Vec3(tx, ty, tz)
    }

    protected fun getFaceOffset(face: String): Triple<Double, Double, Double> =
        when (face) {
            "NORTH" -> Triple(0.5, 0.5, 0.0)
            "SOUTH" -> Triple(0.5, 0.5, 1.0)
            "EAST"  -> Triple(1.0, 0.5, 0.5)
            "WEST"  -> Triple(0.0, 0.5, 0.5)
            "UP"    -> Triple(0.5, 1.0, 0.5)
            "DOWN"  -> Triple(0.5, 0.0, 0.5)
            else    -> Triple(0.5, 0.5, 0.5)
        }

    protected fun holdItem(skyblockItem: String): Boolean {
        val player = mc.player ?: return false
        val slotId = getSlotForSkyblockId(player, skyblockItem)
        if (slotId == null || slotId > 8) return false
        if (player.inventory.selectedSlot != slotId) player.inventory.selectedSlot = slotId
        return true
    }


    protected fun amILookingAtTargetBlock(pos: BlockPos): Boolean {
        val level = mc.level ?: return false
        val player = mc.player ?: return false
        val hit = level.clip(
            ClipContext(
                player.getEyePosition(1f),
                player.getEyePosition(1f).add(player.lookAngle.scale(4.5)), //4.5 to account for shitty desync eventually update to use server position
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            )
        )

        if (hit.type == HitResult.Type.MISS) {
            return false
        }
        return (hit.blockPos == pos)
    }

    protected fun onSuccess() {
        isExecuting = false
        actionAttemptTime = 0L
        attemptedAction = false
        successCallback?.invoke()
    }

    protected fun onFail() {
        isExecuting = false
        actionAttemptTime = 0L
        attemptedAction = false
        failCallback?.invoke()
    }

    protected fun checkTimeout(now: Long): Boolean {
        if (attemptedAction && now - actionAttemptTime >= TIMEOUT_MS) {
            onFail()
            return true
        }
        return false
    }
}