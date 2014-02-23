package thebombzen.mods.mobaura;

import java.awt.Desktop;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigScreen extends ThebombzenAPIConfigScreen {

	public ConfigScreen(MobAura mod, GuiScreen parentScreen,
			Configuration config) {
		super(mod, parentScreen, config);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == 4911) {
			try {
				Desktop.getDesktop().open(
						((Configuration) config).getExtraConfigFile());
			} catch (IOException e) {
				mod.throwException("Unable to open file!", e, false);
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(new GuiButton(4911, this.width / 2 - 100,
				this.height / 6 + 140, 200, 20,
				"Open MobAura Overrides File..."));
	}

}
