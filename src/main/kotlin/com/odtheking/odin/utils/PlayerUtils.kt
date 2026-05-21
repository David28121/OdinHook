package com.odtheking.odin.utils

import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.StringUtil
import net.minecraft.world.entity.Pose
import com.odtheking.mixin.accessors.KeyMappingAccessor
import net.minecraft.world.phys.Vec3
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

fun playSoundSettings(soundSettings: Triple<String, Float, Float>) {
    val (soundName, volume, pitch) = soundSettings
    val identifier = Identifier.tryParse(StringUtil.filterText(soundName.lowercase())) ?: return
    playSoundAtPlayer(SoundEvent.createVariableRangeEvent(identifier), volume, pitch)
}

fun playSoundAtPlayer(event: SoundEvent, volume: Float = 1f, pitch: Float = 1f) = mc.execute {
    mc.soundManager.playDelayed(SimpleSoundInstance.forUI(event, pitch, volume), 0)
}

fun setTitle(title: String) {
    mc.gui.setTimes(0, 20, 5)
    mc.gui.setTitle(Component.literal(title))
}

fun alert(title: String, playSound: Boolean = true) {
    setTitle(title)
    if (playSound) playSoundAtPlayer(SoundEvents.NOTE_BLOCK_PLING.value())
}

fun getPositionString(): String {
    with(mc.player?.blockPosition() ?: BlockPos(0, 0, 0)) {
        return "x: $x, y: $y, z: $z"
    }
}

fun fovDistance(
    eyePos: Vec3,
    targetPos: Vec3,
    cameraYaw: Float,
    cameraPitch: Float,
    etherwarp: Boolean = false //prob cleaner way like allowing you to enter the player pos from the start then handling there but :shrug:
): Float {

    val player = mc.player ?: return 0f

    val dx = targetPos.x - eyePos.x
    var dy : Double
    if (etherwarp && player.pose == Pose.CROUCHING) dy = targetPos.y - player.y + 1.54
    else dy = targetPos.y - eyePos.y
    val dz = targetPos.z - eyePos.z

    val dist = sqrt(dx * dx + dy * dy + dz * dz)
    if (dist == 0.0) return 0f

    val targetYaw      = Math.toDegrees(atan2(-dx, dz)).toFloat()
    val horizontalDist = sqrt(dx * dx + dz * dz)
    val targetPitch    = Math.toDegrees(atan2(-dy, horizontalDist)).toFloat()

    var dyaw = (targetYaw - cameraYaw + 180f) % 360f
    if (dyaw < 0f) dyaw += 360f
    dyaw -= 180f
    val dpitch = targetPitch - cameraPitch

    return sqrt((dyaw * dyaw + dpitch * dpitch).toDouble()).toFloat()
}


fun getAnglesToTarget(targetX: Double, targetY: Double, targetZ: Double, etherwarp: Boolean = false): Pair<Float, Float> {
    val mc = Minecraft.getInstance()
    val player = mc.player ?: return Pair(0f, 0f)
    val partialTick = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
    val eyePos = player.getEyePosition(partialTick)

    val dx = targetX - eyePos.x
    var dy : Double
    if (etherwarp && player.pose == Pose.CROUCHING) dy = targetY - player.y + 1.54
    else dy = targetY - eyePos.y
    val dz = targetZ - eyePos.z

    val targetYaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
    val horizontalDist = sqrt(dx * dx + dz * dz)
    val targetPitch = Math.toDegrees(atan2(-dy, horizontalDist)).toFloat()

    var deltaYaw = (targetYaw - player.getViewYRot(partialTick) + 180f) % 360f
    if (deltaYaw < 0f) deltaYaw += 360f
    deltaYaw -= 180f

    val deltaPitch = targetPitch - player.getViewXRot(partialTick)

    return Pair(deltaYaw, deltaPitch)
}


fun blockDeadzoneToAngle(blockRadius: Float, distanceBlocks: Float): Float {
    if (distanceBlocks <= 0f) return 0f
    return Math.toDegrees(atan((blockRadius / distanceBlocks)).toDouble()).toFloat()
}

fun getStepSize(): Float {
    val mc = Minecraft.getInstance()
    val sensitivity = mc.options.sensitivity().get()
    val curvedSensitivity = (0.6 + sensitivity * 0.4) * 0.3
    return (curvedSensitivity * 0.15f).toFloat()
}

fun rightClick() {
    val options = mc.options ?: return
    val key = (options.keyUse as KeyMappingAccessor).key
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}

fun leftClick() {
    val options = mc.options ?: return
    val key = (options.keyAttack as KeyMappingAccessor).key
    KeyMapping.click(key)
}

fun setCrouchState(state: Boolean) {
    val options = mc.options ?: return
    val key = (options.keyShift as KeyMappingAccessor).key
    KeyMapping.set(key, state)
}
