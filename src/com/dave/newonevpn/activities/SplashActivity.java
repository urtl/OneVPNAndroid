package com.dave.newonevpn.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.dave.newonevpn.model.DNSItem;
import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.model.PortItem;
import com.dave.newonevpn.model.ProtocolItem;
import com.dave.newonevpn.model.ServerItem;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.dave.newonevpn.netutil.WebServiceClient;
import com.dave.newonevpn.util.DBManager;
import com.dave.onevpnfresh.R;
import com.newonevpn.vpn.core.OpenVpnService;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
/**
 * Created by gustaf on 11/2/15.
 */
public class SplashActivity extends Activity {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1241;
    final private int PermissionCode_Read_Phone_State = 1111;
    //    final private int PermissionCode_Write_External_Storage = 1112;
    final private int PermissionCode_Read_External_Storage = 1113;

    private CommonAsyncTask mAsyncTask = null;
    private JSONArray objResArray;
    private JSONObject objRes;
    private String strRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Global.dbManager = new DBManager(this);
        Global.sp = getSharedPreferences("userinfo", 1);
        Global.ed = Global.sp.edit();

        checkForPhoneStatePermission();

    }

    public void callGetServerList(){
        if (OpenVpnService.getManagement() != null ) {
            OpenVpnService.getManagement().stopVPN();
           // Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
            Global.ed.putBoolean("Connect", false);
            Global.ed.commit();
        }else{
            //Toast.makeText(this, "123Connected", Toast.LENGTH_LONG).show();
        }
        if( OpenVpnService.mProcessThread != null ) {
            OpenVpnService.mProcessThread.interrupt();
            //Toast.makeText(this, "456Connected", Toast.LENGTH_LONG).show();
        }else{
            //Toast.makeText(this, "789Connected", Toast.LENGTH_LONG).show();
        }

        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
            @Override
            public Boolean onTaskExecuting() {
                try{

                    ArrayList<NameValuePair> postParameters = null;
                    postParameters = new ArrayList<NameValuePair>();

                    WebServiceClient wsClient = new WebServiceClient(SplashActivity.this);
                    objRes = new JSONObject(wsClient.sendDataToServer(Global.SERVER_URL + "get_server_list.php", postParameters));
                    if( objRes == null )    return false;
                }catch(Exception e){
                    return false;
                }
                return true;
            }

            @Override
            public void onTaskFinish(Boolean result) {
                if (result == true) {
                    Global.g_allDNSLists.clear();
                    Global.g_allServerLists.clear();
                    try {
                        if( objRes.getInt("status") == 1 ){
                            objResArray = objRes.getJSONArray("server");
                            for(int i = 0; i < objResArray.length(); i++){
                                JSONObject objServer = objResArray.getJSONObject(i);
                                ServerItem serverItem = new ServerItem();
                                serverItem.id = objServer.getString("id");
                                serverItem.name = objServer.getString("name");
                                serverItem.country = objServer.getString("country");
                                serverItem.dns = objServer.getString("dns");
                                serverItem.port = objServer.getString("port");
                                serverItem.protocol = objServer.getString("protocol");

                                Global.g_allServerLists.add(serverItem);
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    makeDNSList();

                                }
                            }, 2000);

                        }else{

                        }
                    } catch (JSONException e) {
                        Toast.makeText(SplashActivity.this, "JSON Parse Error!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(SplashActivity.this, "Server Connection Timeout!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAsyncTask.execute();
    }
    public void makeDNSList(){
        DNSItem dnsSelectedItem = null;
        if( Global.g_allServerLists.size() > 0 ) {
            ServerItem serverItem = Global.g_allServerLists.get(0);

            String selectedid = Global.sp.getString("selectedid", "");

            DNSItem dnsItem = new DNSItem();
            dnsItem.dns = serverItem.dns;
            dnsItem.country = serverItem.country;
            dnsItem.name = serverItem.name;

            ProtocolItem protocolItem = new ProtocolItem();
            protocolItem.protocol = serverItem.protocol;

            PortItem portItem = new PortItem();
            portItem.port = serverItem.port;
            portItem.id = serverItem.id;
            if( selectedid.equals(portItem.id) )
                dnsSelectedItem = dnsItem;

            protocolItem.ports.add(portItem);
            dnsItem.protocols.add(protocolItem);


            for (int i = 1; i < Global.g_allServerLists.size(); i++) {
                ServerItem tempItem = Global.g_allServerLists.get(i);
                if( serverItem.dns.equals(tempItem.dns) ){
                    if( serverItem.protocol.equals(tempItem.protocol) == false ){
                        protocolItem = new ProtocolItem();
                        protocolItem.protocol = tempItem.protocol;
                        dnsItem.protocols.add(protocolItem);
                    }

                    portItem = new PortItem();
                    portItem.port = tempItem.port;
                    portItem.id = tempItem.id;

                    if( selectedid.equals(portItem.id) )
                        dnsSelectedItem = dnsItem;

                    protocolItem.ports.add(portItem);
                }else{
                    serverItem = tempItem;
                    Global.g_allDNSLists.add(dnsItem);

                    dnsItem = new DNSItem();
                    dnsItem.dns = serverItem.dns;
                    dnsItem.country = serverItem.country;
                    dnsItem.name = serverItem.name;

                    protocolItem = new ProtocolItem();
                    protocolItem.protocol = serverItem.protocol;

                    portItem = new PortItem();
                    portItem.port = serverItem.port;
                    portItem.id = serverItem.id;

                    if( selectedid.equals(portItem.id) )
                        dnsSelectedItem = dnsItem;


                    protocolItem.ports.add(portItem);
                    dnsItem.protocols.add(protocolItem);
                }
            }

            Global.g_allDNSLists.add(dnsItem);
        }

        /*if (OpenVpnService.getManagement() == null )
        {
            Global.ed.putBoolean("Connect", false);
            Global.ed.commit();

        }else{
            if( Global.sp.getBoolean("Connect", false) ) {
                if( dnsSelectedItem != null ) {
                    Global.g_selectedDNSInfo = dnsSelectedItem;

                }else{
                    for(int i = 0; i < Global.g_allDNSLists.size(); i++){
                        if( Global.g_allDNSLists.get(i).dns.equals(Global.sp.getString("serverdns", "")) &&
                                Global.g_allDNSLists.get(i).name.equals(Global.sp.getString("servername", ""))){
                            Global.g_selectedDNSInfo = dnsSelectedItem;
                        }
                    }
                }
                if( Global.g_selectedDNSInfo != null ) {
                    Global.g_selectedServerInfo.dns = Global.sp.getString("serverdns", "");
                    Global.g_selectedServerInfo.country = Global.sp.getString("servercountry", "");
                    Global.g_selectedServerInfo.name = Global.sp.getString("servername", "");
                    Global.g_selectedServerInfo.protocol = Global.sp.getString("serverprotocol", "");
                    Global.g_selectedServerInfo.port = Global.sp.getString("serverport", "");

                    Intent intent = new Intent(this, ConnectStatusActivity.class);
                    intent.putExtra("auto", true);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        }*/
        if( dnsSelectedItem != null && Global.sp.getBoolean("chkAutoReconnect", false) ){
            Global.g_selectedDNSInfo = dnsSelectedItem;
            Global.g_selectedServerInfo.dns = Global.sp.getString("serverdns", "");
            Global.g_selectedServerInfo.country = Global.sp.getString("servercountry", "");
            Global.g_selectedServerInfo.name = Global.sp.getString("servername", "");
            Global.g_selectedServerInfo.protocol = Global.sp.getString("serverprotocol", "");
            Global.g_selectedServerInfo.port = Global.sp.getString("serverport", "");

            Intent intent = new Intent(this, ConnectStatusActivity.class);
            intent.putExtra("auto", true);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(this, ServerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkForPhoneStatePermission(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

//            final List<String> permissionsList = new ArrayList<>();
//            permissionsList.add(Manifest.permission.READ_PHONE_STATE);
//            permissionsList.add(Manifest.permission.INTERNET);
//
//            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
//                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);



            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
                requestPermissions( new String[]{Manifest.permission.READ_PHONE_STATE}, PermissionCode_Read_Phone_State);
            }
            else
                callGetServerList();
//


//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},  PermissionCode_Read_External_Storage);
//            }


//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionCode_Write_External_Storage);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE},  1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.INTERNET)) {
//                requestPermissions(new String[]{Manifest.permission.INTERNET},  1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE},  1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_SYNC_SETTINGS)) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_SYNC_SETTINGS},  1);
//            }
        }
        else
            callGetServerList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

//        switch (requestCode) {
//            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
//            {
//                Map<String, Integer> perms = new HashMap<>();
//                // Initial
//                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
//                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
//
//                // Fill with results
//                for (int i = 0; i < permissions.length; i++)
//                    perms.put(permissions[i], grantResults[i]);
//                // Check for ACCESS_FINE_LOCATION
//                if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
//                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    // All Permissions Granted
//                    callGetServerList();
//                } else {
//                    // Permission Denied
//                    Toast.makeText(SplashActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
//                            .show();
//                }
//            }
//            break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }


        switch (requestCode) {
            case PermissionCode_Read_Phone_State:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    callGetServerList();
                } else {
                    // Permission Denied
                    Toast.makeText(SplashActivity.this, "READ_PHONE_STATE Denied", Toast.LENGTH_SHORT).show();
                }
                break;
//            case PermissionCode_Read_External_Storage:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission Granted
////                    insertDummyContact();
//                } else {
//                    // Permission Denied
//                    Toast.makeText(SplashActivity.this, "READ_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT).show();
//                }
//                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

//    int nPermisionCnt = 0;
//    private void checkForPhoneStatePermission(){
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
//                nPermisionCnt++;
//                requestPermissions( new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                nPermisionCnt++;
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                nPermisionCnt++;
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},  1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
//                nPermisionCnt++;
//                requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE},  1);
//            }
//            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.INTERNET)) {
//                nPermisionCnt++;
//                requestPermissions(new String[]{Manifest.permission.INTERNET},  1);
//            }if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
//                nPermisionCnt++;
//                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE},  1);
//            }if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_SYNC_SETTINGS)) {
//                nPermisionCnt++;
//                requestPermissions(new String[]{Manifest.permission.WRITE_SYNC_SETTINGS},  1);
//            }
//
//            if( nPermisionCnt == 0 ){
//                callGetServerList();
//            }
//
//        }else{
//            callGetServerList();
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//
//                if (grantResults.length > 0) {
//                    if( nPermisionCnt > 0 ){
//                        nPermisionCnt--;
//                    }
//
//                    if( nPermisionCnt == 0 ){
//                        callGetServerList();
//                    }
//
//            /*HERE PERMISSION IS ALLOWED.
//            *
//            * YOU SHOULD CODE HERE*/
//
//
//                } else {
//                    Toast.makeText(this, "You have to allow permission for this app. Try run app again with allow.",Toast.LENGTH_SHORT).show();
//
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }

}
