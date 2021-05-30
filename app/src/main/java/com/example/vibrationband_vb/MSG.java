package com.example.vibrationband_vb;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static com.example.vibrationband_vb.MainActivity.cntt;
import static com.example.vibrationband_vb.db_test.participants;


public class MSG extends AppCompatActivity  {
    private Button button_sen, button_cancel;
    private EditText phone_number, mms_contents;
    final SmsManager sms = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);

        phone_number = (EditText) findViewById(R.id.txt_phone);
        mms_contents= (EditText) findViewById(R.id.txt_contents);
        button_sen = (Button) findViewById(R.id.button_send);
        button_cancel = (Button) findViewById(R.id.button_cancel);

        Intent intent_phone = getIntent();
        String phone_num = intent_phone.getStringExtra("phone");
        if(participants[0] == null||participants[0].equals("112")){
            participants[0] = "112";
            phone_number.setText(participants[0]);}
        phone_number.setText(participants[cntt]);
        cntt=0;

        Intent intent_gps = getIntent();
        String address = intent_gps.getStringExtra("value");
        mms_contents.setText(address);
    }

    public void go_send(View v){
        Uri n = Uri.parse("smsto: " + phone_number.getText());
        String num = phone_number.getText().toString();
        String t = mms_contents.getText().toString();

        Intent intent_1 = new Intent(Intent.ACTION_SENDTO, n);
        intent_1.putExtra("sms_body", t);
        startActivity(intent_1);
    }

    public void go_cancel(View v) {
        Intent go_cancel = new Intent(this, MainActivity.class);
        startActivity(go_cancel);
    }
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1000) {

            /* 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라
            내가 요청한 게 하나밖에 없기 때문에. 원래 같으면 for문을 돈다.*/
            if ( grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission( this, Manifest.permission.CALL_PHONE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    Uri n = Uri.parse("tel: " + phone_number.getText());
                }
            } else {
                Toast.makeText(MSG.this, "권한 요청을 거부했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void go_test(View v){
        Intent go_test = new Intent(this, db_test.class);
        startActivity(go_test);
    }
}