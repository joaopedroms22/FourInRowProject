package pt.isec.a21210827.fourinrow.Class;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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

        iv.setAdjustViewBounds(true);

        return iv;
    }
}
