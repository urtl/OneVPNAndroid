package com.newonevpn.vpn.main;

public class Setting {
private boolean autoLogin;
private boolean randomPort;
private boolean tcpMode;
private boolean addRootes;
private boolean showlog;

public void setLog(boolean log){
	this.showlog = log;
}
public boolean getLog(){
	return this.showlog;
}
public void setRootEnable(boolean rootEnable){
	this.addRootes = rootEnable;
}
public boolean getRootEnable(){
	return this.addRootes;
}

public void setAutoLogin(boolean autologin){
	this.autoLogin = autologin;
}
public boolean getAutoLogin(){
	return this.autoLogin;
}

public void setRandomPort(boolean randomport){
	this.randomPort = randomport;
}
public boolean getRandomPort(){
	return this.randomPort;
}

public void setTcpMode(boolean tcpmode){
	this.tcpMode = tcpmode;
}
public boolean getTcpMode(){
	return this.tcpMode;
}

}
