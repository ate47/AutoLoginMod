package com.ate.autologin;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

@Mod(version=ModMain.VERSION,modid=ModMain.MODID,name=ModMain.NAME)
public class ModMain {
	public static final String VERSION="1.0",MODID="autologin",NAME="Auto Login Mod",LittleName="AutoLog";
    private IReloadableResourceManager mcResourceManager;
    private final MetadataSerializer metadataSerializer_ = new MetadataSerializer();
	public static HashMap<String, String> passwordList;
	public static String defaultPassword="";
	public static Configuration password_config;
	public static FontRenderer passwordFontRenderer;
	public static KeyBinding optionGui;
	@Mod.Instance(MODID)
    public static ModMain instance;

	public static boolean isConnect=false;
	public static boolean isinConnect=false;
	@SubscribeEvent
	public void onKeyPressed(InputEvent.KeyInputEvent event){
		if(ModMain.optionGui.isPressed())Minecraft.getMinecraft().displayGuiScreen(new GuiOption());
	}
	@SubscribeEvent
	public void onConnectOnServer(ClientConnectedToServerEvent event){
		isConnect=false;
	}
	@SubscribeEvent
	public void onDisconnectOnServer(ClientDisconnectionFromServerEvent event){
		isConnect=false;
	}
	@SubscribeEvent
	public void onChatReceive(ClientChatReceivedEvent event){
		Minecraft mc=Minecraft.getMinecraft();
		if(!Minecraft.getMinecraft().isSingleplayer() && !isinConnect){
			if(event.getMessage().getUnformattedText().contains("/register")){
				if(ModMain.passwordList.containsKey(mc.getCurrentServerData().serverIP)){
					mc.thePlayer.sendChatMessage("/register "+ModMain.passwordList.get(mc.getCurrentServerData().serverIP+" "+ModMain.passwordList.get(mc.getCurrentServerData().serverIP)));
					isConnect=true;
				} else {
					mc.displayGuiScreen(new GuiConnection("register",2));
				}
			}else if(event.getMessage().getUnformattedText().contains("/login")){
				if(ModMain.passwordList.containsKey(mc.getCurrentServerData().serverIP)){
					mc.thePlayer.sendChatMessage("/login "+ModMain.passwordList.get(mc.getCurrentServerData().serverIP));
					isConnect=true;
				} else {
					mc.displayGuiScreen(new GuiConnection("login",1));
				}
				
			}
		}
	}
	public static void syncPasswordList() {
		String category=Minecraft.getMinecraft().getSession().getUsername();
		passwordList.clear();
		String passEncode=Base64.getEncoder().encodeToString(String.format(defaultPassword).getBytes());
		password_config.setCategoryComment(category, "Category for the username : "+category);
		passEncode=password_config.getString("defaultpassword", category, passEncode, "");
		defaultPassword=new String(Base64.getDecoder().decode(passEncode));
		String[] pass = password_config.getStringList("passwords", category, new String[]{}, "");
		for (int i = 0; i < pass.length; i++) {
			String[] valueDecode=new String(Base64.getDecoder().decode(pass[i])).split(",");
			if(valueDecode.length==2){
				passwordList.put(valueDecode[0], valueDecode[1]);
			}
		}
		password_config.save();
	}
	public static void Message(String msg){
		Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString("\u00a73[\u00a7a"+LittleName+"\u00a73]\u00a72 "+msg));
	}
	public static void ChangePassword(String server,String newPassword){
		passwordList.put(server, newPassword);
		String[] strs=new String[passwordList.size()];int i=0;
		for (String name: passwordList.keySet()){
            String key =name.toString();
            String value = passwordList.get(name).toString();
            String valueEncode=Base64.getEncoder().encodeToString(String.format(key+","+value).getBytes());
            strs[i]=valueEncode;
        i++;}
		password_config.get(Minecraft.getMinecraft().getSession().getUsername(), "passwords", passwordList.get(strs)).set(strs);
		password_config.save();
	}
	public static void setDefaultPassword(String newPassword){
		defaultPassword=newPassword;
        String valueEncode=Base64.getEncoder().encodeToString(String.format(newPassword).getBytes());
		password_config.get(Minecraft.getMinecraft().getSession().getUsername(), "defaultpassword", defaultPassword).set(valueEncode);
		password_config.save();
	}
	
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event){
		passwordList=new HashMap<String, String>();
		password_config=new Configuration(new File(new File(event.getModConfigurationDirectory(),"AutoLogin"), "password.cfg"));
		syncPasswordList();
        ClientRegistry.registerKeyBinding(optionGui = new KeyBinding("key.autologin.loginOption", Keyboard.KEY_L, "key.autologin.categories"));
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		FMLCommonHandler.instance().bus().register(instance);
	}
	@EventHandler
	public void init(FMLInitializationEvent event){
		
	}
	@EventHandler
	public void postinit(FMLPostInitializationEvent event){
		Minecraft mc=Minecraft.getMinecraft();
		passwordFontRenderer=new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/password.png"),mc.renderEngine, false);
		this.mcResourceManager = new SimpleReloadableResourceManager(metadataSerializer_);
		this.mcResourceManager.registerReloadListener(passwordFontRenderer);
		
	}
}
