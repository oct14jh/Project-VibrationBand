package com.example.vibrationband_vb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class db_test extends AppCompatActivity {
    Button btn_add;         // 추가시키기 위한 버튼
    Button btn_winner;      // 당첨자를 뽑기 위한 버튼
    EditText et_name;          // 추가하려는 내용
    TextView tv_people;     //  추가한 직원목록을 보여주는 텍뷰
    int version = 1;
    MyDatabaseOpenHelper helper;
    SQLiteDatabase database;

   public static String participants[] = new String[100];

    StringBuffer sb;
    int count = 0;
    public static int cnt=0;
    String sql;
    Cursor cursor;
    String data_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_test);
        setUp();
        nameList();
    }

    private void setUp(){
        btn_add = (Button)findViewById(R.id.button_change_num);
        btn_winner = (Button)findViewById(R.id.btn_checkdata);
        et_name = (EditText)findViewById(R.id.change_no_txt);
        tv_people = (TextView)findViewById(R.id.tvtv);
        btn_add.setOnClickListener(myListener);
        btn_winner.setOnClickListener(myListener);

        helper = new MyDatabaseOpenHelper(db_test.this, MyDatabaseOpenHelper.tableName, null, version);
        database = helper.getWritableDatabase();

        sb = new StringBuffer();
    }


    View.OnClickListener myListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.button_change_num:
                    sb.setLength(0);
                    if(et_name.getText().toString().length()!= 11 && et_name.getText().toString().substring(0,3) != "010"){
                        Toast.makeText(db_test.this, "연락처를 제대로 기입해주세요.(11자리),010부터", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else {
                        helper.insertName(database, (et_name.getText().toString()));
                        nameList();
                        Intent go_data = new Intent(getApplicationContext(), MainActivity.class);
                        go_data.putExtra("phone", data_num);
                        startActivity(go_data);
                        break;
                    }
                        case R.id.btn_checkdata:
                            break;

            }
        }
    };

    private void nameList(){
        sql = "SELECT NAME FROM " + helper.tableName;
        cursor = database.rawQuery(sql, null);
        if(cursor != null){
            count = cursor.getCount();

            for(int i=0; i < count; i++){
                cursor.moveToNext();
                String participant = cursor.getString(0);
                participants[i] = participant;
                data_num = participants[i];
                sb.append(participants[i] + " ");
                cnt=count;
            }
            tv_people.setText("" + sb);
            cursor.close();
        }
    }
    public void go_cancel(View v) {
        Intent go_cancel = new Intent(this, MainActivity.class);
        startActivity(go_cancel);
    }
}

