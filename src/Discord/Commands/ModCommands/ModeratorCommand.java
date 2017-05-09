package Discord.Commands.ModCommands;

import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public abstract class ModeratorCommand extends DiscordChatCommand
{
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		RequestBuffer.request(() -> {
			try {
				message.delete();
			} catch (MissingPermissionsException | DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
		});
	}
	
	
	@Override
	public IGuild[] roleChecks()
	{
		return MainDiscordBot.guildList;
	}
}
