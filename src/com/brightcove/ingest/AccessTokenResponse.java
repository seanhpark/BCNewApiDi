package com.brightcove.ingest;

/**
 * Created by spark on 2017. 6. 15..
 */
public class AccessTokenResponse {

    private String accessToken;
    private String tokenType;
    private String expireIn;

    public String getAccessToken() {return accessToken;}
    public String getTokeyType() {return tokenType;}
    public void setAccessToken(String accessToken) {this.accessToken = accessToken;}
}
