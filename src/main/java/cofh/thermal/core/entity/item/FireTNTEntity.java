package cofh.thermal.core.entity.item;

import cofh.lib.entity.AbstractTNTEntity;
import cofh.lib.util.AreaUtils;
import cofh.lib.util.Utils;
import cofh.thermal.core.entity.projectile.FireGrenadeEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static cofh.thermal.core.ThermalCore.BLOCKS;
import static cofh.thermal.core.init.TCoreReferences.FIRE_TNT_ENTITY;
import static cofh.thermal.lib.common.ThermalIDs.ID_FIRE_TNT;

public class FireTNTEntity extends AbstractTNTEntity {

    public FireTNTEntity(EntityType<? extends FireTNTEntity> type, World worldIn) {

        super(type, worldIn);
    }

    public FireTNTEntity(World worldIn, double x, double y, double z, @Nullable LivingEntity igniter) {

        super(FIRE_TNT_ENTITY, worldIn, x, y, z, igniter);
    }

    @Override
    public Block getBlock() {

        return BLOCKS.get(ID_FIRE_TNT);
    }

    @Override
    protected void explode() {

        if (Utils.isServerWorld(level)) {
            if (!this.isInWater()) {
                AreaUtils.igniteNearbyEntities(this, level, this.blockPosition(), radius, FireGrenadeEntity.effectDuration);
                AreaUtils.igniteSpecial(this, level, this.blockPosition(), radius, true, true, owner);
                AreaUtils.igniteNearbyGround(this, level, this.blockPosition(), radius, 0.2);
                makeAreaOfEffectCloud();
            }
            this.remove();
        }
        this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1.0D, 0.0D, 0.0D);
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 2.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
    }

    private void makeAreaOfEffectCloud() {

        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(level, getX(), getY() + 0.5D, getZ());
        cloud.setRadius(1);
        cloud.setParticle(ParticleTypes.FLAME);
        cloud.setDuration(CLOUD_DURATION);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick((radius - cloud.getRadius()) / (float) cloud.getDuration());

        level.addFreshEntity(cloud);
    }

}