package com.dave.newonevpn.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.dave.newonevpn.util.DBManager;

import java.util.ArrayList;

/**
 * Created by Administrator on 11/20/2015.
 */
public class Global {
    //public static String SERVER_URL = "http://192.168.1.115:82/openvpn/webservice/";

    public static String SERVER_URL = "http://webservice.onevpn.com/onevpn/webservice/";
    public static ArrayList<ServerItem> g_allServerLists = new ArrayList<>();
    public static ArrayList<DNSItem> g_allDNSLists = new ArrayList<>();
    public static ServerItem g_selectedServerInfo = new ServerItem();
    public static DNSItem g_selectedDNSInfo = null;
    public static String g_userEmail = null;
    public static String g_userPassword = null;

    public static SharedPreferences.Editor ed;
    public static SharedPreferences sp;

    public static DBManager dbManager;
    public static ArrayList<ServerInfo> g_server;
    public static ArrayList<ServerListInfo> g_searchedList;


    public static String deviceId;
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    public static void alertDialog(Context context, String msg){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle("Alert");
        // set dialog message
        alertDialogBuilder.setMessage(msg).setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        dialog.dismiss();
                        // current activity
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
