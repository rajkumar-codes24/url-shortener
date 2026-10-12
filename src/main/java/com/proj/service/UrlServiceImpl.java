package com.proj.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.proj.dto.ShortenUrlRequest;
import com.proj.dto.ShortenUrlResponse;
import com.proj.entity.ShortenedUrl;
import com.proj.exception.UrlNotFoundException;
import com.proj.repository.UrlRepository;
import com.proj.shortcode_generator.RandomString;
import com.proj.urlvalidation.UrlValidator;

@Service
public class UrlServiceImpl implements UrlService {

	@Value("${app.base-url}")
	private String baseURL;
	private final UrlRepository repository;
	private final UrlValidator validator;

	UrlServiceImpl(UrlRepository repository, UrlValidator validator) {
		System.out.println("UrlServiceImpl.UrlServiceImpl()");
		this.repository = repository;
		this.validator = validator;
	}

	@Override
	public ShortenUrlResponse registerUrl(ShortenUrlRequest request) {
     
		
		System.out.println("UrlServiceImpl.registerUrl()");
		
		String shortCode = null;
		String originalUrl = null;
		
		originalUrl = request.getOriginalUrl();

		validator.validate(originalUrl);

		do {
			shortCode = RandomString.getAlphaNumericString();
		} while (repository.existsByShortCode(shortCode));
		
        
		ShortenedUrl urlBO = new ShortenedUrl();
		urlBO.setOriginalUrl(originalUrl);
		urlBO.setShortCode(shortCode);
		urlBO.setExpiresAt(null);
		urlBO.setClickCount(0l);
		
	    repository.save(urlBO);
		
	    String shortUrl = baseURL+shortCode;
	    
		ShortenUrlResponse response = new ShortenUrlResponse(originalUrl, shortCode, shortUrl);
		
		return response;
	}

	@Override
	public String getOriginalUrl(String shortCode) {
		System.out.println("UrlServiceImpl.getOriginalUrl()");
		Optional<ShortenedUrl> optional = repository.findByShortCode(shortCode);
		
		ShortenedUrl shortenedUrl = null;
		
		if (optional.isPresent()) {
			
			shortenedUrl = optional.get();
			
			return shortenedUrl.getOriginalUrl();
		}
		else {
			throw new UrlNotFoundException("URL not found for the shortcode");
		}
	}

}
