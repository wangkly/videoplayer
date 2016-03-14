package com.videoplayer.wangkly.myvideoplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.videoplayer.wangkly.myvideoplayer.R;

import java.util.List;
import java.util.Map;

/**
 * Created by wangkly on 2016/3/12.
 */
public class MainListAdapter extends SimpleAdapter {
    private List<Map<String,Object>> mlist;
    private LayoutInflater mInflater;
    private int mResource;
    private String[] mFrom;
    /**
     * RemindViewAdapter所需的id数组
     */
    private int[] mTo;
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public MainListAdapter(Context context, List<Map<String, Object>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        mlist =  data;
        mFrom =from;
        mTo=to;
        mResource=resource;
    }

    private class ViewHolder{
        TextView tv;
        ImageView img;
    }


    /**
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        return mlist.size();
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        return mlist.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String,Object> bean =mlist.get(position);
         ViewHolder holder ;
            if(convertView ==null){
                holder =new ViewHolder();
                convertView =mInflater.inflate(R.layout.activity_mainlist_item ,null);
                holder.tv =(TextView) convertView.findViewById(R.id.tv);
                holder.img =(ImageView)convertView.findViewById(R.id.img);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            if(null!=bean.get("img")){
                String imgpath= (String)bean.get("img");
                Bitmap bitmap= BitmapFactory.decodeFile(imgpath) ;
                holder.img.setImageBitmap(bitmap);
            }

            holder.tv.setText((String)bean.get("title"));

     return convertView;
    }

}
