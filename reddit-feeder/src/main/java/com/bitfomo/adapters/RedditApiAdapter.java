package com.bitfomo.adapters;

import com.bitfomo.domain.RedditPost;
import com.bitfomo.domain.ExternalRedditApiPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RedditApiAdapter implements ExternalRedditApiPort {
    private static final String API_BASE = "https://www.reddit.com";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String userAgent;

    public RedditApiAdapter(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public List<RedditPost> fetchLatestPosts(@NotNull List<String> subreddits, int limit) {
        List<RedditPost> posts = new ArrayList<>();
        for (String sub : subreddits) {
            HttpUrl url = HttpUrl.parse(API_BASE + "/r/" + sub + "/new.json")
                    .newBuilder()
                    .addQueryParameter("limit", String.valueOf(limit))
                    .build();

            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                    .get()
                    .build();

            try (Response resp = client.newCall(req).execute()) {
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
            }
        }
        return posts;
    }
}
