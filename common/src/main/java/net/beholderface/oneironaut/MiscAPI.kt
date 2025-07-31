package net.beholderface.oneironaut

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.beholderface.oneironaut.casting.environments.ReverbRodCastEnv
import net.beholderface.oneironaut.casting.iotatypes.DimIota
import net.beholderface.oneironaut.casting.iotatypes.SoulprintIota
import net.beholderface.oneironaut.network.UnBrainsweepPacket
import net.beholderface.oneironaut.recipe.OneironautRecipeTypes
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.GoalSelector
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeManager
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import net.minecraft.util.math.*
import net.minecraft.village.VillagerProfession
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.World
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.floor

fun List<Iota>.getDimIota(idx: Int, argc: Int = 0): DimIota {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DimIota) {
        return x
    }

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "oneironaut:imprint")
}

/*fun List<Iota>.getStatusEffect(idx: Int, argc: Int = 0, allowShroud : Boolean) : StatusEffect{
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is PotionIota) {
        if (!allowShroud && (x as PotionIota).getEffect().equals(
                OneironautMiscRegistry.DETECTION_RESISTANCE.get())){
            throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "oneironaut:visiblestatus")
        }
        return (x as PotionIota).effect
    }

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "oneironaut:status")
}*/

fun List<Iota>.getSoulprint(idx: Int, argc: Int = 0) : UUID {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is SoulprintIota) {
        return x.entity
    }

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "oneironaut:soulprint")
}

fun getBlockTagKey(id : Identifier?) : TagKey<Block>?{
    if (id != null){
        return TagKey.of(RegistryKeys.BLOCK, id)
    }
    return null
}
fun getEntityTagKey(id : Identifier) : TagKey<EntityType<*>>{
    return TagKey.of(RegistryKeys.ENTITY_TYPE, id)
}
fun getItemTagKey(id : Identifier) : TagKey<Item> {
    return TagKey.of(RegistryKeys.ITEM, id)
}


fun getInfuseResult(targetState: BlockState, world: World) : Triple<BlockState, Long, String?> {
    //at the moment this when thing is just for the wither rose transmutation, since everything without special behavior is now handled in recipe jsons
    var conversionResult : Triple<BlockState, Long, String?> = when(targetState.block){
        Blocks.WITHER_ROSE -> {
            val smallflowers = arrayOf(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY)
            val flowerIndex = kotlin.random.Random.nextInt(0, smallflowers.size)
            Triple(smallflowers[flowerIndex].defaultState, 5, null)
        }
        else -> Triple(Blocks.BARRIER.defaultState, -1, null)
    }
    val debugMessages = false
    if (conversionResult.second == -1L){
        Oneironaut.boolLogger(
            "did not find a hard-coded conversion",
            debugMessages
        )
        val recipeManager : RecipeManager = world.recipeManager
        val infusionRecipes = recipeManager.listAllOfType(OneironautRecipeTypes.INFUSION_TYPE)
        val recipe = infusionRecipes.find { it.matches(targetState) }
        if (recipe != null){
            Oneironaut.boolLogger(
                "found a matching recipe, ${recipe.blockIn} to ${recipe.blockOut.block.name.string}",
                debugMessages
            )
            /*val advancement = recipe.advancement
            val passedAdvancement : String? = if (advancement.equals("")){
                null
            } else {
                advancement
            }*/
            conversionResult = Triple(recipe.blockOut, recipe.mediaCost, null)
        } else {
            Oneironaut.boolLogger(
                "no matching recipe found",
                debugMessages
            )
        }
    } else {
        Oneironaut.boolLogger(
            "found a hard-coded conversion",
            debugMessages
        )
    }
    return Triple(preserveStates(targetState, conversionResult.first), conversionResult.second, conversionResult.third)
}
fun preserveStates(oldState : BlockState, desiredState : BlockState) : BlockState{
    val debugmessages = false
    var newState = desiredState
    if (desiredState != Blocks.BARRIER.defaultState){
        val boolsToKeep : List<Property<Boolean>> = listOf(Properties.WATERLOGGED, Properties.HANGING)
        for (property in boolsToKeep){
            if (oldState.contains(property)){
                val value = oldState.get(property)
                Oneironaut.boolLogger(
                    "property ${property.name} has value $value",
                    debugmessages
                )
                newState = newState.with(property, value)
            }
        }
        val intsToKeep : List<Property<Int>> = listOf(Properties.ROTATION)
        for (property in intsToKeep){
            if (oldState.contains(property)){
                val value = oldState.get(property)
                Oneironaut.boolLogger(
                    "property ${property.name} has value $value",
                    debugmessages
                )
                newState = newState.with(property, value)
            }
        }
        val dirsToKeep : List<Property<Direction>> = listOf(Properties.FACING, Properties.HORIZONTAL_FACING)
        for (property in dirsToKeep) {
            if (oldState.contains(property)) {
                val value = oldState.get(property)
                Oneironaut.boolLogger(
                    "property ${property.name} has value $value",
                    debugmessages
                )
                newState = newState.with(property, value)
            }
        }
    }
    return newState
}

