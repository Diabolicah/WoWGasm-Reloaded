package Discord.Core.ActivitySystem;

import Discord.Commands.ModCommands.BanCommand;
import Discord.Commands.ModCommands.KickCommand;
import Discord.Commands.ModCommands.MuteCommand;
import Discord.Commands.ModCommands.Warn.WarnCommand;
import Discord.Core.ActivitySystem.Staff.StaffActivity;
import Discord.Core.ActivitySystem.User.UserActivity;
import DiscordBotCode.CommandFiles.DiscordChatCommand;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.DiscordBotBase;
import DiscordBotCode.Misc.CustomEntry;
import org.apache.commons.lang.WordUtils;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractActivity
{
	public ConcurrentHashMap<String, Integer> userPoints = new ConcurrentHashMap<>();
	
	protected File pointsStore;
	protected File logsFolder;
	
	public static final int warnPenalty = 100, mutePenalty = 100;
	
	
	public void init() throws IOException
	{
		pointsStore = FileGetter.getFile(DiscordBotBase.FilePath + "/" + getName() + "/points.txt");
		logsFolder = FileGetter.getFolder(DiscordBotBase.FilePath + "/" + getName() + "/logs/");
		
		Files.lines(pointsStore.toPath()).forEach(( e ) -> {
			String[] tt = e.split("=");
			userPoints.put(tt[0], Integer.parseInt(tt[1]));
		});
		
	}
	
	public void resetPoints(){
		System.out.println("Resetting " + getName() + " points!");
		
		userPoints.clear();
		
		for(File fe : logsFolder.listFiles()){
			fe.delete();
		}
		
		FileUtil.removeLineFromFile(pointsStore, "");
	}
	
	
	public void addPoints( IUser user, int points, String reason){
		setPoints(user, getPoints(user) + points);
		if(reason != null) log(user, reason + "=" + points);
	}
	
	public void removePoints(IUser user, int points, String reason){
		setPoints(user, getPoints(user) - points);
		if(reason != null) log(user, reason + "=" + -points);
	}
	
	public int getPoints(IUser user){
		return userPoints.getOrDefault(user.getID(), 0);
	}
	
	public void setPoints(IUser user, int points){
		if(!validUser(user)){
			return;
		}
		
		userPoints.put(user.getID(), points);
		
		FileUtil.removeLineFromFile(pointsStore, user.getID());
		FileUtil.addLineToFile(pointsStore, user.getID() + "=" + points);
	}
	
	public void log(IUser user, String text){
		File file = FileGetter.getFile(StaffActivity.object.logsFolder.getAbsolutePath() + "/" + user.getID() + ".txt");
		FileUtil.addLineToFile(file, text);
	}
	
	public ArrayList<CustomEntry<String, Integer>> getLog( IUser user){
		ArrayList<CustomEntry<String, Integer>> list = new ArrayList<>();
		File file = FileGetter.getFile(StaffActivity.object.logsFolder.getAbsolutePath() + "/" + user.getID() + ".txt");
		
		try {
			Files.lines(file.toPath()).forEach(( e ) -> {
				String[] tt = e.split("=");
				list.add(new CustomEntry<>(tt[0], Integer.parseInt(tt[1])));
			});
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		ArrayList<CustomEntry<String, Integer>> list1 = new ArrayList<>(list);
		
		Collections.reverse(list1);
		if(list1.size() > 10) list1 = new ArrayList<>(list1.subList(0, 10));
		
		return list1;
	}
	
	public abstract String getName();
	public abstract boolean validUser(IUser user);
	
	public static void staffCommandUsed( IUser admin, IUser user, DiscordChatCommand command){
		int penalty = (command instanceof WarnCommand ? warnPenalty : command instanceof MuteCommand ? mutePenalty : 100);
		String name = (command instanceof WarnCommand ? "Warned" : command instanceof MuteCommand ? "Muted" : null);
		
		int points = (command instanceof WarnCommand ? StaffActivity.warnPoints : command instanceof MuteCommand ? StaffActivity.mutePoints :
						command instanceof BanCommand ? StaffActivity.banPoints : command instanceof KickCommand ? StaffActivity.kickPoints : 1);
		
		String stringAdminName = WordUtils.capitalize(command.commandPrefix());
		
		if(UserActivity.object.validUser(user)) {
			UserActivity.object.removePoints(user, penalty, name);
			StaffActivity.object.addPoints(admin, points, stringAdminName);
		}else if(StaffActivity.object.validUser(user)){
			StaffActivity.object.removePoints(user, points, name);
			StaffActivity.object.addPoints(admin, points, stringAdminName);
		}
	}
}
