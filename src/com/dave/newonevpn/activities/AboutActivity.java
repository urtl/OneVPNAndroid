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
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.dave.onevpnfresh.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by gustaf on 11/2/15.
 */
public class AboutActivity extends Activity implements View.OnClickListener {


    private CommonAsyncTask mAsyncTask = null;
    private JSONArray objResArray;
    private JSONObject objRes;
    private String strRes;

    ImageView imgMenu;
    PopupWindow popupMenu, popupLogin, popupExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        imgMenu = (ImageView)findViewById(R.id.imgMenu);
        imgMenu.setOnClickListener(this);

        setResult(RESULT_CANCELED);

    }



    @Override
    public void onClick(View v) {
        if( v == imgMenu ){
            showMenuPopup();
        }
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
                                                      Global.alertDialog(AboutActivity.this, Global.deviceId);
                                                  }
                                              }
        );
        linearSetting.setVisibility(View.GONE);
        linearChooseServer.setVisibility(View.GONE);

        linearAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        popupMenu = new PopupWindow(parent, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
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
                setResult(RESULT_OK);
                AboutActivity.this.finish();
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

}
