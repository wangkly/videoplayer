package com.videoplayer.wangkly.myvideoplayer.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.videoplayer.wangkly.myvideoplayer.R;

/**
 * Created by wangkly on 2016/3/22.
 */
public class OnlinePlayActivity extends Activity{

    private EditText text;

    private Button btn ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webaddressplay);
        text = (EditText) findViewById(R.id.input);
        btn = (Button) findViewById(R.id.play);
        btn.setOnClickListener(onClick());

    }

    public View.OnClickListener onClick(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.play:
                        String url = text.getText().toString();
                        if(url.length() > 0){
                            //跳转播放界面
                            Intent intent = new Intent();
                            intent.setClass(OnlinePlayActivity.this,VideoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("videopath",url);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    break;
                }
            }
        };
    }

}
