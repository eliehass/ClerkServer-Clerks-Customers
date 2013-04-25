import java.net.Socket;

//this class keeps track of the different waiting customers and the socket that they are waiting on
public class WaitingCustomers 
{
	private Socket s;
	public WaitingCustomers(Socket s)
	{
		this.s = s;
	}
	
	public Socket getSocket()
	{
		return s;
	}
}
