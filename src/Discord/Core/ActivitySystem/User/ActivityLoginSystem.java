package Discord.Core.ActivitySystem.User;

import Discord.Core.MainDiscordBot;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.DiscordBotBase;
import Startup.BotCore;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ActivityLoginSystem
{
	public static ArrayList<String> usersLoggedIn = new ArrayList<>();
	
	
	public static long loginTimeCheck = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
	public static long loginResetTime = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);
	
	protected static File loggedInFile;
	
	
	//TODO Login points are not given to everyone, some people start with 1 point
	public static void initLoginCheck(){
		loggedInFile = FileGetter.getFile(DiscordBotBase.FilePath + "/userActivity/logInStatus.txt");
		
		
		try {
			Files.lines(loggedInFile.toPath()).forEach(( e ) -> usersLoggedIn.add(e));
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		BotCore.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run()
			{
				int found = 0;
				for(IUser user : MainDiscordBot.guild.getUsers()){
					if(user.getPresence() != Presences.OFFLINE && user.getPresence() != Presences.UNKNOWN) {
						if(UserActivity.object.getPoints(user) > 0) {
							if (!usersLoggedIn.contains(user.getID())) {
								userLoggedIn(user);
								found += 1;
							}
						}
					}
				}
				
				if(found > 0){
					System.out.println("Daily login check complete, found " + found + " users");
				}
			}
		}, 0, loginTimeCheck);
		
		//TODO Maybe keep a date save to file incase bot is restarted around midnight
		BotCore.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run()
			{
				resetLogin();
			}
		}, loginResetTime, loginResetTime);
	}
	
	public static void resetLogin(){
		System.out.println("Resetting login!");
		usersLoggedIn.clear();
		FileUtil.removeLineFromFile(loggedInFile, "");
	}
	
	public static void userLoggedIn(IUser user){
		FileUtil.addLineToFile(loggedInFile, user.getID());
		usersLoggedIn.add(user.getID());
		UserActivity.object.addPoints(user, UserActivity.loginPoints, null);
	}
}
