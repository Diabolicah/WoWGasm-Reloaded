package Discord.Commands.ModCommands.Warn;

import Discord.Commands.ModCommands.ModeratorCommand;
import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.Utils;
import org.apache.commons.lang.WordUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WarnsCommand extends ModeratorCommand
{
	private static String requiredRole = "";

	public static DateFormat formatter = new SimpleDateFormat("EEE MMM d, yyyy, h:mm a zzz", Locale.ENGLISH);
	
	public WarnsCommand()
	{
		requiredRole = ConfigFile.getPermission("view_warns_permission", "moderator");
		
		subCommands.add(new DiscordSubCommand(this) {
			@Override
			public String commandPrefix()
			{
				return "reload";
			}
			
			@Override
			public void commandExecuted( IMessage message, String[] args )
			{
				System.out.println("Updating warn list...");
				WarnSystem.reloadWarnings();
				System.out.println("Loaded " + WarnSystem.warnings.size() + " warnings!");
			}
			
			@Override
			public boolean canExecute( IMessage message, String[] args )
			{
				return true;
			}
			
			@Override
			public IGuild[] roleChecks()
			{
				return MainDiscordBot.guildList;
			}
			
			@Override
			public String[] getRequiredRoles()
			{
				return new String[]{"programmer"};
			}
		});
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public String commandPrefix()
	{
		return "warns";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "warns";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		try {
			ArrayList<StringBuilder> builderArrayList = new ArrayList<>();
			builderArrayList.add(new StringBuilder());
			
			String start = "```perl\n";
			builderArrayList.get(0).append(start);
			
			int num = 0;
			int length = 0;
			int num1 = 1;
			
			String text1 = String.join(" ", args);
			String[] ids = text1.split(" ");
			
			ArrayList<String> users = WarnSystem.getListOfUsers(message.getGuild());
			int max = 1600;
			
			if(message.getMentions().size() > 0){
				users = new ArrayList<>();
				
				for(IUser user : message.getMentions()){
					users.add(user.getID());
				}
			}
			
			boolean hasId = false;
			
			if(ids.length > 0){
				for(String id : ids){
					if(Utils.isInteger(id)) {
						hasId = true;
						break;
					}
				}
			}
			
			
			if(hasId){
				for(String id : ids){
					if(Utils.isInteger(id)) {
						WarnObject object = WarnSystem.getWarnById(message.getGuild(), Integer.parseInt(id));
						
						String title = "[" + num1 + "]  #" + (object.user != null ? object.user.getName() + "#" + object.user.getDiscriminator() : "User unavailable") + " (" + object.userId + ")";
						
						String text = "";
						
						length += (title.length());
						
						if (length >= max) {
							num += 1;
							
							builderArrayList.add(new StringBuilder());
							builderArrayList.get(num).append(start);
							length = 0;
						}
						
						builderArrayList.get(num).append("\n").append(title);
						num1 += 1;
						
						if (object.time != null) {
							String reason = object.reason;
							
							text += (!object.reason.isEmpty() ? "\n\t  > Reason: \"" + reason + "\"" : "");
							text += "\n\t  > Time: \"" + WordUtils.capitalize(formatter.format(object.time)) + "\"";
							
							if (object.admin != null || object.adminId != null) {
								IUser user = object.admin;
								
								if(user == null && object.adminId != null){
									user = DiscordBotBase.discordClient.getUserByID(object.adminId);
								}
								
								text += "\n\t  > Warned by #" + user.getName() + "";
							}
							
							if (object.channel != null) {
								text += "\n\t  > Warned in #" + object.channel.getName();
							}
							
							text += "\n\t  > Id: #" + object.id;
							text += "\n";
						}
						
						length += (text.length());
						
						if (length >= max) {
							num += 1;
							
							builderArrayList.add(new StringBuilder());
							builderArrayList.get(num).append(start);
							length = 0;
						}
						
						builderArrayList.get(num).append(text);
					}
				}
			}else {
				
				for (String id : users) {
					ArrayList<WarnObject> warnings = WarnSystem.getWarningsForUser(message.getGuild(), id);
					
					if (warnings.size() > 0) {
						IUser user = DiscordBotBase.discordClient.getUserByID(id);
						String title = "[" + num1 + "]  #" + (user != null ? user.getName() + "#" + user.getDiscriminator() : "User unavailable") + " (" + id + ")";
						String text = "";
						
						length += (title.length());
						
						if (length >= max) {
							num += 1;
							
							builderArrayList.add(new StringBuilder());
							builderArrayList.get(num).append(start);
							length = 0;
						}
						
						builderArrayList.get(num).append("\n").append(title);
						num1 += 1;
						
						for (WarnObject warn : warnings) {
							if (warn.time != null) {
								String reason = warn.reason;
								
								text += (!warn.reason.isEmpty() ? "\n\t  > Reason: \"" + reason + "\"" : "");
								text += "\n\t  > Time: \"" + WordUtils.capitalize(formatter.format(warn.time)) + "\"";
								
								if (warn.admin != null || warn.adminId != null) {
									IUser user1 = warn.admin;
									
									if(user1 == null && warn.adminId != null){
										user1 = DiscordBotBase.discordClient.getUserByID(warn.adminId);
									}
									
									text += "\n\t  > Warned by #" + user1.getName() + "";
								}
								
								if (warn.channel != null) {
									text += "\n\t  > Warned in #" + warn.channel.getName();
								}
								
								text += "\n\t  > Id: #" + warn.id;
								text += "\n";
								
							}
						}
						
						length += (text.length());
						
						if (length >= max) {
							num += 1;
							
							builderArrayList.add(new StringBuilder());
							builderArrayList.get(num).append(start);
							length = 0;
						}
						
						builderArrayList.get(num).append(text);
					}
				}
			}
			
			
			if(builderArrayList.size() > 1 || builderArrayList.get(0).length() > 10) {
				for (StringBuilder bd : builderArrayList) {
					RequestBuffer.request(() ->{
						try {
							ChatUtils.sendMessage(DiscordBotBase.debug ? message.getChannel() : message.getAuthor().getOrCreatePMChannel(), bd.toString() + "```");
						} catch (DiscordException e) {
							DiscordBotBase.discordBotBase.handleException(e);
						}
					});
				}
			}else{
				RequestBuffer.request(() ->{
					try {
						ChatUtils.sendMessage(DiscordBotBase.debug ? message.getChannel() : message.getAuthor().getOrCreatePMChannel(), message.getAuthor().mention() + " Found no warnings!");
					} catch (DiscordException e) {
						DiscordBotBase.discordBotBase.handleException(e);
					}
				});
			}
			
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		super.commandExecuted(message, args);
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requiredRole, true);
	}
	
}
