package fr.atesab.autologin;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import fr.atesab.autologin.LoginData.LoginDataType;
import fr.atesab.autologin.gui.GuiAlButton;
import fr.atesab.autologin.gui.GuiOption;
import fr.atesab.autologin.gui.GuiAlButton.TextAnimation;
import fr.atesab.autologin.gui.GuiPasswordBox;
import fr.atesab.autologin.gui.GuiPasswordField;
import fr.atesab.autologin.gui.ModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * AutoLogin main class
 * 
 * @author ATE47
 * @since 1.0
 */
@Mod(name = ModMain.MOD_NAME, version = ModMain.MOD_VERSION, modid = ModMain.MOD_ID, canBeDeactivated = true, clientSideOnly = true, guiFactory = ModMain.MOD_FACTORY)
public class ModMain {
	/**
	 * mod Id
	 * 
	 * @since 2.0
	 */
	public static final String MOD_ID = "autologin";
	/**
	 * mod Name
	 * 
	 * @since 2.0
	 */
	public static final String MOD_NAME = "AutoLogin 2";
	/**
	 * mod Version
	 * 
	 * @since 2.0
	 */
	public static final String MOD_VERSION = "2.0";

	/**
	 * mod {@link IModGuiFactory} class name
	 * 
	 * @since 2.0
	 */
	public static final String MOD_FACTORY = "fr.atesab.autologin.gui.ModGuiFactory";
	/**
	 * mod event handler
	 * 
	 * @since 2.0
	 */
	public static final EventHandler EVENT_HANDLER = new EventHandler();
	/**
	 * mod main command
	 * 
	 * @since 2.0
	 */
	public static final CommandAutoLogin THE_COMMAND = new CommandAutoLogin();
	private static final String DEFAULT_PORT = ":25565";
	private static Map<String, Map<String, LoginData>> loginData = new HashMap<>();
	private static String currentSkin;
	private static File defaultConfigFile;
	private static Map<String, AutoLoginSkin> skins;
	private static Configuration config;
	private static final Gson gson = new Gson();
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final AutoLoginSkin SKIN_AL = new AutoLoginSkin() {
		private int skinCursor = 0;
		private TextAnimation[] anims = { GuiAlButton.TA_SWITCH_AQUA, GuiAlButton.TA_SWITCH_GREEN,
				GuiAlButton.TA_SWITCH_PURPLE, GuiAlButton.TA_SWITCH_RED, GuiAlButton.TA_SWITCH_YELLOW };

		@Override
		public GuiButton createButton(int id, int x, int y, int width, int height, String displayText) {
			return new GuiAlButton(id, x, y, width, height, displayText)
					.animation(anims[skinCursor = (skinCursor + 1) % anims.length]);
		}

		@Override
		public void drawBackground(GuiOption<?> screenOption) {
			int width = screenOption.getGui().width;
			int height = screenOption.getGui().height;
			int c = screenOption.getGui().mc.world == null ? 0xff444444 : 0x44000000;
			screenOption.drawGradientRect(0, 0, width, height, c, c);
		}

		@Override
		public void drawWindow(GuiOption<?> screenOption, int x, int y, int width, int height, String title) {
			FontRenderer fontRendererObj = screenOption.getGui().mc.fontRendererObj;
			screenOption.drawGradientRect(x, y - 48, x + width, y - 28, 0xffffffff, 0xffaaaaaa);
			screenOption.drawGradientRect(x, y - 28, x + width, y + height, 0x99000000, 0x99000000);
			fontRendererObj.drawString(title, x + 10, y - 38 - fontRendererObj.FONT_HEIGHT / 2, 0xff000000);
		}

		@Override
		public String getDisplayName() {
			return MOD_NAME;
		}
	};
	static {
		(skins = new HashMap<>()).put(currentSkin = "al", SKIN_AL);
		skins.put("mc", new AutoLoginSkin() {

			@Override
			public GuiButton createButton(int id, int x, int y, int width, int height, String displayText) {
				return new GuiButton(id, x, y, width, height, displayText);
			}

			@Override
			public void drawBackground(GuiOption<?> screenOption) {
				screenOption.getGui().drawDefaultBackground();
			}

			@Override
			public void drawWindow(GuiOption<?> screenOption, int x, int y, int width, int height, String title) {
				screenOption.drawGradientRect(x, y - 48, x + width, y + height, 0x99000000, 0x99000000);
				FontRenderer fontRendererObj = screenOption.getGui().mc.fontRendererObj;
				fontRendererObj.drawString(title, x + 10, y - 38 - fontRendererObj.FONT_HEIGHT / 2, 0xffffffff);
			}

			@Override
			public String getDisplayName() {
				return I18n.format("autologin.skin.mc");
			}

		});
		skins.put("psy", new AutoLoginSkin() {

			@Override
			public GuiButton createButton(int id, int x, int y, int width, int height, String displayText) {
				return new GuiAlButton(id, x, y, width, height, displayText).colorAnimation(GuiAlButton.CA_PSYCHEDELICS)
						.backgroundColorAnimation(GuiAlButton.BGC_PSYCHEDELICS);
			}

			@Override
			public void drawBackground(GuiOption<?> screenOption) {
				int width = screenOption.getGui().width;
				int height = screenOption.getGui().height;
				int c2 = GuiAlButton.getRandomColorByTime(0F);
				if (screenOption.getGui().mc.world != null)
					c2 = (c2 & 0xffffff) | 0x44000000;
				screenOption.drawGradientRect(0, 0, width, height, c2, c2);
				screenOption.drawGradientRect(0, 0, width, height, 0x44000000, 0x44000000);
			}

			@Override
			public void drawWindow(GuiOption<?> screenOption, int x, int y, int width, int height, String title) {
				FontRenderer fontRendererObj = screenOption.getGui().mc.fontRendererObj;
				screenOption.drawGradientRect(x, y - 48, x + width, y - 28, 0xffffffff, 0xffaaaaaa);
				screenOption.drawGradientRect(x, y - 28, x + width, y + height, 0x99000000, 0x99000000);
				fontRendererObj.drawString(title, x + 10, y - 38 - fontRendererObj.FONT_HEIGHT / 2, 0xff000000);
			}

			@Override
			public String getDisplayName() {
				return I18n.format("autologin.skin.psy");
			}
		});
	}
	/**
	 * The {@link FontRenderer} used in {@link GuiPasswordField}
	 * 
	 * @since 2.0
	 */
	public static FontRenderer passwordFontRenderer;

