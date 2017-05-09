package Discord.Core.ActivitySystem.ManualCommands;

import Discord.Core.ActivitySystem.Staff.StaffActivity;
import Discord.Core.ActivitySystem.User.UserActivity;
import Discord.Core.ConfigFile;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.Utils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;

public class GivePointsCommand extends DiscordChatCommand
{
	public static String requiredRole;
	
	public GivePointsCommand()
	{
		requiredRole = ConfigFile.getPermission("give_points_command", "daddy god");
	}
	
	@Override
	public String commandPrefix()
	{
		return "givePoints";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "givePoints <user> <amount> <reason>";
	}
	
	@Override
	public String getDescription(DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "Gives points to the specific user (or users) for the weekly score system, the reason is used for logging points earned and lost";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		if(message.getMentions().size() == 0){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Givepoints requires a user input!");
			return;
		}
		
		if(args.length < 2){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Invalid command context! usage is \"" + getUsage(this, message) + "\"");
			return;
		}
		
		int numStart = 0;
		
		for(String t : args){
			for(IUser user : message.getMentions()){
				if(t.equalsIgnoreCase(user.mention(false))
			    || t.equalsIgnoreCase(user.mention(true))){
					numStart += 1;
				}
			}
		}
		
		if(numStart >= args.length){
			numStart = 0;
		}
		
		if(!Utils.isInteger(args[numStart])){
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Invalid command context! usage is \"" + getUsage(this, message) + "\", second input has to be a number!");
			return;
		}
		
		int num = Integer.parseInt(args[numStart]);
		String reason = String.join(" ", Arrays.copyOfRange(args, numStart + 1, args.length));
		
		int num1 = 0;
		for(IUser user : message.getMentions()){
			if(StaffActivity.object.validUser(user)) {
				StaffActivity.object.addPoints(user,num,reason);
				num1 += 1;
				
			} else if(UserActivity.object.validUser(user)) {
				UserActivity.object.addPoints(user, num, reason);
				num1 += 1;
				
			}
		}
		
		if(num1 > 0) {
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Points have now been given!");
		}else{
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Invalid users!");
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
}
