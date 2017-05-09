package Discord.Commands.ModCommands;

import Discord.Core.ActivitySystem.AbstractActivity;
import Discord.Core.ConfigFile;
import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.CommandFiles.DiscordSubCommand;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.ResponseException.AbstractResponse;
import DiscordBotCode.Main.ResponseException.TextResponse;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends ModeratorCommand
{
	public static Timer timer = new Timer();
	private static String mutedRoleName = "gagged";
	public static String requireRole = "";
	private static File file;
	public static CopyOnWriteArrayList<TimerTask> tasks = new CopyOnWriteArrayList<>();
	
	public MuteCommand()
	{
		requireRole = ConfigFile.getPermission("mute_role_required", "moderator");
		file = FileGetter.getFile(DiscordBotBase.FilePath + "/currentMutes" + ".log");
		
		subCommands.add(new reload(this));
		reloadFile();
	}
	
	public static void reloadFile(){
		try {
			Files.lines(file.toPath()).forEach(( e ) -> {
				String[] text = e.split("=");
				
				if(text.length < 3){
					return;
				}
				
				IUser user = DiscordBotBase.discordClient.getUserByID(text[0]);
				
				if(user == null){
					FileUtil.removeLineFromFile(file, e);
					return;
				}
				
				long time = Long.parseLong(text[1]) -  System.currentTimeMillis() ;
				IGuild guild = DiscordBotBase.discordClient.getGuildByID(text[2]);
				
				if(time > 0){
					TimerTask task = new TimerTask()
					{
						@Override
						public void run()
						{
							removeMute(user, guild);
							reportUnmute(guild, user);
						}
					};
					
					System.out.println("Un-mute for user \"" + user.getDisplayName(guild) + "\" queued in " + TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS) + " mins");
					
					timer.schedule(task, time);
					tasks.add(task);
				}else{
					removeMute(user, guild);
					reportUnmute(guild, user);
				}
				
			});
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requireRole};
	}
	
	public static void reportUnmute(IGuild guild, IUser user){
		LoggingUtil.log(null, guild, user, EnumLogType.UNMUTE, null);
	}
	
	public static void removeMute(IUser user, IGuild guild){
		List<IRole> rols = guild.getRolesByName(mutedRoleName);
		final IRole role = rols != null && rols.size() > 0 ? guild.getRolesByName(mutedRoleName).get(0) : null;
		
		if (role == null) {
			System.err.println("ERROR: Invalid mute role! Role name is case sensitive! Role name has to be: \"" + mutedRoleName + "\"");
			return;
		}
		
		RequestBuffer.request(() -> {
			if (user != null && guild != null && role != null) {
				try {
					user.removeRole(role);
					FileUtil.removeLineFromFile(file, user.getID());
				} catch (DiscordException | MissingPermissionsException e) {
					DiscordBotBase.discordBotBase.handleException(e);
				}
			}
		});
	}
	
	public static void addMute(IUser user, IGuild guild, long delay){
		List<IRole> rols = guild.getRolesByName(mutedRoleName);
		final IRole role = rols != null && rols.size() > 0 ? guild.getRolesByName(mutedRoleName).get(0) : null;
		
		if (role == null) {
			System.err.println("ERROR: Invalid mute role! Role name is case sensitive! Role name has to be: \"" + mutedRoleName + "\"");
			return;
		}
		
		RequestBuffer.request(() -> {
			try {
				user.addRole(role);
				FileUtil.addLineToFile(file, user.getID() + "=" + (System.currentTimeMillis() + delay) + "=" + guild.getID());
			} catch (DiscordException | MissingPermissionsException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
		});
	}
	
	@Override
	public String commandPrefix()
	{
		return "mute";
	}
	
	@Override
	public String getUsage(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "mute <user> <time 1d, 2h, 3m, 4s> <reason>";
	}
	
	@Override
	public boolean isCommandPermissionServerwide()
	{
		return true;
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
		
		if(text.startsWith(" ")){
			text = text.substring(1);
		}
		
		delay += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
		delay += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
		delay += TimeUnit.MILLISECONDS.convert(mins, TimeUnit.MINUTES);
		delay += TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
		
		if(delay <= 0){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Cannot mute without a duration!");
			return;
		}
		
		List<IRole> rols = message.getGuild().getRolesByName(mutedRoleName);
		final IRole role = rols != null && rols.size() > 0 ? message.getGuild().getRolesByName(mutedRoleName).get(0) : null;
		
		if (role == null) {
			System.err.println("ERROR: Invalid mute role! Role name is case sensitive! Role name has to be: \"" + mutedRoleName + "\"");
		} else {
			
			for (IUser user : message.getMentions()) {
				AbstractActivity.staffCommandUsed(message.getAuthor(), user, this);
				LoggingUtil.log(message.getAuthor(), message.getGuild(), user, EnumLogType.MUTE, time.toString() + (!text.isEmpty() ? ", with reason \"" + text + "\"" : ""));
				addMute(user, message.getGuild(), delay);
			}
			
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					for (IUser user : message.getMentions()) {
						removeMute(user, message.getGuild());
						reportUnmute(message.getGuild(), user);
					}
				}
			};
			
			timer.schedule(task, delay);
			tasks.add(task);
		}
		
		super.commandExecuted(message, args);
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public AbstractResponse getExecuteResponse( IMessage message, String[] args, boolean hasPermission )
	{
		if (!PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requireRole, true)) {
			return new TextResponse(this, message.getAuthor().mention() + " You do not have the required role for this command!");
		}
		return super.getExecuteResponse(message, args, hasPermission);
	}
}

class reload extends DiscordSubCommand{
	public reload( DiscordChatCommand baseCommand )
	{
		super(baseCommand);
	}
	
	@Override
	public String commandPrefix()
	{
		return "reload";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "reload";
	}
	
	@Override
	public String getDescription(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Reloads the bot timer for mutes therefor reloading all registered mutes from file";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		for(TimerTask task : MuteCommand.tasks){
			task.cancel();
		}
		
		MuteCommand.timer.cancel();
		MuteCommand.timer.purge();
		
		MuteCommand.timer = new Timer();
		
		MuteCommand.reloadFile();
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{"Programmer"};
	}
}
