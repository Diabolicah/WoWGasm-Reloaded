package Discord.Core;

import DiscordBotCode.Main.ChatUtils;
import Startup.BotCore;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Announcements
{
	public static void init(){
//		addAnnouncement(TimeUnit.MINUTES.convert(2, TimeUnit.HOURS), "Do you have more than 2k followers on Twitter and want to help promote the Room 34 Community? Send <@267820097047887873> a message to see how you can earn a Promoter rank!","main-chat");
//		addAnnouncement(TimeUnit.MINUTES.convert(6, TimeUnit.HOURS), "```Just a friendly reminder that jokes and memes referring to rape or suicide are not allowed here in any shape, form, or context, no matter who it is directed at or whether it's a general statement. ♥```","main-chat");
//		addAnnouncement(TimeUnit.MINUTES.convert(2, TimeUnit.HOURS),"<@&299132024592138240> If you are 18+ and wish to have full access to Room 34, type, ``I agree`` either here in this chat or in your DM with Gigawhore.","preface");
	}
	
	private static long delay = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	private static long curDelay = 0;
	
	public static void addAnnouncement( long everyXmin, String text,String channel){
		curDelay += delay;
		long time = TimeUnit.MILLISECONDS.convert(everyXmin, TimeUnit.MINUTES);
		
		BotCore.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run()
			{
				if(!MainDiscordBot.silentMode) {
					ChatUtils.sendMessage(ChannelUtils.getChannel(channel), text);
				}
			}
		}, time + curDelay, time);
	}
}
