package com.proj.service;

import com.proj.dto.ShortenUrlRequest;
import com.proj.dto.ShortenUrlResponse;

public interface UrlService {

	ShortenUrlResponse registerUrl(ShortenUrlRequest request);
	
	String getOriginalUrl(String shortCode);
}
