package Discord.Commands.StatusSystem;

import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.DiscordBotBase;
import Startup.BotCore;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class StatusSystemCore
{
	public static ConcurrentHashMap<String, Status> userStatus = new ConcurrentHashMap<>();
	
	public static File statusFile;
	
	
	public static void init(){
		statusFile = FileGetter.getFile(DiscordBotBase.FilePath + "/userStatus.txt");
		
		loadFromFile();
	}
	
	
	private static void loadFromFile(){
		try {
			Files.lines(statusFile.toPath()).forEach(( e ) -> {
				String[] tt = e.split("=");
				String userId = tt[0];
				String statusData = tt[1];
				statusData = statusData.substring(1, statusData.length() - 1);
				
				String[] data = statusData.split("\\|");
				
				String reason = null;
				Date returnDate = null;
				
				if(data[0] != null && !data[0].equalsIgnoreCase(".")){
					reason = data[0];
				}
				
				if(data[1] != null && !data[1].equalsIgnoreCase(".")){
					returnDate = new Date(Long.parseLong(data[1]));
				}
				
				IUser user = DiscordBotBase.discordClient.getUserByID(userId);
				userStatus.put(userId, new Status(reason, returnDate));
				
				queEnd(user, getStatus(user));
			});
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
	}
	
	public static void queEnd(IUser user, Status status){
		if(status.getReturnDate() != null){
			BotCore.timer.schedule(new TimerTask() {
				@Override
				public void run()
				{
					removeStatus(user);
				}
			}, status.getReturnDate());
		}
	}
	
	public static void setStatus( IUser user, Status status){
		userStatus.put(user.getID(), status);
		
		FileUtil.removeLineFromFile(statusFile, user.getID());
		FileUtil.addLineToFile(statusFile, user.getID() + "=[" + (status.getReason() != null ? status.getReason() : ".") + "|" + (status.getReturnDate() != null ? status.getReturnDate().getTime() : ".") + "]");
		
		queEnd(user, getStatus(user));
	}
	
	public static void removeStatus(IUser user){
		userStatus.remove(user.getID());
		
		FileUtil.removeLineFromFile(statusFile, user.getID());
	}
	
	public static Status getStatus(IUser user){
		return userStatus.getOrDefault(user.getID(), null);
	}
	
}
