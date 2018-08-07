package fr.atesab.autologin.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public interface GuiOption<T extends GuiScreen> {
	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);

	public T getGui();

	public GuiScreen getParent();

	public float getZLevel();

	public void renderToolTip(ItemStack stack, int x, int y);
}
