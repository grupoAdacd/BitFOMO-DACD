package com.bitfomo.adapters.serialization;

import com.bitfomo.domain.model.RedditPost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedditPostSerializer {
    private final ObjectMapper mapper;

    public RedditPostSerializer() {
        this.mapper = new ObjectMapper();
    }

    public String serialize(RedditPost post) throws JsonProcessingException {
        return mapper.writeValueAsString(post);
    }
}
