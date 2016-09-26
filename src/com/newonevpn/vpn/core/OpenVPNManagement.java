package com.newonevpn.vpn.core;

public interface OpenVPNManagement {
    enum pauseReason {
        noNetwork,
        userPause,
        screenOff
    }
    
	int mBytecountInterval =2;

	void reconnect();

	void pause(pauseReason reason);

	void resume();

	boolean stopVPN();
	boolean IsRunThread();
	boolean IsConnected();
	
	void setConnected(boolean bVal);

}
