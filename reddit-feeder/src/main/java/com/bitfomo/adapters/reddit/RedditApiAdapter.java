package com.bitfomo.adapters.reddit;

import com.bitfomo.domain.model.RedditPost;
import com.bitfomo.domain.port.out.ExternalRedditApiPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class RedditApiAdapter implements ExternalRedditApiPort {
    private static final String TOKEN_URL = "https://www.reddit.com/api/v1/access_token";
    private static final String API_BASE  = "https://oauth.reddit.com";
    private static final MediaType FORM   = MediaType.parse("application/x-www-form-urlencoded");

    private final OkHttpClient client = new OkHttpClient.Builder().build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String bearerToken;
    private final String userAgent;

    public RedditApiAdapter(String clientId, String clientSecret, String userAgent) {
        this.userAgent   = userAgent;
        this.bearerToken = fetchToken(clientId, clientSecret);
    }

    //TODO erase this to just use the public end points of Reddit
    private String fetchToken(String id, String secret) {
        String credential = Credentials.basic(id, secret);
        RequestBody body = RequestBody.create("grant_type=client_credentials", FORM);
        Request req = new Request.Builder()
                .url(TOKEN_URL)
                .header("Authorization", credential)
                .header("User-Agent",  userAgent)
                .post(body)
                .build();

        Response resp = null;
        try {
            resp = client.newCall(req).execute();
            JsonNode node = mapper.readTree(resp.body().string());
            return node.get("access_token").asText();
        } catch (IOException e) {
            throw new RuntimeException("Error obteniendo token de Reddit", e);
        } finally {
            if (resp != null) {
                resp.body().close();
            }
        }
    }

    @Override
    public List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit) {
        List<RedditPost> posts = new ArrayList<>();
        for (String sub : subreddits) {
            HttpUrl url = HttpUrl.parse(API_BASE + "/r/" + sub + "/new")
                    .newBuilder()
                    .addQueryParameter("limit", String.valueOf(limit))
                    .build();

            Request req = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("User-Agent",  userAgent)
                    .get()
                    .build();

            Response resp = null;
            try {
                resp = client.newCall(req).execute();
                JsonNode items = mapper.readTree(resp.body().string())
                        .path("data")
                        .path("children");
                for (JsonNode child : items) {
                    JsonNode d = child.path("data");
                    posts.add(new RedditPost(
                            d.get("id").asText(),
                            Instant.ofEpochSecond(d.get("created_utc").asLong()),
                            sub,
                            d.get("author").asText(),
                            d.get("title").asText(),
                            d.get("selftext").asText(),
                            null
                    ));
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error fetching /r/" + sub, ex);
            } finally {
                if (resp != null) {
                    resp.body().close();
                }
            }
        }
        return posts;
    }
}
