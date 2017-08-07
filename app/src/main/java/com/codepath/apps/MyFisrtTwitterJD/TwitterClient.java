package com.codepath.apps.MyFisrtTwitterJD;

import android.content.Context;

import com.codepath.apps.MyFisrtTwitterJD.models.Tweet;
import com.codepath.apps.MyFisrtTwitterJD.models.User;
import com.codepath.oauth.OAuthBaseClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.util.List;

//import org.scribejava.builder.api.Api;



/*
 *
 * This is the object responsible for communicating with a REST API.
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes:
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 *
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 *
 */
public class TwitterClient extends OAuthBaseClient {
	//public static final Class <? extends Api> REST_API_CLASS = TwitterApi.class;
    public static final BaseApi REST_API_INSTANCE = TwitterApi.instance();
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "g9l2YcmjnKSBe0FWdosHG9GSf";       // Change this
	public static final String REST_CONSUMER_SECRET = "II6ZvB1DzhPqLv7bFygMoUBlheVKmv1njOMzdw3RMXdmYDafHW"; // Change this
	//public static final String REST_CALLBAK_URL = "oauth://twitterApp";

	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	//public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets";

    public TwitterClient(Context context) {
        super(context, REST_API_INSTANCE, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

	// CHANGE THIS
	// DEFINE METHODS for different API endpoints here
	/*public void getInterestingnessList(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("?nojsoncallback=1&method=flickr.interestingness.getList");
		// Can specify query string params directly or through RequestParams.
		RequestParams params = new RequestParams();
		params.put("format", "json");
		client.get(apiUrl, params, handler);
	}*/

	/*public void getHomeTimeline(AsyncHttpResponseHandler handler){
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("since_id",1);
        getClient().get(apiUrl,params, handler);


    }*/

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */


    public void getHomeTimeline(TimelineResponseHandler handler) {
        getNewerHomeTimeline(handler, 1L);
    }

    public void getNewerHomeTimeline(final TimelineResponseHandler handler, Long sinceId) {
        getHomeTimeline(handler, sinceId, "since_id");
    }

    public void getOlderHomeTimeline(final TimelineResponseHandler handler, Long maxId) {
        getHomeTimeline(handler, maxId, "max_id");
    }

    public void getHomeTimeline(final TimelineResponseHandler handler, Long id, String paramName) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put(paramName, id);
        getClient().get(apiUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<Tweet> tweets = mapper.readValue(responseBody, new TypeReference<List<Tweet>>() {
                    });
                    handler.onSuccess(tweets);
                } catch (IOException e) {
                    handler.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }
        });
    }

    public void getProfile(final TwitterUserResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        getClient().get(apiUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    User user = mapper.readValue(responseBody, User.class);
                    handler.onSuccess(user);
                } catch (IOException e) {
                    handler.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }
        });
    }

    public void updateStatus(String status, final StatusUpdateResponseHandler handler) {
        replyToStatus(status, null, handler);
    }

    public void replyToStatus(String status, Long statusId, final StatusUpdateResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", status);
        if (statusId != null) {
            params.put("in_reply_to_status_id", statusId);
        }
        getClient().post(apiUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                handler.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }
        });
    }

    public void getStatus(Long id, final TweetResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/show.json");
        RequestParams params = new RequestParams();
        params.put("id", id);
        getClient().get(apiUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Tweet tweet = mapper.readValue(responseBody, Tweet.class);
                    handler.onSuccess(tweet);
                } catch (IOException e) {
                    handler.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }
        });
    }

    public void retweet(Long id, final TweetResponseHandler handler) {
        String apiUrl = getApiUrl(String.format("statuses/retweet/%d.json", id));
        getClient().post(apiUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Tweet tweet = mapper.readValue(responseBody, Tweet.class);
                    handler.onSuccess(tweet);
                } catch (IOException e) {
                    handler.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }
        });
    }

    public void favorite(Long id, final TweetResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/create.json");
        RequestParams params = new RequestParams();
        params.put("id", id);
        getClient().post(apiUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Tweet tweet = mapper.readValue(responseBody, Tweet.class);
                    handler.onSuccess(tweet);
                } catch (IOException e) {
                    handler.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }
        });
    }

    public void unfavorite(Long id, final TweetResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/destroy.json");
        RequestParams params = new RequestParams();
        params.put("id", id);
        getClient().post(apiUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Tweet tweet = mapper.readValue(responseBody, Tweet.class);
                    handler.onSuccess(tweet);
                } catch (IOException e) {
                    handler.onFailure(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                handler.onFailure(error);
            }

        });
    }


    public interface TimelineResponseHandler {

        void onSuccess(List<Tweet> tweets);

        void onFailure(Throwable error);

    }

    public interface TwitterUserResponseHandler {

        void onSuccess(User user);

        void onFailure(Throwable error);

    }

    public interface StatusUpdateResponseHandler {

        void onSuccess();

        void onFailure(Throwable error);

    }

    public interface TweetResponseHandler {

        void onSuccess(Tweet tweet);

        void onFailure(Throwable error);

    }


}