package Discord.Commands.ModCommands.ChannelCommands;

import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.EnumSet;

public class ChannelBan extends DiscordChatCommand
{
	@Override
	public String commandPrefix()
	{
		return "channelban";
	}
	
	@Override
	public String getUsage(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "channelban <user> <reason>";
	}
	
	@Override
	public String getDescription( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Removes the users access to the current channel permanently";
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
		
		String reason = String.join(" ", args);
		
		for (IUser user : message.getMentions()) {
			reason = reason.replace(user.mention(), "");
			reason = reason.replace(user.mention(false), "");
		}
		
		while(reason.startsWith(" ")){
			reason = reason.replaceFirst(" ", "");
		}
		
		for (IUser user : message.getMentions()) {
			LoggingUtil.log(message.getAuthor(), message.getGuild(),user, EnumLogType.CHANNEL_BAN, message.getChannel().mention() + " with reason: \"" + reason + "\"");
			RequestBuffer.request(() ->  {
				try {
					message.getChannel().overrideUserPermissions(user, null, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY, Permissions.SEND_MESSAGES));
				} catch (MissingPermissionsException | DiscordException e) {
					DiscordBotBase.discordBotBase.handleException(e);
				}
			});
		}
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
