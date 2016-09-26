package com.newonevpn.vpn.main;

public class User {
private String groupName;
private String expire;
private String bandwidth;
private String bandwidthlimit;
private String version;
private String postedUser;
private String userName;
private String password;
private String haveLicense;

public void setHaveLicense(String license){
	this.haveLicense = license;
}
public String getHaveLicense(){
	return this.haveLicense;
}

public void setPassword(String password){
	this.password = password;
}
public String getPassword(){
	return this.password;
}

public void setUserName(String userName){
	this.userName = userName;
}
public String getUserName(){
	return this.userName;
}

public void setGroupName(String groupName){
	this.groupName = groupName;
}
public String getGroupName(){
	return this.groupName;
}

public void setExpire(String expire){
	this.expire = expire;
}
public String getExpire(){
	return this.expire;
}

public void setBandWidth(String bandWidth){
	this.bandwidth = bandWidth;
}
public String getBandWidth(){
	return this.bandwidth;
}

public void setBandWidthLimit(String bandWidthLimit){
	this.bandwidthlimit = bandWidthLimit;
}
public String getBandWidthLimit(){
	return this.bandwidthlimit;
}

public void setVersion(String version){
	this.version = version;
}
public String getVersion(){
	return this.version;
}

public void setPostedUser(String postedUser){
	this.postedUser = postedUser;
}
public String getPostedUser(){
	return this.postedUser;
}
}
