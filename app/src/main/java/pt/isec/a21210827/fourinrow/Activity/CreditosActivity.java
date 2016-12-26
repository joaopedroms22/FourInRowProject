package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import pt.isec.a21210827.fourinrow.R;

public class CreditosActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_creditos);
    }
}
