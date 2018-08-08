package fr.atesab.autologin.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import static net.minecraft.util.math.MathHelper.clamp;

import java.awt.Color;

public class GuiAlButton extends GuiButton {

	@FunctionalInterface
	public interface BackgroundColorAnimation {
		public int getAnimatedBackgroundColor(boolean hover, long time, GuiAlButton button);
	}

	@FunctionalInterface
	public interface ColorAnimation {
		public int getAnimatedColor(boolean hover, long time, GuiAlButton button);
	}

	public static enum TextAlign {
		LEFT, CENTER, RIGHT;
	}

	@FunctionalInterface
	public interface TextAnimation {
		public String getAnimatedText(String text, long time, GuiAlButton button);
	}

	private static final float TETA = (float) (2 * Math.PI * 500F);
	public static final TextAnimation TA_NONE = (s, t, b) -> s;
	public static final TextAnimation TA_SWITCH_PURPLE = buildSwitch(TextFormatting.DARK_PURPLE,
			TextFormatting.LIGHT_PURPLE);
	public static final TextAnimation TA_SWITCH_RED = buildSwitch(TextFormatting.DARK_RED, TextFormatting.RED);
	public static final TextAnimation TA_SWITCH_AQUA = buildSwitch(TextFormatting.DARK_AQUA,
			TextFormatting.AQUA);
	public static final TextAnimation TA_SWITCH_YELLOW = buildSwitch(TextFormatting.GOLD,
			TextFormatting.YELLOW);
	public static final TextAnimation TA_SWITCH_GREEN = buildSwitch(TextFormatting.DARK_GREEN,
			TextFormatting.GREEN);
	public static final TextAnimation TA_SWITCH_ALL_HEAVY = buildSwitch(TextFormatting.DARK_AQUA,
			TextFormatting.DARK_GREEN, TextFormatting.DARK_RED, TextFormatting.DARK_PURPLE,
			TextFormatting.GOLD);
	public static final TextAnimation TA_SWITCH_ALL_LIGHT = buildSwitch(TextFormatting.AQUA,
			TextFormatting.GREEN, TextFormatting.RED, TextFormatting.LIGHT_PURPLE,
			TextFormatting.YELLOW);

	public static final ColorAnimation CA_NONE = (h, t, b) -> 14737632;
	public static final ColorAnimation CA_PSYCHEDELICS = (h, t, b) -> h ? getRandomColorByTime(0.66F) : 14737632;

	public static final BackgroundColorAnimation BGC_NONE = (h, t, b) -> h ? 0x99777777 : 0x99222222;
	public static final BackgroundColorAnimation BGC_PSYCHEDELICS = (h, t, b) -> h ? getRandomColorByTime(0.33F)
			: 0x99444444;

	private static TextAnimation buildSwitch(TextFormatting... content) {
		return (s, t, b) -> {
			int n = (int) (t % (content.length * (s.length() + 1)));
			int i = n / (s.length() + 1);
			n %= s.length() + 1;
			return content[(i + 1) % content.length] + s.substring(0, n) + content[i] + s.substring(n, s.length());
		};
	}

	static void drawScaledCenterString(FontRenderer fontRenderer, String text, float x, float y, int color,
			float factor) {
		drawScaledString(fontRenderer, text, x - fontRenderer.getStringWidth(text) * factor / 2, (float) y, color,
				factor);
	}

	static void drawScaledString(FontRenderer fontRenderer, String text, float x, float y, int color, float factor) {
		GL11.glScalef(factor, factor, factor);
		fontRenderer.drawStringWithShadow(text, x / factor, y / factor, color);
		GL11.glScalef(1F / factor, 1F / factor, 1F / factor);
	}

	/**
	 * Use the time to give a color with a percentage shift
	 */
	public static int getRandomColorByTime(float shift) {
		float f = (float) (System.currentTimeMillis() % ((long) TETA)) / 500F + shift * TETA / 500F;
		return ((255 << 24) | (((clamp((int) (Math.sin(f) * 150F), 0, 150) + 55) & 0xFF) << 16)
				| (((clamp((int) (Math.sin(f + (float) (2F / 3F * Math.PI)) * 150F), 0, 150) + 55) & 0xFF) << 8)
				| (((clamp((int) (Math.sin(f + (float) (4F / 3F * Math.PI)) * 150F), 0, 150) + 55)) & 0xFF) << 0);
	}

	private float sizeFactor = 1;

	public void resetSizeFactor() {
		sizeFactor = 1;
	}

	private TextAlign align = TextAlign.CENTER;

	private TextAnimation animation = TA_NONE;

	private ColorAnimation colorAnimation = CA_NONE;

	private BackgroundColorAnimation backgroundColorAnimation = BGC_NONE;

	public GuiAlButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}

	public GuiAlButton(int buttonId, int x, int y, String buttonText) {
		super(buttonId, x, y, buttonText);
	}

	public GuiAlButton align(TextAlign align) {
		this.align = align;
		return this;
	}

	public GuiAlButton animation(TextAnimation animation) {
		this.animation = animation;
		return this;
	}

	public GuiAlButton backgroundColorAnimation(BackgroundColorAnimation animation) {
		this.backgroundColorAnimation = animation;
		return this;
	}

	public GuiAlButton colorAnimation(ColorAnimation animation) {
		this.colorAnimation = animation;
		return this;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			FontRenderer fontrenderer = mc.fontRendererObj;
			sizeFactor = clamp(this.hovered ? sizeFactor + 0.1F : sizeFactor - 0.1F, 1F, 1.5F);
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width
					&& mouseY < this.yPosition + this.height;
			int x = xPosition;
			int y = (int) (yPosition + height / 2 - fontrenderer.FONT_HEIGHT * sizeFactor / (2));
			long time = System.currentTimeMillis() / 50;
			String s = (this.hovered
					? !this.enabled ? TextFormatting.STRIKETHROUGH + " " + displayString + " "
							: animation.getAnimatedText(displayString, time, this)
					: displayString);
			switch (align) {
			case LEFT:
				break;
			case CENTER:
				x += width / 2 - fontrenderer.getStringWidth(s) * sizeFactor / 2;
				break;
			case RIGHT:
				x += width - fontrenderer.getStringWidth(s) * sizeFactor;
				break;
			}
			this.mouseDragged(mc, mouseX, mouseY);
			int c = backgroundColorAnimation.getAnimatedBackgroundColor(hovered, time, this);
			drawGradientRect(xPosition, yPosition, xPosition + width, yPosition + height, c, c);
			drawScaledString(fontrenderer, s, x, y,
					packedFGColour != 0 ? packedFGColour
							: !this.enabled ? 10526880 : colorAnimation.getAnimatedColor(hovered, time, this),
					sizeFactor);
		}
	}

	public TextAlign getAlign() {
		return align;
	}

	public TextAnimation getAnimation() {
		return animation;
	}

	public BackgroundColorAnimation getBackgroundColorAnimation() {
		return backgroundColorAnimation;
	}

	public ColorAnimation getColorAnimation() {
		return colorAnimation;
	}
}
