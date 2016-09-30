package com.dave.newonevpn.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dave.newonevpn.api.CurrentIPAPI;
import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.dave.newonevpn.netutil.WebServiceClient;
import com.dave.onevpnfresh.R;
import com.newonevpn.vpn.ConfigConverter;
import com.newonevpn.vpn.LaunchVPNManager;
import com.newonevpn.vpn.core.OpenVpnService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
/**
 * Created by gustaf on 11/2/15.
 */
public class ConnectStatusActivity extends Activity implements View.OnClickListener{

    boolean showPassword = false;
    private CommonAsyncTask mAsyncTask = null;
    private JSONArray objResArray;
    private JSONObject objRes;
    private String strRes;

    ImageView imgMenu;
    TextView txtIpAddress, txtConnectStatus;
    ProgressBar progress;
    LinearLayout linearConnect;
    ImageView imgConnectStatus;
    TextView txtConnectLabel;
    PopupWindow popupMenu, popupLogin, popupExit, popupForgot;


    private final int interval = 50; // 1 Second
    private int nPos = 0;
    private boolean bConnecting = false;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable(){
        public void run() {
            if( bConnecting ){
                if( nPos > 99 ) nPos = 0;
                nPos++;
                progress.setProgress(nPos);
                handler.postAtTime(runnable, System.currentTimeMillis()+interval);
                handler.postDelayed(runnable, interval);
            }
        }
    };


    private ConfigConverter pConverter;
    private LaunchVPNManager pLaunchVPN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_status);
        imgMenu = (ImageView)findViewById(R.id.imgMenu);
        txtIpAddress = (TextView)findViewById(R.id.txtIpAddress);
        txtConnectStatus = (TextView)findViewById(R.id.txtConnectStatus);
        progress = (ProgressBar)findViewById(R.id.progress);
        linearConnect = (LinearLayout)findViewById(R.id.linearConnect);
        imgConnectStatus = (ImageView)findViewById(R.id.imgConnectStatus);
        txtConnectLabel = (TextView)findViewById(R.id.txtConnectLabel);
        progress.setProgress(0);

        imgMenu.setOnClickListener(this);
        linearConnect.setOnClickListener(this);

        //txtIpAddress.setText(getIPAddress(true));
        pLaunchVPN = new LaunchVPNManager(this);
        pConverter = new ConfigConverter(this, pLaunchVPN);

        getIpAddress();

        if (OpenVpnService.getManagement() == null )
        {
            Global.ed.putBoolean("Connect", false);
            Global.ed.commit();

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if( bundle != null && bundle.containsKey("auto") ){
                clickconnect();
            }
        }else{
            if( Global.sp.getBoolean("Connect", false) ) {
                txtConnectStatus.setText("Connected!");
                progress.setProgress(100);
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_disconnect));
                txtConnectLabel.setText("TAP TO DISCONNECT");
                imgMenu.setVisibility(View.GONE);
            }else{
                txtConnectStatus.setText("Not Connected");
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                imgMenu.setVisibility(View.VISIBLE);
            }
        }
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onResume(){
        super.onResume();
        getIpAddress();
    }

    @Override
    public void onClick(View v) {
        if( v == imgMenu ){
            showMenuPopup();
        }else if( v == linearConnect ){
            clickconnect();
        }
    }
    public void showLoginPopup() {
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_credential, null);
        final EditText edtEmail = (EditText) parent.findViewById(R.id.edtEmail);
        final EditText edtPassword = (EditText) parent.findViewById(R.id.edtPassword);

        edtPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    float f1 = event.getRawX()-25;
                    float f2 = (edtPassword.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width());
                    if(f1 <= f2)
                    {
                        // your action here
                        if(!showPassword)
                        {
                            showPassword = true;
                            edtPassword.setTransformationMethod(null);
                        }
                        else
                        {
                            showPassword = false;
                            edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                        }

//                        return true;
                    }
