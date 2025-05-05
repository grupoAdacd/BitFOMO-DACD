package com.bitfomo.adapters.reddit;

import com.bitfomo.domain.model.RedditPost;
import com.bitfomo.domain.port.out.ExternalRedditApiPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación simplificada de ExternalRedditApiPort
 * que consulta los endpoints públicos /r/{subreddit}/new.json.
 */
public class RedditApiAdapter implements ExternalRedditApiPort {
    private static final String API_BASE = "https://www.reddit.com";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String userAgent;

    /**
     * @param userAgent Cadena User-Agent para Reddit (obligatorio para evitar 429)
     */
    public RedditApiAdapter(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit) {
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
