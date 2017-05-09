package Discord.Core;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelUtils
{
	private static ConcurrentHashMap<String, IChannel> channels = new ConcurrentHashMap<>();
	
	public static void loadChannels( IGuild guild){
		for(IChannel channel : guild.getChannels()){
			channels.put(channel.getName().toLowerCase(), channel);
		}
	}
	
	public static IChannel getChannel(String channelName){
		return channels.get(channelName.toLowerCase());
	}
}
