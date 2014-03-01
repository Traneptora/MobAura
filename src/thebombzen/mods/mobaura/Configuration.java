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

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Configuration extends ThebombzenAPIConfiguration<ConfigOption> {

	public static final int CONFIG_VERSION = 1;
	private final String defaultConfig;

	private File extraConfigFile;
	private long extraConfigLastModified;

	private Set<String> alwaysAttack = new HashSet<String>();
	private Set<String> neverAttack = new HashSet<String>();
	private Set<String> playersAvoid = new HashSet<String>();
	private boolean enablePlayers = false;

	public Configuration(MobAura mobAura) {
		super(mobAura, ConfigOption.class);
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
				builder.append(line).append(ThebombzenAPI.newLine);
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
				w.println("The AutoSwitch overrides file has moved to AutoSwitch_Overrides.txt");
				w.close();
			} catch (IOException ioe) {
				mobAura.throwException("Failed to fix redirect old config.",
						ioe, false);
			}
		}
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
		playersAvoid.clear();
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

	public boolean shouldUseSafeMode() {
		return (getProperty(ConfigOption.SAFETY_LEVEL).equalsIgnoreCase("Safe"))
				|| getProperty(ConfigOption.SAFETY_LEVEL).equalsIgnoreCase(
						"Normal") && !Minecraft.getMinecraft().isSingleplayer();
	}

	private void writeExtraConfig() throws IOException {
		FileWriter writer = new FileWriter(extraConfigFile);
		writer.write(defaultConfig);
		writer.flush();
		writer.close();
		extraConfigLastModified = getExtraConfigFile().lastModified();
	}

}
