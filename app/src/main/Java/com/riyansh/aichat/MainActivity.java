package com.riyansh.aichat;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText userInput;
    TextView chatBox;
    Button sendBtn;
    ScrollView chatScroll;

    // placeholder for security on GitHub
    String API_KEY = "PASTE_YOUR_OPENROUTER_KEY_HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.userInput);
        chatBox = findViewById(R.id.chatBox);
        sendBtn = findViewById(R.id.sendBtn);
        chatScroll = findViewById(R.id.chatScroll);

        sendBtn.setOnClickListener(v -> {
            String msg = userInput.getText().toString().trim();
            if(msg.isEmpty()) return;

            chatBox.append("\nYou: " + msg + "\n");
            chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));

            new Thread(() -> {
                String reply = getAI(msg);
                runOnUiThread(() -> {
                    chatBox.append("\nAI: " + reply + "\n");
                    chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
                });
            }).start();
            userInput.setText("");
        });
    }

    private String getAI(String prompt) {
        try {
            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{ \"model\": \"openai/gpt-3.5-turbo\", \"messages\": [{\"role\":\"user\",\"content\":\"" + prompt + "\"}]}";
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder res = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) res.append(line);
            br.close();

            JSONObject jsonObject = new JSONObject(res.toString());
            return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

