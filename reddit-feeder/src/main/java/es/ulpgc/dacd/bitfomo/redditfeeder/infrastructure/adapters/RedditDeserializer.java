package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RedditDeserializer {
    public List<RedditPost> parsePosts(String jsonData) {
        List<RedditPost> posts = new ArrayList<>();
        JSONObject json = new JSONObject(jsonData);
        JSONArray children = json.getJSONObject("data").getJSONArray("children");
        for (int i = 0; i < children.length(); i++) {
            RedditPost post = createPostFromJson(children.getJSONObject(i));
            posts.add(post);
        }
        return posts;
    }

    private RedditPost createPostFromJson(JSONObject postJson) {
        JSONObject data = postJson.getJSONObject("data");
        String id = data.getString("id");
        Instant createdUtc = Instant.ofEpochSecond(data.getLong("created_utc"));
        String subreddit = data.getString("subreddit");
        String author = data.getString("author");
        String title = data.getString("title");
        String selftext = data.optString("selftext", "");
        return new RedditPost(id, createdUtc, subreddit, author, title, selftext);
    }
}