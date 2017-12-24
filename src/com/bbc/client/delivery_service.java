package com.bbc.client;



import com.bbc.client.myservice.myThread_1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class delivery_service{
	
	
	public static final String PREF = "IP_PORT";
	public static final String PREF_MSG = "msg";
    public static final String PREF_SERVER_SOCKET = "servFD";
	
	
    private final Context context;
	 private myThread_1 myT_1;
	 
	
	
	public delivery_service(Context context)
	{
		this.context = context;
		
	}
	
	public synchronized void start()
	{
		myT_1 = new myThread_1();
		myT_1.start();
			
	}
	
	
	public class myThread_1 extends Thread{
		
		 public myThread_1()
		 {
			 
		 }
		
		public void run()
		{
			SharedPreferences settings = context.getSharedPreferences(PREF,0);
			
			String servFD_STR = settings.getString(PREF_SERVER_SOCKET, "");
			if (servFD_STR.equals(""))
			{
				Log.e("client delivery service", "servFD not available!");
				return;
			}
			
			int servFD = Integer.parseInt(servFD_STR);
			
			String msg = settings.getString(PREF_MSG, "");
			
			
			Linuxc.send(servFD, msg);
			
		}
		
		
	}
	
}





