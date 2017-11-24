package com.wtz.aidlcallback.client.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wtz.aidlcallback.R;
import com.wtz.aidlcallback.server.Student;

import java.util.List;

public class ListAdapter extends BaseAdapter {
    private final static String TAG = ListAdapter.class.getName();

    private Context mContext;

    private List<Student> mList;

    public ListAdapter(Context context, List<Student> list) {
        mContext = context;
        mList = list;
    }

    public void update(List<Student> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<Student> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return (mList == null) ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return (mList == null) ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == mContext) {
            Log.d(TAG, "getView...null == mContext");
            return null;
        }

        if (null == mList || mList.isEmpty()) {
            Log.d(TAG, "getView...list isEmpty");
            return null;
        }

        ViewHolder itemLayout = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout, null);
            itemLayout = new ViewHolder();
            itemLayout.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            itemLayout.tvScore = (TextView) convertView.findViewById(R.id.tv_score);
            convertView.setTag(itemLayout);
        } else {
            itemLayout = (ViewHolder) convertView.getTag();
        }

        Student item = null;
        if ((item = mList.get(position)) != null) {
            itemLayout.tvName.setText(item.getName());

            String formatCount = mContext.getString(R.string.format_score);
            itemLayout.tvScore.setText(String.format(formatCount, item.getScore()));
        }

        return convertView;
    }

    class ViewHolder {
        TextView tvName;
        TextView tvScore;
    }

}
