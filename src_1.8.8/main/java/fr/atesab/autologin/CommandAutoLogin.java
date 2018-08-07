package fr.atesab.autologin;

import java.util.ArrayList;
import java.util.List;

import fr.atesab.autologin.gui.GuiAutoLogin;
import fr.atesab.autologin.gui.GuiPasswordBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class CommandAutoLogin implements ICommand {
	private static class Screen {
		private long delay;
		private GuiScreen screen;

		public Screen(GuiScreen screen, long delay) {
			FMLCommonHandler.instance().bus().register(this);
			this.delay = delay;
			this.screen = screen;
		}

		@SubscribeEvent
		public void onTick(TickEvent.ClientTickEvent ev) {
			delay--;
			if (delay < 0) {
				Minecraft.getMinecraft().displayGuiScreen(screen);
				FMLCommonHandler.instance().bus().unregister(this);
			}
		}
	}

	private final String name = "autologin";
	private final List<String> aliases = new ArrayList<>();
	private boolean modEnabled = true;

	public CommandAutoLogin() {
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return new ArrayList<>();
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return modEnabled;
	}

	@Override
	public int compareTo(ICommand arg0) {
		return getCommandName().compareToIgnoreCase(arg0.getCommandName());
	}

	void disable() {
		this.modEnabled = false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		Minecraft mc = Minecraft.getMinecraft();
		if (EventHandler.currentLoginData != null)
			new Screen(new GuiPasswordBox(EventHandler.currentLoginData, mc.currentScreen,
					mc.getCurrentServerData().serverIP, data -> {
					}), 10);
		else
			sender.addChatMessage(new ChatComponentText("No LoginData !")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
	}

	@Override
	public List<String> getCommandAliases() {
		return aliases;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return getCommandName();
	}

	@Override
	public String getCommandName() {
		return name;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}
