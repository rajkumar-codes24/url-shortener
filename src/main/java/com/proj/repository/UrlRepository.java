package com.proj.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.proj.entity.ShortenedUrl;

public interface UrlRepository extends CrudRepository<ShortenedUrl, Long> {

	boolean existsByShortCode(String shortCode);

	boolean existsByOriginalUrl(String originalUrl);

	Optional<ShortenedUrl> findByShortCode(String shortCode);

	Optional<ShortenedUrl> findByOriginalUrl(String originalUrl);

	Optional<ShortenedUrl> findFirstByOriginalUrlOrderByCreatedAtAsc(String originalUrl);
}
