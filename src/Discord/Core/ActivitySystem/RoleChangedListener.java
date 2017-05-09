package Discord.Core.ActivitySystem;

import Discord.Core.ActivitySystem.Staff.StaffActivity;
import Discord.Core.ActivitySystem.User.UserActivity;
import Discord.Core.MainDiscordBot;
import DiscordBotCode.Extra.FileUtil;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.UserRoleUpdateEvent;
import sx.blah.discord.handle.obj.IRole;

public class RoleChangedListener implements IListener<UserRoleUpdateEvent>
{
	@Override
	public void handle( UserRoleUpdateEvent event )
	{
		if(!event.getGuild().getID().equals(MainDiscordBot.guild.getID())){
			return;
		}
		
		boolean isPromotion = false;
		
		for(IRole oldRole : event.getOldRoles()){
			for(IRole newRole : event.getNewRoles()){
				if(newRole.getPosition() > oldRole.getPosition()){
					if(newRole.getPermissions().containsAll(oldRole.getPermissions()) || newRole.getPermissions().size() >= oldRole.getPermissions().size()){
						isPromotion = true;
						break;
					}
				}
			}
		}
		
		if(isPromotion) {
			if (UserActivity.object.validUser(event.getUser())) {
				UserActivity.lastRoleUpdate.put(event.getUser().getID(), System.currentTimeMillis());
				
				FileUtil.removeLineFromFile(UserActivity.roleUpdateFile, event.getUser().getID());
				FileUtil.addLineToFile(UserActivity.roleUpdateFile, event.getUser().getID() + "=" + System.currentTimeMillis());
			}
			
			if (StaffActivity.object.validUser(event.getUser())) {
				StaffActivity.lastRoleUpdate.put(event.getUser().getID(), System.currentTimeMillis());
				
				FileUtil.removeLineFromFile(StaffActivity.roleUpdateFile, event.getUser().getID());
				FileUtil.addLineToFile(StaffActivity.roleUpdateFile, event.getUser().getID() + "=" + System.currentTimeMillis());
			}
		}
	}
}
