package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedditConnection {
    private static final String BASE_URL = "https://www.reddit.com/r/";
    private static final String AFTER = "/new.json?limit=%d";
    private final String userAgent;
    private final OkHttpClient client;

    public RedditConnection(String userAgent) {
        this.userAgent = userAgent;
        this.client = new OkHttpClient();
    }

    public String fetchJsonData(String subreddit, int limit) {
        String url = buildSubredditUrl(subreddit, limit);
        Request request = buildRequest(url);
        return executeRequest(request);
    }

    private String buildSubredditUrl(String subreddit, int limit) {
        return BASE_URL + subreddit + String.format(AFTER, limit);
    }

    private Request buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .build();
    }

    private String executeRequest(Request request) {
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Reddit data", e);
        }
    }
}