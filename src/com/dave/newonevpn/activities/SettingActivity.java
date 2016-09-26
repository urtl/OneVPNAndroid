package com.dave.newonevpn.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.dave.onevpnfresh.R;
import com.dave.newonevpn.adapters.SpinnerAdapter;
import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.model.ProtocolItem;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.newonevpn.vpn.core.OpenVpnService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class SettingActivity extends Activity implements View.OnClickListener{

    private CommonAsyncTask mAsyncTask = null;
    private JSONArray objResArray;
    private JSONObject objRes;
    private String strRes;

    ImageView imgMenu;
    Spinner spinnerProtocol, spinnerMode;
    RadioButton radioTCP, radioUDP;
    EditText edtPort;
    CheckBox chkAutoReconnect, chkLogWindow;
    Button btnSave;

    ArrayList<String> portData = null;
    PopupWindow popupMenu, popupLogin, popupExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_screen);

        imgMenu = (ImageView)findViewById(R.id.imgMenu);
        spinnerProtocol = (Spinner)findViewById(R.id.protocolSpinner);
        spinnerMode = (Spinner)findViewById(R.id.ModeSpinner);
        radioTCP = (RadioButton)findViewById(R.id.radioTCP);
        radioUDP = (RadioButton)findViewById(R.id.radioUDP);
        edtPort = (EditText)findViewById(R.id.edtPortNo);
        chkAutoReconnect = (CheckBox)findViewById(R.id.chkAutoReconnect);
        chkLogWindow = (CheckBox)findViewById(R.id.chkLogWindow);
        btnSave = (Button)findViewById(R.id.btnSave);

        imgMenu.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        ArrayList<String> protocolData = new ArrayList<>();
        protocolData.add("Protocol: OpenVPN");
        SpinnerAdapter adapterProtocol = new SpinnerAdapter(this, protocolData);
        spinnerProtocol.setAdapter(adapterProtocol);

        radioTCP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtPort.setEnabled(false);
                    portData = getPorts(true);
                    SpinnerAdapter adapter = new SpinnerAdapter(SettingActivity.this, portData);
                    spinnerMode.setAdapter(adapter);

                }
            }
        });
        radioUDP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtPort.setEnabled(false);
                    portData = getPorts(false);
                    SpinnerAdapter adapter = new SpinnerAdapter(SettingActivity.this, portData);
                    spinnerMode.setAdapter(adapter);

                }
            }
        });
        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == portData.size() - 1) {
                    edtPort.setEnabled(true);
                } else {
                    edtPort.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if( Global.g_selectedServerInfo.protocol.equals("tcp") ) {
            radioTCP.setChecked(true);
            radioUDP.setChecked(false);
            portData = getPorts(true);
            SpinnerAdapter adapter = new SpinnerAdapter(SettingActivity.this, portData);
            spinnerMode.setAdapter(adapter);
        }else{
            radioTCP.setChecked(false);
            radioUDP.setChecked(true);
            portData = getPorts(false);
            SpinnerAdapter adapter = new SpinnerAdapter(SettingActivity.this, portData);
            spinnerMode.setAdapter(adapter);
        }

        chkAutoReconnect.setChecked(Global.sp.getBoolean("chkAutoReconnect", false));
        chkLogWindow.setChecked(Global.sp.getBoolean("chkLogWindow", false));


        setResult(RESULT_CANCELED);

    }



    @Override
    public void onClick(View v) {
        if( v == imgMenu ){
            showMenuPopup();
        }else if( v == btnSave ){
            if( spinnerMode.getSelectedItemPosition() == portData.size() - 1 ){
                if( edtPort.getText().length() == 0 ){
                    Toast.makeText(this, "You have to input Port", Toast.LENGTH_SHORT).show();
                    return;
                }
                Global.g_selectedServerInfo.port = edtPort.getText().toString();
            }else{
                ProtocolItem selectedProtocol = null;
                for(int i = 0; i < Global.g_selectedDNSInfo.protocols.size(); i++){
                    ProtocolItem protocolItem = Global.g_selectedDNSInfo.protocols.get(i);
                    if(  radioTCP.isChecked() && protocolItem.protocol.equals("tcp")){
                        selectedProtocol = protocolItem;
                        break;
                    }else if( radioUDP.isChecked() && protocolItem.protocol.equals("udp") ){
                        selectedProtocol = protocolItem;
                        break;
                    }
                }

                if( selectedProtocol != null ) {
                    Global.g_selectedServerInfo.port = selectedProtocol.ports.get(spinnerMode.getSelectedItemPosition()).port;
                }else{
                    Toast.makeText(this, "Port Error", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if( radioTCP.isChecked() ){
                Global.g_selectedServerInfo.protocol = "tcp";
            }else{
                Global.g_selectedServerInfo.protocol = "udp";
            }


            Global.ed.putBoolean("radioTCP", radioTCP.isChecked());
            Global.ed.putBoolean("radioUDP", radioUDP.isChecked());
            Global.ed.putBoolean("chkAutoReconnect", chkAutoReconnect.isChecked());
            Global.ed.putBoolean("chkLogWindow", chkLogWindow.isChecked());

            Global.ed.putString("serverdns", Global.g_selectedServerInfo.dns);
            Global.ed.putString("servercountry", Global.g_selectedServerInfo.country);
            Global.ed.putString("servername", Global.g_selectedServerInfo.name);
            Global.ed.putString("serverprotocol", Global.g_selectedServerInfo.protocol);
            Global.ed.putString("serverport", Global.g_selectedServerInfo.port);
            Global.ed.commit();


            finish();
        }
    }

    public ArrayList<String> getPorts(boolean bTCP){
        ProtocolItem selectedProtocol = null;
        for(int i = 0; i < Global.g_selectedDNSInfo.protocols.size(); i++){
            ProtocolItem protocolItem = Global.g_selectedDNSInfo.protocols.get(i);
            if(  bTCP && protocolItem.protocol.equals("tcp")){
                selectedProtocol = protocolItem;
                break;
            }else if( !bTCP && protocolItem.protocol.equals("udp") ){
                selectedProtocol = protocolItem;
                break;
            }
        }

        ArrayList<String> res = new ArrayList<>();
        if( selectedProtocol != null ) {
            for (int i = 0; i < selectedProtocol.ports.size(); i++) {
                String data = "Mode: " + (bTCP ? "TCP " : "UDP ") + selectedProtocol.ports.get(i).port;
                if( selectedProtocol.ports.get(i).port.equals(Global.g_selectedServerInfo.port) )
                {
                    res.add(0, data);
                }
                else
                    res.add(data);
            }
        }
        res.add("Manual");

        return res;
    }
    public void showMenuPopup(){
        View parent = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_menu, null);
        LinearLayout linearChooseServer  = (LinearLayout)parent.findViewById(R.id.linearMenuChooseServer);
        LinearLayout linearAbout = (LinearLayout)parent.findViewById(R.id.linearMenuAbout);
        LinearLayout linearProfile = (LinearLayout)parent.findViewById(R.id.linearMenuProfile);
        LinearLayout linearFAQ = (LinearLayout)parent.findViewById(R.id.linearMenuFAQ);
        LinearLayout linearShare = (LinearLayout)parent.findViewById(R.id.linearMenuShare);
        LinearLayout linearUpgrade = (LinearLayout)parent.findViewById(R.id.linearMenuUpgrade);
        LinearLayout linearSetting = (LinearLayout)parent.findViewById(R.id.linearMenuSetting);
        LinearLayout linearExit = (LinearLayout)parent.findViewById(R.id.linearMenuExit);
        LinearLayout linearMenuUniqueID = (LinearLayout)parent.findViewById(R.id.linearMenuUniqueID);
        linearMenuUniqueID.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      popupMenu.dismiss();
                                                      Global.alertDialog(SettingActivity.this, Global.deviceId);
                                                  }
                                              }
        );
        linearChooseServer.setVisibility(View.GONE);
        linearAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
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

        popupMenu = new PopupWindow(parent, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, true);
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
                setResult(RESULT_OK);
                SettingActivity.this.finish();
            }
        });

        btnExitNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExit.dismiss();
            }
        });


        popupExit = new PopupWindow(parent, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, true);
        popupExit.setOutsideTouchable(true);
        popupExit.setTouchable(true);
        popupExit.setBackgroundDrawable(new BitmapDrawable());
        popupExit.setAnimationStyle(R.style.PopupAnimation);
        popupExit.update();
        popupExit.showAtLocation(imgMenu, Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

}
