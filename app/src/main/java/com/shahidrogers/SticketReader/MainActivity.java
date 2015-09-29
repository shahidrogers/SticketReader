package com.shahidrogers.SticketReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static final String YOUR_APPLICATION_ID = "3zUnInahebnSfVPGbrLIBLflh1yY5Vx8xHq4EmHe";
    public static final String YOUR_CLIENT_KEY = "BUIxAMInkYrB3Wql8VFFwSlTap6UXrGUdRid9VGq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set main layout of activity
        setContentView(R.layout.activity_main);
        // Add your initialization code here
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("");
            integrator.setCameraId(0);  // Use a specific camera of the device
            integrator.setBeepEnabled(false);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);


        if (scanResult != null) {
            // handle scan result
            final String result = scanResult.getContents();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Parking");
            // Specify the object id
            query.whereEqualTo("username", result);
            query.orderByDescending("parkEndDateAndTime");
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, com.parse.ParseException e) {
                    if (object != null) {
                        // The query was successful.
                        Date endTime = object.getDate("parkEndDateAndTime");
                        Date currentTime = new Date();
                        //format string output of date object
                        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                        DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
                        Log.d("Result:", endTime.toString());
                        Log.d("Result:", currentTime.toString());
                        if (currentTime.after(endTime)) {
                            //put your red cross
                            //Log.d("Result:", "True");
                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText("EXPIRED TICKET")
                                    .setContentText("Ticket of " + result + "\nHAS EXPIRED AT\n\n"
                                            + df.format(endTime) + "\n" + df2.format(endTime) + "\n")
                                    .show();
                        } else {
                            //put your green tick
                            //Log.d("Result:", "False");
                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("VALIDATED")
                                    .setContentText("Ticket of " + result + "\nIS VALID\n\nEXPIRES AT\n"
                                            + df.format(endTime) + " " + df2.format(endTime) + "\n")
                                    .show();
                        }


                    } else {
                        // Something went wrong.
                        Log.d("Something went wrong", e.toString());
                    }
                }

            });


            //Toast toast = Toast.makeText(this, "Content:" + result, Toast.LENGTH_LONG);
            //toast.show();
        } else {
            // else continue with any other code you need in the method
            Log.v("BarcodeActivity", "No result");
        }
    }
}
