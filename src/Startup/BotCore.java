package Startup;

import Discord.Core.MainDiscordBot;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.modules.Configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Timer;

@SuppressWarnings( "FieldCanBeLocal" )
public class BotCore
{

	public static String version = null;
	
	public static Timer timer = new Timer();
	
	public static boolean discordEnabled = true;

	public static void main( String[] args ) throws Exception
	{
		Configuration.LOAD_EXTERNAL_MODULES = false;
		
		DiscordBotBase.debug = System.getProperty("debugMode") != null && System.getProperty("debugMode").equalsIgnoreCase("true");
		DiscordBotBase.extraFeedback = DiscordBotBase.debug;
		
		
		discordEnabled = System.getProperty("noDiscord") == null || !Boolean.parseBoolean(System.getProperty("noDiscord"));

		DiscordBotBase.setInfoFile(BotCore.class.getResourceAsStream("build.properties"));
		version = DiscordBotBase.getValue("version_major") + "." + DiscordBotBase.getValue("version_minor") + "." + Integer.parseInt(DiscordBotBase.getValue("version_build"));
		
		if(discordEnabled) MainDiscordBot.launchDiscord();
		if(!discordEnabled) new BotCore().setFilePath();
		
	}
	
	public void setFilePath()
	{
		URL jarLocationUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		String jarLocation = null;
		try {
			jarLocation = Paths.get(jarLocationUrl.toURI()).toFile().getParent();
		} catch (URISyntaxException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		DiscordBotBase.FilePath = jarLocation + File.separator + "botData";
		System.out.println("Launch directory: " + DiscordBotBase.FilePath);
	}
	
	
}
