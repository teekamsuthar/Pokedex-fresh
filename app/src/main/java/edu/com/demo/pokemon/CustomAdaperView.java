package edu.com.demo.pokemon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdaperView extends BaseAdapter {

    private final Activity ctx;

    private final ArrayList<Pokemon> pairs;

    public CustomAdaperView(Activity context,
                            ArrayList<Pokemon> pairs) {
        ctx = context;
        this.pairs = pairs;
    }

    @Override
    public int getCount() {
        return pairs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(
                    R.layout.pokedex_row, null);
            viewHolder = new ViewHolder();

            viewHolder.txt = convertView
                    .findViewById(R.id.pokedex_row_text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txt.setText(pairs.get(position).getName());


        return convertView;
    }

    public static class ViewHolder {
        public TextView txt;

    }
}