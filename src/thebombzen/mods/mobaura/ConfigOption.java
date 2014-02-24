package thebombzen.mods.mobaura;

import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ConfigOption implements ThebombzenAPIConfigOption {

	DEFAULT_ENABLED(0, BOOLEAN, "false", "Enabled by default",
		"Enable MobAura on newly created worlds",
		"and on worlds never used with MobAura."),
	DEFAULT_INTERACT(1, BOOLEAN, "false", "Interact with mobs by default",
		"Enable interacting with mobs (rightclick)",
		"instead of attacking them",
		"on worlds newly created worlds",
		"and on worlds never used with MobAura."),
	HOSTILE_MOBS(-1, BOOLEAN, "true", "Hostile Mobs",
		"Use MobAura on hostile mobs",
		"Example: Zombie"),
	FARM_ANIMALS(-1, BOOLEAN, "true", "Farm Animals",
		"Use MobAura on farm animals",
		"Example: Pig"),
	WATER_CREATURES(-1, BOOLEAN, "true", "Water Creatures",
		"Use MobAura on water creatures",
		"Example: Squid"),
	TAMEABLE_ENTITIES(-1, BOOLEAN, "false", "Tameable Entities",
		"Use MobAura on tameable entities",
		"Example: Wolf"),
	NPCS(-1, BOOLEAN, "false", "NPCs",
		"Use MobAura on NPCs.",
		"Example: Villager"),
	OTHER_LIVING_ENTITIES(-1, BOOLEAN, "false",	"Other Living Entities",
		"Use MobAura on living entities",
		"which don't fall into another category."),
	DEFLECT_FIREBALLS(-1, BOOLEAN, "true", "Deflect Fireballs",
		"Use MobAura to attempt to deflect fireballs."),
	TOGGLE_KEY(-1, KEY, "K", "MobAura Toggle Key",
		"This key toggles whether",
		"MobAura is enabled."),
	INTERACT_KEY(-1, KEY, "L", "MobAura Interact Toggle Key",
		"This key toggles whether",
		"MobAura will interact with mobs (rightclick)",
		"instead of attacking them"),
	USE_AUTOSWITCH(-1, BOOLEAN, "true", "Use AutoSwitch",
		"Use AutoSwitch to switch weapons",
		"when MobAura attacks a mob",
		"(but not when interacting).");

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
