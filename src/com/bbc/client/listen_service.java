package com.bbc.client;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class listen_service{
	
	public static final String PREF = "IP_PORT";
	public static final String PREF_RCV = "rcv";
    public static final String PREF_SERVER_SOCKET = "servFD";
	
    private final Handler mHandler_2;
	 private final Context context;
	 private myThread_2 myT_2;
	 
	 
    
    public listen_service(Context context, Handler handler)
	 {
		 this.context = context;
		 mHandler_2 = handler; 
	 }
	 
	
	 
	public synchronized void start()
	{
		myT_2 = new myThread_2();
		myT_2.start();
			
	}
	

    
   public class myThread_2 extends Thread{
	 
	   public myThread_2()
		{
			
		}
	   
	   
	   public void run(){
		   
		     String received_msg;
			 SharedPreferences settings = context.getSharedPreferences(PREF, 0);
			 
			 String servfd = settings.getString(PREF_SERVER_SOCKET, "");
			 
			 
			 while(true)
			 {
				
				 //Log.e("client listen service", "servfd = " + servfd);
				 
				 received_msg = Linuxc.recv(Integer.valueOf(servfd));
				 
				 
				 //Log.e("client listen service", "received message: " + received_msg);
				 
				 if (received_msg.equals("(client side) failed to receive") || 
						 received_msg.equals(""))
					 continue;
				 
				 mHandler_2.obtainMessage(client.MESSAGE_RECEIVED, 0, 0, received_msg).sendToTarget();
				
				 
			 }
		   
		   
	   }
	   
	   
	   
   };
	
	
	
}
