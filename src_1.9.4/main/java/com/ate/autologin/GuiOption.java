package com.ate.autologin;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiOption extends GuiScreen {
	public GuiOption(){}
	private GuiTextField defaultpass,defaultnewpass1,defaultnewpass2,currentpass,currentnewpass1,currentnewpass2;
	private GuiButton changeDefaultPass,changeCurrentPass,setDefaultToCurrentPass,useChangePassCommand,cancel;
	private boolean failpass=false;
	private int left;
	private int middle;
	public void initGui() {
		String[] stra={"gui.autologin.password","gui.autologin.newpassword","gui.autologin.newpassword.again"};
		left=0;
		middle=0;
		for (int i = 0; i < stra.length; i++) {
			int a=fontRendererObj.getStringWidth(I18n.format(stra[i])+" : ");
			if(a>middle)middle=a;
		}
		left=width/2-(middle+410)/2;
		middle+=left;
		failpass=false;
		buttonList.add(cancel				  =new GuiButton(0, middle+205, 5+84, 200,20,I18n.format("gui.autologin.cancel")));
		buttonList.add(changeDefaultPass	  =new GuiButton(1, middle+205, 5+63, 200,20,I18n.format("gui.autologin.change.defaultpass")));
		buttonList.add(changeCurrentPass	  =new GuiButton(2, middle, 5+63, 99,20,I18n.format("gui.autologin.change.currentpass")));
		buttonList.add(setDefaultToCurrentPass=new GuiButton(3, middle+100, 5+63, 100,20,I18n.format("gui.autologin.change.usedefaultpass")));
		buttonList.add(useChangePassCommand   =new GuiButton(4, middle, 5+84, 200,20,I18n.format("gui.autologin.change.currentpass.cmd")));
		defaultpass	   =new GuiTextField(0, ModMain.passwordFontRenderer, middle+205, 5, 200, 20);
		defaultnewpass1=new GuiTextField(1, ModMain.passwordFontRenderer, middle+205, 5+21, 200, 20);
		defaultnewpass2=new GuiTextField(2, ModMain.passwordFontRenderer, middle+205, 5+42, 200, 20);
		currentpass	   =new GuiTextField(3, ModMain.passwordFontRenderer, middle, 5, 200, 20);
		currentnewpass1=new GuiTextField(4, ModMain.passwordFontRenderer, middle, 5+21, 200, 20);
		currentnewpass2=new GuiTextField(5, ModMain.passwordFontRenderer, middle, 5+42, 200, 20);
		super.initGui();
	}
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if(failpass)drawCenteredString(fontRendererObj, I18n.format("gui.autologin.badpassword2"), width/2, 5+105, GuiConnection.RED);
		drawRightString(I18n.format("gui.autologin.password")+" : ", middle, 5+10-fontRendererObj.FONT_HEIGHT/2);
		drawRightString(I18n.format("gui.autologin.newpassword")+" : ", middle, 5+21+10-fontRendererObj.FONT_HEIGHT/2);
		drawRightString(I18n.format("gui.autologin.newpassword.again")+" : ", middle, 5+42+10-fontRendererObj.FONT_HEIGHT/2);
		if(!ModMain.defaultPassword.isEmpty())defaultpass.drawTextBox();
		defaultnewpass1.drawTextBox();
		defaultnewpass2.drawTextBox();
		currentpass.drawTextBox();
		currentnewpass1.drawTextBox();
		currentnewpass2.drawTextBox();
		if(ModMain.defaultPassword.isEmpty())setDefaultToCurrentPass.enabled=false;
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	public void drawRightString(String str,int posX,int posY){
		drawString(fontRendererObj, str, posX-fontRendererObj.getStringWidth(str), posY, GuiConnection.WHITE);
	}
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button==changeCurrentPass){
			if(currentnewpass1.getText().equals(currentnewpass2.getText()) && currentpass.getText().equals(ModMain.passwordList.get(mc.getCurrentServerData().serverIP))){
				ModMain.ChangePassword(mc.getCurrentServerData().serverIP, currentnewpass1.getText());
				ModMain.Message(I18n.format("gui.autologin.change.password.done").replaceAll("SERVERIP", mc.getCurrentServerData().serverIP));
			} else {
				failpass=true;
			}
		}
		if(button==changeDefaultPass){
			if(defaultnewpass1.getText().equals(defaultnewpass2.getText()) && (defaultpass.getText().equals(ModMain.defaultPassword) || ModMain.defaultPassword.isEmpty())){
				ModMain.setDefaultPassword(defaultnewpass1.getText());
				ModMain.Message(I18n.format("gui.autologin.change.password.default.done"));
			} else {
				failpass=true;
			}
		}
		if(button==setDefaultToCurrentPass){
			String exPass=ModMain.passwordList.get(mc.getCurrentServerData().serverIP);
			ModMain.ChangePassword(mc.getCurrentServerData().serverIP, ModMain.defaultPassword);
			mc.thePlayer.sendChatMessage("/changepassword "+exPass+" "+ModMain.defaultPassword);
			ModMain.Message(I18n.format("gui.autologin.change.password.done").replaceAll("SERVERIP", mc.getCurrentServerData().serverIP));
		}
		if(button==useChangePassCommand){
			if(currentnewpass1.getText().equals(currentnewpass2.getText()) && currentpass.getText().equals(ModMain.passwordList.get(mc.getCurrentServerData().serverIP))){
				String exPass=ModMain.passwordList.get(mc.getCurrentServerData().serverIP);
				ModMain.ChangePassword(mc.getCurrentServerData().serverIP, currentnewpass1.getText());
				mc.thePlayer.sendChatMessage("/changepassword "+exPass+" "+currentnewpass2.getText());
				ModMain.Message(I18n.format("gui.autologin.change.password.done").replaceAll("SERVERIP", mc.getCurrentServerData().serverIP));
			} else {
				failpass=true;
			}
		}
		if(button==cancel)mc.displayGuiScreen(null);
		super.actionPerformed(button);
	}
	public void doCoReg(String password,String method,int addInfo){
		String message="/"+method;
		for (int i = 0; i < addInfo; i++) {
			message+=" "+password;
		}
		ModMain.ChangePassword(mc.getCurrentServerData().serverIP, password);
		ModMain.isConnect=true;
		mc.thePlayer.sendChatMessage(message);
		mc.displayGuiScreen(null);
	}
	public void doCoReg(String method,int addInfo){
		doCoReg(ModMain.defaultPassword,method,addInfo);
	}
	public void updateScreen() {
		defaultpass.updateCursorCounter();
		defaultnewpass1.updateCursorCounter();
		defaultnewpass2.updateCursorCounter();
		currentpass.updateCursorCounter();
		currentnewpass1.updateCursorCounter();
		currentnewpass2.updateCursorCounter();
		super.updateScreen();
	}
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		defaultpass.textboxKeyTyped(typedChar, keyCode);
		defaultnewpass1.textboxKeyTyped(typedChar, keyCode);
		defaultnewpass2.textboxKeyTyped(typedChar, keyCode);
		currentpass.textboxKeyTyped(typedChar, keyCode);
		currentnewpass1.textboxKeyTyped(typedChar, keyCode);
		currentnewpass2.textboxKeyTyped(typedChar, keyCode);
		super.keyTyped(typedChar, keyCode);
	}
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
			throws IOException {
		defaultpass.mouseClicked(mouseX, mouseY, mouseButton);
		defaultnewpass1.mouseClicked(mouseX, mouseY, mouseButton);
		defaultnewpass2.mouseClicked(mouseX, mouseY, mouseButton);
		currentpass.mouseClicked(mouseX, mouseY, mouseButton);
		currentnewpass1.mouseClicked(mouseX, mouseY, mouseButton);
		currentnewpass2.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
