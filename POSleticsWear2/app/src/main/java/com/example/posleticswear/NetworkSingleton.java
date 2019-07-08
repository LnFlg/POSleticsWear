package com.example.posleticswear;

import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class NetworkSingleton {

    private static NetworkSingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private String urlPos = "https://posletics.herokuapp.com/api/pos";
    private String urlGetRoute = "https://posletics.herokuapp.com/api/route?user_id=";

    //TODO urls anpassen
    private String urlPosUpvote = "https://posletics.herokuapp.com/api/pos";
    private String urlPosDownvote = "https://posletics.herokuapp.com/api/pos";

    private NetworkSingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized NetworkSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkSingleton(context);
        }
        return instance;
    }


    public void getRouteFromServer(final int userId) {

        final JSONArray[] routeIds = new JSONArray[1];
        JsonArrayRequest jsnArrq = new JsonArrayRequest(
                Request.Method.GET,
                urlGetRoute.concat(String.valueOf(userId)),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i=0; i<response.length();++i){
                            try {
                                int id = response.getJSONObject(i).getInt("id");
                                RuntimeData.getInstance().addToRoute(id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(RuntimeData.getInstance().getRoute()==null){
                            RuntimeData.getInstance().setDiscoveryRadius(750d);
                        }else{RuntimeData.getInstance().setDiscoveryRadius(500d);}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });

        getRequestQueue().add(jsnArrq);


    }


    public void sendPOS(double lat, double lng, int userId) {
        showProgressDialog();

        Map<String, String> postParam = new HashMap<String, String>();
        postParam.put("lat", String.valueOf(lat));
        postParam.put("lng", String.valueOf(lng));
        postParam.put("uID", String.valueOf(userId));


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                urlPos,
                new JSONObject(postParam),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        hideProgressDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsonObjReq);
    }




    public void getAllPos(){
        JsonArrayRequest jsnArrq = new JsonArrayRequest(
                Request.Method.GET,
                urlPos,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Map<Integer, Pos> posMap=null;
                        for (int i =0; i<response.length();++i) {
                            try {
                                double lat = response.getJSONObject(i).getDouble("lat");
                                double lng = response.getJSONObject(i).getDouble("lng");
                                int id = response.getJSONObject(i).getInt("id");
                                Pos toAdd = new Pos(lat,lng,id);

                                //2 höchsten Hashtags finden und speichern
                                JSONArray hashtags = response.getJSONObject(i).getJSONArray("hashtags");
                                int first=0, second=0;
                                String firstHashtag=null, secondHashtag=null;
                                for(int j =0; j<=hashtags.length();++j){
                                    if(hashtags.getJSONObject(j).getInt("upvotes")>= first){
                                        firstHashtag = hashtags.getJSONObject(j).getString("name");
                                        first= hashtags.getJSONObject(j).getInt("upvotes");
                                    }else if (hashtags.getJSONObject(j).getInt("upvotes")>= second){
                                        secondHashtag = hashtags.getJSONObject(j).getString("name");
                                        second= hashtags.getJSONObject(j).getInt("upvotes");
                                    }
                                }

                                ArrayList hashtagNames = new ArrayList<String>();
                                hashtagNames.add(firstHashtag);
                                hashtagNames.add(firstHashtag);
                                toAdd.setHighestHashtags(hashtagNames);


                                posMap.put(new Integer(id),toAdd);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        RuntimeData.getInstance().setAllPos(posMap);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        hideProgressDialog();
                    }
                });
        getRequestQueue().add(jsnArrq);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    public void upvotePos(int id){
        //TODO
    }
    public void downvotePos(int id){
        //TODO
    }

    private JSONObject findObjInJsonArray(String key, String value, JSONArray jsonArray) {
        JSONObject result = null;

        for (int i = 0; i < jsonArray.length(); ++i) {
            boolean decider = false;
            JSONObject jsonObject;
            try {
                jsonObject=jsonArray.getJSONObject(i);
                decider = jsonArray.getJSONObject(i).getString(key).equalsIgnoreCase(value);
            } catch (JSONException je) {
                je.printStackTrace();
                return null;
            }
            if (decider) { result = jsonObject;}
        }
        return result;
    }





}
