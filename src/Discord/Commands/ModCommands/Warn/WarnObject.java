package Discord.Commands.ModCommands.Warn;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.Date;

public class WarnObject
{
	public IUser user;
	public String userId;
	
	public IUser admin;
	public String adminId;
	
	public IGuild guild;
	public IChannel channel;
	
	public String reason;
	
	public int id;
	
	public Date time;
	
	public WarnObject( IUser user, IUser admin, IGuild guild, IChannel channel, String reason, Date time, int id )
	{
		this.guild = guild;
		this.user = user;
		this.admin = admin;
		this.channel = channel;
		this.reason = reason;
		this.time = time;
		this.id = id;
		
		userId = user.getID();
		adminId = admin.getID();
	}
	
	public WarnObject( String userId, String adminId, IGuild guild, IChannel channel, String reason, Date time, int id )
	{
		this.guild = guild;
		this.userId = userId;
		this.adminId = adminId;
		this.channel = channel;
		this.reason = reason;
		this.time = time;
		this.id = id;
	}
}
