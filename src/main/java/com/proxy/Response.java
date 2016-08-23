package com.proxy;

/**
 * Created by jerry on 2016/8/23.
 */
public class Response {

    public final static String JSON_CONTENT_TYPE= "application/json; charset=utf-8";
    /**
     *
     */
    private String content;
    public int statusCode;
    private String contentType;

    /**
     * 返回结果
     * @param content
     */
    void setResponse(String content){
        this.content = content;
    }

    void setStatusCode(int code){
        this.statusCode = code;
    }

    public String getContent() {
        return content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return this.contentType;
    }
}
