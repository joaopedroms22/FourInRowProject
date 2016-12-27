package pt.isec.a21210827.fourinrow.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import pt.isec.a21210827.fourinrow.R;

public class HistoricActivity extends Activity {

    TextView tvHistoric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_historic);

        tvHistoric = (TextView) findViewById(R.id.tvHistoric);

        try {
            loadScores();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScores() throws IOException {

        File f = new File(getApplicationContext().getFilesDir(), "gameHistoric.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            tvHistoric.setText(everything);
        }
    }
}
