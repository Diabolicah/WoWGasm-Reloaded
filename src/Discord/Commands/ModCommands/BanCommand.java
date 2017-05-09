package Discord.Commands.ModCommands;

import Discord.Commands.ModCommands.Warn.WarnSystem;
import Discord.Core.ActivitySystem.AbstractActivity;
import Discord.Core.ConfigFile;
import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.ResponseException.AbstractResponse;
import DiscordBotCode.Main.ResponseException.TextResponse;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class BanCommand extends ModeratorCommand
{
	private static String requiredRole = "";
	
	public BanCommand()
	{
		requiredRole = ConfigFile.getPermission("ban_permission", "admin");
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public String commandPrefix()
	{
		return "ban";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage )
	{
		return "ban <user> [reason]";
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
			reason = reason.replace(user.mention(), "");
			reason = reason.replace(user.mention(false), "");
		}
		
		if (reason.startsWith(" ")) {
			reason = reason.replaceFirst(" ", "");
		}
		
		for (IUser user : message.getMentions()) {
			LoggingUtil.log(message.getAuthor(), message.getGuild(),user, EnumLogType.BAN, reason);
			AbstractActivity.staffCommandUsed(message.getAuthor(), user, this);
			
			
			RequestBuffer.request(() -> {
				try {
					message.getGuild().banUser(user, 7);
					WarnSystem.clearUserWarns(message.getGuild(), user);
				} catch (DiscordException | MissingPermissionsException e) {
					DiscordBotBase.discordBotBase.handleException(e);
				}
			});
		}
		
		super.commandExecuted(message, args);
	}
	
	@Override
	public AbstractResponse getExecuteResponse( IMessage message, String[] args, boolean hasPermission )
	{
		if (!PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requiredRole, true)) {
			return new TextResponse(this, message.getAuthor().mention() + " You do not have the required role for this command!");
		}
		return message.getMentions().size() <= 0 ? new TextResponse(this, "Banning needs at least one user mention!") : super.getExecuteResponse(message, args, hasPermission);
	}

	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
}
