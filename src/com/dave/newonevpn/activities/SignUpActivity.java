package com.dave.newonevpn.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dave.newonevpn.netutil.CommonAsyncTask;
import com.dave.newonevpn.netutil.WebServiceClient;
import com.dave.onevpnfresh.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by intermarketsecurities on 9/20/16.
 */
public class SignUpActivity extends Activity implements View.OnClickListener{

    private EditText edtName;
    private EditText edtEmail;
//    private EditText edtPassword;
//    private EditText edtConfirmPassword;

    private CommonAsyncTask mAsyncTask = null;
    private JSONObject objRes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edtName = (EditText) findViewById(R.id.edtName);
        edtEmail = (EditText)findViewById(R.id.edtEmail);
//        edtPassword = (EditText)findViewById(R.id.edtPassword);
//        edtConfirmPassword = (EditText)findViewById(R.id.edtConfirmPassword);
        findViewById(R.id.txtTermsnConditions).setOnClickListener(this);
        findViewById(R.id.btnGetVPN).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if( v.getId() == R.id.txtTermsnConditions ){
//            Intent intent = new Intent(SignUpActivity.this, WebViewActivity.class);
//            startActivity(intent);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.onevpn.com/terms-of-service/"));
            startActivity(browserIntent);
        }
        else if( v.getId() == R.id.btnGetVPN ){
            if(validateSignUpDetails())
                signUpUser();
        }
    }

    private boolean validateSignUpDetails(){

        if (edtName.getText().toString().trim().length() == 0)
            edtName.setError("Required");
        else if (edtEmail.getText().toString().trim().length() == 0)
            edtEmail.setError("Required");
//        else if (edtPassword.getText().toString().trim().length() == 0)
//            edtPassword.setError("Required");
//        else if (edtConfirmPassword.getText().toString().trim().length() == 0)
//            edtConfirmPassword.setError("Required");
        else if(!isEmailValid(edtEmail.getText().toString().trim()))
            Toast.makeText(SignUpActivity.this, "Invalid email.", Toast.LENGTH_SHORT).show();
//        else if(!edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString()))
//            Toast.makeText(SignUpActivity.this, "Password mismatch.", Toast.LENGTH_SHORT).show();
        else
            return true;

        return false;
    }

    private void signUpUser(){
        mAsyncTask = new CommonAsyncTask(this, true, new CommonAsyncTask.asyncTaskListener() {
            @Override
            public Boolean onTaskExecuting() {
                try{

                    ArrayList<NameValuePair> postParameters = null;
                    postParameters = new ArrayList<NameValuePair>();

                    postParameters.add(new BasicNameValuePair("firstname", edtName.getText().toString()));
                    postParameters.add(new BasicNameValuePair("email", edtEmail.getText().toString()));
//                    postParameters.add(new BasicNameValuePair("user_pwd", edtPassword.getText().toString()));

                    WebServiceClient wsClient = new WebServiceClient(SignUpActivity.this);
                    String response = wsClient.sendDataToServer("https://www.onevpn.com/onevpn_services/add_client.php", postParameters);
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
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
                        if( objRes.getString("result").equalsIgnoreCase("success") )
                        {
                            alertDialogBuilder.setTitle("Success");
                            alertDialogBuilder.setMessage("Your account has been registered, please check your email for credentials.").setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent();
                                                    intent.putExtra("result","success");
                                                    setResult(RESULT_OK, intent);
                                                    finish();
                                                }
                                            }
                                    );
                        }
                        else
                        {
                            alertDialogBuilder.setTitle("Error");
                            alertDialogBuilder.setMessage(objRes.getString("message")).setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            }
                                    );
                        }
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();

                    } catch (JSONException e) {
                        Toast.makeText(SignUpActivity.this, "JSON Parse Error!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(SignUpActivity.this, "Server Connection Timeout!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAsyncTask.execute();
    }



    public boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
