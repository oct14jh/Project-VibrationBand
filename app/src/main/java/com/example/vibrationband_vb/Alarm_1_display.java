package com.example.vibrationband_vb;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Alarm_1_display extends AppCompatActivity {

    static DatePicker datePicker;
    static Calendar date_picker = Calendar.getInstance();
    static Calendar minDate = Calendar.getInstance();
    Intent alarms_aI;
    static PendingIntent alarms_pI;
    static AlarmManager alarms_aM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_1_display);

        final TimePicker picker = (TimePicker)findViewById(R.id.timePicker);
        picker.setIs24HourView(true);

        // 앞서 설정한 값으로 보여주기
        // 없으면 디폴트 값은 현재시간
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);

        // 이전 설정값으로 TimePicker 초기화
        Date currentTime = nextNotifyTime.getTime();
        SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
        int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));


        if (Build.VERSION.SDK_INT >= 23 ){
            picker.setHour(pre_hour);
            picker.setMinute(pre_minute);
        }
        else{
            picker.setCurrentHour(pre_hour);
            picker.setCurrentMinute(pre_minute);
        }

        Button popup_date = (Button)findViewById(R.id.dateSelect);
        popup_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDate();
            }
        });

        Button alarm_on = (Button)findViewById(R.id.btn_reg);
        alarm_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int hour, hour_24, minute;
                String am_pm;
                if (Build.VERSION.SDK_INT >= 23 ){
                    hour_24 = picker.getHour();
                    minute = picker.getMinute();
                }
                else{
                    hour_24 = picker.getCurrentHour();
                    minute = picker.getCurrentMinute();
                }
                if(hour_24 > 12) {
                    am_pm = "PM";
                    hour = hour_24 - 12;
                }
                else
                {
                    hour = hour_24;
                    am_pm="AM";
                }

                // 현재 지정된 시간으로 알람 시간 설정
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(date_picker.get(Calendar.YEAR), date_picker.get(Calendar.MONTH), date_picker.get(Calendar.DATE));
                calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                Date currentDateTime = calendar.getTime();
                String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.KOREAN).format(currentDateTime);
                Toast.makeText(getApplicationContext(),date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                //  Preference에 설정한 값 저장
                SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                editor.apply();

                diaryNotification(calendar);
            }

        });

        Button alarm_off = (Button)findViewById(R.id.btn_cancel);
        alarm_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarms_pI != null) {
                    Toast.makeText(Alarm_1_display.this, "알람 취소", Toast.LENGTH_SHORT).show();

                    // 알람매니저 취소
                    alarms_aM.cancel(alarms_pI);
                    alarms_pI.cancel();

                    // 알람취소
                    //sendBroadcast(alarms_aI);
                }
                else {
                    Toast.makeText(Alarm_1_display.this, "알람이 설정되지 않음", Toast.LENGTH_SHORT).show();
                }

                alarms_aM = null;
                alarms_aI = null;
                alarms_pI = null;
            }
        });
    }


    void diaryNotification(Calendar calendar)
    {
        Boolean dailyNotify = true; // 무조건 알람을 사용

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, Alarm_3_deviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, Alarm_2_receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        alarms_aI = alarmIntent;
        alarms_pI = pendingIntent;
        alarms_aM = alarmManager;
    }

    void showDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date_picker.set(year, month, dayOfMonth);
                Toast.makeText(getApplicationContext(),"날짜 설정 : "+ year + "-"+(month+1)+"-"+dayOfMonth,Toast.LENGTH_LONG).show();
            }
        }, date_picker.get(Calendar.YEAR), date_picker.get(Calendar.MONTH), date_picker.get(Calendar.DATE));

        datePickerDialog.getDatePicker().setMinDate(minDate.getTime().getTime());
        datePickerDialog.setMessage("날짜 설정");
        datePickerDialog.show();
    }
}