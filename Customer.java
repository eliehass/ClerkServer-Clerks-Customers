import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.Random;

public class Customer implements Runnable, Serializable
{
	//random number generator
	static Random random1 = new Random(System.currentTimeMillis());
	static Random random = new Random(random1.nextInt());
 	Thread myThread;
	public static long time = System.currentTimeMillis();
	//this Vector will hold all of the objects that waiting customers are blocked on
	private static Vector<BlockingObject> waitingCustomers = new Vector();
	//an object for a customer to block on if necessary
	public BlockingObject blockingObject = new BlockingObject(this);
	//keeps track of how many customers are in or waiting for a group
	private static int group = 0;
	//gets the initial number of customers
	private static int totalNumCustomers = CS344Project2.getNumInitCustomers();
	//gets the number of seats per table
	private static int seatsPerTable = CS344Project2.getSeatsPerTable();
	//object to implement mutual exclusion while looking for group
	private static Object blockForGroup = new Object();
	//object to implement mutual exclusion wile looking for a table
	private static Object blockForTable = new Object();
	
	//Constructor
	public Customer(String threadName, Socket s)
	{
		myThread = new Thread(this, threadName);
		myThread.start();
	}
	
	public void run()
	{
		//sleep for random time before entering
		try {
			myThread.sleep(random.nextInt(getRandomNumber()));
		} catch (InterruptedException e1) {}
		this.msg("looking for an item");
		//sleep random time while looking for an item
		try {
			myThread.sleep(random.nextInt(getRandomNumber()));
		} catch (InterruptedException e1) {}
		this.msg("I found an item");
		//object to be synchronized over
		synchronized (blockingObject) {
			//notify a clerk
			this.notifyClerk();
		}
		this.msg("I have recieved my ticket. Now I want to eat");
		//join a group and get a table
		this.getTable();
		this.msg("I have left");
		//decrement the amount of customers to show that you have left
		CS344Project2.decNumCustomers();
	}
	
	//takes a table for a group to eat at
	private void getTable()
	{
		//mutual exclusion
		synchronized(blockForTable)
		{
			//if there are no free tables, block
			while(CS344Project2.getNumTables() == 0)
			{
				while(true)
				{
					try {blockForTable.wait(); break;}
					catch(InterruptedException e) { continue; }
				}
			}
		}
		
		//mutual exclusion
		synchronized(blockForGroup)
		{
			group++;
			//the last customer always forms a group, even if his group is less than the specified group size
			if(group == totalNumCustomers)
			{
				CS344Project2.decNumTables();
				this.msg("I am in a group and we are eating");
			}
			//if there are not yet 3 customers in the group, block
			else if(group % seatsPerTable != 0 && CS344Project2.getNumCustomers() >= seatsPerTable)
			{
				this.msg("I am waiting for a group to eat");
				while(true)
				{
					try {blockForGroup.wait(); break;}
					catch(InterruptedException e) { continue; }
				}
				//once this customer is notified, it simply returns. Everything that needs to be done will be handled by the thread that
				//formed the group and notified the other 2.
				return;
			}
			//if the group now has three customers, take a table and eat
			else if ((group % seatsPerTable == 0 && CS344Project2.getNumCustomers() >= seatsPerTable))
			{
				CS344Project2.decNumTables();
				this.msg("I am in a group and we are eating");
			}
			//this statement catches the situation where there are less than 3 customers left, but a group should not be formed yet because
			//this customer is not the last customer.
			else
			{
				this.msg("I am waiting for a group to eat");
				while(true)
				{
					try {blockForGroup.wait(); break;}
					catch(InterruptedException e) { continue; }
				}
				return;
			}
		}
		
		//sleep for random time to simulate eating
		try {
			myThread.sleep(random.nextInt(getRandomNumber()));
		} catch (InterruptedException e1) {}
		
		//mutual exclusion
		synchronized(blockForGroup)
		{
			this.msg("we are done eating and are leaving");
			//notify the other two members of your group
			blockForGroup.notify();
			blockForGroup.notify();
			//return your table to the pool of available tables
			CS344Project2.incNumTables();
		}
		
		//mutual exclusion
		synchronized(blockForTable)
		{
			//notify everyone who is waiting for a table. Let them all fight out who gets it.
			blockForTable.notifyAll();
		}
	}
	
	private synchronized void notifyClerk() 
	{
		//connect to the ClerkServer
		try{
			Socket s = new Socket(InetAddress.getLocalHost(), 3000);
			InputStream is = s.getInputStream();
			OutputStream os = s.getOutputStream();
			int status = is.read();
			if(status == 0)
			{
				//this will block the customer until the clerk is done with him
				int block = is.read();
			}
			else if(status == 1)
			{
				this.msg("No clerks are free so I will wait");
				int block = is.read();
				this.notifyClerk(s);
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private synchronized void notifyClerk(Socket s)
	{
		//connect to the ClerkServer
		try{
			InputStream is = s.getInputStream();
			OutputStream os = s.getOutputStream();
			//find out if there is a free clerk
			int status = is.read();
			if(status == 0)
			{
				//this will block the customer until the clerk is done with him
				int block = is.read();
			}
			else if(status == 1)
			{
				this.msg("No clerks are free so I will wait");
				int block = is.read();
				this.notifyClerk(s);
			}
					
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static synchronized int getNumWaitingCustomers()
	{
		return waitingCustomers.size();
	}
	
	public  void msg(String m) 
	{
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+ myThread.getName()+": "+m);
	}

	public synchronized static Vector<BlockingObject> getWaitingCustomers() 
	{
		return waitingCustomers;
	}
	
	
	public String getName()
	{
		return myThread.getName();
	}
	
	public BlockingObject getBlockingObject()
	{
		return blockingObject;
	}
	
	public static synchronized int getRandomNumber()
	{
		return 1 + random.nextInt(500);
	}
	
}
