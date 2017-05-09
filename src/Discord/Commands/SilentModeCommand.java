package Discord.Commands;

import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class SilentModeCommand extends DiscordChatCommand
{
	public static String requiredRole;
	
	public SilentModeCommand(){
		requiredRole = ConfigFile.getPermission("silent_mode_role", "daddy god");
	}
	
	@Override
	public String commandPrefix()
	{
		return "Silent";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		MainDiscordBot.silentMode ^= true;
		
		if(MainDiscordBot.silentMode){
			RequestBuffer.request(() -> {
				try {
					ChatUtils.sendMessage(message.getAuthor().getOrCreatePMChannel(), "The bot will now be silent!");
					DiscordBotBase.discordClient.changeStatus(Status.game("Silent mode!"));
				} catch (DiscordException e) {
					DiscordBotBase.discordBotBase.handleException(e);
				}
			});
		}else{
			RequestBuffer.request(() -> {
				try {
					ChatUtils.sendMessage(message.getAuthor().getOrCreatePMChannel(), "The bot will no longer be silent!");
					DiscordBotBase.discordClient.changeStatus(Status.empty());
				} catch (DiscordException e) {
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
	public String[] getRequiredRoles()
	{
		return new String[]{requiredRole};
	}
	
	@Override
	public IGuild[] roleChecks()
	{
		return MainDiscordBot.guildList;
	}
}
