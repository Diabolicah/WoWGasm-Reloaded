package Discord.Commands.ModCommands.ChannelCommands;

import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ChannelKick extends DiscordChatCommand
{
	private static Timer timer = new Timer();
	private static File file;
	
	public ChannelKick()
	{
		file = FileGetter.getFile(DiscordBotBase.FilePath + "/currentChannelKicks" + ".log");
		
		try {
			Files.lines(file.toPath()).forEach(( e ) -> {
				String[] text = e.split("=");
				
				if(text.length < 3){
					return;
				}
				
				IUser user = DiscordBotBase.discordClient.getUserByID(text[0]);
				IChannel channel = DiscordBotBase.discordClient.getChannelByID(text[1]);
				long time = Long.parseLong(text[2]) -  System.currentTimeMillis() ;
				
				if(time > 0){
					timer.schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							removeMute(user, channel);
						}
					}, time);
				}else{
					removeMute(user, channel);
				}
				
			});
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
	}
	public static void removeMute(IUser user, IChannel channel){
		if(user == null || channel == null){
			return; //TODO If it loads a invalid user (user left) remove the line before it is sent to be removeMute
		}
		
		RequestBuffer.request(() -> {
			FileUtil.removeLineFromFile(file, user.getID() + "=" + channel.getID());
			try {
				EnumSet<Permissions> set = channel.getModifiedPermissions(user);
				set.remove(Permissions.READ_MESSAGES);
				set.remove(Permissions.READ_MESSAGE_HISTORY);
				set.remove(Permissions.SEND_MESSAGES);
				
				for(IRole role : user.getRolesForGuild(channel.getGuild())){
					for(Permissions s : channel.getModifiedPermissions(role)){
						set.remove(s);
					}
				}
				
				if(set.size() <= 0) {
					channel.removePermissionsOverride(user);
				}else{
					channel.overrideUserPermissions(user, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY, Permissions.SEND_MESSAGES), null);
				}
			} catch (MissingPermissionsException | DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
			
			LoggingUtil.log(null, channel.getGuild(), user, EnumLogType.OTHER, LoggingUtil.getName(user, channel.getGuild()) + " is no longer kicked from " + channel.mention(), new Color(188, 62, 68));
		});
	}
	
	public static void addMute(IUser user, IChannel channel, long delay){
		RequestBuffer.request(() -> {
			FileUtil.addLineToFile(file, user.getID() + "=" + channel.getID() + "=" + (System.currentTimeMillis() + delay));
			try {
				channel.overrideUserPermissions(user, null, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY, Permissions.SEND_MESSAGES));
			} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
		});
	}
	
	@Override
	public String getDescription(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Removes the users access to the current channel for the specific amount of time";
	}
	
	@Override
	public String commandPrefix()
	{
		return "channelkick";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "channelkick <user> <time 1d, 2h, 3m, 4s> <reason>";
	}
	
	@Override
	protected Permissions[] getRequiredPermissions()
	{
		return new Permissions[]{Permissions.MANAGE_MESSAGES};
	}
	
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(message.getMentions().size() <= 0){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This command requires at least one user mention to work!");
			return;
		}
		
		long days = 0, hours = 0, mins = 0, seconds = 0;
		long delay = 0;
		
		StringJoiner time = new StringJoiner(" ");
		
		String text = String.join(" ", args);
		
		for(IUser user : message.getMentions()){
			text = text.replace(user.mention(false), "");
			text = text.replace(user.mention(true), "");
		}
		
		for (String t : args) {
			if (t.endsWith("d")) {
				t = t.replace("d", "");
				if(Utils.isInteger(t)) {
					days = Integer.parseInt(t);
					text = text.replace(t + "d", "");
					time.add(days + " days");
				}
				
			} else if (t.endsWith("h")) {
				t = t.replace("h", "");
				if(Utils.isInteger(t)) {
					hours = Integer.parseInt(t);
					text = text.replace(t + "h", "");
					time.add(hours + " hours");
				}
				
			} else if (t.endsWith("m")) {
				t = t.replace("m", "");
				if(Utils.isInteger(t)) {
					mins = Integer.parseInt(t);
					text = text.replace(t + "m", "");
					time.add(mins + " minutes");
				}
				
			} else if (t.endsWith("s")) {
				t = t.replace("s", "");
				if(Utils.isInteger(t)) {
					seconds = Integer.parseInt(t);
					text = text.replace(t + "s", "");
					time.add(seconds + " seconds");
				}
			}
		}
		
		while(text.startsWith(" ")){
			text = text.replaceFirst(" ", "");
		}
		
		delay += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
		delay += TimeUnit.MILLISECONDS.convert(mins, TimeUnit.MINUTES);
		delay += TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
		
		if(delay <= 0){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Cannot mute without a duration!");
			return;
		}
		
		for (IUser user : message.getMentions()) {
			LoggingUtil.log(message.getAuthor(), message.getGuild(), user, EnumLogType.CHANNEL_KICK, message.getChannel().mention() + " for " + time.toString() + (!text.isEmpty() ? ", with reason \"" + text + "\"" : ""));
			addMute(user, message.getChannel(), delay);
		}
		
		
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
			for (IUser user : message.getMentions()) {
				removeMute(user, message.getChannel());
			}
			}
		}, delay);
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public boolean canCommandBePrivateChat()
	{
		return false;
	}
}