fun isUnsafe(world: ServerWorld, pos: BlockPos, up: Boolean) : Boolean{
    val state = world.getBlockState(pos)
    var output = when (state.block){
        Blocks.LAVA -> true
        Blocks.FIRE -> true
        Blocks.SOUL_FIRE -> true
        Blocks.CAMPFIRE -> true
        Blocks.SOUL_CAMPFIRE -> true
        Blocks.MAGMA_BLOCK -> true
        Blocks.CACTUS -> true
        Blocks.SCULK_SHRIEKER -> true
        else -> false
    }
    if (state.isOpaque && up){
        output = true
    }
    return output
}
fun isSolid(world: ServerWorld, pos: BlockPos) : Boolean{
    var output = false
    val state = world.getBlockState(pos)
    if (state.fluidState.isEmpty && !state.isAir && !state.block.canMobSpawnInside(state)){
        output = true
    } else if (state.block.defaultState.properties.contains(Properties.WATERLOGGED) && !state.isAir){
        if (state.block.defaultState.get(Properties.WATERLOGGED) == true){
            output = true
        }
    }

    /*if (state.isTranslucent(world.getChunkAsView(floor(pos.x / 16.0).toInt(), floor(pos.z / 16.0).toInt()), pos)){
        output = true
    }*/
    return output
}

fun stringToWorld(key : String, server : MinecraftServer) : ServerWorld?{
    val regKey = RegistryKey.of(RegistryKeys.WORLD, Identifier(key))
    return server.getWorld(regKey)
}

fun playerUUIDtoServerPlayer(uuid: UUID, server: MinecraftServer): ServerPlayerEntity? {
    //val server = player.server
    return server.playerManager?.getPlayer(uuid)
}

fun Vec3d.toVec3i() : Vec3i {
    return Vec3i(floor(this.x).toInt(), floor(this.y).toInt(), floor(this.z).toInt())
}

fun Vec3d.toBlockPos() : BlockPos{
    return BlockPos(floor(this.x).toInt(), floor(this.y).toInt(), floor(this.z).toInt())
}

fun genCircle(world : StructureWorldAccess, center : BlockPos, diameter : Int, state : BlockState, replacable : Array<Block>, fillPortion : Double) : Int{
    val realCenter = Vec3d(center.x + 0.5, center.y + 0.5, center.z + 0.5)
    //val area = diameter * diameter
    val radius = diameter.toDouble() / 2
    var offset = Vec3d.ZERO
    val corner = realCenter.add(-(radius + 0.5), 0.0, -(radius + 0.5))
    var current = corner
    var placed = 0;
    for (x in 0 .. diameter){
        for (y in 0 .. diameter){
            offset = Vec3d(x.toDouble(), 0.0, y.toDouble())
            current = corner.add(offset)
            if (world.random.nextBetween(0, 999) / 10.0 <= fillPortion * 100)
            if (current.distanceTo(realCenter) <= radius && replacable.contains(world.getBlockState(BlockPos(current.toVec3i())).block)){
                world.setBlockState(BlockPos(current.toVec3i()), state, 0b10)
                placed++
            }
        }
    }
    return placed
    /*for (i in 0 .. (area * 3)){

    }*/
}

fun isPlayerEnlightened(player : ServerPlayerEntity) : Boolean {
    val adv = player.server?.advancementLoader?.get(HexAPI.modLoc("enlightenment"))
    val advs = player.advancementTracker
    val enlightened : Boolean = if (advs.getProgress(adv) != null){
        advs.getProgress(adv).isDone
    } else {
        false
    }
    return enlightened;
}

fun isUsingRod(env : CastingEnvironment) : Boolean {
    return env is ReverbRodCastEnv
}

