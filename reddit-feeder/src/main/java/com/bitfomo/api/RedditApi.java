package com.bitfomo.api;

import com.bitfomo.common.fetch.DefaultFetch;

import java.util.ArrayList;

public class RedditApi extends DefaultFetch {
    private static final String baseFetchApiUrl = "https://www.reddit.com/r/%s/new.json?limit=100&t=all";
    private static ArrayList<String> subreddit = new ArrayList<String>();

    public RedditApi(String api_url, String addition) {
        super(api_url, addition);
    }

    public static void main(String[] args) {
        subreddit.add("Criptocurrency");
        subreddit.add("Bitcoin");
        subreddit.add("btc");
        subreddit.add("BitcoinNews");
        for (String s : subreddit) {
            String finalApiUrl = String.format(baseFetchApiUrl, s);
            RedditApi redditApi = new RedditApi(finalApiUrl, "");
            System.out.println(redditApi.fetchInformation());
        }
    }

    @Override
    public String fetchInformation() {
        return super.fetchInformation();
    }
}
