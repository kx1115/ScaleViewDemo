package com.yy.scalewineview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ScaleView mWineView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWineView=findViewById(R.id.wineView);
        mTextView=findViewById(R.id.textview);

        mWineView.setValue(20,50,283);
        mWineView.setValueChangeListener(new ScaleView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {
                mTextView.setText(""+value);
            }
        });
    }
}
