package Discord.Core.ActivitySystem.User;

import Discord.Core.ActivitySystem.AbstractActivity;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.CustomEntry;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class UserActivity extends AbstractActivity
{
	public static UserActivity object = new UserActivity();
	
	public static ConcurrentHashMap<String, Date> lastMessage = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Long> lastRoleUpdate = new ConcurrentHashMap<>();
	
	public static long messageCooldown = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
	
	public static final int messagePoints = 1, loginPoints = 5;
	
	public static File roleUpdateFile;
	
	public static IRole highestRole;
	public static IRole lowestRole;
	
	//TODO Current max points with all systems working correctly is 10.115
	
	public void init(){
		try {
			super.init();
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		roleUpdateFile = FileGetter.getFile(DiscordBotBase.FilePath + "/" + getName() + "/roleUpdates.txt");
		
		highestRole = DiscordBotBase.discordClient.getRoleByID("259029566750851073");
		lowestRole = DiscordBotBase.discordClient.getRoleByID("299132024592138240");
		
		try {
			Files.lines(roleUpdateFile.toPath()).forEach(( e ) -> {
				String[] tt = e.split("=");
				lastRoleUpdate.put(tt[0], Long.parseLong(tt[1]));
			});
			
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		
		ActivityLoginSystem.initLoginCheck();
	}
	
	
	
	public void resetPoints(){
		super.resetPoints();
		lastMessage.clear();
	}
	
	@Override
	public String getName()
	{
		return "userActivity";
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
		
		
		IRole role = UserActivityCommand.getHighestRole(user);
		return role != null && role.getPosition() < highestRole.getPosition() && role.getPosition() > lowestRole.getPosition();
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
		
		if(list.size() > 0) {
			if (list.size() > (page * viewSize))
				list = new ArrayList<>(list.subList(page * viewSize, list.size()));
			
			if (list.size() > viewSize)
				list = new ArrayList<>(list.subList(0, viewSize));
		}
		
		return list;
	}
	
	public static Date getLastRoleChange(IUser user){
		if(lastRoleUpdate.containsKey(user.getID())){
			long t = lastRoleUpdate.get(user.getID());
			return new Date(t);
		}
		
		return null;
	}
	
	
	
	public void messagePoint( IMessage message){
		if(lastMessage.containsKey(message.getAuthor().getID())){
			Date date = new Date();
			long timeSince = date.getTime() - lastMessage.get(message.getAuthor().getID()).getTime();
			
			if(timeSince > messageCooldown){
				lastMessage.remove(message.getAuthor().getID());
			}else{
				return;
			}
		}
		
		lastMessage.put(message.getAuthor().getID(), Date.from(message.getCreationDate().atZone(ZoneId.systemDefault()).toInstant()));
		addPoints(message.getAuthor(), messagePoints, null);
	}
	
}

