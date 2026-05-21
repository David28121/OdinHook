package com.odtheking.odin.features.impl.dungeon.autoroutes

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.rotateToNorth
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Rotations
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import java.io.File
import java.lang.reflect.Type

object AutoRouteManager {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(RouteStep::class.java, RouteStepAdapter())
        .create()
    private val routeFile: File by lazy {
        FabricLoader.getInstance().configDir.resolve("teoh/autoroutes.json").toFile()
    }

    // roomName -> list of authored routes
    private val routes = mutableMapOf<String, MutableList<RoomRoute>>()

    // currently active route for this room visit
    var currentRoute: RoomRoute? = null
        private set

    // route being authored right now via command
    private var authoringStartingPoint: RouteNode? = null
    private val authoringSteps = mutableListOf<RouteStep>()

    fun init() {
        println("Dear odin, I would greatly like to wonder if the errors I am getting are due to your code being so good I cannot understand it, or so Shit that " +
                "somehow porting code that was BUILT ON ODIN, breaks and causes errors checking an aabb on a render event.extract" +
                "Sincerly, Daveed")
        loadRoutes()
        on<RenderEvent.Extract> {
            currentRoute = null
            authoringStartingPoint = null
            authoringSteps.clear()
        }
    }


    fun setAuthoringStartingPoint(pos: BlockPos?) {
        val room = DungeonUtils.currentRoom ?: return
        if (pos == null) return
        val relative = pos.subtract(room.clayPos.atY(0)).rotateToNorth(room.rotation)
        authoringStartingPoint = RouteNode(relative.x, relative.y, relative.z)
    }

    fun <T : RouteStep> addStepWithFace(pos: BlockPos?, face: String, factory: (RouteNode, String) -> T) {
        val room = DungeonUtils.currentRoom ?: return
        if (pos == null) return
        val relative = pos.subtract(room.clayPos.atY(0)).rotateToNorth(room.rotation)
        val relativeFace = rotateFaceToNorth(face, room.rotation)
        authoringSteps.add(factory(RouteNode(relative.x, relative.y, relative.z), relativeFace))
    }

    fun addStepNoFace(pos: BlockPos?, factory: (RouteNode) -> RouteStep) {
        val room = DungeonUtils.currentRoom ?: return
        if (pos == null) return
        val relative = pos.subtract(room.clayPos.atY(0)).rotateToNorth(room.rotation)
        authoringSteps.add(factory(RouteNode(relative.x, relative.y, relative.z)))
    }

    fun getAuthoringStartingPoint(): RouteNode? = authoringStartingPoint

    fun saveCurrentRoute() {
        val start = authoringStartingPoint ?: return
        val roomName = DungeonUtils.currentRoomName
        if (roomName == "Unknown") return

        val route = RoomRoute(startingPoint = start, steps = authoringSteps.toList())
        routes.getOrPut(roomName) { mutableListOf() }.add(route)
        writeRoutes()
        authoringStartingPoint = null
        authoringSteps.clear()

        selectRouteForRoom(roomName)
    }
    private fun loadRoutes() {
        if (!routeFile.exists()) return
        try {
            val type = object : TypeToken<Map<String, List<RoomRoute>>>() {}.type
            val loaded: Map<String, List<RoomRoute>> = gson.fromJson(routeFile.readText(), type)
            routes.clear()
            loaded.forEach { (room, roomRoutes) ->
                routes[room] = roomRoutes.toMutableList()
                println(routes)
            }
        } catch (e: Exception) {
            println("Failed to load autoroutes: ${e.message}")
        }
    }

    private fun writeRoutes() {
        routeFile.parentFile.mkdirs()
        routeFile.writeText(gson.toJson(routes))
    }

    fun reset() {
        currentRoute = null
        authoringStartingPoint = null
        authoringSteps.clear()
    }

    fun selectRouteForRoom(roomName: String) {
        val roomRoutes = routes[roomName] ?: run {
            currentRoute = null  // clear stale route
            return
        }
        if (roomRoutes.isEmpty()) {
            currentRoute = null  // clear stale route
            return
        }
        currentRoute = roomRoutes.random()
    }

    fun getRoutesForRoom(roomName: String): List<RoomRoute>? = routes[roomName]

    fun getAuthoringSteps(): List<RouteStep> = authoringSteps.toList()

    fun undoLastStep() {
        if (authoringSteps.isNotEmpty()) {
            authoringSteps.removeLastOrNull()
        } else {
            // no steps left, undo the starting point
            authoringStartingPoint = null
        }
    }

    fun clearRoomRoutes(roomName: String) {
        routes.remove(roomName)
        currentRoute = null
        writeRoutes()
    }

    fun rotateFaceToNorth(face: String, rotation: Rotations): String {
        val dir = Direction.byName(face.lowercase()) ?: return face
        val rotated = when (rotation) {
            Rotations.NORTH -> dir
            Rotations.SOUTH -> when (dir) {
                Direction.NORTH -> Direction.SOUTH
                Direction.EAST -> Direction.WEST
                Direction.SOUTH -> Direction.NORTH
                Direction.WEST -> Direction.EAST
                else -> dir
            }
            Rotations.WEST  -> when (dir) {
                Direction.NORTH -> Direction.EAST
                Direction.SOUTH -> Direction.WEST
                Direction.EAST  -> Direction.SOUTH
                Direction.WEST  -> Direction.NORTH
                else -> dir
            }
            Rotations.EAST  -> when (dir) {
                Direction.NORTH -> Direction.WEST
                Direction.SOUTH -> Direction.EAST
                Direction.EAST  -> Direction.NORTH
                Direction.WEST  -> Direction.SOUTH
                else -> dir
            }
            else -> dir
        }
        return rotated.name
    }

