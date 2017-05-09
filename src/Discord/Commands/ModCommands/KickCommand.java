package Discord.Commands.ModCommands;

import Discord.Core.ActivitySystem.AbstractActivity;
import Discord.Core.ConfigFile;
import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import Discord.Core.MainDiscordBot;
import Discord.Twitter.TwitterHandle;
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

public class KickCommand extends ModeratorCommand
{
	private static String requiredRole = "";
	
	public KickCommand()
	{
		requiredRole = ConfigFile.getPermission("kick_permission", "moderator");
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public String commandPrefix()
	{
		return "kick";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "kick <user> [reason]";
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
			LoggingUtil.log(message.getAuthor(), message.getGuild(), user, EnumLogType.KICK, reason);
			AbstractActivity.staffCommandUsed(message.getAuthor(), user, this);
			
			RequestBuffer.request(() -> {
				try {
					message.getGuild().kickUser(user);
					TwitterHandle.removeUser(user);
				} catch (DiscordException | MissingPermissionsException e) {
					DiscordBotBase.discordBotBase.handleException(e);
				}
			});
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
		if (!PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requiredRole, true)) {
			return new TextResponse(this, message.getAuthor().mention() + " You do not have the required role for this command!");
		}
		return message.getMentions().size() <= 0 ? new TextResponse(this, "Kicking needs at least one user mention!") : null;
	}
	
}
