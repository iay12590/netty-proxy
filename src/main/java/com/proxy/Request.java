package com.proxy;

import java.util.HashMap;

/**
 * Created by jerry on 2016/8/23.
 */
public class Request {

    public final static String POST = "POST";
    public final static String GET = "GET";


    private HashMap<String, String> httpHeaders = new HashMap<String, String>();
    private String method;
    private String uri;

    public Request setHeader(String headerName, String headerValue){
        if(StringUtils.isEmpty(headerName) || StringUtils.isEmpty(headerValue)){
            throw new NullPointerException("header empty");
        }
        httpHeaders.put(headerName, headerValue);
        return this;
    }

    public Request method(String method){
        this.method = method;
        return this;
    }

    /**
     *
     * @param uri example：/v6/welcome
     * @return
     */
    public Request uri(String uri){
        this.uri = uri;
        return this;
    }

    public String getUri() {
        return uri;
    }
}
