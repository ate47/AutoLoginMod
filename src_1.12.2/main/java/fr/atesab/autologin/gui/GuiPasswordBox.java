package fr.atesab.autologin.gui;

import java.io.IOException;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;

import fr.atesab.autologin.LoginData;
import fr.atesab.autologin.ModMain;
import fr.atesab.autologin.LoginData.LoginDataType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiPasswordBox extends GuiAl {
	static void drawCenterString(FontRenderer fontRenderer, String text, int x, int y, int height, int color) {
		fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text) / 2,
				y + height / 2 - fontRenderer.FONT_HEIGHT / 2, color);
	}

	private static void drawRightString(FontRenderer fontRenderer, String text, GuiTextField field, int color) {
		if (field.getVisible())
			fontRenderer.drawString(text, field.x - fontRenderer.getStringWidth(text),
					field.y + field.height / 2 - fontRenderer.FONT_HEIGHT / 2, color);
	}

	private LoginData data;
	private String server;
	private GuiPasswordField oldPassword;
	private GuiPasswordField password;
	private GuiPasswordField passwordAgain;
	private GuiTextField loginPattern;
	private GuiTextField registerPattern;
	private GuiTextField[] fields;
	private GuiButton done;
	private GuiButton cancel;
	private GuiButton type;
	private GuiButton[] buttons;
	private int sizeY = 0, y = 0;

	private boolean checkOld;

	private Consumer<LoginData> setter;

	public GuiPasswordBox(LoginData data, GuiScreen parent, String server, Consumer<LoginData> setter) {
		super(parent);
		this.data = data;
		this.parent = parent;
		this.server = server;
		this.setter = setter;
		(this.oldPassword = new GuiPasswordField(0, 0, 0, 246, 16)).setVisible(this.checkOld = !data.emptyPassword());
		password = new GuiPasswordField(0, 0, 0, 246, 16);
		passwordAgain = new GuiPasswordField(0, 0, 0, 246, 16);
		fontRenderer = Minecraft.getMinecraft().fontRenderer;
		loginPattern = new GuiTextField(0, fontRenderer, 0, 0, 246, 16);
		loginPattern.setMaxStringLength(100);
		loginPattern.setText(data.getLoginPattern());
		registerPattern = new GuiTextField(0, fontRenderer, 0, 0, 246, 16);
		registerPattern.setMaxStringLength(100);
		registerPattern.setText(data.getRegisterPattern());
		fields = new GuiTextField[] { oldPassword, password, passwordAgain, loginPattern, registerPattern };
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			if (checkPass()) {
				if (data.getType() != LoginDataType.DISABLE)
					data.password(password.getText());
				if (setter != null)
					setter.accept(data);
			}
		case 1:
			mc.displayGuiScreen(parent);
			break;
		case 2:
			LoginDataType[] types = LoginDataType.values();
			data.type(types[(data.getType().ordinal() + 1) % types.length]);
			updateBox();
			break;
		}
		super.actionPerformed(button);
	}

	private boolean checkPass() {
		return data.getType() != LoginDataType.AUTO || ((!checkOld || ((checkOld && data.match(oldPassword.getText()))))
				&& password.getText().equals(passwordAgain.getText()));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground();
		ModMain.getSkin().drawWindow(option, width / 2 - 204, y, 408, sizeY,
				I18n.format("key.autologin.loginOption") + " - " + server);
		if (!checkPass())
			drawCenterString(fontRenderer, I18n.format("gui.autologin.badpassword" + (checkOld ? "2" : "")), width / 2,
					y - 24, 20, 0xffcc0000);
		for (GuiTextField field : fields)
			if (field.getVisible())
				field.drawTextBox();
		drawRightString(fontRenderer, I18n.format("gui.autologin.currentpass") + ": ", oldPassword, 0xffffffff);
		drawRightString(fontRenderer, I18n.format("gui.autologin.password") + ": ", password, 0xffffffff);
		drawRightString(fontRenderer, I18n.format("gui.autologin.password.again") + ": ", passwordAgain, 0xffffffff);
		drawRightString(fontRenderer, I18n.format("gui.autologin.change.pattern.register") + ": ", registerPattern,
				0xffffffff);
		drawRightString(fontRenderer, I18n.format("gui.autologin.change.pattern.login") + ": ", loginPattern,
				0xffffffff);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		int x = width / 2 - 48;
		for (GuiTextField field : fields)
			field.x = x;
		done = createButton(0, width / 2 + 70, height / 2 + 48, 130, 20, I18n.format("gui.done"));
		cancel = createButton(1, width / 2 - 66, height / 2 + 48, 132, 20, I18n.format("gui.cancel"));
		type = createButton(2, width / 2 - 200, height / 2 + 48, 130, 20, "");
		buttons = new GuiButton[] { type, cancel, done };
		for (GuiButton button : buttons)
			buttonList.add(button);
		updateBox();
		super.initGui();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_TAB) {
			int i;
			a: {
				for (i = 0; i < fields.length; i++)
					if (fields[i].isFocused()) {
						fields[i].setFocused(false);
						break a;
					}
				i--;
			}
			int k = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? -1 : 1;
			i = modulus(i + k, fields.length);
			for (int j = 0; j < fields.length; i = modulus(i + k, fields.length), j++)
				if (fields[i].getVisible()) {
					fields[i].setFocused(true);
					break;
				}
		} else if ((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) && done.enabled
				&& done.visible) {
			actionPerformed(done);
		} else {
			for (GuiTextField field : fields)
				if (field.getVisible())
					field.textboxKeyTyped(typedChar, keyCode);
			super.keyTyped(typedChar, keyCode);
		}
	}

	private int modulus(int a, int b) {
		int n = a % b;
		return n < 0 ? n + b : n;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (GuiTextField field : fields)
			if (field.getVisible())
				field.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void updateBox() {
		type.displayString = data.getType().getLang();
		switch (data.getType()) {
		case AUTO:
			oldPassword.setVisible(checkOld);
			password.setVisible(true);
			passwordAgain.setVisible(true);
			loginPattern.setVisible(true);
			registerPattern.setVisible(true);
			break;
		case SECURE_LOGIN:
			password.setVisible(true);
			loginPattern.setVisible(true);
			registerPattern.setVisible(true);

			oldPassword.setVisible(false);
			passwordAgain.setVisible(false);
			break;
		case DISABLE:
			for (int i = 0; i < fields.length; i++)
				fields[i].setVisible(false);
			break;
		}
		int y = this.y = height / 2 - 46;
		sizeY = 24;
		for (GuiTextField field : fields)
			if (field.getVisible()) {
				field.y = y;
				y += 24;
				sizeY += 24;
			}
		int l = (400 - 4 * (buttons.length - 1));
		int sX = l / buttons.length;
		int x = buttons[0].x = width / 2 - 200;
		buttons[0].width = sX + l % buttons.length;
		x += 4 + buttons[0].width;
		buttons[0].y = y;
		for (int i = 1; i < buttons.length; i++) {
			buttons[i].x = x;
			buttons[i].y = y;
			x += 4 + (buttons[i].width = sX);
		}
		y += 4;
	}

	@Override
	public void updateScreen() {
		done.enabled = checkPass();
		data.loginPattern(loginPattern.getText());
		data.registerPattern(registerPattern.getText());
		for (GuiTextField field : fields)
			field.updateCursorCounter();
		super.updateScreen();
	}
}
