package com.huruwo.behook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static {
        try {
            CLogUtils.e("开始加载 Test so文件 ");
            System.loadLibrary("Test");
            init();
        } catch (Throwable e) {
            CLogUtils.e("加载So出现异常 " + e.toString());
            e.printStackTrace();
        }
    }

    private TextView textView;
    private Button button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText(md5("11111"));
            }
        });

    }

    public native String md5(String str);

    public static native void init();
}