package com.proj.dto;

public class ShortenUrlResponse {

    private String originalUrl;
    private String shortCode;
    private String shortUrl;

    public ShortenUrlResponse() {
    }

    public ShortenUrlResponse(
            String originalUrl,
            String shortCode,
            String shortUrl) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}

