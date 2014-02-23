package thebombzen.mods.mobaura;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid = "MobAura", name = "MobAura", version = "2.5.0", dependencies = "required-after:ThebombzenAPI")
public class MobAura extends ThebombzenAPIBaseMod implements ITickHandler {

	private long ticks = 0;

	private Queue<Entity> entityQueue = new ArrayDeque<Entity>();

	private Map<EntityLivingBase, Integer> hurtResistantTimes = new HashMap<EntityLivingBase, Integer>();

	public static final Minecraft mc = Minecraft.getMinecraft();

	public static final int OTHER_NONLIVING = 0;

	public static final int OTHER_LIVING = 1;

	public static final int PLAYER = 2;

	public static final int TAMEABLE_OWNED = 3;

	public static final int MOB = 4;
	public static final int FARM_ANIMAL = 5;
	public static final int WATER_ANIMAL = 6;
	public static final int TAMEABLE_UNOWNED = 7;

	public static final int FIREBALL = 8;
	public static final int NPC = 9;

	@Instance(value = "MobAura")
	public static MobAura instance;
	
	public static Object autoSwitch;

	private Configuration configuration;

	public MobAura() {
		configuration = new Configuration(this);
	}

	@Override
	public void activeKeyPressed(int keyCode) {

	}

	public void attackEntity(Entity entity) {
		if (isToggleEnabled(1)) {
			mc.playerController.func_78768_b(mc.thePlayer, entity);
		} else {
			if (configuration.getPropertyBoolean(ConfigOption.USE_AUTOSWITCH) && autoSwitch != null && entity instanceof EntityLivingBase) {
				try {
					autoSwitch.getClass().getMethod("potentiallySwitchWeapons", EntityLivingBase.class)
							.invoke(autoSwitch, entity);
				} catch (Exception e) {
					throwException("Error switching with AutoSwitch.", e, false);
				}
			}
			mc.thePlayer.swingItem();
			mc.playerController.attackEntity(mc.thePlayer, entity);
		}
		if (entity instanceof EntityLivingBase) {
			hurtResistantTimes.put((EntityLivingBase) entity,
					((EntityLivingBase) entity).maxHurtResistantTime);
		}
	}

	public boolean canAttackEntity(Entity entity) {

		if (configuration.shouldNeverAttackEntity(entity)) {
			return false;
		}

		boolean shouldAttack = canKillEntityType(getEntityType(entity))
				|| configuration.shouldAlwaysAttackEntity(entity);

		if (shouldAttack && entity instanceof EntityLivingBase
				&& ((EntityLivingBase) entity).getHealth() == 0F) {
			shouldAttack = false;
		}

		if (shouldAttack && entity.isDead) {
			shouldAttack = false;
		}

		if (shouldAttack && mc.thePlayer.isDead) {
			shouldAttack = false;
		}

		if (shouldAttack
				&& entity instanceof EntityLivingBase
				&& hurtResistantTimes.containsKey(entity)
				&& hurtResistantTimes.get(entity) > ((EntityLivingBase) entity).maxHurtResistantTime / 2.0F) {
			shouldAttack = false;
		}

		double distanceSq = 25D;

		if (shouldAttack && !mc.thePlayer.canEntityBeSeen(entity)) {
			distanceSq = 5D;
		}

		if (shouldAttack
				&& mc.thePlayer.getDistanceSqToEntity(entity) >= distanceSq) {
			shouldAttack = false;
		}

		return shouldAttack;
	}

	public boolean canKillEntityType(int type) {
		switch (type) {
		case OTHER_NONLIVING:
		case PLAYER:
		case TAMEABLE_OWNED:
			return false;
		case MOB:
			return configuration.getPropertyBoolean(ConfigOption.ATTACK_MOBS);
		case FARM_ANIMAL:
			return configuration
					.getPropertyBoolean(ConfigOption.ATTACK_ANIMALS);
		case WATER_ANIMAL:
			return configuration.getPropertyBoolean(ConfigOption.ATTACK_WATER);
		case OTHER_LIVING:
			return configuration.getPropertyBoolean(ConfigOption.ATTACK_LIVING);
		case TAMEABLE_UNOWNED:
			return configuration
					.getPropertyBoolean(ConfigOption.ATTACK_TAMEABLE);
		case FIREBALL:
			return configuration
					.getPropertyBoolean(ConfigOption.ATTACK_FIREBALL);
		case NPC:
			return configuration.getPropertyBoolean(ConfigOption.ATTACK_NPC);
		default:
			return false;
		}
	}

