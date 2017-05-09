package Discord.Commands.StatusSystem;

import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatusCommand extends DiscordChatCommand
{
	public static String requiredRole;
	
	public StatusCommand()
	{
		requiredRole = ConfigFile.getPermission("status_command_permission", "moderator");
		
		StatusSystemCore.init();
		
		subCommands.add(new away(this));
		subCommands.add(new returned(this));
	}
	
	@Override
	public String commandPrefix()
	{
		return "status";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "status <user/away/returned> [return date (1w, 1d, 2h, 3m)] [reason]";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "Allows setting a away status by using status away, the command can be given a return date in the format of 1d = 1 day, 1h = 1 hour, 1m = 1 min which is time until return, it can also be given a reason for the away status, away status will be auto removed if return date was given otherwise the manual returned command can be used.";
	}
	
	public static DateFormat formatter = new SimpleDateFormat("EEE MMM d, yyyy", Locale.ENGLISH);
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		ArrayList<IUser> list = new ArrayList();
		
		list.addAll(message.getMentions());
		
		if(message.getMentions().size() <= 0 && message.getRoleMentions().size() <= 0){
			list.add(message.getAuthor());
		}
		
		for(IRole role : message.getRoleMentions()){
			for(IUser user : message.getGuild().getUsersByRole(role)){
				if(StatusSystemCore.getStatus(user) != null){
					list.add(user);
				}
			}
		}
		
		for(IUser user : list) {
			Status status = StatusSystemCore.getStatus(user);
			
			EmbedBuilder builder = new EmbedBuilder();
			
			builder.withTitle(user.getDisplayName(message.getGuild()) + "'s Status");
			builder.withThumbnail(user.getAvatarURL());
			
			builder.withColor(status == null ? Color.green : Color.red);
			
			if (status != null) {
				builder.withDescription(user.getDisplayName(message.getGuild()) + " is currently away.");
				
				if (status.getReason() != null) {
					builder.appendField("Reason", status.getReason(), false);
				}
				
				if (status.getReturnDate() != null) {
					Date returnDate = status.getReturnDate();
					String text = formatter.format(status.getReturnDate());
					final long millis = returnDate.getTime() - System.currentTimeMillis();
					
					long years = (TimeUnit.MILLISECONDS.toDays(millis) / 365);
					long months = (TimeUnit.MILLISECONDS.toDays(millis) / 30) - (TimeUnit.MILLISECONDS.toDays(millis) * 365);
					long weeks = (TimeUnit.MILLISECONDS.toDays(millis) / 7) - (TimeUnit.MILLISECONDS.toDays(millis) * 30);
					long days = TimeUnit.MILLISECONDS.toDays(millis) - (TimeUnit.MILLISECONDS.toDays(millis) * 7);
					long hours = TimeUnit.MILLISECONDS.toHours(millis) - (TimeUnit.MILLISECONDS.toDays(millis) * 24);
					long min = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
					
					StringJoiner joiner = new StringJoiner(", ");
					
					if (years > 0) {
						joiner.add(years + "years");
					}
					
					if (months > 0) {
						joiner.add(months + "months");
					}
					
					if (weeks > 0) {
						joiner.add(weeks + "weeks");
					}
					
					if (days > 0) {
						joiner.add(days + "days");
					}
					
					if (hours > 0) {
						joiner.add(hours + "hours");
					}
					
					if (min > 0) {
						joiner.add(min + "minutes");
					}
					
					if (joiner.length() > 0) {
						text += " (" + joiner.toString() + ")";
					}
					
					builder.appendField("Return date", text, false);
				}
			} else {
				builder.withDescription(user.getDisplayName(message.getGuild()) + " is currently available.");
			}
			
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention(), builder.build());
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public IGuild[] roleChecks()
	{
		return MainDiscordBot.guildList;
	}
}

class away extends DiscordSubCommand
{
	public away( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "away";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		
		long weeks = 0, days = 0, hours = 0, mins = 0;
		long delay = 0;
		
		String text = String.join(" ", args);
		
		for(IUser user : message.getMentions()){
			text = text.replace(user.mention(false), "");
			text = text.replace(user.mention(true), "");
		}
		
		for (String t : args) {
			if (t.endsWith("w")) {
				t = t.replace("w", "");
				if(Utils.isInteger(t)) {
					weeks = Integer.parseInt(t);
					text = text.replace(t + "w", "");
				}
				
			}else if (t.endsWith("d")) {
				t = t.replace("d", "");
				if(Utils.isInteger(t)) {
					days = Integer.parseInt(t);
					text = text.replace(t + "d", "");
				}
				
			} else if (t.endsWith("h")) {
				t = t.replace("h", "");
				if(Utils.isInteger(t)) {
					hours = Integer.parseInt(t);
					text = text.replace(t + "h", "");
				}
				
			} else if (t.endsWith("m")) {
				t = t.replace("m", "");
				if(Utils.isInteger(t)) {
					mins = Integer.parseInt(t);
					text = text.replace(t + "m", "");
				}
				
			}
		}
		
		if(text.startsWith(" ")){
			text = text.substring(1);
		}
		
		delay += TimeUnit.MILLISECONDS.convert(weeks * 7, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
		delay += TimeUnit.MILLISECONDS.convert(mins, TimeUnit.MINUTES);
		
		Date curDate = new Date();
		Date returnDate = null;
		
		if(delay > 0){
			returnDate = new Date(curDate.getTime() + delay);
		}
		
		Status status = new Status((text.isEmpty() || text.length() <= 1 ? null : text), returnDate);
		StatusSystemCore.setStatus(message.getAuthor(), status);
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Your status has now been updated as away!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}

class returned extends DiscordSubCommand{
	public returned( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "returned";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		StatusSystemCore.removeStatus(message.getAuthor());
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Your status has now been updated and you are no longer away!");
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}