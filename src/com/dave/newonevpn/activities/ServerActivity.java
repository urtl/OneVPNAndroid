package com.dave.newonevpn.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.dave.onevpnfresh.R;
import com.dave.newonevpn.adapters.ChooseServerListAdapter;
import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.newonevpn.vpn.core.OpenVpnService;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by gustaf on 11/2/15.
 */
public class ServerActivity extends Activity implements View.OnClickListener {


    private CommonAsyncTask mAsyncTask = null;
    private JSONArray objResArray;
    private JSONObject objRes;
    private String strRes;

    ImageView imgMenu;
    ListView listServers;

    ChooseServerListAdapter adapter;

    PopupWindow popupMenu, popupExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_server);

        imgMenu = (ImageView)findViewById(R.id.imgMenu);
        listServers = (ListView)findViewById(R.id.listServers);

        imgMenu.setOnClickListener(this);

        adapter = new ChooseServerListAdapter(this, Global.g_allDNSLists);
        listServers.setAdapter(adapter);

        listServers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < Global.g_allDNSLists.size(); i++) {
                    Global.g_allDNSLists.get(i).bSelected = false;
                }
                Global.g_allDNSLists.get(position).bSelected = true;

                adapter.notifyDataSetChanged();
                Global.g_selectedServerInfo.name = Global.g_allDNSLists.get(position).name;
                Global.g_selectedServerInfo.dns = Global.g_allDNSLists.get(position).dns;
                Global.g_selectedServerInfo.country = Global.g_allDNSLists.get(position).country;
                Global.g_selectedServerInfo.city = Global.g_allDNSLists.get(position).city;
                Global.g_selectedServerInfo.protocol = Global.g_allDNSLists.get(position).protocols.get(0).protocol;
                Global.g_selectedServerInfo.port = Global.g_allDNSLists.get(position).protocols.get(0).ports.get(0).port;
                Global.g_selectedServerInfo.id = Global.g_allDNSLists.get(position).protocols.get(0).ports.get(0).id;

                Global.ed.putString("selectedid", Global.g_selectedServerInfo.id);
                Global.ed.putString("serverdns", Global.g_selectedServerInfo.dns);
                Global.ed.putString("servercountry", Global.g_selectedServerInfo.country);
                Global.ed.putString("servername", Global.g_selectedServerInfo.name);
                Global.ed.putString("serverprotocol", Global.g_selectedServerInfo.protocol);
                Global.ed.putString("servercity", Global.g_selectedServerInfo.city);
                Global.ed.putString("serverport", Global.g_selectedServerInfo.port);
                Global.ed.commit();

                Global.g_selectedDNSInfo = Global.g_allDNSLists.get(position);
                Intent intent = new Intent(ServerActivity.this, ConnectStatusActivity.class);
                intent.putExtra("Selected_Server_Position",position);
                startActivityForResult(intent, 1);
                //showLoginPopup();
            }
        });

        Global.deviceId = getDeviceInformation();
    }



    @Override
    public void onClick(View v) {
        if( v == imgMenu ){
            showMenuPopup();
        }
    }

    public void showMenuPopup(){
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_menu, null);
        LinearLayout linearMenuUniqueID = (LinearLayout)parent.findViewById(R.id.linearMenuUniqueID);
        linearMenuUniqueID.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      popupMenu.dismiss();
                                                      Global.alertDialog(ServerActivity.this, Global.deviceId);
                                                  }
                                              }
        );
        LinearLayout linearChooseServer  = (LinearLayout)parent.findViewById(R.id.linearMenuChooseServer);
        LinearLayout linearAbout = (LinearLayout)parent.findViewById(R.id.linearMenuAbout);
        LinearLayout linearProfile = (LinearLayout)parent.findViewById(R.id.linearMenuProfile);
        LinearLayout linearFAQ = (LinearLayout)parent.findViewById(R.id.linearMenuFAQ);
        LinearLayout linearShare = (LinearLayout)parent.findViewById(R.id.linearMenuShare);
        LinearLayout linearUpgrade = (LinearLayout)parent.findViewById(R.id.linearMenuUpgrade);
        LinearLayout linearSetting = (LinearLayout)parent.findViewById(R.id.linearMenuSetting);
        LinearLayout linearExit = (LinearLayout)parent.findViewById(R.id.linearMenuExit);

        linearChooseServer.setVisibility(View.GONE);
        linearSetting.setVisibility(View.GONE);


        linearAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerActivity.this, AboutActivity.class);
                startActivityForResult(intent, 0);
                popupMenu.dismiss();
            }
        });
        linearProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://profile.onevpn.com"));
                startActivity(i);
            }
        });
        linearFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://faqs.onevpn.com"));
                startActivity(i);
            }
        });
        linearShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                String shareText = "http://www.facebook.com/TheOneVPN/?fref=ts";

                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_SUBJECT, "OneVPN");
                share.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(share, "Share link!"));
            }
        });
        linearUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://upgrade.onevpn.com"));
                startActivity(i);
            }
        });
        linearExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                showExitPopup();
            }
        });

        popupMenu = new PopupWindow(parent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
        popupMenu.setOutsideTouchable(true);
        popupMenu.setTouchable(true);
        popupMenu.setBackgroundDrawable(new BitmapDrawable());
        popupMenu.setAnimationStyle(R.style.PopupAnimation);
        popupMenu.update();
        popupMenu.setWidth(Global.dpToPx(150));
        popupMenu.setHeight(Global.dpToPx(300));
        popupMenu.showAtLocation(imgMenu, Gravity.RIGHT|Gravity.TOP, Global.dpToPx(2) , Global.dpToPx(100));
    }
    public void showExitPopup(){
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_exit, null);
        Button btnExitYes = (Button)parent.findViewById(R.id.btnExitYes);
        Button btnExitNo = (Button)parent.findViewById(R.id.btnExitNo);
        btnExitYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExit.dismiss();
                if (OpenVpnService.getManagement() != null )
                    OpenVpnService.getManagement().stopVPN();
                if( OpenVpnService.mProcessThread != null )
                    OpenVpnService.mProcessThread.interrupt();
                ServerActivity.this.finish();
            }
        });

        btnExitNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExit.dismiss();
            }
        });


        popupExit = new PopupWindow(parent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
        popupExit.setOutsideTouchable(true);
        popupExit.setTouchable(true);
        popupExit.setBackgroundDrawable(new BitmapDrawable());
        popupExit.setAnimationStyle(R.style.PopupAnimation);
        popupExit.update();
        popupExit.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // your code
            showExitPopup();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (OpenVpnService.getManagement() != null )
                    OpenVpnService.getManagement().stopVPN();
                if( OpenVpnService.mProcessThread != null )
                    OpenVpnService.mProcessThread.interrupt();
                ServerActivity.this.finish();
            }
        }else if( requestCode == 1 ){
            if( resultCode == RESULT_OK ){
                if (OpenVpnService.getManagement() != null )
                    OpenVpnService.getManagement().stopVPN();
                if( OpenVpnService.mProcessThread != null )
                    OpenVpnService.mProcessThread.interrupt();
                ServerActivity.this.finish();
            }
        }
    }
    public String getDeviceInformation(){
        String res = "";
        TelephonyManager telephonyManager  =
                (TelephonyManager)getSystemService( Context.TELEPHONY_SERVICE );

       /*
        * getDeviceId() function Returns the unique device ID.
        * for example,the IMEI for GSM and the MEID or ESN for CDMA phones.
        */
        res = telephonyManager.getDeviceId();
        if( res != null )   return res;

       /*
        * getSubscriberId() function Returns the unique subscriber ID,
        * for example, the IMSI for a GSM phone.
        */
        res = telephonyManager.getSubscriberId();
        if( res != null )   return res;

/*
        * returns the MacAddress
        */

        WifiManager wifiManager =
                (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        res = wInfo.getMacAddress();
        if( res != null )   return res;
    /*
     * Settings.Secure.ANDROID_ID returns the unique DeviceID
     * Works for Android 2.2 and above
     */
        res = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if( res != null )   return res;
        return null;
    }


}
