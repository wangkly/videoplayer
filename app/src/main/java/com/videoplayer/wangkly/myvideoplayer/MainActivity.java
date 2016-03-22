package com.videoplayer.wangkly.myvideoplayer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.videoplayer.wangkly.myvideoplayer.activities.OnlinePlayActivity;
import com.videoplayer.wangkly.myvideoplayer.activities.VideoActivity;
import com.videoplayer.wangkly.myvideoplayer.adapter.MainListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView mainlist;

    private MainListAdapter adapter;
    private ImageView trash;
    private Button cancel;
    private Button delete;
    private RelativeLayout rlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rlayout =(RelativeLayout)findViewById(R.id.relative);
        trash = (ImageView) findViewById(R.id.trash);
        cancel = (Button) findViewById(R.id.cancel);
        delete = (Button) findViewById(R.id.delete);
        mainlist = (ListView) findViewById(R.id.mianlist);
        trash.setOnClickListener(onClick());
        cancel.setOnClickListener(onClick());
        delete.setOnClickListener(onClick());
        adapter = new MainListAdapter(MainActivity.this,getVideoList(),R.layout.activity_main,
                new String[]{"tv","img"},new int[]{R.id.tv,R.id.img});
        mainlist.setAdapter(adapter);
        mainlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                HashMap<String, Object> data = (HashMap<String, Object>) listView.getItemAtPosition(position);
                String path = (String) data.get("path");
                Intent intent =new Intent(MainActivity.this, VideoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("videopath", path);

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent();
                intent.setClass(MainActivity.this,OnlinePlayActivity.class);
                startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * 按钮点击
     * @return
     */
    public View.OnClickListener onClick(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
//                    case R.id.download_back:
//                        //结束activity,返回
//                        finish();
//                        break;
                    case R.id.trash:
                        beforeDelete();
                        break;
                    case R.id.cancel:
                        cancelDelete();
                        break;
                    case R.id.delete:
                        deleteSelectedItem();
                        break;
                    default:
                        break;
                }
            }
        };
    }
    /**
     * 删除前处理
     */
    public void beforeDelete(){
        //显示底部菜单
        rlayout.setVisibility(View.VISIBLE);
        //显示每一项前面的checkbox
        HashMap<Integer,Integer> cv =  new HashMap<Integer, Integer>();
        for(int i =0; i<adapter.getMlist().size();i++){
            cv.put(i, CheckBox.VISIBLE);
        }
        adapter.setVisiblecheck(cv);
        adapter.notifyDataSetChanged();
    }


    /**
     * 取消删除操作
     */
    public void cancelDelete(){

        rlayout.setVisibility(View.INVISIBLE);
        HashMap<Integer,Integer> cv =  new HashMap<Integer, Integer>();
        for(int i =0; i<adapter.getMlist().size();i++){
            cv.put(i, CheckBox.INVISIBLE);
        }
        adapter.setVisiblecheck(cv);
        adapter.notifyDataSetChanged();
    }

    /**
     * 删除
     */
    public  void deleteSelectedItem(){
    Map<Integer,Boolean> map = adapter.getIsSelected();
    List<Map<String,Object>> mlist =adapter.getMlist();
    for(Object ob :map.keySet()){
    if(map.get(ob)){
            //删除对应位置的文件
        Map<String,Object> bean= mlist.get((int)ob);
            String filepath =bean.get("path").toString();
            File file = new File(filepath);
            if(file.exists()){
                file.delete();//删除本地文件
             }
            mlist.remove(bean);//列表中删除
            }
        }

        //通知adapter数据集变化
        for (int i = 0; i < mlist.size(); i++) {
            adapter.getIsSelected().put(i, false);
            adapter.getVisiblecheck().put(i, CheckBox.INVISIBLE);
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * 获取内存上的视频信息
     * @return
     */
    private  List<Map<String,Object>> getVideoList() {
       List<Map<String,Object>> sysVideoList = null;// 视频信息集合
        sysVideoList = new ArrayList<Map<String,Object>>();
        Cursor cursor;
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID };

        // MediaStore.Video.Media.DATA：视频文件路径；
        // MediaStore.Video.Media.DISPLAY_NAME : 视频文件名，如 testVideo.mp4
        // MediaStore.Video.Media.TITLE: 视频标题 : testVideo
        String[] mediaColumns = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DISPLAY_NAME };

        cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);

        if(cursor==null){
            Toast.makeText(MainActivity.this, "没有找到可播放视频文件", Toast.LENGTH_LONG).show();
            return null;
        }
        if (cursor.moveToFirst()) {
            do {
               Map<String ,Object> map = new HashMap<String,Object>();
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Video.Media._ID));
                Cursor thumbCursor = managedQuery(
                        MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
                                + "=" + id, null, null);
                if (thumbCursor.moveToFirst()) {
                    map.put("img", thumbCursor.getString(thumbCursor
                            .getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
                }
                map.put("path",cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                map.put("title", cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));

                map.put("dispalyname",cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
                map.put("mimetype", cursor
                        .getString(cursor
                                .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));

                sysVideoList.add(map);
            } while (cursor.moveToNext());
        }

        return sysVideoList;
    }

}
