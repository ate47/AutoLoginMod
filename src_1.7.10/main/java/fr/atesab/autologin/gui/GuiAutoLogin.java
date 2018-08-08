package fr.atesab.autologin.gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import fr.atesab.autologin.ModMain;
import fr.atesab.autologin.LoginData;
import fr.atesab.autologin.LoginData.LoginDataType;
import fr.atesab.autologin.gui.GuiAlButton.TextAlign;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class GuiAutoLogin extends GuiAl {

	private static class BiDialogBox extends GuiAl {
		private Runnable a;
		private Runnable b;
		private String title;
		private String aTitle;
		private String bTitle;

		public BiDialogBox(GuiScreen parent, Runnable a, String aTitle, Runnable b, String bTitle, String title) {
			super(parent);
			this.a = a;
			this.aTitle = aTitle;
			this.b = b;
			this.bTitle = bTitle;
			this.title = title;
		}

		@Override
		protected void actionPerformed(GuiButton button) {
			switch (button.id) {
			case 0:
				mc.displayGuiScreen(parent);
				break;
			case 1:
				a.run();
				break;
			case 2:
				b.run();
				break;
			}
			super.actionPerformed(button);
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			drawBackground();
			String s = I18n.format(title);
			GuiPasswordBox.drawCenterString(fontRendererObj, s, width / 2, height / 2 - 24, fontRendererObj.FONT_HEIGHT,
					0xffffffff);
			int sizeX = Math.max(fontRendererObj.getStringWidth(s), 306) / 2 + 8;
			drawGradientRect(width / 2 - sizeX, height / 2 - 32, width / 2 + sizeX, height / 2 + 52, 0x44000000,
					0x44000000);
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		@Override
		public void initGui() {
			buttonList.add(createButton(1, width / 2 - 152, height / 2, 150, 20, I18n.format(aTitle)));
			buttonList.add(createButton(2, width / 2 + 2, height / 2, 150, 20, I18n.format(bTitle)));
			buttonList.add(createButton(0, width / 2 - 75, height / 2 + 24, 150, 20, I18n.format("gui.cancel")));
			super.initGui();
		}
	}

	private Thread loadThread = null;
	private AtomicReference<Map<String, Map<String, LoginData>>> loadContent = new AtomicReference<>(null);

	public GuiAutoLogin(GuiScreen parent) {
		super(parent);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			ModMain.saveConfigs();
			mc.displayGuiScreen(parent);
			break;
		case 1:
			BiDialogBox box = new BiDialogBox(this, null, "gui.autologin.clear.user", null, "gui.autologin.clear.all",
					"gui.autologin.clear");
			BiDialogBox nBox = new BiDialogBox(this, null, "gui.autologin.clear.yes", () -> mc.displayGuiScreen(box),
					"gui.autologin.clear.no", "gui.autologin.clear.message");
			box.a = () -> {
				nBox.a = () -> {
					ModMain.clearLoginData(false);
					mc.displayGuiScreen(GuiAutoLogin.this);
				};
				mc.displayGuiScreen(nBox);
			};
			box.b = () -> {
				nBox.a = () -> {
					ModMain.clearLoginData(true);
					mc.displayGuiScreen(GuiAutoLogin.this);
				};
				mc.displayGuiScreen(nBox);
			};
			mc.displayGuiScreen(box);
			break;
		case 2:
			mc.displayGuiScreen(new GuiSkinSelector(this));
			break;
		case 3:
			if (!(loadThread != null && loadThread.isAlive())) {
				loadContent.set(null);
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().endsWith(".cfg");
					}

					@Override
					public String getDescription() {
						return "Config file (*.cfg)";
					}
				});
				fileChooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "AL1.X / ATEHUD 1.x (password.cfg)";
					}

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith("password.cfg");
					}
				});
				fileChooser.setSelectedFile(ModMain.getDefaultConfigFile());
				(loadThread = new Thread(() -> {
					if (fileChooser.showDialog(null,
							I18n.format("gui.autologin.data")) == JFileChooser.APPROVE_OPTION) {
						File f = fileChooser.getSelectedFile();
						Configuration config = new Configuration(f);
						Map<String, Map<String, LoginData>> map = new HashMap<>();
						// AUTOLOGIN 2
						try {
							if (config.hasCategory("data") && config.getCategory("data").containsKey("passwords"))
								ModMain.loadLoginFromString(config.getString("passwords", "data", "e30=", ""), map);
						} catch (Exception e) {
						}
						// AUTOLOGIN / ATEHUD 1.x PATTERN :
						config.getCategoryNames().forEach(username -> {
							if (config.getCategory(username).containsKey("passwords")
									&& config.getCategory(username).get("passwords").isList()) {
								Map<String, LoginData> subMap = new HashMap<>();
								map.put(ModMain.hash(username), subMap);
								Arrays.stream(config.getStringList("passwords", username, new String[0], ""))
										.forEach(d -> {
											String[] data = new String(Base64.getDecoder().decode(d)).split(",", 2);
											if (data.length == 2) {
												subMap.put(ModMain.hash(data[0]), new LoginData("/login %s",
														"/register %s %s", data[1], LoginDataType.AUTO));
											}
										});
							}
						});
						loadContent.set(map);
					}
				})).start();
			}
			break;
		}
		super.actionPerformed(button);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground();
		int k = mc.theWorld == null ? width / 16 : 0;
		drawGradientRect(k, k, width - k, height - k, 0x44000000, 0x44000000);
		GuiAlButton.drawScaledCenterString(fontRendererObj, ModMain.MOD_NAME, width / 2, height / 2 - 60, 0xffeb00bd,
				3.0F);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void updateScreen() {
		if (loadContent.get() != null) {
			mc.displayGuiScreen(new GuiLoginContentLoader(this, loadContent.getAndSet(null)));
		} else
			super.updateScreen();
	}

	@Override
	public void initGui() {
		buttonList.add(createButton(1, width / 2 - 100, height / 2 - 24, I18n.format("gui.autologin.clear")));
		buttonList.add(createButton(3, width / 2 - 100, height / 2, I18n.format("gui.autologin.data")));
		buttonList.add(createButton(2, width / 2 - 100, height / 2 + 24, I18n.format("gui.autologin.skin")));
		buttonList.add(createButton(0, width / 2 - 100, height / 2 + 48, I18n.format("gui.done")));
		super.initGui();
	}
}
