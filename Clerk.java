import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.Random;

public class Clerk implements Runnable 
{
	//random number generator
	static Random random1 = new Random(System.currentTimeMillis());
	static Random random = new Random(random1.nextInt());
	Thread myThread;
	public static long time = System.currentTimeMillis();
	//object for Clerk to block on if necessary
	public BlockingObject blockingObject = new BlockingObject(this);
	//A vector that contains the objects that the Clerks are blocked on
	private static Vector<BlockingObject> waitingClerks = new Vector();
	private Socket s;
	
	public Clerk(String threadName, Socket s)
	{
		myThread = new Thread(this, threadName);
		myThread.start();
		this.s = s;
	}
	
	public void run() 
	{
		this.msg("I am here to help");
		try{
			OutputStream os = s.getOutputStream();
			this.msg("giving a ticket to a customer");
			//sleep for random time to simulate dealing with customer
			try {
				myThread.sleep(random.nextInt(getRandomNumber()));
			} catch (InterruptedException e2) {}
			//notify the customer
			os.write(0);
		}catch(IOException e){
			e.printStackTrace();
		}
		ClerkServer.decNumRunningClerks();
	}
	
	public static synchronized Vector<BlockingObject> getWaitingClerks()
	{
		return waitingClerks;
	}
	
	public void msg(String m) 
	{
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+ myThread.getName()+": "+m);
	}

	public  String getName() 
	{
		return myThread.getName();
	}
	
	public static synchronized int getRandomNumber()
	{
		return 1 + random.nextInt(500);
	}

}
