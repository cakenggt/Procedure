package com.aleclownes.procedure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by alownes on 11/16/2015.
 */
public class ChecklistSyncTask extends AsyncTask<String, Void, List<Checklist>> {
    private String baseUrl = "192.168.188.123:8000/procedure/";
    private final ArrayAdapter adapter;
    private final List<Checklist> checklists;
    private final Context context;

    public static final String GET_ALL_CHECKLISTS = "get-all-checklists";
    public static final String CREATE_CHECKLIST = "create-checklist";
    public static final String LOGIN = "api-token-auth";

    public ChecklistSyncTask(ArrayAdapter adapter, List<Checklist> checklists, Context context){
        this.adapter = adapter;
        this.checklists = checklists;
        this.context = context;
    }

    @Override
    protected List<Checklist> doInBackground(String... params) {
        // params comes from the execute() call. Pass in stringified object for json as index 1
        String type = params[0];
        switch(type) {
            case GET_ALL_CHECKLISTS:
                break;
            case CREATE_CHECKLIST:
                break;
            case LOGIN:
                String username = params[1];
                String password = params[2];
                JSONObject json = new JSONObject();
                try {
                    json.put("username", username);
                    json.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(baseUrl+LOGIN);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);

                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    byte[] outputInBytes = json.toString().getBytes();
                    out.write(outputInBytes);

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    byte[] contents = new byte[1024];

                    int bytesRead=0;
                    String strFileContents = "";
                    while( (bytesRead = in.read(contents)) != -1){
                        strFileContents = new String(contents, 0, bytesRead);
                    }
                    JSONObject result = new JSONObject(strFileContents);
                    if (result.getString("token") != null){
                        String token = result.getString("token");
                        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key),
                                Context.MODE_PRIVATE);
                        sharedPref.edit().putString(context.getString(R.string.token_key), token);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                break;
        }
        return checklists;
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(List<Checklist> checklists) {
        if (isCancelled()) {
            checklists = null;
        }

        if (adapter != null) {
            if (checklists != null) {
                checklists.clear();
                checklists.addAll(this.checklists);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
