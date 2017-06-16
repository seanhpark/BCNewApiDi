package com.brightcove.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Main {

    public static final String oAuthClientId = "4de69a0b-5918-423f-a9f2-c505208b6adc";
    public static final String oAuthClientSecret = "DlAYySEZcCp7Y31R4-WrrA-if4mlsBUsXHCKBn-nB_Kx-Lks9Ywlwjxtolu5LwbShk1YFO2VbB9ief3RJ_lZ7A";
    public static final String accountId = "706104234001";
    public static final String masterfileUrl = "http://brightcove05.brightcove.com/o1/706104234001/706104234001_5309119220001_5309089424001.mp4?pubId=706104234001&videoId=5309089424001";

    public static final String accessTokenUrl = "https://oauth.brightcove.com/v3/access_token";
    public static final String createVideoUrl = "https://cms.api.brightcove.com/v1/accounts/ACCOUNT_ID/videos/";
    public static final String dynamicIngestUrl = "https://ingest.api.brightcove.com/v1/accounts/ACCOUNT_ID/videos/VIDEO_ID/ingest-requests";


    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public static String getAccessToken() throws Exception {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(oAuthClientId, oAuthClientSecret));

        // The use of an AuthCache and an HttpClient context is required to perform Preemptive authorization
        // as required by oauth.brightcove.com
        AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost("oauth.brightcove.com", 443, "https"), new BasicScheme());

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        HttpPost request = new HttpPost(accessTokenUrl);

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        request.setEntity(new UrlEncodedFormEntity(postParameters));

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request, context);

        System.out.println(response.getStatusLine());
        HttpEntity entity = response.getEntity();
        String strEntity = EntityUtils.toString(entity);
        System.out.println(strEntity);

        AccessTokenResponse atr = gson.fromJson(strEntity, AccessTokenResponse.class);
        System.out.println(atr.getTokeyType());
        return atr.getAccessToken();

    }

    public static Object executeAuthorizedRequest(HttpUriRequest request, Object returnType) throws Exception {
        String accessToken = getAccessToken();
        request.setHeader("Authorization", "Bearer " + accessToken);

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity);

        System.err.println(responseString);
        return gson.fromJson(responseString, returnType.getClass());
    }

    public static CreateVideoResponse createVideo(String accountId) throws Exception {
        Map<String, String> videoData = new HashMap<String, String>();
        videoData.put("name", "api test video");

        String url = createVideoUrl.replace("ACCOUNT_ID", accountId);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(gson.toJson(videoData)));

        return (CreateVideoResponse)executeAuthorizedRequest(request, new CreateVideoResponse());
    }

    public static DynamicIngestResponse submitDynamicIngest(String accountId, String videoId, String masterUrl) throws Exception {
        Map<String, String> masterData = new HashMap<String, String>();
        masterData.put("url", masterUrl);

        Map<String, Object> requestData = new HashMap<String, Object>();
        requestData.put("master", masterData);

        String url = dynamicIngestUrl.replace("ACCOUNT_ID", accountId).replace("VIDEO_ID", videoId);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(gson.toJson(requestData)));

        return (DynamicIngestResponse)executeAuthorizedRequest(request, new DynamicIngestResponse());
    }

    public static void main(String[] args) throws Exception {
	    // write your code here

        //String token = getAccessToken();
        //Print Access Token
        //System.out.println(token);

        CreateVideoResponse video = createVideo(accountId);

//        System.out.println(video);

        System.out.println("Submitting Dynamic Ingest request");
        DynamicIngestResponse di = submitDynamicIngest(accountId, video.getId(), masterfileUrl);
        System.out.println(di.getId());
    }
}