	public static void clearLoginData(boolean all) {
		if (all)
			loginData.clear();
		else {
			Map<String, LoginData> subLoginData = loginData.get(hash(mc.getSession().getUsername()));
			if (subLoginData != null)
				subLoginData.clear();
		}
		saveConfigs();
	}

	/**
	 * Get a login data of the current player with a serverAddress (can be null)
	 * 
	 * @see #getLoginData(String, boolean)
	 * @since 2.0
	 */
	public static LoginData getLoginData(String serverAddress) {
		return getLoginData(serverAddress, false);
	}

	/**
	 * Get a login data of the current player with a serverAddress with a option to
	 * create it if null
	 * 
	 * @see #getLoginData(String)
	 * @since 2.0
	 */
	public static LoginData getLoginData(String serverAddress, boolean createIfAbsent) {
		// hash server and username to only store password and options
		String husern = hash(mc.getSession().getUsername());
		String hsaddr = hash((serverAddress.endsWith(DEFAULT_PORT)
				? serverAddress.substring(0, serverAddress.length() - DEFAULT_PORT.length())
				: serverAddress).toLowerCase());
		Map<String, LoginData> subLoginData = loginData.get(husern);
		if (subLoginData == null)
			loginData.put(husern, subLoginData = new HashMap<>());
		LoginData data = subLoginData.get(hsaddr);
		if (createIfAbsent && data == null)
			subLoginData.put(hsaddr, data = new LoginData());
		return data;
	}

