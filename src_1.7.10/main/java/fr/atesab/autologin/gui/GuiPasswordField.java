package fr.atesab.autologin.gui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import org.lwjgl.opengl.GL11;

import fr.atesab.autologin.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;

/**
 * A {@link GuiTextField} used to write password
 * 
 * @author ATE47
 * @since 2.0
 *
 */
public class GuiPasswordField extends GuiTextField {
	private static final char[] WHY_NOT_CHARS = "ImAPotatoAndIdLikeToDoSomethingStupidBecauseIWantIt<3-".toCharArray();
	private static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
	private boolean allowCopy;

	public GuiPasswordField(int x, int y, int width, int height) {
		this(x, y, width, height, false);
	}

	public GuiPasswordField(int x, int y, int width, int height, boolean allowCopy) {
		super(ModMain.passwordFontRenderer, x, y, width, height);
		this.allowCopy = allowCopy;
	}

	@Override
	public void drawTextBox() {
		String text = getText();
		int pos = getCursorPosition();
		int sPos = getSelectionEnd();
		char[] t = new char[text.length()];
		for (int i = 0; i < t.length; i++)
			t[i] = WHY_NOT_CHARS[i % WHY_NOT_CHARS.length];
		setText(new String(t));
		setCursorPosition(pos);
		setSelectionPos(sPos);
		boolean lock = isFocused() && getVisible()
				&& Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
		if (lock)
			this.width -= 26;
		super.drawTextBox();
		if (lock) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(BUTTON_TEXTURES);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(this.xPosition + this.width + 3, this.yPosition + this.height / 2 - 10, 0, 146, 20,
					20);
			this.width += 26;
		}
		setText(text);
		setCursorPosition(pos);
		setSelectionPos(sPos);
	}

	@Override
	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		if (allowCopy)
			return super.textboxKeyTyped(typedChar, keyCode);
		if (isFocused() && keyCode == 24) { // CUT
			writeText("");
			return true;
		}
		return !(keyCode == 3) && super.textboxKeyTyped(typedChar, keyCode); // COPY
	}
}
