package Discord.Core.ActivitySystem;

import Discord.Core.ActivitySystem.Staff.StaffActivity;
import Discord.Core.ActivitySystem.User.UserActivity;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Main.DiscordBotBase;
import Startup.BotCore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ActivitySystem
{
	
	protected static File properties;
	public static Properties prop = new Properties();
	
	public static void init(){
		properties = FileGetter.getFile(DiscordBotBase.FilePath + "/userActivity/data.properties");
		
		try {
			prop.load(new FileInputStream(properties));
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		UserActivity.object.init();
		StaffActivity.object.init();
		
		initResetTimer();
	}
	
	public static int resetDay = Calendar.SATURDAY;
	
	
	public static Calendar nextDayOfWeek(int dow) {
		Calendar date = Calendar.getInstance();
		int diff = dow - date.get(Calendar.DAY_OF_WEEK);
		if (!(diff > 0)) {
			diff += 7;
		}
		date.add(Calendar.DAY_OF_MONTH, diff);
		return date;
	}
	
	public static void initResetTimer(){
		BotCore.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run()
			{
				
				try {
					prop.load(new FileInputStream(properties));
				} catch (IOException e) {
					DiscordBotBase.discordBotBase.handleException(e);
				}
				
				if(!prop.containsKey("resetTime")){
					Date nextDate = nextDayOfWeek(Calendar.SATURDAY).getTime();
					Date lastDate = new Date(nextDate.getTime() - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
					
					prop.setProperty("resetTime", Long.toString(lastDate.getTime()));
					try {
						prop.store(new FileOutputStream(properties), "");
					} catch (IOException e) {
						DiscordBotBase.discordBotBase.handleException(e);
					}
				}
				
				if(prop.containsKey("resetTime")){
					Date date = new Date(Long.parseLong(prop.getProperty("resetTime")));
					Date curDate = new Date();
					
					long time = curDate.getTime() - date.getTime();
					long daysSince = TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
					
					System.out.println((7 - daysSince) + " days until weekly reset!");
					
					if(daysSince < 7){
						return;
					}
				}
				
				reset();
			}
		}, 0, TimeUnit.MILLISECONDS.convert(6, TimeUnit.HOURS));
		
	}
	
	public static void reset(){
		System.out.println("Resetting weekly points!");
		
		try {
			prop.setProperty("resetTime", Long.toString(new Date().getTime()));
			prop.store(new FileOutputStream(properties), "");
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		UserActivity.object.resetPoints();
	}
	
	
}
