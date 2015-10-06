package za.co.sovtech.deloitte.merlin.appcontroller;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import za.co.sovtech.deloitte.merlin.R;
import za.co.sovtech.deloitte.merlin.utils.AlertManager;
import za.co.sovtech.deloitte.merlin.utils.Constants;

public class ApiRequest {

    Context oContext;
    IApiResponse apiResponse;

    public ApiRequest(Context oContext, IApiResponse apiResponse) {
        this.oContext = oContext;
        this.apiResponse = apiResponse;
    }

    public static long getMinutesDifference(long timeStart, long timeStop) {
        long diff = timeStop - timeStart;
        long diffMinutes = diff / (60 * 1000);
        return diffMinutes;
    }

    public void jsonRequest(String url) {

        if (CheckConnection.isNetworkAvailable(oContext)) {
            // We first check for cached request
            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(url);

            if (entry != null) {
                Calendar calendar = Calendar.getInstance();
                long serverDate = AppController.getInstance().getRequestQueue().getCache().get(url).serverDate;
                if (getMinutesDifference(serverDate, calendar.getTimeInMillis()) >= Constants.REFRESH_TIME) {
                    //AppController.getInstance().getRequestQueue().getCache().invalidate(Constants.URL_GET_GRADES, true);
                    freshRequest(url);
                } else {
                    // fetch the data from cache
                    try {
                        String data = new String(entry.data, "UTF-8");
                        try {
                            apiResponse.onResultReceived(new JSONObject(data));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                freshRequest(url);
            }
        } else {
            AlertManager.showToast(oContext, oContext.getString(R.string.No_internet_connection));
        }
    }

    public void freshRequest(String url) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            // Tag used to cancel the request
            String tag_json_obj = "json_obj_req";
            final ProgressDialog pDialog = new ProgressDialog(oContext);
            try {

                pDialog.setMessage("Loading...");
                pDialog.show();
            } catch (Exception e) {
            }

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, (String) null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Events", response.toString());
                            pDialog.hide();

                            apiResponse.onResultReceived(response);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Events", "Error: " + error.getMessage());

                    //AlertManager.showToast(oContext, error.getMessage());
                    // hide the progress dialog
                    pDialog.hide();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        } else {
            AlertManager.showToast(oContext, oContext.getString(R.string.No_internet_connection));
        }
    }

    public void postRequest(String url) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            // Tag used to cancel the request
            String tag_json_obj = "post_request";
            final ProgressDialog pDialog = new ProgressDialog(oContext);
            pDialog.setMessage("Loading...");
            pDialog.show();

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, (String) null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("Response ", response.toString());

                            pDialog.hide();
                            apiResponse.onResultReceived(response);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Response", "Error: " + error.getMessage());
                    // hide the progress dialog
                    pDialog.hide();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        } else {
            AlertManager.showToast(oContext, oContext.getString(R.string.No_internet_connection));
        }
    }

    public void postRequestForImageUpload(String url, final String base64_data) {
        if (CheckConnection.isNetworkAvailable(oContext)) {
            // Tag used to cancel the request
            String tag_json_obj = "json_image_obj_req";

            final ProgressDialog pDialog = new ProgressDialog(oContext);
            pDialog.setMessage("Loading...");
            pDialog.show();

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    url, (String) null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Image upload response", response.toString());
                            pDialog.hide();
                            apiResponse.onResultReceived(response);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Image upload ", "Error: " + error.getMessage());
                    pDialog.hide();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    //params.put("userid", Preferences.getUserId(oContext));
                    params.put("imagecontent", base64_data);
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        } else {
            AlertManager.showToast(oContext, oContext.getString(R.string.No_internet_connection));
        }
    }

    public void stringRequest(String url) {

        final ProgressDialog pDialog = new ProgressDialog(oContext);
        pDialog.setMessage("Loading...");
        pDialog.show();

        //final String URL = "/volley/resource/recent.xml";
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                VolleyLog.v("Response:%n %s", response);
                String str = "";
                try {
                    str = URLEncoder.encode(response, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                JSONObject object = null;
                try {
                    object = new JSONObject(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                apiResponse.onResultReceived(object);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                pDialog.hide();
            }
        });

        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }
}
