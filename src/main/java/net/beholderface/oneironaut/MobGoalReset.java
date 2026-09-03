package net.beholderface.oneironaut;

import net.beholderface.oneironaut.mixin.MobGoalSelectorAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

public final class MobGoalReset {
    private MobGoalReset() {}

    public static void reset(Mob mob) {
        ((MobGoalSelectorAccessor) mob).oneironaut$setGoalSelector(
            new GoalSelector(mob.level().getProfilerSupplier())
        );
    }
}
