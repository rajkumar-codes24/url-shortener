package com.proj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShortenUrlRequest {

    @NotBlank(message = "URL cannot be empty")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    private String originalUrl;

    public ShortenUrlRequest() {
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}