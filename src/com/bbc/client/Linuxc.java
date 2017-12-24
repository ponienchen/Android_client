package com.bbc.client;

public class Linuxc{
	public static native String conn(String ip, int port);
	
	//note that this String must be converted to char string first while in the native c code.  
	public static native void send(int servFD, String msg);
	
	public static native String recv(int servFD);
	
	
	public static native  int opengpp();
	



	public static native  int closegpp();
	

	//send signal to GPP8
	public static native void sendSignal(int selection, int on_off);

	
	
	
	
	

	static{
		System.loadLibrary("client_conn");
	}
}
