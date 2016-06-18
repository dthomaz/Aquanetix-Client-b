package uk.co.aquanetix.network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Enough meta data to create an HttpURLConnection.
 * This class is used to decouple the creation of an HTTP request and
 * its execution, so that we can cache requests in off-line mode.
 */
public class RequestDescription {

    /**
     * Convenience method to easily generates RequestDescription instances with authentication headers
     */
    public static RequestDescription makeAuthenticated(Context ctx, String method, int urlResource, Object... urlVars) {
        // Build the base URL
        AquanetixSharedPreferences p = new AquanetixSharedPreferences(ctx);
        String url = ctx.getString(R.string.url_server) + ctx.getString(urlResource);
        // Replace all variables
        url = url.replaceAll(":company_id", "" + p.getCompanyId());
        for (int i=0; i<urlVars.length; i+=2) {
            url = url.replaceAll(urlVars[i].toString(), urlVars[i+1].toString());
        }
        // Create RequestDescription and set headers
        RequestDescription req = new RequestDescription(url, method);
        if (p.getUUID()!=null) {
            req.addAuth(p.getUUID(), p.getDeviceSecret());
        }
        if (p.getUserId()>1) {
            req.addUserId(p.getUserId());
        }
        return req;
    }
    
	private final String url;
	private final String method;
	private final Map<String, String> headers;
	private final Map<String, String> params;
	
	public RequestDescription(String url, String method) {
	    this.url = url;
	    this.method = method;
	    this.headers = new HashMap<String, String>();
	    this.params = new HashMap<String, String>();
	}
	
    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getParams() { return params; }

    public void addAuth(String deviceUUID, String deviceSecret) {
	    headers.put("X-Device-UUID", deviceUUID);
	    headers.put("X-Device-Secret", deviceSecret);
	}
	
	public void addUserId(int userId) {
	    headers.put("X-Current-User", "" + userId);
	}
	
	public void addHeader(String key, String value) {
	    headers.put(key, value);
	}
	
	public void addParam(String key, String value) {
	    params.put(key, value);
	}
	
	public String toJson() {
	    try {
            JSONObject json = new JSONObject();
            json.put("url", url);
            json.put("method", method);
            json.put("headers", new JSONObject(headers));
            json.put("params", new JSONObject(params));
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
	}
	
    public static RequestDescription fromJson(String jsonStr) {
	    try {
	        JSONObject json = new JSONObject(jsonStr);
	        
	        String url = json.getString("url");
	        String method = json.getString("method");
	        RequestDescription req = new RequestDescription(url, method);
	        
	        //Headers
	        JSONObject headerJson = json.getJSONObject("headers");
	        Iterator<String> hKeys = headerJson.keys();
	        while (hKeys.hasNext()) {
	            String key = hKeys.next();
	            String header = headerJson.getString(key);
	            req.addHeader(key, header);
	        }
	        
	        //Params
	        JSONObject paramJson = json.getJSONObject("params");
	        Iterator<String> pKeys = paramJson.keys();
	        while (pKeys.hasNext()) {
	            String key = pKeys.next();
	            String param = paramJson.getString(key);
	            req.addParam(key, param);
	        }
	        
	        return req;
	        
	    } catch (JSONException e) {
	        throw new RuntimeException(e);
	    }
	}
	
    //Send the request synchronously and return result
    public String sendSync() {
        HttpURLConnection conn = null;
        try {
            //Create a connection
            URL url = new URL(getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(getMethod());
            //Set headers
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            if (conn instanceof HttpsURLConnection) {
                // gzip compression is enabled by default for HttpURLConnection, but not Https
                conn.setRequestProperty("Accept-Encoding", "gzip");
            }
            for (String key:getHeaders().keySet()) {
                conn.setRequestProperty(key, getHeaders().get(key));
            }
            //Create parameters string
            StringBuilder sb = new StringBuilder();
            for (String key:getParams().keySet()) {
                if (sb.length()>0) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(key, "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(getParams().get(key), "UTF-8"));
            }
            //Send params in body
            if (sb.length()>0) {
                byte[] paramsEnc = sb.toString().getBytes("UTF-8");
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(paramsEnc.length);
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(paramsEnc);
                out.close();
            }
            // Handle gzip for HTTPS
            InputStream inStream = conn.getInputStream();
            String contentEncoding = conn.getContentEncoding();
            if ( contentEncoding!=null && 
                    contentEncoding.equalsIgnoreCase("gzip") && 
                    !(inStream instanceof GZIPInputStream) ) {
                inStream = new GZIPInputStream(inStream);
            }
            //Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String line = null;
            StringBuilder result = new StringBuilder();
            while ((line = in.readLine()) != null) {
                result.append(line + "\n");
            }
            in.close();
            // Log what we send
            AquaLog.info(this, conn.getResponseCode() + "\t" + result.toString());
            return result.toString();
        } catch (IOException e) {
            AquaLog.info(this, "HTTP request error ", e);
            return null;
        } finally {
            if (conn!=null) {
                conn.disconnect();
            }
        }
    }
    
    public void sendAsync(final OnResult<String> callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return sendSync();
            }
            @Override
            protected void onPostExecute(String result) {
                callback.getResult(result);
            }
        }.execute();
    }
	
}
