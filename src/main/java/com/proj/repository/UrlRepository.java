package com.proj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.entity.ShortenedUrl;

public interface UrlRepository extends JpaRepository<ShortenedUrl, Long> {

	boolean existsByShortCode(String shortCode);

	boolean existsByOriginalUrl(String originalUrl);

	Optional<ShortenedUrl> findByShortCode(String shortCode);

	Optional<ShortenedUrl> findByOriginalUrl(String originalUrl);

	Optional<ShortenedUrl> findFirstByOriginalUrlOrderByCreatedAtAsc(String originalUrl);
	
	Optional<ShortenedUrl> findByOriginalUrlHash(String originalUrlHash);
	
}
