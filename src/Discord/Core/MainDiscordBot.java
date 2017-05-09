package Discord.Core;

import Discord.Commands.*;
import Discord.Commands.ModCommands.BanCommand;
import Discord.Commands.ModCommands.ChannelCommands.ChannelBan;
import Discord.Commands.ModCommands.ChannelCommands.ChannelKick;
import Discord.Commands.ModCommands.ChannelCommands.ChannelUnban;
import Discord.Commands.ModCommands.KickCommand;
import Discord.Commands.ModCommands.MuteCommand;
import Discord.Commands.ModCommands.UnMuteCommand;
import Discord.Commands.ModCommands.Warn.*;
import Discord.Commands.StatusSystem.StatusCommand;
import Discord.Core.ActivitySystem.ActivitySystem;
import Discord.Core.ActivitySystem.RoleChangedListener;
import Discord.Core.ActivitySystem.Staff.StaffActivityCommand;
import Discord.Core.ActivitySystem.User.ActivityMessageListener;
import Discord.Core.ActivitySystem.ManualCommands.GivePointsCommand;
import Discord.Core.ActivitySystem.ManualCommands.RemovePointsCommand;
import Discord.Core.ActivitySystem.User.UserActivityCommand;
import Discord.Core.Events.UserJoin;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.LoggerUtil;
import Startup.BotCore;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;

import java.util.EnumSet;

public class MainDiscordBot
{
	private static final EnumSet<Permissions> BOT_PERMISSIONS = EnumSet.of(Permissions.ADMINISTRATOR);
	
	private static String command_prefix = "!";
	private static IUser errorUser = null;
	
	public static boolean silentMode = false;
	
	public static void launchDiscord() throws Exception
	{
		String token = DiscordBotBase.debug ? DiscordBotBase.getValue("debug_token") : DiscordBotBase.getValue("normal_token");
		
		new DiscordBotBase(token, BOT_PERMISSIONS, "botData")
		{
			@Override
			public String getCommandSign()
			{
				return DiscordBotBase.debug ? "-" : command_prefix;
			}
			
			@Override
			public String getVersion()
			{
				return BotCore.version;
			}
			
			@Override
			public void handleException( Exception e )
			{
				LoggerUtil.exception(e);
				LoggerUtil.warnUserError(e, errorUser);
			}
		};
		
		Discord4J.disableChannelWarnings();
		ConfigFile.initConfig();
		
		try {
			errorUser = DiscordBotBase.discordClient.getApplicationOwner();
			System.out.println("Error report user set to: \"" + errorUser.getName() + "\"");
		} catch (DiscordException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		init();
		
//		ActivitySystem.init();
		
		registerCommands();
		registerEvents();
		
		Announcements.init();

		WarnSystem.init();
	}
	

	
	public static String guildID = null;
	public static IRole defaultRole;
	public static IGuild guild;
	
	public static IGuild[] guildList;
	
	public static void init(){
		guildID = "309224369085743114";
		command_prefix = ConfigFile.getValueOrDefault("command_prefix", command_prefix);
		
		guild = DiscordBotBase.discordClient.getGuildByID(guildID);
		defaultRole = DiscordBotBase.discordClient.getRoleByID("311285900556369920");
		
		ChannelUtils.loadChannels(guild);
		
		guildList = new IGuild[]{guild};
	}
	
	public static void registerCommands(){
		
		DiscordBotBase.discordBotBase.registerCommand(new KickCommand(), "kickCommand");
		DiscordBotBase.discordBotBase.registerCommand(new BanCommand(), "banCommand");
		DiscordBotBase.discordBotBase.registerCommand(new MuteCommand(), "muteCommand");
		DiscordBotBase.discordBotBase.registerCommand(new UnMuteCommand(), "unMuteCommand");
		
		DiscordBotBase.discordBotBase.registerCommand(new ChannelBan(), "channelBan");
		DiscordBotBase.discordBotBase.registerCommand(new ChannelKick(), "channelKick");
		DiscordBotBase.discordBotBase.registerCommand(new ChannelUnban(), "channelUnBan");
		
		DiscordBotBase.discordBotBase.registerCommand(new WarnCommand(), "warnCommand");
		DiscordBotBase.discordBotBase.registerCommand(new WarnsCommand(), "warnsCommand");
		DiscordBotBase.discordBotBase.registerCommand(new RemoveWarnCommand(), "removeWarnCommand");
		DiscordBotBase.discordBotBase.registerCommand(new EditWarnCommand(), "editWarnCommand");

		
		DiscordBotBase.discordBotBase.registerCommand(new ReminderCommand(), "reminderCommand");

		DiscordBotBase.discordBotBase.registerCommand(new SilentModeCommand(), "silentModeCommand");
		
//		DiscordBotBase.discordBotBase.registerCommand(new UserActivityCommand(), "userActivityCommand");
//		DiscordBotBase.discordBotBase.registerCommand(new StaffActivityCommand(), "staffActivityCommand");
		
//		DiscordBotBase.discordBotBase.registerCommand(new GivePointsCommand(), "givePointsCommand");
//		DiscordBotBase.discordBotBase.registerCommand(new RemovePointsCommand(), "removePointsCommand");
		
		DiscordBotBase.discordBotBase.registerCommand(new StatusCommand(), "statusCommand");
	}
	
	public static void registerEvents(){
		DiscordBotBase.discordClient.getDispatcher().registerListener(new UserJoin());
//		DiscordBotBase.discordClient.getDispatcher().registerListener(new ActivityMessageListener());
		DiscordBotBase.discordClient.getDispatcher().registerListener(new RoleChangedListener());
	}
}