    fun rotateFaceFromNorth(face: String, rotation: Rotations): String {

        val dir = Direction.byName(face.lowercase()) ?: return face
        val rotated = when (rotation) {
            Rotations.NORTH -> dir
            Rotations.SOUTH -> when (dir) {
                Direction.NORTH -> Direction.SOUTH
                Direction.EAST -> Direction.WEST
                Direction.SOUTH -> Direction.NORTH
                Direction.WEST -> Direction.EAST
                else -> dir
            }
            Rotations.WEST  -> when (dir) {
                Direction.NORTH -> Direction.WEST
                Direction.SOUTH -> Direction.EAST
                Direction.EAST  -> Direction.NORTH
                Direction.WEST  -> Direction.SOUTH
                else -> dir
            }
            Rotations.EAST  -> when (dir) {
                Direction.NORTH -> Direction.EAST
                Direction.SOUTH -> Direction.WEST
                Direction.EAST  -> Direction.SOUTH
                Direction.WEST  -> Direction.NORTH
                else -> dir
            }
            else -> dir
        }
        return rotated.name
    }

}

class RouteStepAdapter : JsonDeserializer<RouteStep>, JsonSerializer<RouteStep> {
    override fun serialize(src: RouteStep, type: Type, ctx: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        when (src) {
            is RouteStep.Etherwarp -> {
                obj.addProperty("type", "etherwarp")
                obj.add("target", ctx.serialize(src.target))
                obj.add("face", ctx.serialize(src.face))
            }
            is RouteStep.BreakBlock -> {
                obj.addProperty("type", "breakblock")
                obj.add("target", ctx.serialize(src.target))
                obj.add("face", ctx.serialize(src.face))
            }
            is RouteStep.Superboom -> {
                obj.addProperty("type", "superboom")
                obj.add("target", ctx.serialize(src.target))
                obj.add("face", ctx.serialize(src.face))
            }
            is RouteStep.SecretChest -> {
                obj.addProperty("type", "secretchest")
                obj.add("target", ctx.serialize(src.target))
                obj.add("face", ctx.serialize(src.face))
            }
            is RouteStep.SecretSpecial -> {
                obj.addProperty("type", "secretspecial")
                obj.add("target", ctx.serialize(src.target))
                obj.add("face", ctx.serialize(src.face))
            }
            is RouteStep.RotateTo -> {
                obj.addProperty("type", "rotateto")
                obj.add("target", ctx.serialize(src.target))
                obj.add("face", ctx.serialize(src.face))
            }
            is RouteStep.SecretItem -> {
                obj.addProperty("type", "secretitem")
                obj.add("target", ctx.serialize(src.target))
            }
            is RouteStep.SecretLever -> {
                obj.addProperty("type", "secretlever")
                obj.add("target", ctx.serialize(src.target))
            }
            is RouteStep.SecretBat -> {
                obj.addProperty("type", "secretbat")
                obj.add("target", ctx.serialize(src.target))
            }
            is RouteStep.AwaitNode -> {
                obj.addProperty("type", "await")
                obj.add("target", ctx.serialize(src.target))
            }
            is RouteStep.Movement -> {
                obj.addProperty("type", "movement")
                obj.add("target", ctx.serialize(src.target))
            }
            is RouteStep.StopMovement -> {
                obj.addProperty("type", "stopmovement")
                obj.add("target", ctx.serialize(src.target))
            }
            else -> {}
        }
        return obj
    }

    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): RouteStep {
        val obj = json.asJsonObject
        val target = ctx.deserialize<RouteNode>(obj.get("target"), RouteNode::class.java)
        return when (obj.get("type").asString) {
            "etherwarp" -> RouteStep.Etherwarp(target, obj.get("face")?.asString ?: "UP")
            "breakblock" -> RouteStep.BreakBlock(target, obj.get("face")?.asString ?: "UP")
            "superboom" -> RouteStep.Superboom(target, obj.get("face")?.asString ?: "UP")
            "secretchest" -> RouteStep.SecretChest(target, obj.get("face")?.asString ?: "UP")
            "rotateto" -> RouteStep.RotateTo(target, obj.get("face")?.asString ?: "UP")
            "secretspecial" -> RouteStep.SecretSpecial(target, obj.get("face")?.asString ?: "UP")
            "secretitem" -> RouteStep.SecretItem(target)
            "secretlever" -> RouteStep.SecretLever(target)
            "secretbat" -> RouteStep.SecretBat(target)
            "awaitnode" -> RouteStep.AwaitNode(target)
            "movement" -> RouteStep.Movement(target)
            "stopmovement" -> RouteStep.StopMovement(target)
            else -> throw JsonParseException("Unknown step type")
        }
    }
}