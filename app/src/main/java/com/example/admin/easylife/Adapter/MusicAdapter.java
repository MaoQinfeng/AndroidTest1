package com.example.admin.easylife.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.admin.easylife.R;
import com.example.admin.easylife.db.Music;

import java.util.List;

public class MusicAdapter extends ArrayAdapter<Music> {
    public int resourceID;
    public MusicAdapter(Context context, int resource, List<Music> objects){
        super(context,resource,objects);
        resourceID = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Music music = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceID,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.title = view.findViewById(R.id.MusicName);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.title.setText(music.getName());
        return view;
    }

    class ViewHolder{
        TextView title;
    }

}
