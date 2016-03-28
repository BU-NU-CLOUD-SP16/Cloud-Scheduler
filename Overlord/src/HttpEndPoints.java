/**
 * Created by Praveen on 3/26/2016.
 */

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static spark.Spark.*;

public class HttpEndPoints {

    private static final int HTTP_SUCCESS_RESPONSE = 200;
    private static final int HTTP_CREATE_CODE = 201;
    private static final int HTTP_CLIENT_ERROR = 400;
    private static final int HTTP_SERVER_ERROR = 500;
    private static final int SUCCESS = 1;

    private Overlord overlordHandle;


    public HttpEndPoints(Overlord overlordHandle) {
        this.overlordHandle = overlordHandle;
    }

    public void configureHttpEndPoints(){

        port(6000);
        post("/registerCEAgent", (request, response) -> {
            response.status(overlordHandle.registerCEAgent(request.body(), request.ip(), request.port()));
            System.out.println("Got register");
            return SUCCESS;
        });

        post("/requestNode", (request, response) -> {
            response.body( overlordHandle.requestNode( request.body() ) );
            response.type("application/json");

            if(response.body() == "")
                response.status(HTTP_SERVER_ERROR);
            else
                response.status(HTTP_CREATE_CODE);

            return response.body();
        });

        post("/releaseNode", (request, response) -> {
            overlordHandle.releaseNode( request.body() );
            response.status(HTTP_SUCCESS_RESPONSE);
            return SUCCESS;
        });

        post("/getNodeList", (request, response) -> {
           response.body( overlordHandle.getNodeList( request.body() ) );
            response.status(HTTP_SUCCESS_RESPONSE);
            response.type("application/json");
            return response.body();
        });
    }

}
