package com.example.jhw.check;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by JHW_PC on 2015-08-18.
 */
public class CsrfGetter {
    private String csrfString;
    private String urlString = "http://ec2-52-69-253-17.ap-northeast-1.compute.amazonaws.com:8000/pickroom/";
    private URL url;
    public String getCsrfString(String target){
        try{
            url = new URL(urlString+target+"/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            try {
                urlConnection.getContent();
            }catch(Exception e){
                e.printStackTrace();
            }

            List<String> cookies = urlConnection.getHeaderFields().get("set-cookie");
            Log.d("@COOKIE", "print cookies");
            if (cookies != null) {
                for (String cookie : cookies) {
                    Log.d("@COOKIE", cookie.split(";\\s*")[0]);
                    if (cookie.split(";\\s*")[0].contains("csrftoken=")) {
                        csrfString = cookie.split(";\\s*")[0];
                    }
                }
            }
            urlConnection.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
        return csrfString;
    }
}
