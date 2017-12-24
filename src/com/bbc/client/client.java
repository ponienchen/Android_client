package com.bbc.client;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class client extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        findViews();
        
        setListeners();
        
        //irda_buzzer_fd = Linuxc.opengpp();
        //if (irda_buzzer_fd < 0)
        //{
        	//Toast.makeText(client.this, "(client side) irda_buzzer fd not acquired!", Toast.LENGTH_LONG).show();
        	//finish();
        //}
        
        
       
    }
    
    
 
    protected static final int CONNECT=Menu.FIRST;
    protected static final int DISCONNECT=Menu.FIRST+1;



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, CONNECT, 0, "Connect");
		menu.add(0, DISCONNECT, 0, "Disconnect");
		return super.onCreateOptionsMenu(menu);
	}

	
	private Dialog myDialog;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		
		switch(item.getItemId())
		{
		case CONNECT:
			myDialog = onCreateDialog(R.layout.socket);
			myDialog.show();
			

			String ip_addr = settings.getString(PREF_IP, "");
			String port = settings.getString(PREF_PORT, "");
			((EditText)myDialog.findViewById(R.id.ip_addr)).setText(ip_addr);
			((EditText)myDialog.findViewById(R.id.portno)).setText(port);
			break;
		case DISCONNECT:
			
			if (local_timer != null)
			{
				Toast.makeText(client.this, "Please STOP the DEMO first!", Toast.LENGTH_SHORT).show();
				break;
			}
			
			//if I invoke stop_command() here, note that the three lines of codes below will get 
			//executed faster than the stop_service( invoked by the stop_command();
			settings.edit()
			.putString(PREF_SERVER_SOCKET, "")
			.commit();
			
			
			rcv_msg.setText("");
			//Linuxc.sendSignal(4, 2);
			//Linuxc.closegpp();
			
			
			//finish();
			break;
		
		}
	
		return super.onOptionsItemSelected(item);
	}
    
    
