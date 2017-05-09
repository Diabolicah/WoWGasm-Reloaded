package Discord.Commands.ModCommands;

import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.ResponseException.AbstractResponse;
import DiscordBotCode.Main.ResponseException.TextResponse;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.awt.*;

public class UnMuteCommand extends DiscordChatCommand
{
	@Override
	public String commandPrefix()
	{
		return "unmute";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "unmute <user>";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(message.getMentions().size() <= 0){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " This command requires at least one user mention to work!");
			return;
		}
		
		for (IUser user : message.getMentions()) {
			MuteCommand.removeMute(user, message.getGuild());
			LoggingUtil.log(null, null, null, EnumLogType.OTHER, message.getAuthor().mention() + " Has un-muted " + LoggingUtil.getName(user, message.getGuild()), new Color(91, 87, 182));
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
	
	@Override
	public AbstractResponse getExecuteResponse( IMessage message, String[] args, boolean hasPermission )
	{
		if (!PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, MuteCommand.requireRole, true)) {
			return new TextResponse(this, message.getAuthor().mention() + " You do not have the required role for this command!");
		}
		return super.getExecuteResponse(message, args, hasPermission);
	}

	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{MuteCommand.requireRole};
	}
}
