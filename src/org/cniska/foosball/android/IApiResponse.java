package za.co.sovtech.deloitte.merlin.appcontroller;

import org.json.JSONObject;

public interface IApiResponse {

    void onResultReceived(JSONObject response);
    //void onError(JSONObject response);

}
