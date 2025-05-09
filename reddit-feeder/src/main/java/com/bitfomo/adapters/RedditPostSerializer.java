package com.bitfomo.adapters;

import com.bitfomo.domain.RedditPost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class RedditPostSerializer {
    private final ObjectMapper mapper;

    public RedditPostSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public String serialize(RedditPost post) throws JsonProcessingException {
        return mapper.writeValueAsString(post);
    }
}
