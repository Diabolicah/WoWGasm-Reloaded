package Discord.Core.ActivitySystem.User;

import Discord.Core.ActivitySystem.ActivitySystem;
import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.CustomEntry;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings( "StringConcatenationInsideStringBufferAppend" )
public class UserActivityCommand extends DiscordChatCommand
{
	public static String requiredRole;
	
	public UserActivityCommand(){
		requiredRole = ConfigFile.getPermission("activity_command_special", "daddy god");
		
		subCommands.add(new reset(this));
	}
	
	@Override
	public String commandPrefix()
	{
		return "activity";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		if(PermissionUtils.hasRole(callerMessage.getAuthor(), MainDiscordBot.guild, requiredRole, true)) return "activity [mentions/page num/reset]";
		return "activity";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		boolean hasMentions = message.getMentions().size() > 0;
		boolean permission = PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requiredRole, true);
		
		int page = 0;
		
		if(args.length > 0 && Utils.isInteger(args[0])){
			page = Integer.parseInt(args[0]) - 1;
		}
		
		int amount = 0;
		for(Map.Entry<String, Integer> ent : UserActivity.object.userPoints.entrySet()){
			IUser user = DiscordBotBase.discordClient.getUserByID(ent.getKey());
			
			if(	UserActivity.object.validUser(user)) {
				amount += 1;
			}
		}
		
		int size = amount;
		int pages = ((size / UserActivity.viewSize));
		
		
		if(size > (pages * UserActivity.viewSize) && size != (pages * UserActivity.viewSize)){
			pages += 1;
		}
		
		if(page > pages || (page + 1) > pages){
			page = pages - 1;
		}
		
		StringBuilder builder = new StringBuilder();
		
		String start = "```perl\n";
		builder.append(start);
		
		int num = 1 + (page * UserActivity.viewSize);
		
		int average = -1;
		int min = -1, max = -1;
		int numA = 0;
		
		for(Map.Entry<String, Integer> ent : UserActivity.object.userPoints.entrySet()){
			if(min == -1 || ent.getValue() < min){
				min = ent.getValue();
			}
			
			if(ent.getValue() > max){
				max = ent.getValue();
			}
			if(ent.getValue() > 0) {
				average += ent.getValue();
				numA += 1;
			}
		}
		if(average > 0 && numA > 0) {
			average /= numA;
		}
		
		if(permission){
			if(hasMentions){
				ArrayList<IUser> users = new ArrayList<>();
				users.addAll(message.getMentions());
				
				for(IRole role : message.getRoleMentions()){
					users.addAll(message.getGuild().getUsersByRole(role));
				}
				
				for(IUser user : users){
					if(UserActivity.object.userPoints.containsKey(user.getID())) {
						String text = getText(user, num, true);
						
						if(builder.toString().length() + text.length() > 1800){
							break;
						}
						
						builder.append(text);
						num += 1;
					}
				}
			}else{
				for(CustomEntry<IUser, Integer> ent : UserActivity.object.getUsers(page)){
					String text = getText(ent.getKey(), num, false);
					
					builder.append(text);
					num += 1;
				}
			}
		}else{
			if(UserActivity.object.userPoints.containsKey(message.getAuthor().getID())) {
				String text = getText(message.getAuthor(), num, true);
				
				builder.append(text);
				num += 1;
			}
		}
		
		if(!hasMentions && permission) {
			builder.append("Min: " + min + " | Max: " + max + " | Avg: " + average + " | Amount: " + amount + "\n");
			
			Date date = new Date(Long.parseLong(ActivitySystem.prop.getProperty("resetTime")));
			Date curDate = new Date();
			
			long time = curDate.getTime() - date.getTime();
			long daysSince = TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
			long days = (7 - daysSince);
			
			builder.append("Reset in: " + days + " " + (days > 1 ? "days" : "day") + "\n\n");
			builder.append("Page: [ " + (page + 1) + " / " + (pages) + " ]\n");
			
		}
		
		if(num > 1 && builder.toString().length() > 10){
			ChatUtils.sendMessage(message.getChannel(), builder.toString() + "```");
		}else{
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no activity to show!");
		}
	}
	
	public static DateFormat formatter = new SimpleDateFormat("EEE MMM d, yyyy", Locale.ENGLISH);
	
	
	public static String getText( IUser user, int num, boolean full){
		StringBuilder builder = new StringBuilder();
		IRole role = getHighestRole(user);
		
		builder.append("[" + num + "]" + "  #" + user.getDisplayName(MainDiscordBot.guild) + " (" + user.getName() + "#" + user.getDiscriminator() + ")" + "\n");
		builder.append("\t  > Points: " + UserActivity.object.getPoints(user) + "\n");
		if(role != null) builder.append("\t  > Role: #" + role.getName() + "\n");
		
		Date date = UserActivity.getLastRoleChange(user);
		Date joinDate = null;
		try {
			joinDate = Date.from(MainDiscordBot.guild.getJoinTimeForUser(user).atZone(ZoneId.systemDefault()).toInstant());
		} catch (DiscordException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		if(date != null){
			builder.append("\t  > Last promotion: " + formatter.format(date) + "\n");
		}
		
		if(joinDate != null){
			builder.append("\t  > Join date: " + formatter.format(joinDate) + "\n");
		}
		
		if(full){
			if(UserActivity.object.getLog(user).size() > 0) {
				builder.append("\t  > Log: \n");
				for (CustomEntry<String, Integer> ent : UserActivity.object.getLog(user)) {
					String text = ent.getKey() + "\"";
					int length = 0;
					
					for (CustomEntry<String, Integer> ent1 : UserActivity.object.getLog(user)) {
						if(ent1.getKey().length() > length){
							length = ent1.getKey().length();
						}
					}
					
					while(text.length() < (length + 5)){
						text += " ";
					}
					
					builder.append("\t\t > Reason: \"" + text + "|  " + (ent.getValue() > 0 ? "+" : "") + ent.getValue() + " Points \n");
				}
			}
		}
		
		builder.append("\n");
		
		return builder.toString();
	}
	
	
	public static IRole getHighestRole( IUser user){
		if(!DiscordBotBase.debug) {
			if (UserActivity.highestRole == null || UserActivity.lowestRole == null) {
				return null;
			}
		}
		
		IRole role = null;
		int num = -1;
		
		for(IRole role1 : user.getRolesForGuild(MainDiscordBot.guild)){
			if(!DiscordBotBase.debug) {
				if (role1.getPosition() > UserActivity.highestRole.getPosition() || role1.getPosition() <= UserActivity.lowestRole.getPosition()) {
					continue;
				}
			}
			
			if(role1.getPosition() > num){
				num = role1.getPosition();
				role = role1;
			}
		}
		
		return role;
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}

class reset extends DiscordSubCommand{
	public reset( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "reset";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		UserActivity.object.resetPoints();
		RequestBuffer.request(() -> {
			try {
				ChatUtils.sendMessage(message.getAuthor().getOrCreatePMChannel(), message.getAuthor().mention() + " Activity has been reset!");
			} catch (DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
		});
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{UserActivityCommand.requiredRole};
	}
	
	@Override
	public IGuild[] roleChecks()
	{
		return MainDiscordBot.guildList;
	}
}