fun getPositionsInCuboid(corner1 : BlockPos, corner2 : BlockPos, pointsToExclude : List<BlockPos>) : List<BlockPos>{
    val cuboid = Box(corner1, corner2)
    val lowerCorner = BlockPos(cuboid.minX.toInt(), cuboid.minY.toInt(), cuboid.minZ.toInt())
    val outputList : MutableList<BlockPos> = mutableListOf()
    var currentPos : BlockPos
    for (i in 0 .. cuboid.xLength.toInt()){
        for (j in 0 .. cuboid.yLength.toInt()){
            for (k in 0 .. cuboid.zLength.toInt()){
                currentPos = lowerCorner.add(i, j, k)
                if (!pointsToExclude.contains(currentPos)){
                    outputList.add(currentPos)
                }
            }
        }
    }
    return outputList.toList()
}

fun getPositionsInCuboid(corner1 : BlockPos, corner2 : BlockPos, pointToExclude : BlockPos) : List<BlockPos>{
    return getPositionsInCuboid(corner1, corner2, listOf(pointToExclude))
}

fun getPositionsInCuboid(corner1 : BlockPos, corner2 : BlockPos) : List<BlockPos>{
    return getPositionsInCuboid(
        corner1,
        corner2,
        listOf(corner2.add((corner1.x - corner2.x).absoluteValue + 20, 0, 0))
    )
}

fun Box.corners() : List<Vec3d>{
    return listOf(
        Vec3d(this.minX, this.minY, this.minZ), Vec3d(this.maxX, this.minY, this.minZ),
        Vec3d(this.maxX, this.maxY, this.minZ), Vec3d(this.maxX, this.maxY, this.maxZ),
        Vec3d(this.minX, this.maxY, this.maxZ), Vec3d(this.minX, this.minY, this.maxZ),
        Vec3d(this.maxX, this.minY, this.maxZ), Vec3d(this.minX, this.maxY, this.minZ)
    )
}

fun vecProximity(a: Vec3d, b: Vec3d): Double {
    //I'm not sure what the best way to do this is, but this way works for hexes so it's what I tried first
    return a.normalize().subtract(b.normalize()).length()
}

fun vecProximity(a: Direction, b: Vec3d): Double {
    return vecProximity(Vec3d.of(a.vector), b)
}

fun MobEntity.unbrainsweep(){
    assert(false)
    val patient = this
    if (!patient.world.isClient){
        //Oneironaut.LOGGER.info("Attempting to unbrainsweep ${this.name} client-side")
        IXplatAbstractions.INSTANCE.sendPacketNear(patient.pos, 256.0, patient.world as ServerWorld, UnBrainsweepPacket(patient.id))
    }/* else {
        Oneironaut.LOGGER.info("Attempting to unbrainsweep ${this.name} server-side")
    }*/
    val component = HexCardinalComponents.BRAINSWEPT.get(patient)
    component.isBrainswept = false
    patient.isAiDisabled = false
    val brain = patient.brain
    patient.goalSelector = GoalSelector(patient.world.profilerSupplier)
    brain.resetPossibleActivities()
    brain.refreshActivities(patient.world.timeOfDay, patient.world.time)
    if (patient is VillagerEntity){
        val newData = patient.villagerData.withLevel(0).withProfession(VillagerProfession.NITWIT)
        patient.villagerData = newData
    }
    //patient.dataTracker.allEntries?.get(0)?.isDirty = true
    val refreshNBT = patient.writeNbt(NbtCompound())
    patient.readNbt(refreshNBT)
}

fun Box.longestAxisLength() : Double{
    val x = this.xLength
    val y = this.yLength
    val z = this.zLength
    return if (x >= y && x >= z){
        x
    } else if (y >= x && y >= z){
        y
    } else {
        z
    }
}

fun Box.intersectsPermissive(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Boolean {
    return this.minX <= maxX && this.maxX >= minX && this.minY <= maxY && this.maxY >= minY && this.minZ <= maxZ && this.maxZ >= minZ
}

fun Box.intersectsPermissive(box : Box): Boolean {
    return this.intersectsPermissive(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
}

fun Box.volume() : Double {
    return this.xLength * this.yLength * this.zLength
}

fun FrozenPigment.rawColor(time : Float, pos : Vec3d){
    this.colorProvider.getColor(time, pos)
}

fun List<Iota>.getNonlivingIfAllowed(idx: Int, argc: Int = 0): Entity {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    val nonlivingAllowed = OneironautConfig.server.planeShiftNonliving
    if (x is EntityIota) {
        val e = x.entity
        if (nonlivingAllowed || (e is LivingEntity && e !is ArmorStandEntity)){
            return e
        }
    }
    val stub = if (nonlivingAllowed){
        "entity"
    } else {
        "entity.living"
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), stub)
}