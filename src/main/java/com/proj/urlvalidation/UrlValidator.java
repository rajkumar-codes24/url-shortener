package com.proj.urlvalidation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.proj.exception.InvalidUrlException;

@Component
public class UrlValidator {

    public void validate(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        if (value.length() > 2048) {
            throw new InvalidUrlException(
                "URL cannot exceed 2048 characters"
            );
        }

        // Reject whitespace and control characters.
        if (value.chars().anyMatch(
                c -> Character.isWhitespace(c) || Character.isISOControl(c))) {
            throw new InvalidUrlException(
                "URL must not contain spaces or control characters"
            );
        }

        final URI uri;

        try {
            uri = new URI(value);
        } catch (URISyntaxException ex) {
            throw new InvalidUrlException("Invalid URL format");
        }

        String scheme = uri.getScheme();

        if (scheme == null ||
                !(scheme.equalsIgnoreCase("http")
                || scheme.equalsIgnoreCase("https"))) {
            throw new InvalidUrlException(
                "Only HTTP and HTTPS URLs are allowed"
            );
        }

        if (uri.getRawUserInfo() != null) {
            throw new InvalidUrlException(
                "URLs containing usernames or passwords are not allowed"
            );
        }

        String host = uri.getHost();

        if (host == null || host.isBlank()) {
            throw new InvalidUrlException(
                "URL must contain a valid hostname"
            );
        }

        int port = uri.getPort();

        if (port != -1 && (port < 1 || port > 65535)) {
            throw new InvalidUrlException("Invalid port number");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);

        if (normalizedHost.equals("localhost")
                || normalizedHost.endsWith(".localhost")
                || normalizedHost.endsWith(".local")) {
            throw new InvalidUrlException(
                "Local network destinations are not allowed"
            );
        }

        if (normalizedHost.equals("127.0.0.1")
                || normalizedHost.equals("::1")
                || normalizedHost.equals("0.0.0.0")) {
            throw new InvalidUrlException(
                "Loopback destinations are not allowed"
            );
        }
    }
}

