package Discord.Core.Events;

import Discord.Core.MainDiscordBot;
import DiscordBotCode.Main.ChatUtils;
import DiscordBotCode.Main.DiscordBotBase;
import org.junit.Before;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;

public class UserJoin implements IListener<UserJoinEvent>
{
	private static final String prefaceText = "Welcome to WoWGasm Reloaded {user}! Please visit http://wowgasm-reloaded.org/ to create your in-game account and to download our 3.3.5a client! Be sure to check out our <#311313590311321600> and enjoy your stay!";

	public UserJoin() {

	}
	
	@Override
	public void handle( UserJoinEvent event )
	{
		RequestBuffer.request(() -> {
			try {
				event.getUser().addRole(MainDiscordBot.defaultRole);
			} catch (MissingPermissionsException | DiscordException e) {
				DiscordBotBase.discordBotBase.handleException(e);
			}

			ChatUtils.sendMessage(event.getGuild().getChannelByID("309224369085743114"), prefaceText.replace("{user}", event.getUser().mention()));
		});
		
	}
}
