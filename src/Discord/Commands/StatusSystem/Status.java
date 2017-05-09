package Discord.Commands.StatusSystem;

import java.util.Date;

public class Status
{
	public Status( String reason, Date returnDate )
	{
		this.reason = reason;
		this.returnDate = returnDate;
	}
	
	private String reason;
	private Date returnDate;
	
	public String getReason()
	{
		return reason;
	}
	
	public Date getReturnDate()
	{
		return returnDate;
	}
	
	@Override
	public String toString()
	{
		return "Status{" + "reason='" + reason + '\'' + ", returnDate=" + returnDate.getTime() + '}';
	}
}
