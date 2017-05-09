package Discord.Core.Logging;

import Discord.Core.ChannelUtils;
import DiscordBotCode.Extra.FileGetter;
import DiscordBotCode.Extra.FileUtil;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingUtil
{
	private static IChannel channel = null;
	
	public static String getName(IUser user, IGuild guild){
		if(guild != null){
			return user.getDisplayName(guild) + " (" + user.getName() + "#" + user.getDiscriminator() + ")";
		}
		
		return user.getName() + "#" + user.getDiscriminator();
	}
	
	public static void log( IUser user, IGuild guild, IUser targetUser, EnumLogType type, String log){
		log(user, guild, targetUser, type, log, null);
	}
	
	public static void log( IUser user, IGuild guild, IUser targetUser, EnumLogType type, String log, Color color )
	{
		if (channel == null) {
			channel = ChannelUtils.getChannel(DiscordBotBase.debug ? "general" : "server-logs");
		}
		
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		DateFormat formatterTime = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
		Date today = new Date();
		
		File file = FileGetter.getFile(DiscordBotBase.FilePath + "/logs/" + formatter.format(today) + ".log");
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(color != null ? color : Color.gray);
		
		builder.withTimestamp(System.currentTimeMillis());
		builder.withFooterText(type.name());
		
		String name = targetUser != null ? getName(targetUser, guild) : null;
		
		if(type != null) {
			switch (type) {
				case BAN: {
					builder.withDescription(user.mention() + " Banned " +  name + " " + (!log.isEmpty() ? "with reason \"" + log + "\"" : ""));
					builder.withColor(new Color(255, 122, 100));
					RequestBuffer.request(() -> {
						try {
							ChatUtils.sendMessage(targetUser.getOrCreatePMChannel(), " You have been banned from " + guild.getName() + " with reason \"" + log + "\"");
							ChatUtils.sendMessage(user.getOrCreatePMChannel(), " You have successfully banned " + getName(targetUser, guild) + " from " + guild.getName() + " with reason \"" + log + "\"");
							
						} catch (DiscordException e) {
							DiscordBotBase.discordBotBase.handleException(e);
						}
					});
					break;
				}
				case KICK: {
					builder.withDescription(user.mention() + " Kicked " + name + " " + (!log.isEmpty() ? "with reason \"" + log + "\"" : ""));
					builder.withColor(new Color(255, 196, 89));
					
					RequestBuffer.request(() -> {
						try {
							ChatUtils.sendMessage(targetUser.getOrCreatePMChannel(), " You have been kicked from " + guild.getName() + " with reason \"" + log + "\"");
							ChatUtils.sendMessage(user.getOrCreatePMChannel(), " You have successfully kicked " + getName(targetUser, guild) + " from " + guild.getName() + " with reason \"" + log + "\"");
							
						} catch (DiscordException e) {
							DiscordBotBase.discordBotBase.handleException(e);
						}
					});
					break;
				}
				
				case MUTE: {
					builder.withDescription(user.mention() + " Muted " + name + " for " + log);
					builder.withColor(new Color(104, 95, 255));
					break;
				}
				
				case UNMUTE: {
					builder.withDescription(name + " has now been un-muted.");
					builder.withColor(new Color(91, 87, 182));
					break;
				}
				
				case WARN: {
					builder.withDescription(user.mention() + " Warned " + name + " " + (!log.isEmpty() ? "with reason \"" + log + "\"" : ""));
					builder.withColor(new Color(110, 238, 142));
					
					RequestBuffer.request(() -> {
						try {
							ChatUtils.sendMessage(targetUser.getOrCreatePMChannel(), "You have been warned by " + user.getDisplayName(guild) + (!log.isEmpty() ? " for reason \"" + log + "\"" : "."));
							ChatUtils.sendMessage(user.getOrCreatePMChannel(), " You have successfully warned " + getName(targetUser, guild) + (!log.isEmpty() ? " for reason \"" + log + "\"" : "."));
							
						} catch (DiscordException e) {
							DiscordBotBase.discordBotBase.handleException(e);
						}
					});
					
					break;
				}
				
				case CHANNEL_BAN: {
					builder.withDescription(user.mention() + " Banned " + name + " from " + log);
					builder.withColor(new Color(163, 60, 66));
					break;
				}
				
				case CHANNEL_KICK: {
					builder.withDescription(user.mention() + " Kicked " + name + " from " + log);
					builder.withColor(new Color(188, 62, 68));
					break;
				}
				
				case OTHER: {
					builder.withDescription(log);
					break;
				}
			}
		}
		
		EmbedObject object = builder.build();
		if(user != null && guild != null) System.out.println(object.description.replace(user.mention(), user.getDisplayName(guild).replace(targetUser.mention(), targetUser.getDisplayName(guild))));
		if(user != null && guild != null) FileUtil.addLineToFile(file, "[" + formatterTime.format(today) + "] " + object.description.replace(user.mention(), user.getDisplayName(guild).replace(targetUser.mention(), targetUser.getDisplayName(guild))));
		
		if(channel != null) {
			ChatUtils.sendMessage(channel, object);
		}
	}
}
