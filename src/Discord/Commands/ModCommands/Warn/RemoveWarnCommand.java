package Discord.Commands.ModCommands.Warn;

import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.PermissionUtils;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class RemoveWarnCommand extends DiscordChatCommand
{
	public static String requiredRole = "";
	
	public RemoveWarnCommand()
	{
		requiredRole = ConfigFile.getPermission("remove_warn_permission", "moderator");
	}
	
	@Override
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public String commandPrefix()
	{
		return "removeWarn";
	}
	
	@Override
	public String getUsage(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "removeWarn <user>";
	}
	
	@Override
	public String getDescription(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Removes the specific warn specified by the warn id";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		for(String t : args){
			if(Utils.isInteger(t)) {
				WarnSystem.removeWarn(message.getGuild(), Integer.parseInt(t));
			}
		}
		RequestBuffer.request(() -> {
			try {
				ChatUtils.sendMessage(message.getAuthor().getOrCreatePMChannel(), "The specified warns have now been removed!");
			} catch (DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}
		});
	}
	
	@Override
	public boolean canExecute( IMessage message, String[] args )
	{
		return PermissionUtils.hasRole(message.getAuthor(), MainDiscordBot.guild, requiredRole, true);
	}
	
	@Override
	public IGuild[] roleChecks()
	{
		return MainDiscordBot.guildList;
	}
}
