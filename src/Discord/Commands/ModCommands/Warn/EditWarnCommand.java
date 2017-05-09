package Discord.Commands.ModCommands.Warn;

import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Arrays;

public class EditWarnCommand extends DiscordChatCommand
{
	public static String requiredRole;
	
	public EditWarnCommand()
	{
		requiredRole = ConfigFile.getPermission("edit_warn_permission", "daddy god");
	}
	
	@Override
	public String commandPrefix()
	{
		return "editWarn";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callMessage )
	{
		return "editWarn <id> <reason>";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(args.length <= 0){
			return;
		}
		
		String id = args[0];
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		
		if(!Utils.isInteger(id)){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Invalid id!");
			return;
		}
		
		WarnObject object = WarnSystem.getWarnById(message.getGuild(), Integer.parseInt(id));
		
		if(object == null){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no warn to edit!");
			return;
		}
		
		object.reason = reason;
		
		WarnSystem.setWarnFromId(message.getGuild(), Integer.parseInt(id), object);
		WarnSystem.reloadWarnings();
		
		ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Warn has now been edited!");
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
