package com.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jerry on 2016/8/23.
 */
public class ExceptRequest {

    private Request exceptRequest;
    private Response exceptResponse = new Response();
    static List<ExceptRequest> exceptRequestList = new ArrayList<ExceptRequest>();

    /**
     * 预期
     */
    public static ExceptRequest except(Request req){
        ExceptRequest exceptRequest =  new ExceptRequest();
        exceptRequest.exceptRequest = req;
        return exceptRequest;
    }

    public ExceptRequest response(final String exceptResponse){
        this.exceptResponse.setResponse(exceptResponse);
        return this;
    }

    public void submit(){
        this.exceptRequestList.add(this);
    }
    void remove(){
        if(exceptRequestList.contains(this)) {
            exceptRequestList.remove(this);
        }
    }

    Request getExceptRequest() {
        return exceptRequest;
    }

    Response getExceptResponse() {
        return exceptResponse;
    }
}
