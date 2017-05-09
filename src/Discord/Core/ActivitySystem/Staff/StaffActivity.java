package Discord.Core.ActivitySystem.Staff;

import Discord.Core.ActivitySystem.AbstractActivity;
import Discord.Core.ActivitySystem.User.ActivityLoginSystem;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.CustomEntry;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaffActivity extends AbstractActivity
{
	
	public static StaffActivity object = new StaffActivity();
	
	public static int warnPoints = 1, kickPoints = 1, banPoints = 1, mutePoints = 1;
	
	public static ConcurrentHashMap<String, Long> lastRoleUpdate = new ConcurrentHashMap<>();
	
	public static File roleUpdateFile;
	
	public static IRole highestRole;
	public static IRole lowestRole;
	
	public void init(){
		try {
			super.init();
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		roleUpdateFile = FileGetter.getFile(DiscordBotBase.FilePath + "/" + getName() + "/roleUpdates.txt");
		
		try {
			Files.lines(roleUpdateFile.toPath()).forEach(( e ) -> {
				String[] tt = e.split("=");
				lastRoleUpdate.put(tt[0], Long.parseLong(tt[1]));
			});
			
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		highestRole = DiscordBotBase.discordClient.getRoleByID("259380730109362177");
		lowestRole = DiscordBotBase.discordClient.getRoleByID("279765544054292480");
		
		ActivityLoginSystem.initLoginCheck();
	}
	
	
	@Override
	public String getName()
	{
		return "staffActivity";
	}
	
	
	
	public boolean validUser(IUser user){
		if(user == null){
			return false;
		}
		
		if(DiscordBotBase.debug){
			return true;
		}
		
		if(user.isBot()){
			return false;
		}
		
		
		IRole role = StaffActivityCommand.getHighestRole(user);
		return role != null && role.getPosition() < highestRole.getPosition() && role.getPosition() >= lowestRole.getPosition();
	}
	
	public static final int viewSize = 10;
	public ArrayList<CustomEntry<IUser, Integer>> getUsers(int page){
		ArrayList<CustomEntry<IUser, Integer>> list = new ArrayList<>();
		
		for(Map.Entry<String, Integer> ent : userPoints.entrySet()){
			IUser user = DiscordBotBase.discordClient.getUserByID(ent.getKey());
			list.add(new CustomEntry<>(user, ent.getValue()));
		}
		
		list.sort(( o1, o2 ) -> o2.getValue().compareTo(o1.getValue()));
		list.removeIf((e) -> !validUser(e.getKey()));
		
		if(list.size() > (page * viewSize)) list = new ArrayList<>(list.subList(page * viewSize, list.size()));
		if(list.size() > viewSize) list = new ArrayList<>(list.subList(0, viewSize));
		
		return list;
	}
	
	public static Date getLastRoleChange( IUser user){
		if(lastRoleUpdate.containsKey(user.getID())){
			long t = lastRoleUpdate.get(user.getID());
			return new Date(t);
		}
		
		return null;
	}
	
}

