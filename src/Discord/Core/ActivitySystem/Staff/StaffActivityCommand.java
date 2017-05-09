package Discord.Core.ActivitySystem.Staff;

import Discord.Core.ConfigFile;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.CommandFiles.DiscordCommand;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Main.Utils;
import DiscordBotCode.Misc.CustomEntry;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings( "StringConcatenationInsideStringBufferAppend" )
public class StaffActivityCommand extends DiscordChatCommand
{
	public static String requiredRole;
	
	public StaffActivityCommand(){
		requiredRole = ConfigFile.getPermission("staff_activity_commandl", "daddy god");
	}
	
	@Override
	public String commandPrefix()
	{
		return "staffActivity";
	}
	
	@Override
	public String getUsage( DiscordCommand sourceCommand, IMessage callerMessage)
	{
		return "staffActivity [mentions/page num]";
	}
	
	@Override
	public void commandExecuted( IMessage message, String[] args )
	{
		boolean hasMentions = message.getMentions().size() > 0;
		
		int page = 0;
		
		if(args.length > 0 && Utils.isInteger(args[0])){
			page = Integer.parseInt(args[0]) - 1;
		}
		
		int amount = 0;
		for(Map.Entry<String, Integer> ent : StaffActivity.object.userPoints.entrySet()){
			IUser user = DiscordBotBase.discordClient.getUserByID(ent.getKey());
			
			if(	StaffActivity.object.validUser(user)) {
				amount += 1;
			}
		}
		
		int size = amount;
		int pages = ((size / StaffActivity.viewSize));
		
		
		if(size > (pages * StaffActivity.viewSize) && size != (pages * StaffActivity.viewSize)){
			pages += 1;
		}
		
		if(page > pages || (page + 1) > pages){
			page = pages - 1;
		}
		
		StringBuilder builder = new StringBuilder();
		
		String start = "```perl\n";
		builder.append(start);
		
		int num = 1 + (page * StaffActivity.viewSize);
		
		int average = -1;
		int min = -1, max = -1;
		int numA = 0;
		
		for(Map.Entry<String, Integer> ent : StaffActivity.object.userPoints.entrySet()){
			if(min == -1 || ent.getValue() < min){
				min = ent.getValue();
			}
			
			if(ent.getValue() > max){
				max = ent.getValue();
			}
			if(ent.getValue() > 0) {
				average += ent.getValue();
				numA += 1;
			}
		}
		if(average > 0 && numA > 0) {
			average /= numA;
		}
		
		if(hasMentions){
			ArrayList<IUser> users = new ArrayList<>();
			users.addAll(message.getMentions());
			
			for(IRole role : message.getRoleMentions()){
				users.addAll(message.getGuild().getUsersByRole(role));
			}
			
			for(IUser user : users){
				if(StaffActivity.object.userPoints.containsKey(user.getID())) {
					String text = getText(user, num, true);
					
					if(builder.toString().length() + text.length() > 1800){
						break;
					}
					
					builder.append(text);
					num += 1;
				}
			}
			
		}else{
			for(CustomEntry<IUser, Integer> ent : StaffActivity.object.getUsers(page)){
				String text = getText(ent.getKey(), num, false);
				
				builder.append(text);
				num += 1;
			}
		}
		
		if(!hasMentions) {
			builder.append("Min: " + min + " | Max: " + max + " | Avg: " + average + " | Amount: " + amount + "\n");
			builder.append("Page: [ " + (page + 1) + " / " + (pages) + " ]\n");
		}
		
		if(num > 1){
			ChatUtils.sendMessage(message.getChannel(), builder.toString() + "```");
		}else{
			ChatUtils.sendMessage(message.getChannel(), message.getAuthor().mention() + " Found no activity to show!");
		}
	}
	
	public static DateFormat formatter = new SimpleDateFormat("EEE MMM d, yyyy", Locale.ENGLISH);
	
	
	public static String getText( IUser user, int num, boolean full){
		StringBuilder builder = new StringBuilder();
		IRole role = getHighestRole(user);
		
		builder.append("[" + num + "]" + "  #" + user.getName() + "\n");
		builder.append("\t  > Points: " + StaffActivity.object.getPoints(user) + "\n");
		if(role != null) builder.append("\t  > Role: #" + role.getName() + "\n");
		
		Date date = StaffActivity.getLastRoleChange(user);
		
		if(date != null){
			builder.append("\t  > Last promotion: " + formatter.format(date) + "\n");
		}
		
		if(full){
			if(StaffActivity.object.getLog(user).size() > 0) {
				builder.append("\t  > Log: \n");
				for (CustomEntry<String, Integer> ent : StaffActivity.object.getLog(user)) {
					String text = ent.getKey() + "\"";
					int length = 0;
					
					for (CustomEntry<String, Integer> ent1 : StaffActivity.object.getLog(user)) {
						if(ent1.getKey().length() > length){
							length = ent1.getKey().length();
						}
					}
					
					while(text.length() < (length + 5)){
						text += " ";
					}
					
					builder.append("\t\t > Reason: \"" + text + "|  " + (ent.getValue() > 0 ? "+" : "") + ent.getValue() + " Points \n");
				}
			}
		}
		
		builder.append("\n");
		
		return builder.toString();
	}
	
	
	public static IRole getHighestRole( IUser user){
		if(!DiscordBotBase.debug) {
			if (StaffActivity.highestRole == null || StaffActivity.lowestRole == null) {
				return null;
			}
		}
		
		IRole role = null;
		int num = -1;
		
		for(IRole role1 : user.getRolesForGuild(MainDiscordBot.guild)){
			if(!DiscordBotBase.debug) {
				if (role1.getPosition() > StaffActivity.highestRole.getPosition() || role1.getPosition() <= StaffActivity.lowestRole.getPosition()) {
					continue;
				}
			}
			
			if(role1.getPosition() > num){
				num = role1.getPosition();
				role = role1;
			}
		}
		
		return role;
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
	public boolean canCommandBePrivateChat()
	{
		return false;
	}
}
