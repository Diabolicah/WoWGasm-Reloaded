package Discord.Commands;

import Discord.Core.ConfigFile;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ReminderCommand extends DiscordChatCommand
{
	private static Timer timer = new Timer();
	private static String requiredRole = "";
	private static File file;
	
	private static DateFormat formatterTime = new SimpleDateFormat("EEE MMM d, yyyy, h:mm a zzz", Locale.ENGLISH);
	
	public ReminderCommand()
	{
		requiredRole = ConfigFile.getPermission("reminder_permission", "none");
		file = FileGetter.getFile(DiscordBotBase.FilePath + "/events");

		try {
			Files.lines(file.toPath()).forEach(( e ) -> {
				String[] tt = e.split("-");
				
				Date date = new Date(Long.parseLong(tt[0]));
				String eventText = tt[ 1 ];
				String mention = tt[ 2 ];
				IChannel channel = DiscordBotBase.discordClient.getChannelByID(tt[ 3 ]);
				
				if(channel != null) {
					setReminder(date.getTime() - System.currentTimeMillis(), channel, mention, eventText, file, date.getTime());
				}
			});
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}

	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	private static void setReminder( long delay, IChannel channel, String messageMention, String messageEvent, File file, Long dateF )
	{
		if (delay <= 0) {
			FileUtil.removeLineFromFile(file, Long.toString(dateF) + "-" + messageEvent);
			return;
		}

		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				ChatUtils.sendMessage(channel, messageMention + " The event \"" + messageEvent + "\" has now begun!");
				FileUtil.removeLineFromFile(file, Long.toString(dateF) + "-" + messageEvent);
			}
		}, delay);


		if (TimeUnit.MINUTES.convert(delay, TimeUnit.MILLISECONDS) > 5) {
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					ChatUtils.sendMessage(channel, messageMention + " The event \"" + messageEvent + "\" will begin in 5 min!");
				}
			}, delay - (TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)));
		}

		if (TimeUnit.MINUTES.convert(delay, TimeUnit.MILLISECONDS) > 15) {
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					ChatUtils.sendMessage(channel, messageMention + " The event \"" + messageEvent + "\" will begin in 15 min.");
				}
			}, delay - (TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES)));
		}

		if (TimeUnit.MINUTES.convert(delay, TimeUnit.MILLISECONDS) > 30) {
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					ChatUtils.sendMessage(channel, messageMention + " The event \"" + messageEvent + "\" will begin in 30 min.");
				}
			}, delay - (TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES)));
		}

		if (TimeUnit.HOURS.convert(delay, TimeUnit.MILLISECONDS) > 1) {
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					ChatUtils.sendMessage(channel, messageMention + " The event \"" + messageEvent + "\" will begin in 1 hour.");
				}
			}, delay - (TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)));
		}
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Sets a reminder for a certain event, will give a reminder of the event a certain intervals";
	}
	
	@Override
	public String commandPrefix()
	{
		return "setreminder";
	}
	
	@Override
	public String getUsage(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "setreminder <(reminder)> <time (1d, 2h, 3m, 4s)> [mentions]";
	}
	
	@Override
	protected Permissions[] getRequiredPermissions()
	{
		return new Permissions[]{ Permissions.MANAGE_MESSAGES };
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		String text = String.join(" ", args);
		long days = 0, hours = 0, mins = 0, seconds = 0;
		long delay = 1;

		String event = "";


		String mention = "";
		for (IRole t : message.getRoleMentions()) {
			mention += t.mention() + " ";
		}

		if (!message.mentionsEveryone()) {
			for (IUser t : message.getMentions()) {
				mention += t.mention() + " ";
			}
		}

		if (message.mentionsEveryone()) {
			mention += "@everyone";
		}

		if (text.contains("(")) {
			event = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
			text = text.replace(event, "");
		}

		for (String t : text.split(" ")) {
			if (t.endsWith("d")) {
				t = t.replace("d", "");
				if(!t.isEmpty() && Utils.isInteger(t)) {
					days = Integer.parseInt(t);
					text = text.replace(days + "d", "");
				}

			} else if (t.endsWith("h")) {
				t = t.replace("h", "");
				if( !t.isEmpty() && Utils.isInteger(t)) {
					hours = Integer.parseInt(t);
					text = text.replace(hours + "h", "");
				}

			} else if (t.endsWith("m")) {
				t = t.replace("m", "");
				if(!t.isEmpty() && Utils.isInteger(t)) {
					mins = Integer.parseInt(t);
					text = text.replace(mins + "m", "");
				}

			} else if (t.endsWith("s")) {
				t = t.replace("s", "");
				if(!t.isEmpty() && Utils.isInteger(t)) {
					seconds = Integer.parseInt(t);
					text = text.replace(seconds + "s", "");
				}

			}
		}

		delay += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
		delay += TimeUnit.MILLISECONDS.convert(mins, TimeUnit.MINUTES);
		delay += TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);

		Date date = new Date(System.currentTimeMillis() + delay);
		date.setSeconds(0);

		FileUtil.addLineToFile(file, date.getTime() + "-" + event + "-" + mention + "-" + message.getChannel().getID());
		setReminder(delay, message.getChannel(), mention, event, file, date.getTime());
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " A reminder for \"" + event + "\" has now been added for " + formatterTime.format(date));
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}
