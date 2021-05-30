package com.example.vibrationband_vb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Locale;

public class TTS extends AppCompatActivity implements TextToSpeech.OnInitListener{
    //변수 선언
    private TextToSpeech tts;
    private Button btSpeak;
    private EditText getTextToSpeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        tts = new TextToSpeech(this, this); //첫번째는 Context 두번째는 리스너

        getTextToSpeek = (EditText) findViewById(R.id.txt_contents);
        btSpeak = (Button) findViewById(R.id.bt_speakOut);
        btSpeak.setEnabled(true);

        //버튼 클릭하면 작동
        btSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOutNow();
            }
        });
    }

    //앱종료시 tts를 같이 종료해 준다.
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int language = tts.setLanguage(Locale.KOREAN);

            if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                btSpeak.setEnabled(false);
                Toast.makeText(this, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
            } else {
                btSpeak.setEnabled(true);
                speakOutNow();
            }
        } else {
            Toast.makeText(this, "TTS 실패!", Toast.LENGTH_SHORT).show();
        }
    }

    //Speak out...
    private void speakOutNow() {
        String text = getTextToSpeek.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void go_cancel(View v) {
        Intent go_cancel = new Intent(this, MainActivity.class);
        startActivity(go_cancel);
    }
}

