
package com.proj.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
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

	private static final Duration CACHE_TTL = Duration.ofHours(24);

	@Value("${app.base-url}")
	private String baseURL;

	private final UrlRepository repository;
	private final UrlValidator validator;
	private final StringRedisTemplate redisTemplate;

	public UrlServiceImpl(UrlRepository repository, UrlValidator validator, StringRedisTemplate redisTemplate) {

		this.repository = repository;
		this.validator = validator;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public ShortenUrlResponse registerUrl(ShortenUrlRequest request) {

		System.out.println("UrlServiceImpl.registerUrl()");

		String originalUrl = request.getOriginalUrl();
		validator.validate(originalUrl);

		// 1. Create a Redis key for this original URL.
		String cacheKey = createCacheKey(originalUrl);

		// 2. Try Redis first.
		String cachedCode = getCachedCode(cacheKey);

		if (cachedCode != null) {
			return createResponse(originalUrl, cachedCode);
		}

		// 3. Redis miss: check MySQL.
		Optional<ShortenedUrl> existing = repository.findFirstByOriginalUrlOrderByCreatedAtAsc(originalUrl);

		if (existing.isPresent()) {
			String existingCode = existing.get().getShortCode();

			// Warm the cache for the next request.
			cacheCode(cacheKey, existingCode);

			return createResponse(originalUrl, existingCode);
		}

		// 4. No existing URL: generate a unique short code.
		String shortCode;

		do {
			shortCode = RandomString.getAlphaNumericString();
		} while (repository.existsByShortCode(shortCode));

		ShortenedUrl url = new ShortenedUrl();
		url.setOriginalUrl(originalUrl);
		url.setShortCode(shortCode);
		url.setExpiresAt(null);
		url.setClickCount(0L);

		// 5. Persist first; cache only after successful save.
		repository.save(url);

		cacheCode(cacheKey, shortCode);

		return createResponse(originalUrl, shortCode);
	}

	@Override
	public String getOriginalUrl(String shortCode) {

		// 1. Build the Redis key for this short code.
		String cacheKey = "url:code:" + shortCode;

		// 2. Check Redis first.
		String cachedOriginalUrl = getCachedOriginalUrl(cacheKey);

		if (cachedOriginalUrl != null) {
			System.out.println("REDIRECT CACHE HIT: " + cacheKey);
			return cachedOriginalUrl;
		}

		System.out.println("REDIRECT CACHE MISS: " + cacheKey);

		// 3. Cache miss: look up the short code in MySQL.
		Optional<ShortenedUrl> optional = repository.findByShortCode(shortCode);

		if (optional.isPresent()) {

			String originalUrl = optional.get().getOriginalUrl();

			// 4. Cache the result for future redirects.
			cacheOriginalUrl(cacheKey, originalUrl);

			return originalUrl;
		}

		// 5. Neither Redis nor MySQL has this short code.
		throw new UrlNotFoundException("URL not found for the shortcode");
	}

	private String getCachedOriginalUrl(String cacheKey) {
		try {
			return redisTemplate.opsForValue().get(cacheKey);
		} catch (DataAccessException ex) {
			// Redis unavailable: fall back to MySQL.
			return null;
		}
	}

	private void cacheOriginalUrl(String cacheKey, String originalUrl) {
		try {
			redisTemplate.opsForValue().set(cacheKey, originalUrl, CACHE_TTL);
		} catch (DataAccessException ex) {
			// Redirects should still work if Redis is unavailable.
		}
	}

	private String getCachedCode(String cacheKey) {

		System.out.println("UrlServiceImpl.getCachedCode()");
		try {
			String cachedCode = redisTemplate.opsForValue().get(cacheKey);

			if (cachedCode != null) {
				System.out.println("REDIS CACHE HIT: " + cacheKey);
			} else {
				System.out.println("REDIS CACHE MISS: " + cacheKey);
			}

			return cachedCode;
		} catch (DataAccessException ex) {
			System.out.println("REDIS UNAVAILABLE: " + ex.getMessage());
			return null;
		}
	}

	private void cacheCode(String cacheKey, String shortCode) {

		System.out.println("UrlServiceImpl.cacheCode()");
		try {
			redisTemplate.opsForValue().set(cacheKey, shortCode, CACHE_TTL);
		} catch (DataAccessException ex) {
			// Saving in MySQL must not fail just because
			// Redis could not be updated.
		}
	}

	private String createCacheKey(String originalUrl) {

		System.out.println("UrlServiceImpl.createCacheKey()");

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));

			return "url:original:" + HexFormat.of().formatHex(hash);

		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	private ShortenUrlResponse createResponse(String originalUrl, String shortCode) {

		String shortUrl = baseURL + shortCode;

		return new ShortenUrlResponse(originalUrl, shortCode, shortUrl);
	}
}
