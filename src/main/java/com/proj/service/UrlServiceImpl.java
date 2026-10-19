
package com.proj.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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

    public UrlServiceImpl(
            UrlRepository repository,
            UrlValidator validator,
            StringRedisTemplate redisTemplate) {

        this.repository = repository;
        this.validator = validator;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ShortenUrlResponse registerUrl(ShortenUrlRequest request) {

        String originalUrl = request.getOriginalUrl();
        validator.validate(originalUrl);

        String originalUrlHash = hashOriginalUrl(originalUrl);
        String cacheKey = "url:original:" + originalUrlHash;

        String cachedCode = getCachedCode(cacheKey);

        if (cachedCode != null) {
            return createResponse(originalUrl, cachedCode);
        }

        Optional<ShortenedUrl> existing =
                repository.findByOriginalUrlHash(originalUrlHash);

        if (existing.isPresent()) {
            String existingCode = existing.get().getShortCode();
            cacheCode(cacheKey, existingCode);
            return createResponse(originalUrl, existingCode);
        }

        String shortCode;

        do {
            shortCode = RandomString.getAlphaNumericString();
        } while (repository.existsByShortCode(shortCode));

        ShortenedUrl url = new ShortenedUrl();
        url.setOriginalUrl(originalUrl);
        url.setOriginalUrlHash(originalUrlHash);
        url.setShortCode(shortCode);
        url.setExpiresAt(null);
        url.setClickCount(0L);

        try {
            repository.saveAndFlush(url);

        } catch (DataIntegrityViolationException ex) {

            Optional<ShortenedUrl> existingUrl =
                    repository.findByOriginalUrlHash(originalUrlHash);

            if (existingUrl.isPresent()) {
                String existingCode = existingUrl.get().getShortCode();
                cacheCode(cacheKey, existingCode);
                return createResponse(originalUrl, existingCode);
            }

            throw ex;
        }

        cacheCode(cacheKey, shortCode);

        return createResponse(originalUrl, shortCode);
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        String cacheKey = "url:code:" + shortCode;

        String cachedOriginalUrl = getCachedOriginalUrl(cacheKey);

        if (cachedOriginalUrl != null) {
            return cachedOriginalUrl;
        }

        Optional<ShortenedUrl> optional =
                repository.findByShortCode(shortCode);

        if (optional.isPresent()) {
            String originalUrl = optional.get().getOriginalUrl();
            cacheOriginalUrl(cacheKey, originalUrl);
            return originalUrl;
        }

        throw new UrlNotFoundException(
                "URL not found for the shortcode");
    }

    private String getCachedCode(String cacheKey) {
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private void cacheCode(String cacheKey, String shortCode) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey, shortCode, CACHE_TTL);
        } catch (DataAccessException ex) {
            // Redis failure must not prevent URL creation.
        }
    }

    private String getCachedOriginalUrl(String cacheKey) {
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private void cacheOriginalUrl(
            String cacheKey, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey, originalUrl, CACHE_TTL);
        } catch (DataAccessException ex) {
            // Redis failure must not prevent redirects.
        }
    }

    private String hashOriginalUrl(String originalUrl) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    originalUrl.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available", ex);
        }
    }

    private ShortenUrlResponse createResponse(
            String originalUrl, String shortCode) {

        String shortUrl = baseURL + shortCode;

        return new ShortenUrlResponse(
                originalUrl, shortCode, shortUrl);
    }
}
