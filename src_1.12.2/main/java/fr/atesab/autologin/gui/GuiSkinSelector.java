package fr.atesab.autologin.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import fr.atesab.autologin.AutoLoginSkin;
import fr.atesab.autologin.ModMain;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiSkinSelector extends GuiAl {
	private Map<Integer, String> buttonsId = new HashMap<>();
	private boolean preventer = false;

	public GuiSkinSelector(GuiScreen parent) {
		super(parent);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mc.displayGuiScreen(parent);
		} else {
			ModMain.setCurrentSkin(buttonsId.getOrDefault(button.id, ""));
			setButtons();
		}
		super.actionPerformed(button);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground();
		GuiAlButton.drawScaledCenterString(fontRenderer, I18n.format("gui.autologin.skin"), width / 2,
				height / 2 - 12 * (ModMain.getSkinsMap().size()), 0xffffffff, 2F);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		preventer = false;
		setButtons();
		super.initGui();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		preventer = false;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void setButtons() {
		if (preventer)
			return;
		buttonList.clear();
		buttonsId.clear();
		final int posY = height / 2 - 12 * (ModMain.getSkinsMap().size()) - 24;
		AtomicInteger i = new AtomicInteger(1);
		ModMain.getSkinsMap().forEach((id, skin) -> {
			int bid;
			buttonsId.put(bid = i.getAndIncrement(), id);
			buttonList.add(createButton(bid, width / 2 - 100, i.get() * 24 + posY, skin.getDisplayName()));
		});
		buttonList.add(createButton(0, width / 2 - 100, height - 24, I18n.format("gui.done")));
		preventer = true;
	}

}
