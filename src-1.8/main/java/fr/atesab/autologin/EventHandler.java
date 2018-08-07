package fr.atesab.autologin;

import java.lang.reflect.Field;

import fr.atesab.autologin.LoginData.LoginDataType;
import fr.atesab.autologin.gui.GuiPasswordBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenAddServer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

public class EventHandler {
	private static final Field SERVER_ADDRESS_FIELD = getNField(GuiScreenAddServer.class, GuiTextField.class, 1);

	static LoginData currentLoginData;

	private static Field getNField(Class<?> cls, Class<?> type, int n) {
		for (Field field : GuiScreenAddServer.class.getDeclaredFields()) {
			field.setAccessible(true);
			if (field.getType().equals(type)) {
				n--;
				if (n == 0)
					return field;
			}
		}
		return null;
	}

	private static String serverAddress(GuiScreen gui) {
		try {
			return ((GuiTextField) SERVER_ADDRESS_FIELD.get(gui)).getText();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	private Minecraft mc = Minecraft.getMinecraft();
	private boolean isConnected = false;
	private GuiButton loginButton = new GuiButton(0, 0, 0, "");

	@SubscribeEvent
	public void onActionPerformed(ActionPerformedEvent.Pre ev) {
		if (ev.gui instanceof GuiScreenAddServer && ev.button.equals(loginButton)) {
			try {
				String server = serverAddress(ev.gui);
				LoginData logAddress = ModMain.getLoginData(server, true);
				mc.displayGuiScreen(new GuiPasswordBox(logAddress, ev.gui, server, data -> ModMain.saveConfigs()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			ev.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent ev) {
		if (!isConnected && !mc.isSingleplayer() && mc.getCurrentServerData() != null) {
			if (ev.message.getUnformattedText().toLowerCase().contains("/login")) {
				if (currentLoginData.getType() == LoginDataType.SECURE_LOGIN)
					mc.displayGuiScreen(
							new GuiPasswordBox(currentLoginData.cloneEmpty(), null, mc.getCurrentServerData().serverIP,
									data -> mc.thePlayer.sendChatMessage(data.getLoginMessage())));
				else if (currentLoginData.emptyPassword())
					mc.displayGuiScreen(new GuiPasswordBox(currentLoginData, null, mc.getCurrentServerData().serverIP,
							data -> mc.thePlayer.sendChatMessage(data.getLoginMessage())));
				else
					mc.thePlayer.sendChatMessage(currentLoginData.getLoginMessage());
			} else if (ev.message.getUnformattedText().toLowerCase().contains("/register"))
				if (currentLoginData.getType() == LoginDataType.SECURE_LOGIN)
					mc.displayGuiScreen(
							new GuiPasswordBox(currentLoginData.cloneEmpty(), null, mc.getCurrentServerData().serverIP,
									data -> mc.thePlayer.sendChatMessage(data.getRegisterMessage())));
				else if (currentLoginData.emptyPassword())
					mc.displayGuiScreen(new GuiPasswordBox(currentLoginData, null, mc.getCurrentServerData().serverIP,
							data -> mc.thePlayer.sendChatMessage(data.getRegisterMessage())));
				else
					mc.thePlayer.sendChatMessage(currentLoginData.getRegisterMessage());
			isConnected = true;
		}
	}

	@SubscribeEvent
	public void onConnect(ClientConnectedToServerEvent ev) {
		isConnected = false;
		currentLoginData = null;
		if (!mc.isSingleplayer() && mc.getCurrentServerData() != null) {
			currentLoginData = ModMain.getLoginData(mc.getCurrentServerData().serverIP, true);
			isConnected = currentLoginData.getType() == LoginDataType.DISABLE;
		}
	}

	@SubscribeEvent
	public void onInitGui(InitGuiEvent.Post ev) {
		if (ev.gui instanceof GuiScreenAddServer) {
			for (Object obj : ev.buttonList) {
				GuiButton button = (GuiButton) obj;
				switch (button.id) {
				case 0: // done
					loginButton.xPosition = button.xPosition;
					loginButton.yPosition = button.yPosition;
					button.yPosition = ev.gui.height / 4 + 120 + 18;
					button.xPosition = ev.gui.width / 2 + 2;
					button.width = 98;
					break;
				case 1: // cancel
					button.width = 98;
					break;
				}
			}
			loginButton.displayString = I18n.format("key.autologin.loginOption");
			ev.buttonList.add(loginButton);
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent ev) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui != null && gui instanceof GuiScreenAddServer)
			loginButton.enabled = !serverAddress(gui).trim().isEmpty();
	}
}
