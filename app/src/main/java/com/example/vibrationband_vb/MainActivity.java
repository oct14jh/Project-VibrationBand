package com.example.vibrationband_vb;

import java.io.IOException;
import java.io.InputStream;import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import static android.content.ContentValues.TAG;
import static com.example.vibrationband_vb.db_test.cnt;
import static com.example.vibrationband_vb.db_test.participants;


import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;

    private static PowerManager.WakeLock sCpuWakeLock;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    int version = 1;
    MyDatabaseOpenHelper helper;
    SQLiteDatabase database;
    StringBuffer sb;
    int count = 0;
    public static int cnt=0;
    String sql;
    Cursor cursor;
    String data_num;
    public static int cntt=0;
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    public static BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    public static OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치

    private GpsTracker gpsTracker;
    Switch btSwitch;
    SeekBar seekBar1, seekBar2;
    public int sb_val1 = 0, sb_val2 = 0;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        setUpM();
        nameListM();

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        seekBar1 = (SeekBar)findViewById(R.id.seekbar_decibel);
        seekBar2 = (SeekBar)findViewById(R.id.seekbar_vibration);

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // 최초에 탭하여 드래그 시작할때 발생
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sb_val1 = seekBar.getProgress();
            }

            // 드래그 하는 중에 발생
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sb_val1 = seekBar.getProgress();
            }

            // 드래그를 멈출때 발생
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String stmp1 = "st";
                String stmp2 = "";
                sb_val1 = seekBar.getProgress();
                stmp2 = Integer.toString(sb_val1);
                sendData(stmp1.concat(stmp2));
                Toast.makeText(MainActivity.this, "진동 세기 : " + stmp2, Toast.LENGTH_LONG).show();
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            // 최초에 탭하여 드래그 시작할때 발생
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sb_val2 = seekBar.getProgress();
            }

            // 드래그 하는 중에 발생
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sb_val2 = seekBar.getProgress();
            }

            // 드래그를 멈출때 발생
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String stmp1 = "vt";
                String stmp2 = "";
                sb_val2 = seekBar.getProgress();
                stmp2 = Integer.toString(sb_val2);
                sendData(stmp1.concat(stmp2));
                Toast.makeText(MainActivity.this, "데시벨 감도 : " + stmp2, Toast.LENGTH_LONG).show();
            }
        });

        btSwitch = (Switch) findViewById(R.id.bluetooth_switch);

        btSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                bluetoothCheckState();
            }
        });

    }

    public void go_stt(View v) {
        Intent go_stt = new Intent(this, STT.class);
        startActivity(go_stt);
    }

    public void go_tts(View v) {
        Intent go_tts = new Intent(this, TTS.class);
        startActivity(go_tts);
    }

    public void go_msg(View v) {
        gpsTracker = new GpsTracker(MainActivity.this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        Intent intent_phone = getIntent();
        String str = intent_phone.getStringExtra("phone");

        Toast.makeText(MainActivity.this, participants[0], Toast.LENGTH_LONG).show();
        String address = getCurrentAddress(latitude, longitude);

        Toast.makeText(MainActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

        Intent go_msg = new Intent(this, MSG.class);
        go_msg.putExtra("value", address);
        go_msg.putExtra("phone", str);
        startActivity(go_msg);
    }

    public void go_alarm(View v) {
        Intent go_alarm = new Intent(this, Alarm_1_display.class);
        startActivity(go_alarm);
    }

    public void go_test(View v){
        Intent go_test = new Intent(this, db_test.class);
        startActivity(go_test);
    }
    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();

                }else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setUpM() {
        helper = new MyDatabaseOpenHelper(MainActivity.this, MyDatabaseOpenHelper.tableName, null, version);
        database = helper.getWritableDatabase();
        sb = new StringBuffer();
    }
    private void nameListM(){
        sql = "SELECT NAME FROM " + helper.tableName;
        cursor = database.rawQuery(sql, null);
        if(cursor != null){
            count = cursor.getCount();

            for(int i=0; i < count; i++){
                cursor.moveToNext();
                String participant = cursor.getString(0);
                participants[i] = participant;
                data_num = participants[i];                                         ///ghdgndhgdngngasdifsadfji
                sb.append(participants[i] + " ");
                cntt=i;

            }
            // tv_people.setText("" + sb);
            cursor.close();
        }
    }

    public void bluetoothCheckState(){
        if(btSwitch.isChecked()){
            if(bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때

            }

            else { // 디바이스가 블루투스를 지원 할 때
                if(bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                }
                else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                    // 블루투스를 활성화 하기 위한 다이얼로그 출력
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // 선택한 값이 onActivityResult 함수에서 콜백된다.
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                            }
                        }
                    }, 4000);// 2초 정도 딜레이를 준 후 시작
                }
            }
        }
        else{
            if(bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        int pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if(pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }

            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for(BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if(deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 데이터 송,수신 스트림을 얻어옵니다.
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sendData(String text) {
        text += " ";
        try{
            // 데이터 송신
            outputStream.write(text.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void receiveData() {
        final Handler handler = new Handler();
        // 데이터를 수신하기 위한 버퍼를 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(!(Thread.currentThread().isInterrupted())) {
                    try {
                        // 데이터를 수신했는지 확인합니다.
                        int byteAvailable = inputStream.available();
                        // 데이터가 수신 된 경우
                        if(byteAvailable > 0) {
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                            for(int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                // 개행문자를 기준으로 받음(한줄)
                                if(tempByte == '\n') {
                                    // readBuffer 배열을 encodedBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    // 인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            if(text.contains("f")) {
                                                createNotification();
                                            }
                                        }

                                    });
                                } // 개행 문자가 아닐 경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        // 1초마다 받아옴
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        workerThread.start();
    }

    @SuppressLint("InvalidWakeLockTag")
    private void createNotification() {

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, STT.class);
        notificationIntent.putExtra("notificationId", 0); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle("밴드 stt기능 작동")
                .setContentText("stt기능을 이용하려면 클릭")
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(12345, builder.build()); // 고유숫자로 노티피케이션 동작시킴

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "hi");

        sCpuWakeLock.acquire();

        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }

    public void go_memo(View v){
        Intent go_memo = new Intent(this, Memo.class);
        startActivity(go_memo);
    }
}