private LayoutInflater dialog_layout;
    
    
    @Override
	protected Dialog onCreateDialog(int id) {
    	
    	dialog_layout = LayoutInflater.from(this);
        final View textEntry = dialog_layout.inflate(R.layout.socket, null);
    	
    	
    	
		return new AlertDialog.Builder(client.this)
		.setIcon(android.R.drawable.alert_dark_frame)
		.setTitle("Establishing a connection!")
		.setView(textEntry)
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
			
				if (((EditText)myDialog.findViewById(R.id.ip_addr)).getText().length() == 0 ||
						((EditText)myDialog.findViewById(R.id.portno)).getText().length() == 0)
				{
					Toast.makeText(client.this, "Invalid ip and port!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				SharedPreferences settings = getSharedPreferences(PREF, 0);
				String servFD_STR = settings.getString(PREF_SERVER_SOCKET, "");
				if (!servFD_STR.equals(""))
				{
					Toast.makeText(client.this, "server fd already acquired!", Toast.LENGTH_SHORT).show();
					
					settings.edit()
					.putString(PREF_IP, ((EditText)myDialog.findViewById(R.id.ip_addr)).getText().toString())
					.putString(PREF_PORT, ((EditText)myDialog.findViewById(R.id.portno)).getText().toString())
					.commit();
					
					return;

				} 
		
	
				settings.edit()
				.putString(PREF_IP, ((EditText)myDialog.findViewById(R.id.ip_addr)).getText().toString())
				.putString(PREF_PORT, ((EditText)myDialog.findViewById(R.id.portno)).getText().toString())
				.commit();
	
				
				
				
			
				
				
				mService = new myservice(client.this, mHandler);
				mService.start();
				
				
				L_service = new listen_service(client.this, mHandler_1);
				
				
				
				
								
				
				
			}
			
		})
		.setNeutralButton("CANCEL", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				SharedPreferences settings = getSharedPreferences(PREF, 0);

				settings.edit()
				.putString(PREF_IP, ((EditText)myDialog.findViewById(R.id.ip_addr)).getText().toString())
				.putString(PREF_PORT, ((EditText)myDialog.findViewById(R.id.portno)).getText().toString())
				.commit();
				
				
			}
			
		})
		.create();
	}

	
	
	

    int irda_buzzer_fd;
    
    private Button up; 
    private Button down;
    private Button left;
    private Button right;
    private Button stop;
    private Button demo;
    
    private EditText rcv_msg;
    
    public static final int MESSAGE_SERVFD = 2;
    public static final int MESSAGE_RECEIVED = 3;
    
    private void findViews()
    {
    	
    	up = (Button)   findViewById(R.id.up);
    	down = (Button)findViewById(R.id.down);
    	left = (Button) findViewById(R.id.left);
    	right = (Button) findViewById(R.id.right);
    	stop = (Button)findViewById(R.id.stop);
    	rcv_msg = (EditText) findViewById(R.id.rcv_msg);
    	demo = (Button)findViewById(R.id.demo);
    }
    
    private void setListeners()
    {
    	up.setOnClickListener(up_handler);
    	down.setOnClickListener(down_handler);
    	left.setOnClickListener(left_handler);
    	right.setOnClickListener(right_handler);
    	stop.setOnClickListener(stop_handler);
    	demo.setOnClickListener(demo_handler);
    }
    
    Timer local_timer = null;
    TimerTask local_task = null;
    
    private Button.OnClickListener demo_handler = new Button.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (local_timer != null)
			{
				Toast.makeText(client.this, "DEMO NOT DONE YET!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			//reset the value of corner_count to 0
			corner_count = 1;
			
			//start going forward after 0.5 seconds!
			go_forward(500);
			
			
		}
    	
    };
    
    private int corner_count;
    
    
    //go forward after "seconds" seconds
    private void go_forward(int seconds)
	{
    	
    	
    	
	    local_timer = new Timer();
		local_task = new TimerTask(){
	
		
			@Override
			public void run() {
				// TODO Auto-generated method stub
	
				if (corner_count == 5)
		    	{
		    		local_timer.cancel();
		    		local_timer = null;
		    		
		    		turn_timer.cancel();
		    		turn_timer = null;
		    		
		    		stop_command();
		    	
		    		return;
		    	}
		    	
				corner_count++;
				
			
				up_command();	
			
				//starting turning right after 5 seconds!
				turn_right(5000);
					
			}
			
		};
		
		local_timer.schedule(local_task, seconds);
	}
    
    
    
    
    
    Timer turn_timer = null;
    TimerTask turn_task = null;
    //start turning after "seconds" seconds
    private void turn_right(int seconds)
    {
    
    	
    	turn_timer = new Timer();
    	turn_task = new TimerTask()
    	{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				right_command();
				
				//start going forward after 3 seconds!
				go_forward(3000);
			}
    		
    	};
    
    	
    
    	turn_timer.schedule(turn_task, seconds);
    	
    	
    }
    
    
    
    private delivery_service up_service;
    private delivery_service down_service;
    private delivery_service left_service;
    private delivery_service right_service;
    private delivery_service stop_service;
    
    private void up_command()
    {
    	SharedPreferences settings = getSharedPreferences(PREF,0);
		settings.edit()
		.putString(PREF_MSG, "AA")
		.commit();
		
		
		up_service =  new delivery_service(client.this);
		up_service.start();
    }
    
    
    private Button.OnClickListener up_handler = new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			up_command();
			
			
		}
    	
    };
    
    
    private void down_command()
    {
    	SharedPreferences settings = getSharedPreferences(PREF,0);
		settings.edit()
		.putString(PREF_MSG, "BB")
		.commit();
		
		
		down_service =  new delivery_service(client.this);
		down_service.start();
    }
    
    
    private Button.OnClickListener down_handler = new Button.OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			down_command();
			
			
		}
    	
    };
    
    	
    private myservice mService;
    
    
    private void left_command()
    {
    	SharedPreferences settings = getSharedPreferences(PREF,0);
		settings.edit()
		.putString(PREF_MSG, "AB")
		.commit();
		
		
		left_service =  new delivery_service(client.this);
		left_service.start();
    }
    
    
    private Button.OnClickListener left_handler = new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			left_command();
			
			
		}
    };
	
	
    private void right_command()
    {
    	SharedPreferences settings = getSharedPreferences(PREF,0);
		settings.edit()
		.putString(PREF_MSG, "BA")
		.commit();
		
		
		right_service =  new delivery_service(client.this);
		right_service.start();
    }
    
    private Button.OnClickListener right_handler = new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			right_command();
			
			
		}
    };
    
    private void stop_command()
    {
    	if (local_timer != null)
    	{
    		local_timer.cancel();
    		local_timer = null;
    	}
    	
    	if (turn_timer != null)
    	{
    		
    		turn_timer.cancel();
    		turn_timer = null;
    		
    	}
    	
    	SharedPreferences settings = getSharedPreferences(PREF,0);
		settings.edit()
		.putString(PREF_MSG, "CC")
		.commit();
		
		
		stop_service =  new delivery_service(client.this);
		stop_service.start();
    }

    private Button.OnClickListener stop_handler = new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			stop_command();
			
		}
    	
    };
    
    
    //for retrieving the servFD
    private final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case MESSAGE_SERVFD:
				SharedPreferences settings = client.this.getSharedPreferences(PREF, 0);
				String text = (String) msg.obj;	
				
				Pattern p = Pattern.compile("\\d+"); 
				Matcher m = p.matcher(text);
				
				String servfd_STR = "";
				
				while(m.find())
				{
					servfd_STR = m.group();
					
				}
				
				if (servfd_STR.equals(""))
				{
					Toast.makeText(client.this, "servfd not acquired!", Toast.LENGTH_SHORT).show();
					return;
				}		
				
				rcv_msg.setText("connection established!");
				rcv_msg.append("\n");
				
				
				settings.edit()
				.putString(PREF_SERVER_SOCKET, servfd_STR)
				.commit();
				
				
				break;
				
			default:
				break;
			
			
			
			}
			
			
			
			L_service.start();
			super.handleMessage(msg);
		}
    	
    	
    	
    	
    	
    };
    
    
    private listen_service L_service;
    
    
    
  //for retrieving the servFD
    private final Handler mHandler_1 = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//Toast.makeText(client.this, "received message: " + (String)msg.obj, Toast.LENGTH_SHORT).show();
			String text = (String) msg.obj;
			
			if (text.equals(""))
				return;
			
			if (text.equals("stop_demo"))
			{
				stop_command();
			}
			
			/*
			if (text.equals("1"))
			{
				//send signale "on" to GPP8
				Linuxc.sendSignal(4, 1);
				rcv_msg.append(text);
				
				return;
			}
			else if (text.equals("0"))
			{
				
				//send signal "off" to GPP8
				Linuxc.sendSignal(4, 2);
				rcv_msg.append(text);
				
				return;
			}
			*/
			
			switch(msg.what)
			{
			case MESSAGE_RECEIVED:
				
				rcv_msg.append((String) msg.obj);
				rcv_msg.append("\n");
				rcv_msg.setSelection(rcv_msg.getText().length());
				
				break;
			default:
				break;
			
			
			
			}
			
			super.handleMessage(msg);
		}
    	
    	
    	
    	
    	
    };
    
    
    

    public static final String PREF = "IP_PORT";
    public static final String PREF_IP = "IP_ADDRESS";
    public static final String PREF_PORT = "PORT_NO";
    public static final String PREF_MSG = "msg";
    public static final String PREF_SERVER_SOCKET = "servFD";
    public static final String PREF_RCV = "rcv";

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		
		/*
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		settings.edit()
		.putString(PREF_IP, ((EditText)myDialog.findViewById(R.id.ip_addr)).getText().toString())
		.putString(PREF_PORT, ((EditText)myDialog.findViewById(R.id.portno)).getText().toString())
		.commit();
		*/
		
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		/*
		SharedPreferences settings = getSharedPreferences(PREF,0);
		String ip = settings.getString(PREF_IP, "");
		String port = settings.getString(PREF_PORT, "");
		if ( !ip.equals("") && !port.equals(""))
		{
			((EditText)myDialog.findViewById(R.id.ip_addr)).setText(ip);
			((EditText)myDialog.findViewById(R.id.portno)).setText(port);
		}
		
		*/
		
		
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		
		super.onDestroy();
	};
    
    
    
}




