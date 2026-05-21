package com.odtheking.odin.features.impl.dungeon.autoroutes

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.KeybindSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleEtherwarp
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleEtherwarp.renderEtherwarp
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleStartingPoint
import com.odtheking.odin.features.impl.dungeon.autoroutes.handles.HandleStartingPoint.renderStartingPoint
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.setCrouchState
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import org.lwjgl.glfw.GLFW

object AutoRoutes : Module(
    name = "Auto Routes",
    description = "Auto Routes for Dungeons. Run \"ar/autoroutes\" for more information."
){
    var rotationSpeed by NumberSetting("Rotation Speed", 180.0, 0.0, 720.0, 0.1, "Rotation speed of Auto Routes. Degrees per Second.")
    var rotationVary by NumberSetting("Rotation Vary", 0.0, 0.0, 30.0, 0.1, "How vary rotation speed is.")
    var renderNodesThroughWalls by BooleanSetting("Depth Check", true, "Should Nodes render through Walls.")

    private val nodeSettings by DropdownSetting("Node Settings")

    private val nodeColorSetting by DropdownSetting("Color").withDependency { nodeSettings }

    var authoringNodesColor by ColorSetting("Authoring Nodes Color", Colors.MINECRAFT_GREEN, true, "Color of the nodes when editing").withDependency { nodeSettings && nodeColorSetting }
    var awaitNodeColor by ColorSetting(name = "Await Color", Colors.MINECRAFT_DARK_PURPLE, true, desc = "Color of the Await Node").withDependency { nodeSettings && nodeColorSetting }
    var chestNodeColor by ColorSetting("Chest Color", Colors.MINECRAFT_YELLOW, true, "Color of the Chest Node").withDependency { nodeSettings && nodeColorSetting }
    var leverNodeColor by ColorSetting("Lever Color", Colors.MINECRAFT_GOLD, true, "Color of the Lever Node").withDependency { nodeSettings && nodeColorSetting }
    var itemNodeColor by ColorSetting("Item Color", Colors.MINECRAFT_GRAY, true, "Color of the Item Node").withDependency { nodeSettings && nodeColorSetting }
    var batNodeColor by ColorSetting("Bat Color", Colors.MINECRAFT_DARK_GRAY, true, "Color of the Bat Node").withDependency { nodeSettings && nodeColorSetting }
    var specialNodeColor by ColorSetting("Special Color", Colors.MINECRAFT_DARK_GRAY, true, "Color of the Special Node").withDependency { nodeSettings && nodeColorSetting }
    var etherwarpNodeColor by ColorSetting("Etherwarp Color", Colors.MINECRAFT_AQUA, true, "Color of the Etherwarp Node").withDependency { nodeSettings && nodeColorSetting }
    var dungeonbreakerNodeColor by ColorSetting("Dungeonbreaker Color", Colors.MINECRAFT_RED, true, "Color of the Dungeonbreaker Node").withDependency { nodeSettings && nodeColorSetting }
    var superboomNodeColor by ColorSetting("Superboom Color", Colors.MINECRAFT_DARK_RED, true, "Color of the Superboom Node").withDependency { nodeSettings && nodeColorSetting }
    var movementNodeColor by ColorSetting("Movement Color", Colors.MINECRAFT_BLUE, true, "Color of the Movement Node").withDependency { nodeSettings && nodeColorSetting }
    var stopMovementNodeColor by ColorSetting("Stop Movement Color", Colors.MINECRAFT_DARK_BLUE, true, "Color of the Stop Movement Node").withDependency { nodeSettings && nodeColorSetting }
    var rotateNodeColor by ColorSetting("Rotate Color", Colors.MINECRAFT_LIGHT_PURPLE, true, "Color of the Rotate Node").withDependency { nodeSettings && nodeColorSetting }
    var startingPointNodeColor by ColorSetting("Starting Point Color", Colors.MINECRAFT_DARK_AQUA, true, "Color of the Starting Point").withDependency { nodeSettings && nodeColorSetting }

    private val nodeRenderFilled by DropdownSetting("Render Filled").withDependency { nodeSettings }
    var awaitNodeRenderFilled by BooleanSetting(name = "Await Filled", true, desc = "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var chestNodeRenderFilled by BooleanSetting("Chest Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var leverNodeRenderFilled by BooleanSetting("Lever Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var itemNodeRenderFilled by BooleanSetting("Item Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var batNodeRenderFilled by BooleanSetting("Bat illed", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var specialNodeRenderFilled by BooleanSetting("Special Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var etherwarpNodeRenderFilled by BooleanSetting("Etherwarp Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var dungeonbreakerNodeRenderFilled by BooleanSetting("Dungeonbreaker Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var superboomRenderFilled by BooleanSetting("Superboom Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var movementRenderFilled by BooleanSetting("Movement Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var stopMovementRenderFilled by BooleanSetting("Stop Movement Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var rotateRenderFilled by BooleanSetting("Rotate Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    var startingPointRenderFilled by BooleanSetting("Starting Point Filled", true, "Whether to render the node filled or not.").withDependency { nodeSettings && nodeRenderFilled }
    private val nodePlaceKeybind by DropdownSetting("Place Keybinds").withDependency { nodeSettings }
    var awaitNodeKeybind by KeybindSetting(name ="Await", defaultKeyCode =  GLFW.GLFW_KEY_UNKNOWN, desc = "Keybind to place an Await node.").withDependency { nodeSettings && nodePlaceKeybind }
    var chestNodeKeybind by KeybindSetting("Chest", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Chest node.").withDependency { nodeSettings && nodePlaceKeybind }
    var leverNodeKeybind by KeybindSetting("Lever", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Lever node.").withDependency { nodeSettings && nodePlaceKeybind }
    var itemNodeKeybind by KeybindSetting("Item", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place an Item node.").withDependency { nodeSettings && nodePlaceKeybind }
    var batNodeKeybind by KeybindSetting("Bat", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Bat node.").withDependency { nodeSettings && nodePlaceKeybind }
    var specialNodeKeybind by KeybindSetting("Special", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Special node.").withDependency { nodeSettings && nodePlaceKeybind }
    var etherwarpNodeKeybind by KeybindSetting("Etherwarp", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place an Etherwarp node.").withDependency { nodeSettings && nodePlaceKeybind }
        .onPress { sendCommand("/odinhook ar etherwarp") }
    var dungeonbreakerNodeKeybind by KeybindSetting("Dungeonbreaker", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Dungeonbreaker node.").withDependency { nodeSettings && nodePlaceKeybind }
    var superboomNodeKeybind by KeybindSetting("Superboom", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Superboom node.").withDependency { nodeSettings && nodePlaceKeybind }
    var movementNodeKeybind by KeybindSetting("Movement", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Movement node.").withDependency { nodeSettings && nodePlaceKeybind }
    var stopMovementNodeKeybind by KeybindSetting("Stop Movement", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Stop Movement node.").withDependency { nodeSettings && nodePlaceKeybind }
    var rotateNodeKeybind by KeybindSetting("Rotate", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Rotate node.").withDependency { nodeSettings && nodePlaceKeybind }
    var startingPointNodeKeybind by KeybindSetting("Starting Point", GLFW.GLFW_KEY_UNKNOWN, "Keybind to place a Starting Point.").withDependency { nodeSettings && nodePlaceKeybind }

    var etherwarpNodeRenderLines by BooleanSetting("Etherwarp Lines", true, "Whether to render lines to Etherwarp Nodes").withDependency { nodeSettings }


    private var routeState = RouteState.IDLE
    private var currentStepIndex = 0

    init {
        on<RenderEvent.Extract> {
            val room = DungeonUtils.currentRoom ?: return@on
            renderStartingPoint(room, this)
            renderEtherwarp(room, this)

            when (routeState) {
                RouteState.IDLE -> {
                    if (HandleStartingPoint.checkAndActivate(room)) {
                        routeState = RouteState.WAITING
                        currentStepIndex = 0
                    }
                }
                RouteState.WAITING -> {
                    println("dispatching Step")
                    dispatchStep(room)
                }
                RouteState.EXECUTING -> {
                    val route = AutoRouteManager.currentRoute
                    val now = System.currentTimeMillis()
                    when (route?.steps?.getOrNull(currentStepIndex)) {
                        is RouteStep.Etherwarp -> HandleEtherwarp.tick(now)
                        else -> {}
                    }
                }
                RouteState.COMPLETE -> {
                    routeState = RouteState.IDLE
                    currentStepIndex = 0
                    println("Route Completed!")
                    setCrouchState(false)
                }
                RouteState.FAILED -> {
                    routeState = RouteState.IDLE
                    currentStepIndex = 0
                    println("Route Failed!")
                    setCrouchState(false)
                }
            }
        }
    }

    fun dispatchStep(room: Room) {
        val route = AutoRouteManager.currentRoute ?: return
        val steps = route.steps

        if (currentStepIndex >= steps.size) {
            routeState = RouteState.COMPLETE
            return
        }

        routeState = RouteState.EXECUTING
        when (val step = steps[currentStepIndex]) {
            is RouteStep.Etherwarp -> HandleEtherwarp.execute(step, room, this,
                onSuccess = { setCrouchState(false); advance(room) },
                onFail = { setCrouchState(false); fail() }
            )
            else -> {}
        }
    }

    override fun onDisable() {
        routeState = RouteState.IDLE
        currentStepIndex = 0
        AutoRouteManager.reset()
        setCrouchState(false)
    }

    private fun advance(room: Room) {
        currentStepIndex++
        dispatchStep(room)
    }

    private fun fail(cleanup: () -> Unit = {}) {
        cleanup()
        routeState = RouteState.FAILED
    }
}

