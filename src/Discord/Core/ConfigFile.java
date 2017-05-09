package Discord.Core;

import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.DiscordBotBase;

import java.io.File;
import java.io.IOException;

public class ConfigFile
{
	private static final String fileTag = "CONFIG_VALUES";
	private static final String fileTagPerms = "PERMISSION_VALUES";
	private static File file, file2;
	
	static void initConfig()
	{
		file = FileGetter.getFile(DiscordBotBase.FilePath + "/config.txt");
		file2 = FileGetter.getFile(DiscordBotBase.FilePath + "/permissions.txt");
		initConfigValues();
	}
	
	private static void initConfigValues()
	{
		try {
			FileUtil.initFile(file, fileTag);
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		try {
			FileUtil.initFile(file2, fileTagPerms);
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
	}
	
	public static String getValueOrDefault( String key, String defaultValue )
	{
		String t = FileUtil.getValue(fileTag, key);
		
		if (t == null || t.isEmpty()) {
			FileUtil.addLineToFile(file, key + "=" + defaultValue);
			initConfigValues();
			return defaultValue;
		}
		
		return t;
	}
	
	public static String getPermission( String key, String defaultValue )
	{
		String t = FileUtil.getValue(fileTagPerms, key);
		
		if (t == null || t.isEmpty()) {
			FileUtil.addLineToFile(file2, key + "=" + defaultValue);
			initConfigValues();
			return defaultValue;
		}
		
		return t;
	}
}
