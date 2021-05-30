package com.example.vibrationband_vb;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.util.ArrayList;

public class Memo extends AppCompatActivity {
    int sb_val = 0;
    int base_width = 10;
    Paint p = new Paint();
    class Point {
        float x, y;
        boolean check;
        int color;

        public Point(float x, float y, boolean check, int color) {
            this.x = x;
            this.y = y;
            this.check = check;
            this.color = color;
        }
    }

    class MyView extends View {
        public MyView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 1; i < points.size(); i++) {
                p.setColor(points.get(i).color);
                if (!points.get(i).check)
                    continue;
                canvas.drawLine(points.get(i - 1).x, points.get(i - 1).y, points.get(i).x, points.get(i).y, p);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    points.add(new Point(x, y, false, color));
                case MotionEvent.ACTION_MOVE:
                    points.add(new Point(x, y, true, color));
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            invalidate();
            return true;
        }
    }

    ArrayList<Point> points = new ArrayList<Point>();
    Button clearbtn;
    //LinearLayout drawlinear;
    FrameLayout fraggg;
    int color = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        final MyView m = new MyView(this);
        p.setStrokeWidth(base_width);

        clearbtn = findViewById(R.id.button_clear);
        fraggg = findViewById(R.id.fraggg);
        clearbtn.setOnClickListener(new View.OnClickListener() { //지우기 버튼 눌렸을때
            @Override
            public void onClick(View v) {
                points.clear();
                m.invalidate();
            }
        });
        fraggg.addView(m);


        SeekBar sb = (SeekBar)findViewById(R.id.seekbar_width);
        sb.setProgress(50);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // 최초에 탭하여 드래그 시작할때 발생
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sb_val = seekBar.getProgress();
            }

            // 드래그 하는 중에 발생
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sb_val = seekBar.getProgress();
            }

            // 드래그를 멈출때 발생
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sb_val = seekBar.getProgress();
                p.setStrokeWidth(sb_val / 5);
                base_width = sb_val/5;
                Toast.makeText(Memo.this, "선 굵기 : " + sb_val / 5, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void go_cancel(View v){
        Intent go_cancel = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(go_cancel);
    }
}