//                    else
//                    {
//                        edtPassword.setTransformationMethod(new PasswordTransformationMethod());
////                        return false;
//                    }
                }
                return false;
            }
        });

        LinearLayout linearLogin = (LinearLayout) parent.findViewById(R.id.linearLogin);
        final CheckBox chkRememberMe = (CheckBox) parent.findViewById(R.id.chkRememberMe);
        TextView txtForgetPassword = (TextView) parent.findViewById(R.id.txtForgetPassword);
        TextView txtSignUp = (TextView) parent.findViewById(R.id.txtSignUp);

        if (Global.sp.getBoolean("chkRememberMe", false)) {
            edtEmail.setText(Global.sp.getString("email", ""));
            edtPassword.setText(Global.sp.getString("password", ""));
            chkRememberMe.setChecked(true);
        } else {
            chkRememberMe.setChecked(false);
        }

        linearLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().length() == 0) {
                    Toast.makeText(ConnectStatusActivity.this, "Input Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().length() == 0) {
                    Toast.makeText(ConnectStatusActivity.this, "Input Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (chkRememberMe.isChecked()) {
                    Global.ed.putBoolean("chkRememberMe", true);
                } else {
                    Global.ed.putBoolean("chkRememberMe", false);
                }
                Global.ed.putString("email", edtEmail.getText().toString());
                Global.ed.putString("password", edtPassword.getText().toString());
                Global.ed.commit();


                popupLogin.dismiss();


                bConnecting = true;
                nPos = 0;
                progress.setProgress(nPos);
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_cancel));
                txtConnectLabel.setText("TAP TO CANCEL");
                txtConnectStatus.setText("Connecting...");
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);

                imgMenu.setVisibility(View.GONE);
                Lock(false);

                ReceiveBroadCast();
                tryConnect();


            }
        });

        chkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chkRememberMe.isChecked()) {
                    Global.ed.putBoolean("chkRememberMe", true);
                    Global.ed.putString("email", edtEmail.getText().toString());
                    Global.ed.putString("password", edtPassword.getText().toString());
                    Global.ed.commit();
                } else {
                    Global.ed.putBoolean("chkRememberMe", false);
                    Global.ed.commit();
                }
            }
        });

        txtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupLogin.dismiss();
                showForgetDialog(false);
            }
        });

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupLogin.dismiss();
                showSignUpDialog();
            }
        });




        popupLogin = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupLogin.setOutsideTouchable(true);
        popupLogin.setTouchable(true);
        popupLogin.setBackgroundDrawable(new BitmapDrawable());
        popupLogin.setAnimationStyle(R.style.PopupAnimation);
        popupLogin.update();
        popupLogin.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }
    public void showForgetDialog(boolean bChange){
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_forgot, null);
        TextView txtDesc = (TextView)parent.findViewById(R.id.txtDesc);
        if( bChange ){
            txtDesc.setText("You've Already Claimed Your Free Account. For further details contact support at www.onevpn.com");
        }
        Button btnOK = (Button)parent.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupForgot.dismiss();
            }
        });



        popupForgot = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupForgot.setOutsideTouchable(true);
        popupForgot.setTouchable(true);
        popupForgot.setBackgroundDrawable(new BitmapDrawable());
        popupForgot.setAnimationStyle(R.style.PopupAnimation);
        popupForgot.update();
        popupForgot.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }

//    public void showSignUpDialog(){
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        // set title
//        alertDialogBuilder.setTitle("Alert");
//        // set dialog message
//        alertDialogBuilder.setMessage("You can claim FREE Account only once. Do you wish to continue?").setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // if this button is clicked, close
//                                dialog.dismiss();
//                                checkSignUp();
//                                // current activity
//                            }
//                        }
//                )
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // if this button is clicked, close
//                                dialog.dismiss();
//                                // current activity
//                            }
//                        }
//                );
//
//        AlertDialog alertDialog = alertDialogBuilder.create();
//
//        alertDialog.show();
//    }

