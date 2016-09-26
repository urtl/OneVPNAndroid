package com.dave.newonevpn.model;

import java.util.ArrayList;

/**
 * Created by Administrator on 7/6/2016.
 */
public class DNSItem {
    public String name;
    public String dns;
    public String country;
    public boolean bSelected = false;
    public ArrayList<ProtocolItem> protocols = new ArrayList<>();
}
