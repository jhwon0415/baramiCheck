package com.example.jhw.check;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//test commit
/**
 * Created by JHW_PC on 2015-08-17.
 */
public class HttpRequester {
    static final CsrfGetter csrf_getter = new CsrfGetter();
    static String serviceDomain = "";
    static String postUrl = serviceDomain + "";
    static final String CRLF= "\r\n";
    static final String twoHyphens = "--";
    static String boundary = "*****mgd*****";
    private String csrftoken;
    enum enum_result {success, connect_error, wrong, fail_etc}
    private DataOutputStream dataStream = null;
    private BufferedWriter dataStreamWriter = null;
    private CookieManager cookieManager;
    private CookieStore cookieStore;
    private URI baseUri;
    private String baseString;


    public HttpRequester(){
        cookieManager = new CookieManager(null,CookiePolicy.ACCEPT_ALL);
        cookieStore = cookieManager.getCookieStore();
        cookieStore.removeAll();
        baseString =
                "http://ec2-52-69-253-17.ap-northeast-1.compute.amazonaws.com:8000/";
        CookieHandler.setDefault(cookieManager);
        try{
            baseUri = new URI("http://ec2-52-69-253-17.ap-northeast-1" +
                    ".compute.amazonaws.com:8000/");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public String login(String id, String password,Context context){
        String name = null;
        String image_profile=null;
        enum_result login_result;
        try{
            String body = "username=" + id + "&" + "password=" + password;
            String csrftoken;
            csrftoken = new CsrfGetter().getCsrfString("login");
            URL url = new URL("http://ec2-52-69-253-17.ap-northeast-1.compute.amazonaws.com:8000/pickroom/loginret/");
            HttpURLConnection  conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            body = "csrfmiddlewaretoken=" + csrftoken.substring(10) + "&" + body;
            Log.d("@body", body);
            //post setting(include csrftoken)
            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            Log.d("Connection Out", out.toString());
            out.write(body.getBytes("UTF-8"));
            out.flush();
            out.close();
            //send

            StringBuffer sb;
            byte[] b;
            if (conn.getErrorStream() != null) {
                InputStream error = conn.getErrorStream();
                sb = new StringBuffer();
                b = new byte[4096];
                for (int n; (n = error.read(b)) != -1; ) {
                    sb.append(new String(b, 0, n));
                }
                Log.d("Connection Error", sb.toString());
            }//get error


            InputStream in = new BufferedInputStream(conn.getInputStream());
            sb = new StringBuffer();
            b = new byte[4096];
            for (int n; (n = in.read(b)) != -1; ) {
                sb.append(new String(b, 0, n));
            }
            Log.d("Connection input", sb.toString());
            conn.disconnect();
            //get result

            JSONObject json = new JSONObject(sb.toString());
            if (json.has("message")) {
                Log.d("InputData", (String) json.get("message"));
                login_result= enum_result.wrong;
                return "false";
            }//wrong id or password


            name = (String) json.get("nick");
            image_profile = (String) json.get("img_url");

            login_result = enum_result.success;
        }catch (Exception e){
            Log.d("LOGIN","fail");
            login_result = enum_result.fail_etc;
            e.printStackTrace();
        }
        return "success";
    }

    public int uploadRegister(String userName,
                              String realName,
                              String password,
                              String phonenumber){
        try{

            URL connURL = new URL("http://ec2-52-69-253-17.ap-northeast-1" +
                    ".compute.amazonaws.com:8000/pickroom/register/");

            HttpURLConnection conn = (HttpURLConnection)connURL.openConnection();
            setConnection(conn);
            assert csrftoken != null;

            List<HttpCookie> cookies = cookieManager.getCookieStore().get(baseUri);
            for (HttpCookie cookie : cookies) {
                Log.d("TEST", cookie.toString());
                if (cookie.toString().startsWith("csrftoken")) {
                    csrftoken = cookie.toString();
                    Log.d("CSRF COOKIE", csrftoken);
                }
            }
            if(csrftoken == null)
                csrftoken = csrf_getter.getCsrfString("login");

            dataStream = new DataOutputStream(conn.getOutputStream());
            dataStreamWriter = new BufferedWriter(new OutputStreamWriter(dataStream,"UTF-8"));
            writeFormField("csrfmiddlewaretoken", csrftoken.substring(10));
            writeFormField("username", userName);
            writeFormField("realname",realName);
            writeFormField("password",password);
            writeFormField("phonenumber",phonenumber);
            dataStreamWriter.write(twoHyphens + boundary + twoHyphens + CRLF);
            dataStreamWriter.close();
            dataStream.flush();
            dataStream.close();
            dataStream = null;
            StringBuffer sb;
            byte[] b;
            if (conn.getErrorStream() != null) {
                InputStream error = conn.getErrorStream();
                sb = new StringBuffer();
                b = new byte[4096];
                for (int n; (n = error.read(b)) != -1; ) {
                    sb.append(new String(b, 0, n));
                }
                Log.d("Connection Error", sb.toString());
            }//get error


            InputStream in = new BufferedInputStream(conn.getInputStream());
            sb = new StringBuffer();
            b = new byte[4096];
            for (int n; (n = in.read(b)) != -1; ) {
                sb.append(new String(b, 0, n));
            }
            Log.d("Connection input", sb.toString());
            conn.disconnect();

        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
        return 0;
    }
    private void setConnection(HttpURLConnection conn){
        try{
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //asd
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int dropOut(){


        try{
            URL connURL = new URL("http://ec2-52-69-253-17.ap-northeast-1" +
                    ".compute.amazonaws.com:8000/pickroom/upload/");
            HttpURLConnection conn = (HttpURLConnection) connURL.openConnection();
            setConnection(conn);
            conn.connect();
            conn.disconnect();

        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * write one form field to dataSream
     * @param fieldName
     * @param fieldValue
     */
    private void writeFormField(String fieldName, String fieldValue) {
        try
        {
//            dataStream.writeBytes(twoHyphens + boundary + CRLF);
//            dataStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF);
//            dataStream.writeBytes(CRLF);
//            dataStream.writeBytes(fieldValue);
//            dataStream.writeBytes(CRLF);

            dataStreamWriter.write(twoHyphens + boundary + CRLF);
            dataStreamWriter.write("Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF);
            dataStreamWriter.write(CRLF);
            dataStreamWriter.write(fieldValue);
            dataStreamWriter.write(CRLF);
        }
        catch(Exception e)
        {
            System.out.println("GeoPictureUploader.writeFormField: got: " + e.getMessage());
            //Log.e(TAG, "GeoPictureUploader.writeFormField: got: " + e.getMessage());
        }
    }
    /**
     * write one file field to dataSream
     * @param fieldName - name of file field
     * @param fieldValue - file name
     * @param type - mime type
     * @param fis - stream of bytes that get sent up
     */
    private void writeFileField(
            String fieldName,
            String fieldValue,
            String type,
            FileInputStream fis)
    {
        try
        {
            // opening boundary line
            dataStreamWriter.write(twoHyphens + boundary + CRLF);
            dataStreamWriter.write("Content-Disposition: form-data; name=\""
                    + fieldName
                    + "\";filename=\""
                    + fieldValue
                    + "\""
                    + CRLF);
            dataStreamWriter.write("Content-Type: " + type + CRLF);
            dataStreamWriter.write(CRLF);

            // create a buffer of maximum size
            int bytesAvailable = fis.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            // read file and write it into form...
            int bytesRead = fis.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
                dataStream.write(buffer, 0, bufferSize);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fis.read(buffer, 0, bufferSize);
            }

            // closing CRLF
            dataStreamWriter.write(CRLF);
        }
        catch(Exception e)
        {
            System.out.println("GeoPictureUploader.writeFormField: got: " + e.getMessage());
            //Log.e(TAG, "GeoPictureUploader.writeFormField: got: " + e.getMessage());
        }
    }

    /**
     * @param conn
     * @return
     */
    private String getResponse(HttpURLConnection conn)
    {
        try
        {
            DataInputStream dis = new DataInputStream(conn.getInputStream());
            byte []        data = new byte[1024];
            int             len = dis.read(data, 0, 1024);

            dis.close();
            int responseCode = conn.getResponseCode();

            if (len > 0)
                return new String(data, 0, len);
            else
                return "";
        }
        catch(Exception e)
        {
            System.out.println("GeoPictureUploader: biffed it getting HTTPResponse");
            //Log.e(TAG, "GeoPictureUploader: biffed it getting HTTPResponse");
            return "";
        }
    }
}
