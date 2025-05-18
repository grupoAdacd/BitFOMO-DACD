package es.ulpgc.dacd.bitfomo.businessunit.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditEvent(String id, Instant ts, String title, String selftext, double sentiment) {}