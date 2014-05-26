package thebombzen.mods.mobaura;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.configuration.ConfigOption;
import thebombzen.mods.thebombzenapi.configuration.SingleMultiBoolean;
import thebombzen.mods.thebombzenapi.configuration.ThebombzenAPIConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Configuration extends ThebombzenAPIConfiguration {
	
	public static final ConfigOption TOGGLE_KEY = new ConfigOption(Keyboard.getKeyIndex("K"), "TOGGLE_KEY", "MobAura Toggle Key",
			"This key toggles whether",
			"MobAura is enabled.");
	public static final ConfigOption INTERACT_KEY = new ConfigOption(Keyboard.getKeyIndex("L"), "INTERACT_KEY", "MobAura Interact Toggle Key",
			"This key toggles whether",
			"MobAura will interact with mobs (rightclick)",
			"instead of attacking them.");
	public static final ConfigOption DEFAULT_ENABLED = new ConfigOption(0, false, "DEFAULT_ENABLED", "Enabled by default",
			"Enable MobAura on newly created worlds",
			"and on worlds never used with MobAura.");
	public static final ConfigOption DEFAULT_INTERACT = new ConfigOption(1, false, "DEFAULT_INTERACT", "Interact with mobs by default",
			"Enable interacting with mobs (rightclick)",
			"instead of attacking them",
			"on worlds newly created worlds",
			"and on worlds never used with MobAura.");
	public static final ConfigOption HOSTILE_MOBS = new ConfigOption(SingleMultiBoolean.ALWAYS, "HOSTILE_MOBS", "Hostile Mobs",
			"Use MobAura on hostile mobs",
			"Example: Zombie");
	public static final ConfigOption FARM_ANIMALS = new ConfigOption(SingleMultiBoolean.ALWAYS, "FARM_ANIMALS", "Farm Animals",
			"Use MobAura on farm animals",
			"Example: Pig");
	public static final ConfigOption WATER_CREATURES = new ConfigOption(SingleMultiBoolean.NEVER, "WATER_CREATURES", "Water Creatures",
			"Use MobAura on water creatures",
			"Example: Squid");
	public static final ConfigOption TAMEABLE_ENTITIES = new ConfigOption(SingleMultiBoolean.NEVER, "TAMEABLE_ENTITIES", "Tameable Entities",
			"Use MobAura on tameable entities",
			"Example: Wolf");
	public static final ConfigOption NPCS = new ConfigOption(SingleMultiBoolean.NEVER, "NPCS", "NPCs",
			"Use MobAura on NPCs.",
			"Example: Villager");
	public static final ConfigOption OTHER_LIVING_ENTITIES = new ConfigOption(SingleMultiBoolean.NEVER, "OTHER_LIVING_ENTITIES", "Other Living Entities",
			"Use MobAura on living entities",
			"which don't fall into another category.");
	public static final ConfigOption DEFLECT_FIREBALLS = new ConfigOption(SingleMultiBoolean.ALWAYS, "DEFLECT_FIREBALLS", "Deflect Fireballs",
			"Use MobAura to attempt to deflect fireballs.");
	public static final ConfigOption USE_AUTOSWITCH = new ConfigOption(SingleMultiBoolean.ALWAYS, "USE_AUTOSWITCH", "Use AutoSwitch",
			"Use AutoSwitch to switch weapons",
			"when MobAura attacks a mob",
			"(but not when interacting).");
	public static final ConfigOption LARGE_PAUSE = new ConfigOption(SingleMultiBoolean.MULTIPLAYER_ONLY, "LARGE_PAUSE", "Large Pause",
			"Pause for 1/2 second between attacking",
			"or interacting with mobs.",
			"",
			EnumChatFormatting.RED + "WARNING!" + EnumChatFormatting.RESET + " May have highly undesirable results",
			"if this is OFF and MobAura is interacting.");
	public static final ConfigOption ATTACK_MULTIPLE = new ConfigOption(SingleMultiBoolean.SINGLEPLAYER_ONLY, "ATTACK_MULTIPLE", "Attack Multiple",
			"Attack (or interact with) multiple",
			"mobs at the same time.");
	public static final ConfigOption USE_IN_GUI = new ConfigOption(SingleMultiBoolean.NEVER, "USE_IN_GUI", "Use in GUI",
			"Use MobAura when a GUI Screen is open.");
	public static final ConfigOption IGNORE_HURT_TIMERS = new ConfigOption(SingleMultiBoolean.NEVER, "IGNORE_HURT_TIMERS", "Ignore Hurt Timers",
		"Ignore \"Hurt Resistant Timers.\"",
		"Normally an entity gains attack resistance",
		"right after it is hurt. Some mods bypass this.",
		"MobAura normally refrains from attacking during",
		"this time. Turn on to disable this feature.");
	public static final ConfigOption ATTACK_FROM_BEHIND = new ConfigOption(SingleMultiBoolean.SINGLEPLAYER_ONLY, "ATTACK_FROM_BEHIND", "Attack From Behind",
		"Attack (or interact with) entities",
		"that are behind you.");

	
	
	public static final int CONFIG_VERSION = 1;
	private final String defaultConfig;

	private File extraConfigFile;
	private long extraConfigLastModified;

	private Set<String> alwaysAttack = new HashSet<String>();
	private Set<String> neverAttack = new HashSet<String>();
	private Set<String> playersAvoid = new HashSet<String>();
	private boolean enablePlayers = false;

	public Configuration(MobAura mobAura) {
		super(mobAura);
		extraConfigFile = new File(
				ThebombzenAPI.sideSpecificUtilities.getMinecraftDirectory()
						+ File.separator + "config" + File.separator
						+ "MobAura_Overrides.txt");
		File oldExtraConfigFile = new File(extraConfigFile.getParentFile(),
				"MobAura_Overrides.cfg");
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ThebombzenAPI.getResourceAsStream(mobAura,
							"MobAura_Overrides.txt")));
			String line;
			while (null != (line = reader.readLine())) {
				builder.append(line).append(ThebombzenAPI.NEWLINE);
			}
			reader.close();
		} catch (IOException ioe) {
			mobAura.throwException("Could not read default config!", ioe, true);
		} finally {
			defaultConfig = builder.toString();
		}
		if (oldExtraConfigFile.exists()) {
			try {
				PrintWriter w = new PrintWriter(new FileWriter(
						oldExtraConfigFile));
				w.println("The MobAura overrides file has moved to MobAura_Overrides.txt");
				w.close();
			} catch (IOException ioe) {
				mobAura.throwException("Failed to fix redirect old config.",
						ioe, false);
			}
		}
	}
	

	@Override
	public ConfigOption[] getAllOptions() {
		return new ConfigOption[]{TOGGLE_KEY, INTERACT_KEY, DEFAULT_ENABLED, DEFAULT_INTERACT, HOSTILE_MOBS, FARM_ANIMALS, WATER_CREATURES, TAMEABLE_ENTITIES, NPCS, OTHER_LIVING_ENTITIES, DEFLECT_FIREBALLS, USE_AUTOSWITCH, LARGE_PAUSE, ATTACK_MULTIPLE, USE_IN_GUI, IGNORE_HURT_TIMERS, ATTACK_FROM_BEHIND};
	}

	public File getExtraConfigFile() {
		return extraConfigFile;
	}

	@Override
	protected void loadProperties() throws IOException {
		super.loadProperties();
		MobAura.instance.setToggleKeyCode(
				DEFAULT_ENABLED.getDefaultToggleIndex(),
				getKeyCodeProperty(TOGGLE_KEY));
		MobAura.instance.setToggleKeyCode(
				DEFAULT_INTERACT.getDefaultToggleIndex(),
				getKeyCodeProperty(INTERACT_KEY));
		if (!extraConfigFile.exists()) {
			writeExtraConfig();
			parseConfig(defaultConfig);
			return;
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(
				extraConfigFile));
		String s;
		while (null != (s = reader.readLine())) {
			sb.append(s).append(ThebombzenAPI.NEWLINE);
		}
		reader.close();
		parseConfig(sb.toString());
		extraConfigLastModified = getExtraConfigFile().lastModified();
	}

	protected void parseConfig(String config) {
		alwaysAttack.clear();
		neverAttack.clear();
		playersAvoid.clear();
		Scanner s = new Scanner(config);
		s.useDelimiter(ThebombzenAPI.NEWLINE);
		int version = -1;
		enablePlayers = false;
		while (s.hasNext()) {
			String line = s.next();
			int index = line.indexOf('#');
			if (index >= 0) {
				line = line.substring(0, index);
			}
			line = line.replaceAll("\\s", "");
			if (line.length() == 0) {
				continue;
			} else if (line.length() < 2) {
				System.err.println("Error on line: " + line);
				continue;
			}
			char first = line.charAt(0);
			if (first == 'R' || first == 'r') {
				String sub = line.substring(1);
				try {
					version = Integer.parseInt(sub);
				} catch (NumberFormatException nfe) {
					version = -1;
				}
				if (version != CONFIG_VERSION) {
					try {
						writeExtraConfig();
					} catch (IOException ioe) {
						mod.throwException("Could not write config file!", ioe,
								true);
					} finally {
						parseConfig(defaultConfig);
					}
					s.close();
					return;
				}
			} else if (first == '+') {
				String sub = line.substring(1);
				alwaysAttack.add(sub);
			} else if (first == '-') {
				String sub = line.substring(1);
				neverAttack.add(sub);
			} else if (first == 'K' || first == 'k') {
				enablePlayers = true;
				String sub = line.substring(1);
				playersAvoid.add(Minecraft.getMinecraft().getSession()
						.getUsername());
				playersAvoid.add(sub);
			}
		}
		if (version != CONFIG_VERSION) {
			try {
				writeExtraConfig();
			} catch (IOException ioe) {
				mod.throwException("Could not write config file!", ioe, true);
			} finally {
				parseConfig(defaultConfig);
			}
		}
		s.close();
	}

	@Override
	protected void setPropertyWithoutSave(ConfigOption option,
			String value) {
		super.setPropertyWithoutSave(option, value);
		if (option.equals(TOGGLE_KEY)) {
			mod.setToggleKeyCode(
					DEFAULT_ENABLED.getDefaultToggleIndex(),
					ThebombzenAPI.getExtendedKeyIndex(value));
		} else if (option.equals(INTERACT_KEY)) {
			mod.setToggleKeyCode(
					DEFAULT_INTERACT.getDefaultToggleIndex(),
					ThebombzenAPI.getExtendedKeyIndex(value));
		}
	}

	public boolean shouldAlwaysAttackEntity(Entity entity) {
		if (entity instanceof EntityPlayer) {
			if (enablePlayers) {
				for (String name : playersAvoid) {
					if (name.equalsIgnoreCase(((EntityPlayer) entity)
							.getCommandSenderName())) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		String name = EntityList.getEntityString(entity);
		if (alwaysAttack.contains(name)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean shouldNeverAttackEntity(Entity entity) {
		if (entity instanceof EntityPlayer) {
			if (enablePlayers) {
				for (String name : playersAvoid) {
					if (name.equalsIgnoreCase(((EntityPlayer) entity)
							.getCommandSenderName())) {
						return true;
					}
				}
				return false;
			} else {
				return true;
			}
		}
		String name = EntityList.getEntityString(entity);
		if (neverAttack.contains(name)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean shouldRefreshConfig() {
		if (super.shouldRefreshConfig()) {
			return true;
		}
		if (extraConfigLastModified != getExtraConfigFile().lastModified()) {
			return true;
		} else {
			return false;
		}
	}

	private void writeExtraConfig() throws IOException {
		FileWriter writer = new FileWriter(extraConfigFile);
		writer.write(defaultConfig);
		writer.flush();
		writer.close();
		extraConfigLastModified = getExtraConfigFile().lastModified();
	}

}
