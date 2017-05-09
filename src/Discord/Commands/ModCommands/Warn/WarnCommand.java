package Discord.Commands.ModCommands.Warn;

import Discord.Commands.ModCommands.ModeratorCommand;
import Discord.Core.ActivitySystem.AbstractActivity;
import Discord.Core.ConfigFile;
import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.ResponseException.AbstractResponse;
import DiscordBotCode.Main.ResponseException.TextResponse;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WarnCommand extends ModeratorCommand
{
	public static String requiredRole = "";
	private static File warnsFile;
	public static DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy [HH:mm]");
	
	public WarnCommand()
	{
		requiredRole = ConfigFile.getPermission("warn_permission", "moderator");
		if (warnsFile == null) {
			warnsFile = FileGetter.getFile(DiscordBotBase.FilePath + "/warns.txt");
		}
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public String commandPrefix()
	{
		return "warn";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "warn <user> [reason]";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(message.getMentions().size() <= 0){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This command requires at least one user mention to work!");
			return;
		}
		String reason = String.join(" ", args);
		
		for (IUser user : message.getMentions()) {
			reason = reason.replace(user.mention(true), "");
			reason = reason.replace(user.mention(false), "");
		}
		
		if (reason.startsWith(" ")) {
			reason = reason.replaceFirst(" ", "");
		}
		for (IUser user : message.getMentions()) {
			LoggingUtil.log(message.getAuthor(), message.getGuild(),user, EnumLogType.WARN, reason);
			AbstractActivity.staffCommandUsed(message.getAuthor(), user, this);
			WarnSystem.createAndSaveWarn(message.getGuild(), message.getChannel(), user, message.getAuthor(), user.getID(), reason, new Date(), WarnSystem.genId(message.getGuild()));
		}
		
		WarnSystem.reloadWarnings();
		super.commandExecuted(message, args);
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return PermissionUtils.hasRole(message.getAuthor(), message.getGuild(), requiredRole, true);
	}
	
	@Override
	public AbstractResponse getExecuteResponse( IMessage message, String[] args, boolean hasPermission )
	{
		if (!PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requiredRole, true)) {
			return new TextResponse(this, message.getAuthor().mention() + " You do not have the required role for this command!");
		}
		return message.getMentions().size() <= 0 ? new TextResponse(this, "Warning needs at least one user mention!") : null;
	}
	
}
