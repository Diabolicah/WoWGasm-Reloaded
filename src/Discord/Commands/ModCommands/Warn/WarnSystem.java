package Discord.Commands.ModCommands.Warn;

import Discord.Core.ConfigFile;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.DiscordBotBase;
import Startup.BotCore;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WarnSystem
{
	public static ConcurrentHashMap<String, HashMap<String, ArrayList<WarnObject>>> warnings = new ConcurrentHashMap<>();
	
	public static File warnsFile;
	private static int daysToKeep = 14;
	
	private static Long updateInterval = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);
	
	
	public static void init(){
		if (warnsFile == null) {
			warnsFile = FileGetter.getFile(DiscordBotBase.FilePath + "/warns.txt");
		}
		
		daysToKeep = Integer.parseInt(ConfigFile.getValueOrDefault("warn_list_days_to_expire", "14"));
		
		BotCore.timer.schedule(new TimerTask() {
			@Override
			public void run()
			{
				System.out.println("Updating warn list...");
				reloadWarnings();
				System.out.println("Loaded " + warnings.size() + " warnings!");
			}
		}, 0, updateInterval);
	}
	
	
	private static final Random rand = new Random();
	private static final int minNumber = 10000, maxNumber = 90000;
	
	public static int genId(IGuild guild){
		int n = -1;
		
		while(n == -1 || getWarnById(guild, n) != null){
			n = (int)(minNumber + rand.nextFloat() * maxNumber);
		}
		
		return n;
	}
	
	public static WarnObject createWarn(IGuild guild, IChannel channel, String user, String admin, String userId, String reason, Date date,  int id){
		WarnObject warning = new WarnObject(user, admin, guild, channel, reason, date, id);
		
		if (guild != null) {
			if (!warnings.containsKey(guild.getID())) {
				warnings.put(guild.getID(), new HashMap<>());
				warnings.get(guild.getID()).put(userId, new ArrayList<>());
			}
			
			if (warnings.get(guild.getID()).containsKey(userId)) {
				warnings.get(guild.getID()).get(userId).add(warning);
			} else {
				warnings.get(guild.getID()).put(userId, new ArrayList<>(Collections.singletonList(warning)));
			}
		}
		
		return warning;
	}
	
	public static WarnObject createWarn(IGuild guild, IChannel channel, IUser user, IUser admin, String userId, String reason, Date date,  int id){
		WarnObject warning = new WarnObject(user, admin, guild, channel, reason, date, id);
		
		if (guild != null) {
			if (!warnings.containsKey(guild.getID())) {
				warnings.put(guild.getID(), new HashMap<>());
				warnings.get(guild.getID()).put(userId, new ArrayList<>());
			}
			
			if (warnings.get(guild.getID()).containsKey(userId)) {
				warnings.get(guild.getID()).get(userId).add(warning);
			} else {
				warnings.get(guild.getID()).put(userId, new ArrayList<>(Collections.singletonList(warning)));
			}
		}
		
		return warning;
	}
	
	public static WarnObject createAndSaveWarn(IGuild guild, IChannel channel, IUser user, IUser admin, String userId, String reason, Date date, int id){
		WarnObject object = createWarn(guild, channel, user, admin, userId, reason, date, id);
		FileUtil.addLineToFile(warnsFile, guild.getID() + "_" + user.getID() + "_" + reason + "_" + System.currentTimeMillis() + "_" + admin.getID() + "_" + channel.getID() + "_" + object.id);
		return object;
	}
	
	public static void reloadWarnings(){
		warnings.clear();
		
		ArrayList<WarnObject> reSave = new ArrayList<>();
		Date nowDate = new Date();
		
		try {
			Files.lines(warnsFile.toPath()).forEach(( e ) -> {
				if(e.contains("_")) {
					String[] tt = e.split("_");
					
					IGuild guild = DiscordBotBase.discordClient.getGuildByID(tt[ 0 ]);
					IChannel channel = null;
					String reason = tt[ 2 ];
					Date date;
					int id = minNumber;
					
					if (reason == null || reason.isEmpty()) {
						reason = "";
					}
					
					if (tt.length > 5) {
						channel = DiscordBotBase.discordClient.getChannelByID(tt[ 5 ]);
					}
					
					boolean idSaved = true;
					if(tt.length > 6){
						id = Integer.parseInt(tt[6]);
					}else if(guild != null){
						id = WarnSystem.genId(guild);
						idSaved = false;
					}
					
					date = new Date(Long.parseLong(tt[ 3 ]));
					
					
					if ((nowDate.getTime() - date.getTime()) < TimeUnit.MILLISECONDS.convert(daysToKeep, TimeUnit.DAYS)) {
						WarnObject object = createWarn(guild, channel, tt[ 1 ], tt[ 4 ], tt[1], reason, date, id);
						
						if(!idSaved){
							reSave.add(object);
						}
					}else{
						FileUtil.removeLineFromFile(warnsFile, e);
					}
					
					if(!idSaved){
						FileUtil.removeLineFromFile(warnsFile, e);
					}
				}
			});
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		for(WarnObject object : reSave){
			FileUtil.addLineToFile(warnsFile, object.guild.getID() + "_" + object.userId + "_" + object.reason + "_" + object.time.getTime() + "_" + object.admin.getID() + "_" + object.channel.getID() + "_" + object.id);
		}
	}
	
	public static void clearUserWarns(IGuild guild, IUser user){
		FileUtil.removeLineFromFile(warnsFile, guild.getID() + "_" + user.getID());
		reloadWarnings();
	}
	
	public static WarnObject getWarnById(IGuild guild, int id){
		try {
			for(ArrayList<WarnObject> obList : getListForGuild(guild).values()){
				for(WarnObject ob : obList){
					if(ob.id == id){
						return ob;
					}
				}
			}
		} catch (IOException e) {
			DiscordBotBase.discordBotBase.handleException(e);
		}
		
		return null;
	}
	
	public static void removeWarn(IGuild guild, int id){
		WarnObject object = getWarnById(guild, id);
		
		if(object != null){
			FileUtil.removeLineFromFile(warnsFile, object.guild.getID() + "_" + object.user.getID() + "_" + object.reason + "_" + object.time.getTime() + "_" + object.admin.getID() + "_" + object.channel.getID() + "_" + id);
		}
	}
	
	public static void setWarnFromId(IGuild guild, int id, WarnObject object){
		WarnObject oldWarn = getWarnById(guild, id);
		
		FileUtil.removeLineFromFile(warnsFile, oldWarn.channel.getID() + "_" + id);
		FileUtil.addLineToFile(warnsFile, object.guild.getID() + "_" + object.user.getID() + "_" + object.reason + "_" + object.time.getTime() + "_" + object.admin.getID() + "_" + object.channel.getID() + "_" + id);
	}
	
	
	
	public static ArrayList<WarnObject> getWarningsForUser( IGuild guild, String userId) throws IOException
	{
		ArrayList<WarnObject> list = getListForGuild(guild).getOrDefault(userId, new ArrayList<>());
		
		list.sort(Comparator.comparing(o -> o.time));
		Collections.reverse(list);
		
		return list;
	}
	
	public static HashMap<String, ArrayList<WarnObject>> getListForGuild( IGuild guild) throws IOException
	{
		return getList().getOrDefault(guild.getID(), new HashMap<>());
	}
	
	public static ArrayList<String> getListOfUsers(IGuild guild) throws IOException
	{
		ArrayList<String> list1 = new ArrayList<>();
		HashMap<String, ArrayList<WarnObject>> list = getList().getOrDefault(guild.getID(), new HashMap<>());
		
		for(Map.Entry<String, ArrayList<WarnObject>> ent : list.entrySet()){
			list1.add(ent.getKey());
		}
		
		return list1;
	}
	
	public static ConcurrentHashMap<String, HashMap<String, ArrayList<WarnObject>>> getList() throws IOException
	{
		return warnings;
	}
}
