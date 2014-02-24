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

	public ConfigScreen(GuiScreen parentScreen) {
		super(MobAura.instance, parentScreen, MobAura.instance.getConfiguration());
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == 4911) {
			try {
				try {
					Desktop.getDesktop().edit(((Configuration)config).getExtraConfigFile());
				} catch (UnsupportedOperationException e){
					Desktop.getDesktop().open(((Configuration)config).getExtraConfigFile());
				}
			} catch (IOException e) {
				mod.throwException("Unable to open file!", e, false);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int i = buttonList.size() - 1;
		this.buttonList.add(new GuiButton(4911, width / 2 - 206 + (i & 1) * 207,
				height / 6 + 23 * (i >> 1) - 18, 205, 20,
				"Open MobAura Overrides File..."));
	}

}
