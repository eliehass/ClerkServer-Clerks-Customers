import java.net.*;
import java.io.*;
import java.util.*;

public class ClerkServer 
{
	private static int numRunningClerks = 0;
	private static int maxNumClerks = 3;
	private static int numCustomers = 1;
	private static Vector<WaitingCustomers> waitingCustomers = new Vector();
	
	public static void main(String args[])
	{
		try{
			 // create a serverSocket
            ServerSocket ssock = new ServerSocket(3000);
            System.out.println("Date Time Server Running on "+
                InetAddress.getLocalHost().getHostName()+
                " on port #"+ 3000);
            
            Socket ncs = ssock.accept();
            InputStream ncsis = ncs.getInputStream();
            //get the number of customers
            numCustomers = ncsis.read();
            
            //connect to all of the customers
            for(int i = numCustomers; i > 0; i--)
            {
                // accept client connection
            	System.out.println("I am about to wait for a customer. There are " + i + " customers left");
            	
                Socket s = ssock.accept();
                // if no clients comes in Server is just sitting there
                // waiting for clients

                OutputStream os = s.getOutputStream();

                InputStream is = s.getInputStream();
                
                //if there are available clerks
                if(numRunningClerks < maxNumClerks)
                {
                	System.out.println("there is a free clerk for this customer");
                	incNumRunningClerks();
                	new Clerk("Clerk " + numRunningClerks, s);
                	//send 0 to the customer, to let them know they received a clerk
                	os.write(0);
                }
                else
                {
                	//send 1 to the customer, to let them know that there is currently no free clerk
                	os.write(1);
                	waitingCustomers.add(new WaitingCustomers(s));
                	System.out.println("A customer is waiting for a free clerk");
                }

            } // end while
            
            System.out.println("waiting for the last customer to leave");
            
            System.out.println("the last customer has left");
            System.out.println("it's closing time");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static synchronized void incNumRunningClerks()
	{
		numRunningClerks++;
	}
	
	public static synchronized void setNumCustomers(int number)
	{
		numCustomers = number;
	}
	
	//when the number of running clerks is decremented, the newly free clerk immediatly checks to see if there are any waiting customers.
	public static synchronized void decNumRunningClerks()
	{
		System.out.println("A clerk is now free, there are " + numCustomers + " customers left");
		numRunningClerks--;
		numCustomers--;
		if(!waitingCustomers.isEmpty())
		{
			System.out.println("This free clerk will now help a customer that was waiting");
			incNumRunningClerks();
			WaitingCustomers customer = waitingCustomers.elementAt(0);
			waitingCustomers.remove(0);
			Socket s = customer.getSocket();
			try{
			OutputStream os = s.getOutputStream();
        	new Clerk("Clerk " + numRunningClerks, s);
        	//send 0 to the customer, to let them know they received a clerk
        	os.write(0);
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static Vector<WaitingCustomers> getWaitingCustomers()
	{
		return waitingCustomers;
	}
	
	private static synchronized int getNumCustomers()
	{
		return numCustomers;
	}
}
