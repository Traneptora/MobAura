package thebombzen.mods.mobaura;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import net.minecraft.client.Minecraft;
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
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid = "mobaura", name = "MobAura", version = "2.7.0pre2", dependencies = "required-after:thebombzenapi", guiFactory = "thebombzen.mods.mobaura.ConfigGuiFactory")
public class MobAura extends ThebombzenAPIBaseMod {

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

	public static final int INTERACT_TOGGLE_INDEX = Configuration.DEFAULT_INTERACT
			.getDefaultToggleIndex();
	public static final int TOGGLE_INDEX = Configuration.DEFAULT_ENABLED
			.getDefaultToggleIndex();

	@Instance("mobaura")
	public static MobAura instance;

	public static Object autoSwitch;

	private Configuration configuration;

	public void attackEntity(Entity entity) {
		if (isToggleEnabled(INTERACT_TOGGLE_INDEX)
				&& !(entity instanceof EntityFireball)) {
			mc.playerController.interactWithEntitySendPacket(mc.thePlayer,
					entity);
		} else {
			if (configuration.getSingleMultiProperty(Configuration.USE_AUTOSWITCH)
					&& autoSwitch != null && entity instanceof EntityLivingBase) {
				try {
					autoSwitch
							.getClass()
							.getMethod("potentiallySwitchWeapons",
									EntityLivingBase.class)
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

		if (entity.isDead) {
			shouldAttack = false;
		}

		if (mc.thePlayer.isDead) {
			shouldAttack = false;
		}

		if (shouldAttack
				&& !configuration
						.getSingleMultiProperty(Configuration.IGNORE_HURT_TIMERS)
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
		
		if (shouldAttack && !configuration.getSingleMultiProperty(Configuration.ATTACK_FROM_BEHIND)){
			double diffX = entity.posX - mc.thePlayer.posX;
			double diffZ = entity.posZ - mc.thePlayer.posZ;
			double diffY = entity.posY - mc.thePlayer.posY;
			
			double headLookX = -Math.sin(mc.thePlayer.rotationYawHead * Math.PI / 180);
			double headLookZ = Math.cos(mc.thePlayer.rotationYawHead * Math.PI / 180);
			double headLookY = -Math.sin(mc.thePlayer.rotationPitch * Math.PI / 180);
			
			double diffAngle = Math.acos((diffX * headLookX + diffZ * headLookZ + diffY * headLookY) / Math.sqrt((diffX * diffX + diffY * diffY + diffZ * diffZ) * (headLookX * headLookX + headLookY * headLookY + headLookZ * headLookZ)));
			if (diffAngle > 0.5D * Math.PI){
				shouldAttack = false;
			}	
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
			return configuration.getSingleMultiProperty(Configuration.HOSTILE_MOBS);
		case FARM_ANIMAL:
			return configuration.getSingleMultiProperty(Configuration.FARM_ANIMALS);
		case WATER_ANIMAL:
			return configuration
					.getSingleMultiProperty(Configuration.WATER_CREATURES);
		case OTHER_LIVING:
			return configuration
					.getSingleMultiProperty(Configuration.OTHER_LIVING_ENTITIES);
		case TAMEABLE_UNOWNED:
			return configuration
					.getSingleMultiProperty(Configuration.TAMEABLE_ENTITIES);
		case FIREBALL:
			return configuration
					.getSingleMultiProperty(Configuration.DEFLECT_FIREBALLS);
		case NPC:
			return configuration.getSingleMultiProperty(Configuration.NPCS);
		default:
			return false;
		}
	}

	@SubscribeEvent
	public void clientTick(ClientTickEvent event) {

		if (!event.phase.equals(TickEvent.Phase.END)) {
			return;
		}

		if (mc.theWorld == null) {
			hurtResistantTimes.clear();
			entityQueue.clear();
			return;
		}

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

		if (mc.currentScreen != null
				&& !configuration.getSingleMultiProperty(Configuration.USE_IN_GUI)) {
			return;
		}

		if (ticks % (configuration.getSingleMultiProperty(Configuration.LARGE_PAUSE) ? 10 : 2) != 0) {
			return;
		}

		if (!isToggleEnabled(TOGGLE_INDEX)) {
			return;
		}

		if (!configuration.getSingleMultiProperty(Configuration.ATTACK_MULTIPLE)) {
			Entity entity = getNextAvailableEntityFromQueue();
			if (entity != null) {
				attackEntity(entity);
				return;
			}
		}

		@SuppressWarnings("unchecked")
		List<Entity> entities = mc.theWorld.getLoadedEntityList();

		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			boolean shouldAttack = canAttackEntity(entity);
			if (shouldAttack) {
				entityQueue.offer(entity);
			}
		}

		while (true) {
			Entity entity = getNextAvailableEntityFromQueue();
			if (entity != null) {
				attackEntity(entity);
				if (!configuration.getSingleMultiProperty(Configuration.ATTACK_MULTIPLE)) {
					break;
				}
			} else {
				break;
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public String getDownloadLocationURLString() {
		return "http://is.gd/ThebombzensMods#MobAura";
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
	public String getLongName() {
		return "MobAura";
	}

	@Override
	public String getLongVersionString() {
		return "MobAura, version 2.7.0pre2, Minecraft 1.7.2";
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
	public int getNumToggleKeys() {
		return 2;
	}

	@Override
	public String getShortName() {
		return "MA";
	}

	@Override
	protected String getToggleMessageString(int index, boolean enabled) {
		if (index == TOGGLE_INDEX) {
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
		//return "https://dl.dropboxusercontent.com/u/51080973/Mods/MobAura/MAVersion.txt";
		return "";
	}

	@Override
	public void init1(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		configuration = new Configuration(this);
		FMLCommonHandler.instance().findContainerFor(this).getMetadata().authorList = Arrays
				.asList("Thebombzen");
	}

	@Override
	public void init3(FMLPostInitializationEvent event) {
		autoSwitch = Loader.instance().getModObjectList()
				.get(Loader.instance().getIndexedModList().get("autoswitch"));
		if (autoSwitch != null) {
			System.out.println("MobAura has found AutoSwitch!");
		}
	}

}
