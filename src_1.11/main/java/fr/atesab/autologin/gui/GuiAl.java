package fr.atesab.autologin.gui;

import fr.atesab.autologin.ModMain;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

class GuiAl extends GuiScreen {
	protected GuiScreen parent;
	protected GuiOption<GuiAl> option;

	public GuiAl(GuiScreen parent) {
		this.parent = parent;
		this.option = new GuiOption<GuiAl>() {

			@Override
			public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
				GuiAl.this.drawGradientRect(left, top, right, bottom, startColor, endColor);
			}

			@Override
			public GuiAl getGui() {
				return GuiAl.this;
			}

			@Override
			public GuiScreen getParent() {
				return GuiAl.this.parent;
			}

			@Override
			public float getZLevel() {
				return GuiAl.this.zLevel;
			}

			@Override
			public void renderToolTip(ItemStack stack, int x, int y) {
				GuiAl.this.renderToolTip(stack, x, y);
			}
		};
	}

	protected GuiButton createButton(int id, int x, int y, int width, int height, String displayText) {
		return ModMain.getSkin().createButton(id, x, y, width, height, displayText);
	}

	protected GuiButton createButton(int id, int x, int y, String displayText) {
		return ModMain.getSkin().createButton(id, x, y, displayText);
	}

	protected void drawBackground() {
		ModMain.getSkin().drawBackground(this.option);
	}
}
