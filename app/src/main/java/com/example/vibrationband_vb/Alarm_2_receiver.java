package com.example.vibrationband_vb;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

public class Alarm_2_receiver extends BroadcastReceiver {

    private static PowerManager.WakeLock sCpuWakeLock;
    MainActivity m = new MainActivity();

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onReceive(Context context, Intent intent) {


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, Alarm_1_display.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

        //OREO API 26 이상에서는 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남


            String channelName ="매일 알람 채널";
            String description = "매일 정해진 시간에 알람합니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남


        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())

                .setTicker("{Time to watch some cool stuff!}")
                .setContentTitle("알람 On")
                .setContentText("밴드 진동 시작")
                .setContentInfo("INFO")
                .setContentIntent(pendingI);

        if (notificationManager != null) {
            // 노티피케이션 동작시킴
            notificationManager.notify(1234, builder.build());
            if(m.bluetoothAdapter.isEnabled()) {
                sendData("at");
            }

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            sCpuWakeLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, "al");

            sCpuWakeLock.acquire();

            if (sCpuWakeLock != null) {
                sCpuWakeLock.release();
                sCpuWakeLock = null;
            }
        }
    }

    void sendData(String text) {
        try{
            // 데이터 송신
            m.outputStream.write(text.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