	public void clientTick() {
		Iterator<Entry<EntityLivingBase, Integer>> iterator = hurtResistantTimes
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<EntityLivingBase, Integer> e = iterator.next();
			EntityLivingBase living = e.getKey();
			if (living.isDead || living.getHealth() <= 0F || e.getValue() <= 0) {
				iterator.remove();
			} else {
				e.setValue(e.getValue() - 1);
			}
		}

		ticks++;

		if (mc.currentScreen != null) {
			return;
		}

		if (ticks % 10 != 0) {
			return;
		}

		if (!isToggleEnabled(0)) {
			return;
		}

		Entity entity = getNextAvailableEntityFromQueue();
		if (entity != null) {
			attackEntity(entity);
			return;
		}

		List<Entity> entities = mc.theWorld.getLoadedEntityList();
		for (int i = 0; i < entities.size(); i++) {
			entity = entities.get(i);
			boolean shouldAttack = canAttackEntity(entity);
			if (shouldAttack) {
				entityQueue.offer(entity);
			}
		}

		entity = getNextAvailableEntityFromQueue();
		if (entity != null) {
			attackEntity(entity);
			return;
		}
	}

	@Override
	public ThebombzenAPIConfigScreen createConfigScreen(GuiScreen base) {
		return new ConfigScreen(this, base, configuration);
	}

	@Override
	public ThebombzenAPIConfiguration<?> getConfiguration() {
		return configuration;
	}

	public int getEntityType(Entity entity) {
		if (entity instanceof EntityFireball) {
			return FIREBALL;
		}
		if (entity instanceof EntityPlayer) {
			return PLAYER;
		}
		if (entity instanceof EntityTameable) {
			if (((EntityTameable) entity).getOwner() != null) {
				return TAMEABLE_OWNED;
			} else {
				return TAMEABLE_UNOWNED;
			}
		}
		if (entity instanceof EntityWaterMob) {
			return WATER_ANIMAL;
		}
		if (entity instanceof EntityAnimal) {
			return FARM_ANIMAL;
		}
		if (entity instanceof IMob) {
			return MOB;
		}
		if (entity instanceof INpc) {
			return NPC;
		}
		if (entity instanceof EntityLivingBase) {
			return OTHER_LIVING;
		}

		return OTHER_NONLIVING;
	}

	@Override
	public String getLabel() {
		return "thebombzen.mods.mobaura.MobAura";
	}

	@Override
	public String getLongName() {
		return "MobAura";
	}

	@Override
	public String getLongVersionString() {
		return "MobAura v2.5.0 for Minecraft 1.6.4";
	}

	public Entity getNextAvailableEntityFromQueue() {
		while (!entityQueue.isEmpty()) {
			Entity entity = entityQueue.poll();
			if (canAttackEntity(entity)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public int getNumActiveKeys() {
		return 0;
	}

	@Override
	public int getNumToggleKeys() {
		return 2;
	}

	@Override
	public String getShortName() {
		return "MA";
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getToggleMessageString(int index, boolean enabled) {
		if (index == 0) {
			if (enabled) {
				return "MobAura is now enabled.";
			} else {
				return "MobAura is now disabled.";
			}
		} else {
			if (enabled) {
				return "MobAura will now interact with entities.";
			} else {
				return "MobAura will now attack entities.";
			}
		}

	}

	@Override
	public String getVersionFileURLString() {
		return "https://dl.dropboxusercontent.com/u/51080973/MobAura/MAVersion.txt";
	}

	@Override
	public boolean hasConfigScreen() {
		return true;
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		TickRegistry.registerTickHandler(this, Side.CLIENT);
	}

	@Override
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		autoSwitch = Loader.instance().getModObjectList().get(Loader.instance().getIndexedModList().get("AutoSwitch"));
		if (autoSwitch != null){
			System.out.println("MobAura has found AutoSwitch!");
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		clientTick();
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {

	}

}
