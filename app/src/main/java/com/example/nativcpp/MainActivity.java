package com.example.nativcpp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("regression-post-processing");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Map<String, Object> jsonToMap() throws JSONException
    {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String contents = loadJSONFromAsset();
        JSONObject json = new JSONObject(contents);
        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }

        return retMap;
    }

    public String loadJSONFromAsset()
    {
        String json = null;
        try {
            InputStream is = getAssets().open("output.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException
    {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText("asd");
        HashMap<String, ArrayList<ArrayList<Float>>> input = new HashMap<String, ArrayList<ArrayList<Float>>>();
        try {
            Map<String, Object> output;
            output = jsonToMap();

            for (String key : output.keySet()) {

                ArrayList<ArrayList<Float>> arr = (ArrayList<ArrayList<Float>>) output.get(key);
                input.put(key, arr);
            }
            HashMap<String, ArrayList<HashMap<String, Float>>> events = outputMapToMidiEvents(input);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native HashMap<String, ArrayList<HashMap<String, Float>>> outputMapToMidiEvents(HashMap<String, ArrayList<ArrayList<Float>>> inputHashMap);
}
