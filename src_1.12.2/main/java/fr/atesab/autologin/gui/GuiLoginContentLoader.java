package fr.atesab.autologin.gui;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import fr.atesab.autologin.LoginData;
import fr.atesab.autologin.ModMain;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiLoginContentLoader extends GuiAl {
	private Map<String, Map<String, LoginData>> content;
	private String msg;

	public GuiLoginContentLoader(GuiScreen parent, Map<String, Map<String, LoginData>> content) {
		super(parent);
		this.content = content;
		AtomicInteger i = new AtomicInteger(0);
		content.entrySet().parallelStream().forEach(e -> i.addAndGet(e.getValue().size()));
		msg = I18n.format(i.get() > 1 ? "gui.autologin.data.found.p" : "gui.autologin.data.found.s", i);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground();
		int k = mc.world == null ? width / 16 : 0;
		drawGradientRect(k, k, width - k, height - k, 0x44000000, 0x44000000);
		GuiPasswordBox.drawCenterString(fontRenderer, msg, width / 2, height / 2 - 24, 20, 0xffffffff);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		buttonList.add(createButton(1, width / 2 - 102, height / 2, 100, 20, I18n.format("gui.cancel")));
		buttonList.add(createButton(0, width / 2 + 2, height / 2, 100, 20, I18n.format("gui.done")));
		super.initGui();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			ModMain.mergeContent(content);
			ModMain.saveConfigs();
		case 1:
			mc.displayGuiScreen(parent);
			break;
		}
		super.actionPerformed(button);
	}
}
