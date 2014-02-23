package thebombzen.mods.mobaura;

import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ConfigOption implements ThebombzenAPIConfigOption {

	DEFAULT_ENABLED(0, BOOLEAN, "false", "Enabled by default",
			"Enable MobAura on new worlds",
			"and on worlds never used with MobAura."), DEFAULT_INTERACT(1,
			BOOLEAN, "false", "Interact with mobs by default",
			"Enable interacting with mobs (rightclick)",
			"instead of attacking them", "on worlds never used with MobAura."), ATTACK_MOBS(
			-1, BOOLEAN, "true", "Attack Hostile Mobs",
			"Use MobAura on hostile mobs ", "Example: Zombie"), ATTACK_ANIMALS(
			-1, BOOLEAN, "true", "Attack Farm Animals",
			"Use MobAura on farm animals", "Example: Pig"), ATTACK_WATER(-1,
			BOOLEAN, "true", "Attack Water Creatures",
			"Use MobAura on water creatures", "Example: Squid"), ATTACK_TAMEABLE(
			-1, BOOLEAN, "false", "Attack Tameable Entities",
			"Use MobAura on tameable entities", "Example: Wolf"), ATTACK_NPC(
			-1, BOOLEAN, "false", "Attack NPCs", "Use MobAura on NPCs.",
			"Example: Villager"), ATTACK_LIVING(-1, BOOLEAN, "false",
			"Attack Other Living Entities", "Use MobAura on living entities",
			"which don't fall into another category."), ATTACK_FIREBALL(-1,
			BOOLEAN, "true", "Attack Fireballs",
			"Use MobAura to attempt to deflect fireballs."), TOGGLE_KEY(-1,
			KEY, "K", "MobAura Toggle Key", "This key toggles whether",
			"MobAura is enabled."), INTERACT_KEY(-1, KEY, "L",
			"MobAura Interact Toggle Key", "This key toggles whether",
			"MobAura will interact with mobs (rightclick)",
			"instead of attacking them"),
		USE_AUTOSWITCH(-1, BOOLEAN, "true", "Use AutoSwitch", "Use AutoSwitch to switch weapons", "when MobAura attacks a mob.");

	private int defaultToggleIndex;

	private String defaultValue;
	private String[] info;
	private String shortInfo;
	private int optionType;

	private ConfigOption(int defaultToggleIndex, int optionType,
			String defaultValue, String shortInfo, String... info) {
		this.defaultToggleIndex = defaultToggleIndex;
		this.defaultValue = defaultValue;
		this.info = info;
		this.shortInfo = shortInfo;
		this.optionType = optionType;
	}

	@Override
	public int getDefaultToggleIndex() {
		return defaultToggleIndex;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String[] getFiniteStringOptions() {
		throw new UnsupportedOperationException(
				"Only supported for Finite Strings!");
	}

	@Override
	public String[] getInfo() {
		return info;
	}

	@Override
	public int getOptionType() {
		return optionType;
	}

	@Override
	public String getShortInfo() {
		return shortInfo;
	}

}