//    public void checkSignUp(){
//        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
//            @Override
//            public Boolean onTaskExecuting() {
//                try{
//
//                    ArrayList<NameValuePair> postParameters = null;
//                    postParameters = new ArrayList<NameValuePair>();
//
//                    postParameters.add(new BasicNameValuePair("ieme", Global.deviceId));
//                    WebServiceClient wsClient = new WebServiceClient(ConnectStatusActivity.this);
//                    objRes = new JSONObject(wsClient.sendDataToServer(Global.SERVER_URL + "check_ieme.php", postParameters));
//                    if( objRes == null )    return false;
//                }catch(Exception e){
//                    return false;
//                }
//                return true;
//            }
//
//            @Override
//            public void onTaskFinish(Boolean result) {
//                if (result == true) {
//                    try {
//                        if( objRes.getInt("status") == 1 ){
//                            Intent intent = new Intent(ConnectStatusActivity.this, WebViewActivity.class);
//                            startActivity(intent);
//                        }else{
//                            // Global.alertDialog(ConnectStatusActivity.this, objRes.getString("msg"));
//                            showForgetDialog(true);
//                        }
//                    } catch (JSONException e) {
//                        Toast.makeText(ConnectStatusActivity.this, "JSON Parse Error!", Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
//                }else{
//                    Toast.makeText(ConnectStatusActivity.this, "Server Connection Timeout!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        mAsyncTask.execute();
//    }

    public void showSignUpDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle("Alert");
        // set dialog message
        alertDialogBuilder.setMessage("You can claim FREE Account only once. Do you wish to continue?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                dialog.dismiss();
                                checkSignUp();
                                // current activity
                            }
                        }
                )
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                dialog.dismiss();
                                // current activity
                            }
                        }
                );

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
    public void checkSignUp(){
        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
            @Override
            public Boolean onTaskExecuting() {
                try{

                    ArrayList<NameValuePair> postParameters = null;
                    postParameters = new ArrayList<NameValuePair>();

                    postParameters.add(new BasicNameValuePair("ieme", Global.deviceId));
                    WebServiceClient wsClient = new WebServiceClient(ConnectStatusActivity.this);
                    String str = wsClient.sendDataToServer(Global.SERVER_URL + "check_ieme.php", postParameters);
                    objRes = new JSONObject(wsClient.sendDataToServer(Global.SERVER_URL + "check_ieme.php", postParameters));
                    if( objRes == null )    return false;
                }catch(Exception e){
                    return false;
                }
                return true;
            }

            @Override
            public void onTaskFinish(Boolean result) {
                if (result == true) {
                    try {
                        if( objRes.getString("status").equalsIgnoreCase("1") ){
//                            Intent intent = new Intent(ConnectStatusActivity.this, WebViewActivity.class);
//                            startActivity(intent);

//                            Intent intent = new Intent(ConnectStatusActivity.this, SignUpActivity.class);
//                            startActivity(intent);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConnectStatusActivity.this);
                            alertDialogBuilder.setTitle("Alert");
                            alertDialogBuilder.setMessage("You already have claimed your free trial, please upgrade!!!").setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // if this button is clicked, close
                                                    dialog.dismiss();
                                                }
                                            }
                                    );

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        }else{
                            // Global.alertDialog(ConnectStatusActivity.this, objRes.getString("msg"));
//                            showForgetDialog(true);
                            Intent intent = new Intent(ConnectStatusActivity.this, SignUpActivity.class);
                            startActivityForResult(intent, 2);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ConnectStatusActivity.this, "JSON Parse Error!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(ConnectStatusActivity.this, "Server Connection Timeout!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAsyncTask.execute();
    }

    public void showMenuPopup(){
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_menu, null);
        LinearLayout linearMenuUniqueID = (LinearLayout)parent.findViewById(R.id.linearMenuUniqueID);
        linearMenuUniqueID.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      popupMenu.dismiss();
                                                      Global.alertDialog(ConnectStatusActivity.this, Global.deviceId);
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

        linearChooseServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                setResult(RESULT_CANCELED);
                ConnectStatusActivity.this.finish();
            }
        });
        linearSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectStatusActivity.this, SettingActivity.class);
                startActivityForResult(intent, 1);

                popupMenu.dismiss();
            }
        });
        linearAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
                Intent intent = new Intent(ConnectStatusActivity.this, AboutActivity.class);
                startActivityForResult(intent, 0);

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

        popupMenu = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupMenu.setOutsideTouchable(true);
        popupMenu.setTouchable(true);
        popupMenu.setBackgroundDrawable(new BitmapDrawable());
        popupMenu.setAnimationStyle(R.style.PopupAnimation);
        popupMenu.update();
        popupMenu.setWidth(Global.dpToPx(150));
        popupMenu.setHeight(Global.dpToPx(300));
        popupMenu.showAtLocation(imgMenu, Gravity.RIGHT | Gravity.TOP, Global.dpToPx(2), Global.dpToPx(100));
    }
    public void showExitPopup(){
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_exit, null);
        Button btnExitYes = (Button)parent.findViewById(R.id.btnExitYes);
        Button btnExitNo = (Button)parent.findViewById(R.id.btnExitNo);
        btnExitYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OpenVpnService.getManagement() != null )
                    OpenVpnService.getManagement().stopVPN();
                if( OpenVpnService.mProcessThread != null )
                    OpenVpnService.mProcessThread.interrupt();

                popupExit.dismiss();
                setResult(RESULT_OK);
                ConnectStatusActivity.this.finish();
            }
        });

        btnExitNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExit.dismiss();
            }
        });


        popupExit = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        popupExit.setOutsideTouchable(true);
        popupExit.setTouchable(true);
        popupExit.setBackgroundDrawable(new BitmapDrawable());
        popupExit.setAnimationStyle(R.style.PopupAnimation);
        popupExit.update();
        popupExit.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }

    public String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
    public void getIpAddress(){
        String ip = getLocalIpAddress();
        txtIpAddress.setText(ip);
        getCurrentIP();
    }
    private void getCurrentIP () {

        CurrentIPAPI api = new CurrentIPAPI(this, new CurrentIPAPI.OnTaskCompleted(){

            @Override
            public void onTaskCompleted(String response) {
                txtIpAddress.setText(response);
            }
        });
        api.setURL("http://ifcfg.me/ip");
        api.execute();



    }
    public String getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    // for getting IPV4 format
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {

                        String ip = inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    public void ReceiveBroadCast(){
        IntentFilter filter = new IntentFilter("com.newonevpn.vpn.VPN_STATUS");
        if( filter != null )
            this.registerReceiver(new Receiver(), filter);
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if( arg1 == null )	return;
            if( arg1.getExtras() == null ) return;

            String strStatus = arg1.getExtras().getString("status");
            String detailStatus = arg1.getExtras().getString("detailstatus");

            if( strStatus == null || detailStatus == null )  	    	return;

            if(detailStatus.equals("CONNECTING") ) detailStatus = "connection in progress";
            else if(detailStatus.equals("WAIT") ) detailStatus = "waiting for server reply";
            else if(detailStatus.equals("RECONNECTING") ) detailStatus = "reconnecting";
            else if(detailStatus.equals("ASSIGN_IP") ) detailStatus = "assigning ip addresses";
            else if(detailStatus.equals("RESOLVE") ) detailStatus = "detecting server address";
            else if(detailStatus.equals("TCP_CONNECT") ) detailStatus = "connection in progress(TCP)";
            else if(detailStatus.equals("AUTH") ) detailStatus = "authentication";
            else if(detailStatus.equals("GET_CONFIG") ) detailStatus = "loading sever parameters";
            else if(detailStatus.equals("ASSIGN_UP") ) detailStatus = "setting up new connection";
            else if(detailStatus.equals("ADD_ROUTES") ) detailStatus = "setting up secured routes";
            else if(detailStatus.equals("NETWORK" )) detailStatus = "waiting for usable network";
            else if(detailStatus.equals("SCREENOFF" )) detailStatus = "paused - screen off";
            else if(detailStatus.equals("USERPAUSE" )) detailStatus = "pause requested by user";
            else if(strStatus.equals("LEVEL_CONNECTING_NO_SERVER_REPLY_YET") )
                detailStatus = "waiting for server reply";
            else if(strStatus.equals("LEVEL_CONNECTING_SERVER_REPLIED") )
                detailStatus = "secure connect";
            else if(strStatus.equals("EXITING") )
                detailStatus = "exiting";
            else
                detailStatus = "waiting";

           // txtConnectStatus.setText(detailStatus);

            if(strStatus.equals("LEVEL_CONNECTED") )
            {
                bConnecting = false;
                Global.ed.putBoolean("Connect", true);
                Global.ed.commit();
                txtConnectStatus.setText("Connected!");
                progress.setProgress(100);
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_disconnect));
                txtConnectLabel.setText("TAP TO DISCONNECT");
                Lock(true);
                getIpAddress();
                imgMenu.setVisibility(View.GONE);
                ConnectStatusActivity.this.setResult(RESULT_CANCELED);

            }
            else if(strStatus.equals("LEVEL_NOTCONNECTED") )
            {
                bConnecting = false;
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                txtConnectStatus.setText("Not Connected");
                imgMenu.setVisibility(View.VISIBLE);

                progress.setProgress(0);
            }else if(strStatus.equals("connection aborted"))
            {
                bConnecting = false;
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                txtConnectStatus.setText("Not Connected");
                imgMenu.setVisibility(View.VISIBLE);

                progress.setProgress(0);
            }else if(strStatus.equals("DISCONNECTED")){
                bConnecting = false;
                imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
                txtConnectLabel.setText("TAP TO CONNECT");
                txtConnectStatus.setText("Not Connected");
                imgMenu.setVisibility(View.VISIBLE);
                progress.setProgress(0);
            }else if( arg1.getExtras().getString("detailstatus").equals("NEXTOVPNTRY") ){

            }
        }
    }
    public void tryConnect(){
        pConverter.StartConfigConverter();
    }
    public void clickconnect(){
        if( txtConnectLabel.getText().toString().equals("TAP TO CONNECT") == false ){
            bConnecting = false;
            nPos = 0;
            progress.setProgress(nPos);
            imgConnectStatus.setImageDrawable(getResources().getDrawable(R.drawable.png_btn_connect));
            txtConnectLabel.setText("TAP TO CONNECT");
            txtConnectStatus.setText("Not Connected");

            imgMenu.setVisibility(View.VISIBLE);
            Global.ed.putBoolean("Connect", false);
            Global.ed.commit();
            if (OpenVpnService.getManagement() != null )
                OpenVpnService.getManagement().stopVPN();
            if( OpenVpnService.mProcessThread != null )
                OpenVpnService.mProcessThread.interrupt();
            getIpAddress();
        }else{
            showLoginPopup();
        }
    }
    public void Lock(boolean bLock){
      //  linearConnect.setEnabled(bLock);
      //  imgMenu.setEnabled(bLock);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == 0 ){
            if( resultCode == RESULT_OK ){
                setResult(RESULT_OK);
                finish();
            }
        }else if( requestCode == 1 ){
            if( resultCode == RESULT_OK ){
                setResult(RESULT_OK);
                finish();
            }
        }else if( requestCode == 2 ){
            if( resultCode == RESULT_OK ){
                // this will change the status of registerd device to 1 from 0
                String result = data.getStringExtra("result");
                if(result.equalsIgnoreCase("success"))
                    changeStatus();
            }
        }
        else {
            if (resultCode == RESULT_OK) {
                tryConnect();
                //   ReceiveBroadCast();
            }
        }
    }

    private void changeStatus(){
        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
            @Override
            public Boolean onTaskExecuting() {
                try{

                    ArrayList<NameValuePair> postParameters = null;
                    postParameters = new ArrayList<NameValuePair>();

                    postParameters.add(new BasicNameValuePair("imei", Global.deviceId));
                    postParameters.add(new BasicNameValuePair("status", "1"));

                    WebServiceClient wsClient = new WebServiceClient(ConnectStatusActivity.this);
                    String response = wsClient.sendDataToServer("http://webservice.onevpn.com/onevpn/webservice/update_imei.php", postParameters);
                    objRes = new JSONObject(response);
                    if( objRes == null )    return false;
                }catch(Exception e){
                    return false;
                }
                return true;
            }

            @Override
            public void onTaskFinish(Boolean result) {
                if (result == true) {
                    try {
//                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
                        if(objRes.getBoolean("message"))
                        {
//                            alertDialogBuilder.setTitle("Success");
//                            alertDialogBuilder.setMessage("Your account has been registered, please check your email for credentials.").setCancelable(false)
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    changeStatus();
//                                                    dialog.dismiss();
//                                                    finish();
//                                                }
//                                            }
//                                    );
                        }
                        else
                        {
//                            alertDialogBuilder.setTitle("Error");
//                            alertDialogBuilder.setMessage(objRes.getString("message")).setCancelable(false)
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    dialog.dismiss();
//                                                }
//                                            }
//                                    );
                        }
//                        AlertDialog alertDialog = alertDialogBuilder.create();
//                        alertDialog.show();

                    } catch (JSONException e) {
//                        Toast.makeText(SignUpActivity.this, "JSON Parse Error!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else{
//                    Toast.makeText(SignUpActivity.this, "Server Connection Timeout!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAsyncTask.execute();
    }

}
