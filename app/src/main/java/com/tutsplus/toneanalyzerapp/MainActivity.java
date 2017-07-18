package com.tutsplus.toneanalyzerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ElementTone;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Tone;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneCategory;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            JSONObject credentials = new JSONObject(IOUtils.toString(
                    getResources().openRawResource(R.raw.credentials), "UTF-8"
            ));

            String username = credentials.getString("username");
            String password = credentials.getString("password");

            final ToneAnalyzer toneAnalyzer =
                    new ToneAnalyzer("2017-07-01");
            toneAnalyzer.setUsernameAndPassword(username, password);

            Button analyzeButton = (Button)findViewById(R.id.analyze_button);
            analyzeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText userInput = (EditText)findViewById(R.id.user_input);
                    final String textToAnalyze = userInput.getText().toString();

                    ToneOptions options = new ToneOptions.Builder()
                            .addTone(Tone.EMOTION)
                            .html(false).build();

                    toneAnalyzer.getTone(textToAnalyze, options).enqueue(new ServiceCallback<ToneAnalysis>() {
                        @Override
                        public void onResponse(ToneAnalysis response) {
                            List<ToneScore> scores = response.getDocumentTone().getTones()
                                                        .get(0).getTones();

                            String detectedTones = "";
                            for(ToneScore score:scores) {
                                if(score.getScore() > 0.5f) {
                                    detectedTones += score.getName() + " ";
                                }
                            }

                            final String toastMessage = "The following emotions were detected:\n\n"
                                                        + detectedTones.toUpperCase();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getBaseContext(),
                                            toastMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