	/**
	 * the current skin
	 * 
	 * @since 2.0
	 */
	public static AutoLoginSkin getSkin() {
		return skins.getOrDefault(currentSkin, SKIN_AL);
	}

	/**
	 * the map of skins
	 * 
	 * @since 2.0
	 */
	public static Map<String, AutoLoginSkin> getSkinsMap() {
		return skins;
	}

	public static String hash(String data) {
		try {
			byte[] bytes = MessageDigest.getInstance("MD5").digest(data.getBytes());
			StringBuffer buffer = new StringBuffer();
			for (byte b : bytes)
				buffer.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
			return buffer.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Register an {@link AutoLoginSkin} with an id
	 * 
	 * @since 2.0
	 */
	public static void registerSkin(String id, AutoLoginSkin skin) {
		skins.put(id, skin);
	}

	public static void saveConfigs() {
		// serialize and encode login data
		Map<String, Map<String, Map<String, Object>>> serializedData = new HashMap<>();
		loginData.forEach((hashedUsername, data) -> {
			Map<String, Map<String, Object>> serializedSubData = new HashMap<>();
			data.forEach((hashedServerAddress, loginData) -> serializedSubData.put(hashedServerAddress,
					loginData.serialize()));
			serializedData.put(hashedUsername, serializedSubData);
		});
		config.get("data", "passwords", "e30=", "Don't send this to anybody !") // {} in Base64
				.set(Base64.getEncoder().encodeToString(gson.toJson(serializedData).getBytes()));
		config.get("data", "currentSkin", currentSkin).set(currentSkin);
		config.save();
	}

	/**
	 * set the currentskin with his id
	 * 
	 * @since 2.0
	 */
	public static void setCurrentSkin(String currentSkin) {
		ModMain.currentSkin = currentSkin;
	}

	public static void mergeContent(Map<String, Map<String, LoginData>> newContent) {
		newContent.forEach((hu, nsm) -> {
			Map<String, LoginData> sm = loginData.get(hu);
			if (sm == null)
				loginData.put(hu, nsm);
			else
				nsm.forEach(sm::put);
		});
	}

	public static void loadLoginFromString(String s, Map<String, Map<String, LoginData>> loginData) {
		Map<String, Object> data = gson.fromJson(new String(Base64.getDecoder().decode(s)), Map.class);
		data.forEach((hashedUsername, subData) -> {
			Map<String, LoginData> subLoginData = new HashMap<>();
			((Map<String, Object>) subData).forEach((hashedServerAddress, login) -> subLoginData
					.put(hashedServerAddress, LoginData.deserialize((Map<String, Object>) login)));
			loginData.put(hashedUsername, subLoginData);
		});
	}

	public static void syncConfigs() {
		try {
			Map<String, Map<String, LoginData>> loginData = new HashMap<>();
			loadLoginFromString(config.getString("passwords", "data", "e30=", "Don't send this to anybody !"),
					loginData);
			ModMain.loginData = loginData;
		} catch (Exception e) {
			System.err.println("Fatal error while loading passwords");
			e.printStackTrace();
		}
		currentSkin = config.getString("currentSkin", "data", currentSkin, "");
		config.save();
	}

	@Mod.EventHandler
	public void onDisabled(FMLModDisabledEvent ev) {
		MinecraftForge.EVENT_BUS.unregister(EVENT_HANDLER);
		FMLCommonHandler.instance().bus().unregister(EVENT_HANDLER);
		THE_COMMAND.disable();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent ev) {
		passwordFontRenderer = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/password.png"),
				mc.renderEngine, false);
		((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(passwordFontRenderer);
	}

	public static File getDefaultConfigFile() {
		return defaultConfigFile;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		config = new Configuration(defaultConfigFile = ev.getSuggestedConfigurationFile());
		syncConfigs();
		try {
			ClientCommandHandler.instance.registerCommand(THE_COMMAND);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MinecraftForge.EVENT_BUS.register(EVENT_HANDLER);
		FMLCommonHandler.instance().bus().register(EVENT_HANDLER);
	}
}
