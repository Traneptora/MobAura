package thebombzen.mods.mobaura;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Configuration extends ThebombzenAPIConfiguration<ConfigOption> {

	public static final int CONFIG_VERSION = 0;
	private final String defaultConfig;

	private File extraConfigFile;
	private long extraConfigLastModified;

	private Set<Integer> alwaysAttack = new HashSet<Integer>();
	private Set<Integer> neverAttack = new HashSet<Integer>();
	private Set<String> playersAvoid = new HashSet<String>();
	private boolean enablePlayers = false;

	public Configuration(MobAura mod) {
		super(mod, ConfigOption.class);
		extraConfigFile = new File(new File(
				ThebombzenAPI.proxy.getMinecraftFolder(), "config"),
				"MobAura_Overrides.cfg");
		StringBuilder dcb = new StringBuilder();
		dcb.append(
				"# Use this file to override whether MobAura will attack a particular entity")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Lines beginning with # are ignored").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Config version number:").append(ThebombzenAPI.newLine);
		dcb.append(
				"# If this is not found or does not match the current number, MobAura will replace your config with the default one.")
				.append(ThebombzenAPI.newLine);
		dcb.append("R0").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# ==== HOW TO SPECIFY ENTITES ====").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Use the entity's NETWORK ID").append(
				ThebombzenAPI.newLine);
		dcb.append("# Network IDs can be found on the Minecraft Wiki").append(
				ThebombzenAPI.newLine);
		dcb.append(
				"# For entities from mods, ask your mod author to provide the network IDS")
				.append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append(
				"# Note that the Network ID is the same as the damage value on the spawn egg")
				.append(ThebombzenAPI.newLine);
		dcb.append("# A pig spawn egg is 383:90, so a Pig's Network ID is 90")
				.append(ThebombzenAPI.newLine);
		dcb.append(
				"# Sure enough, on the Pig article on the Minecraft Wiki, it lists ")
				.append(ThebombzenAPI.newLine);
		dcb.append("# 90 as the Network ID").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# =========== OVERRIDES ==========").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Use").append(ThebombzenAPI.newLine);
		dcb.append("# + ID").append(ThebombzenAPI.newLine);
		dcb.append(
				"# to tell MobAura to always attack a particular entity. For example,")
				.append(ThebombzenAPI.newLine);
		dcb.append("# to tell MobAura to always attack zombies").append(
				ThebombzenAPI.newLine);
		dcb.append("# even if attacking hostile mobs is off, use").append(
				ThebombzenAPI.newLine);
		dcb.append("# + 54").append(ThebombzenAPI.newLine);
		dcb.append("# Similarly, use").append(ThebombzenAPI.newLine);
		dcb.append("# - ID").append(ThebombzenAPI.newLine);
		dcb.append(
				"# to tell MobAura to never attack a particular entity. For example,")
				.append(ThebombzenAPI.newLine);
		dcb.append("# - 90").append(ThebombzenAPI.newLine);
		dcb.append("# tells MobAura to never attack pigs.").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("# Place MobAura Overrides here (without the #) ").append(
				ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		dcb.append("").append(ThebombzenAPI.newLine);
		defaultConfig = dcb.toString();
	}

	public File getExtraConfigFile() {
		return extraConfigFile;
	}

	@Override
	protected void loadProperties() throws IOException {
		super.loadProperties();
		MobAura.instance.setToggleKeyCode(
				ConfigOption.DEFAULT_ENABLED.getDefaultToggleIndex(),
				Keyboard.getKeyIndex(getProperty(ConfigOption.TOGGLE_KEY)));
		MobAura.instance.setToggleKeyCode(
				ConfigOption.DEFAULT_INTERACT.getDefaultToggleIndex(),
				Keyboard.getKeyIndex(getProperty(ConfigOption.INTERACT_KEY)));
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
			sb.append(s).append(ThebombzenAPI.newLine);
		}
		reader.close();
		parseConfig(sb.toString());
		extraConfigLastModified = getExtraConfigFile().lastModified();
	}

	protected void parseConfig(String config) {
		alwaysAttack.clear();
		neverAttack.clear();
		Scanner s = new Scanner(config);
		s.useDelimiter(ThebombzenAPI.newLine);
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
			if (first == 'R') {
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
				int id = -1;
				try {
					id = Integer.parseInt(sub);
				} catch (NumberFormatException nfe) {

				}
				if (id > 0) {
					alwaysAttack.add(id);
				} else {
					System.err.println("Error on line: " + line);
				}
			} else if (first == '-') {
				String sub = line.substring(1);
				int id = -1;
				try {
					id = Integer.parseInt(sub);
				} catch (NumberFormatException nfe) {

				}
				if (id > 0) {
					neverAttack.add(id);
				} else {
					System.err.println("Error on line: " + line);
				}
			} else if (first == 'K') {
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
	protected void setPropertyWithoutSave(ThebombzenAPIConfigOption option,
			String value) {
		super.setPropertyWithoutSave(option, value);
		if (option.equals(ConfigOption.TOGGLE_KEY)) {
			mod.setToggleKeyCode(
					ConfigOption.DEFAULT_ENABLED.getDefaultToggleIndex(),
					Keyboard.getKeyIndex(value));
		} else if (option.equals(ConfigOption.INTERACT_KEY)) {
			mod.setToggleKeyCode(
					ConfigOption.DEFAULT_INTERACT.getDefaultToggleIndex(),
					Keyboard.getKeyIndex(value));
		}
	}

	public boolean shouldAlwaysAttackEntity(Entity entity) {
		if (entity instanceof EntityPlayer) {
			if (enablePlayers) {
				for (String name : playersAvoid) {
					if (name.equalsIgnoreCase(((EntityPlayer) entity).username)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		int id = EntityList.getEntityID(entity);
		if (alwaysAttack.contains(id)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean shouldNeverAttackEntity(Entity entity) {
		if (entity instanceof EntityPlayer) {
			if (enablePlayers) {
				for (String name : playersAvoid) {
					if (name.equalsIgnoreCase(((EntityPlayer) entity).username)) {
						return true;
					}
				}
				return false;
			} else {
				return true;
			}
		}
		int id = EntityList.getEntityID(entity);
		if (neverAttack.contains(id)) {
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
