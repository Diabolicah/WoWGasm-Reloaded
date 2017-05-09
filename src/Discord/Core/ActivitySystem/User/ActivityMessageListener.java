package Discord.Core.ActivitySystem.User;

import DiscordBotCode.Main.DiscordBotBase;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class ActivityMessageListener implements IListener<MessageReceivedEvent>{
	@Override
	public void handle( MessageReceivedEvent event )
	{
		IMessage message = event.getMessage();
		
		if(message.getChannel().isPrivate() || (!message.getGuild().getID().equals("259028003877552139") && !DiscordBotBase.debug)){
			return;
		}
		
		UserActivity.object.messagePoint(message);
	}
}
