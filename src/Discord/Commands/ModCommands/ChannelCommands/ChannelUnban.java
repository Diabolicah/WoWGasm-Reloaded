package Discord.Commands.ModCommands.ChannelCommands;

import Discord.Core.Logging.EnumLogType;
import Discord.Core.Logging.LoggingUtil;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.awt.*;
import java.util.EnumSet;

public class ChannelUnban extends DiscordChatCommand
{
	@Override
	public String commandPrefix()
	{
		return "channelunban";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "channelunban <user>";
	}
	
	@Override
	public String getDescription(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Removes the channel ban from the specific user";
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
		
		for (IUser user : message.getMentions()) {
			LoggingUtil.log(message.getAuthor(), message.getGuild(),user, EnumLogType.OTHER, LoggingUtil.getName(user, message.getGuild()) + " is no longer banned from " + message.getChannel().mention(), new Color(163, 60, 66));
			
			try {
				EnumSet<Permissions> set = message.getChannel().getModifiedPermissions(user);
				set.remove(Permissions.READ_MESSAGES);
				set.remove(Permissions.READ_MESSAGE_HISTORY);
				set.remove(Permissions.SEND_MESSAGES);
				
				for(IRole role : user.getRolesForGuild(message.getGuild())){
					for(Permissions s : message.getChannel().getModifiedPermissions(role)){
						set.remove(s);
					}
				}
				
				if(set.size() <= 0) {
					message.getChannel().removePermissionsOverride(user);
				}else{
					message.getChannel().overrideUserPermissions(user, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY, Permissions.SEND_MESSAGES), null);
				}
			} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
		}
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return true;
	}
}
