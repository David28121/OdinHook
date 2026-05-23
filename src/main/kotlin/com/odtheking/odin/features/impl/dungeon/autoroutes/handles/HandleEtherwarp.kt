package com.odtheking.odin.features.impl.dungeon.autoroutes.handles

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRouteManager.rotateFaceFromNorth
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes
import com.odtheking.odin.features.impl.dungeon.autoroutes.AutoRoutes.etherwarpNodeTimeout
import com.odtheking.odin.features.impl.dungeon.autoroutes.RouteStep
import com.odtheking.odin.features.impl.dungeon.autoroutes.toWorldPos
import com.odtheking.odin.features.impl.render.Etherwarp
import com.odtheking.odin.features.impl.render.Etherwarp.getEtherPos
import com.odtheking.odin.utils.component1
import com.odtheking.odin.utils.component2
import com.odtheking.odin.utils.component3
import com.odtheking.odin.utils.getPositionString
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.rightClick
import com.odtheking.odin.utils.setCrouchState
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.Relative
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object HandleEtherwarp : HandleAction() {
    private var currentStep: RouteStep.Etherwarp? = null
    private var expectedPos: Vec3? = null

    private var etherPos: Etherwarp.EtherPos? = null

    fun execute(
        step: RouteStep.Etherwarp,
        room: Room,
        module: AutoRoutes,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {

        modMessage("Etherwarp Node")

        currentStep = step
        val coord = step.target.toWorldPos(room)

        println("Executing Etherwarp")

        expectedPos = Vec3(coord.x + 0.5, coord.y + 1.0, coord.z + 0.5)

        val level = mc.level
        if (level?.getBlockState(coord)?.isAir == true) {
            println("Etherwarp Failed due to block being air")
            onFail()
            return
        }

        if (!holdItem("ASPECT_OF_THE_VOID")) return
        baseExecute(room, module, coord, onSuccess, onFail)
    }

    init {
        onReceive<ClientboundPlayerPositionPacket> {
            if (!isExecuting || !attemptedAction) return@onReceive
            val pos = change().position()
            val expected = etherPos?.pos ?: return@onReceive

            val xAbsolute = !relatives.contains(Relative.X)
            val yAbsolute = !relatives.contains(Relative.Y)
            val zAbsolute = !relatives.contains(Relative.Z)

            if (xAbsolute && yAbsolute && zAbsolute) {
                if (pos.distanceTo(expected.center) < 1.5) onSuccess()
                else {modMessage("Etherwarp Failed") ; onFail()}
            }
        }
    }

    fun tick(now: Long) {
        if (!isExecuting) return
        val step = currentStep ?: return
        val room = currentRoom ?: return
        val module = currentModule ?: return

        if (now - delayStartTime >= etherwarpNodeTimeout) {
            modMessage("Etherwarp failed due to timeout, preventing lockup")
            onFail()
        }

        val deltaTime = now - lastFrameTimestamp
        lastFrameTimestamp = now

        val coord = step.target.toWorldPos(room)
        val worldFace = rotateFaceFromNorth(step.face, room.rotation)
        val faceOffset = getFaceOffset(worldFace, coord, true)

        val (tx, ty, tz) = getFinalTargetCoords(coord, faceOffset)

        val player = mc.player ?: return
        if (player.pose != Pose.CROUCHING) setCrouchState(true)

        // -0.27 is to account for shitty 1.14+ crouch height and aiming cause im lazy
        val deadzone = rotateToward(tx, ty - 0.27, tz, module, deltaTime)

        if (!attemptedAction) etherPos = getEtherPos(mc.player?.oldPosition(), 63.0, etherWarp = true)

        if (deadzone && (etherPos?.pos == coord) && (etherPos?.succeeded == true) && (player.pose == Pose.CROUCHING)) {
            if (!attemptedAction) {
                modMessage("Attempted Etherwarp")
                attemptedAction = true
                actionAttemptTime = now
                rightClick()
            }
        }
    }

    // too lazy to clean up but i stopped setting a val to the through walls and
    // just used the var directly cause i refractored render method from the original
    // code to work directly in odin's codebase i be it very scuffed
    fun AutoRoutes.renderEtherwarp(room: Room, event: RenderEvent.Extract) {
        val throughWalls = renderNodesThroughWalls

        if (etherwarpNodeRenderLines) {
            getLastNode(room)?.let { from ->
                val player = mc.player ?: return@let
                event.drawLine(
                    listOf(
                        Vec3(from.x + 0.5, from.y + 1.0, from.z + 0.5),
                        Vec3(player.x, player.y, player.z)
                    ),
                    authoringNodesColor,
                    throughWalls
                )
            }
        }

        AutoRouteManager.currentRoute?.let { route ->
            val nodes = mutableListOf<BlockPos>()
            nodes.add(route.startingPoint.toWorldPos(room))
            route.steps.filterIsInstance<RouteStep.Etherwarp>().forEach { step ->
                nodes.add(step.target.toWorldPos(room))
            }

            nodes.forEach { world ->
                val aabb = AABB(
                    world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                    world.x + 1.0, world.y + 1.0, world.z + 1.0
                )
                event.drawStyledBox(
                    aabb,
                    etherwarpNodeColor,
                    if (etherwarpNodeRenderFilled) 0 else 1,
                    renderNodesThroughWalls
                )
            }

            if (etherwarpNodeRenderLines) {
                for (i in 0 until nodes.size - 1) {
                    val from = nodes[i]
                    val to = nodes[i + 1]
                    event.drawLine(
                        listOf(
                            Vec3(from.x + 0.5, from.y + 1.0, from.z + 0.5),
                            Vec3(to.x + 0.5, to.y + 1.0, to.z + 0.5)
                        ),
                        etherwarpNodeColor,
                        throughWalls
                    )
                }
            }
        }

        AutoRouteManager.getAuthoringSteps().filterIsInstance<RouteStep.Etherwarp>().forEach { step ->
            val world = step.target.toWorldPos(room)
            val aabb = AABB(
                world.x.toDouble(), world.y.toDouble(), world.z.toDouble(),
                world.x + 1.0, world.y + 1.0, world.z + 1.0
            )
            event.drawStyledBox(
                aabb,
                authoringNodesColor,
                if (startingPointRenderFilled) 0 else 1,
                renderNodesThroughWalls
            )
        }
    }


    fun getLastNode(room: Room): BlockPos? {
        val authoringSteps = AutoRouteManager.getAuthoringSteps()
        val lastEtherwarp = authoringSteps.filterIsInstance<RouteStep.Etherwarp>().lastOrNull()

        if (lastEtherwarp != null) {
            return lastEtherwarp.target.toWorldPos(room)
        }

        return AutoRouteManager.getAuthoringStartingPoint()?.toWorldPos(room)
    }
}