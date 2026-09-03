package net.beholderface.oneironaut;

import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

//this class exists for API things that I understand better without kotlin
public class MiscAPIJ {
    public static class departure{
        private static final Map<LivingEntity, Map<ServerLevel, Tuple<Vec3, Long>>> departureData = new HashMap<>();
        public static boolean putNewUser(LivingEntity user) {
            if (!departureData.containsKey(user)) {
                Map<ServerLevel, Tuple<Vec3, Long>> newMap = new HashMap<>();
                departureData.put(user, newMap);
                return true;
            } else {
                return false;
            }
        }
        public static boolean addDepartureEntry(LivingEntity user){
            if (departureData.containsKey(user)){
                Map<ServerLevel, Tuple<Vec3, Long>> data = departureData.get(user);
                data.put((ServerLevel) user.level(), new Tuple<>(user.position(), user.level().getGameTime()));
                return true;
            } else {
                return false;
            }
        }
        public static boolean departedThisTick(LivingEntity user, ServerLevel world){
            if (departureData.containsKey(user)){
                Map<ServerLevel, Tuple<Vec3, Long>> data = departureData.get(user);
                if (data.containsKey(world)){
                    Tuple<Vec3, Long> dimData = data.get(world);
                    return dimData.getB() == world.getGameTime();
                }
            }
            return false;
        }
    }
}
