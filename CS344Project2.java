import java.io.IOException;
import java.io.OutputStream;
import java.net.*;


public class CS344Project2 
{
	private static int numInitCustomers;
	private static int numCustomers;
	private static int numTables;
	private static int seatsPerTable;
	private static Socket s;
	
	public static void main(String args[])
	{
		//try to read in command line arguments
		try{
			numInitCustomers = new Integer(args[0]).intValue();
			numTables = new Integer(args[2]).intValue();
			seatsPerTable = new Integer(args[3]).intValue();
			numCustomers = numInitCustomers;
			//if there are no command line arguments, or not enough, just use default values.
		}catch(Exception e){
			numInitCustomers = 14;
			numCustomers = numInitCustomers;
			numTables = 5;
			seatsPerTable = 3;
		}
		
		try {
			s = new Socket(InetAddress.getLocalHost(), 3000);
			OutputStream os = s.getOutputStream();
			//let the server know how many customers there are
			os.write(numCustomers);
			//start all of the customers
			for(int i = 0; i < numInitCustomers; i++)
			{
				new Customer("Customer " + i, s);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	//return the amount of customers
	public synchronized static int getNumCustomers()
	{
		return numCustomers;
	}
	
	//decrement customers by 1
	public synchronized static void decNumCustomers()
	{
		numCustomers--;
		if(numCustomers == 0)
		{
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//return the amount of tables
	public synchronized static int getNumTables()
	{
		return numTables;
	}
	
	//return the amount of seats per table
	public synchronized static int getSeatsPerTable()
	{
		return seatsPerTable;
	}
	
	//decrement tables by 1
	public synchronized static void decNumTables()
	{
		numTables--;
	}
	
	//increment tables by 1
	public synchronized static void incNumTables()
	{
		numTables++;
	}
	
	//return the initial number of customers
	public synchronized static int getNumInitCustomers()
	{
		return numInitCustomers;
	}
}
