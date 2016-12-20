package pt.isec.a21210827.fourinrow.Class;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import pt.isec.a21210827.fourinrow.Logic.GameEngine;
import pt.isec.a21210827.fourinrow.R;

import static android.widget.ImageView.ScaleType.FIT_XY;

public class GameGridViewAdapter extends BaseAdapter {

    private int[] list;
    private Context context;

    public GameGridViewAdapter(int[] list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int i) {
        return list[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {

        ImageView iv = new ImageView(context);
        iv.setBackgroundColor(Color.rgb(255, 166, 77));
        iv.setImageResource(list[pos]);

//        DisplayMetrics metrics = new DisplayMetrics();
//        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;


        //Teste Commit
        iv.setAdjustViewBounds(true);
//        iv.setMaxHeight(height);
//        iv.setMinimumHeight(height);

        return iv;
    }
}
