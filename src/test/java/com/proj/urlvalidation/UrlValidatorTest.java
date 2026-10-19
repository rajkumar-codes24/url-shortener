
package com.proj.urlvalidation;

import org.junit.jupiter.api.Test;

import com.proj.exception.InvalidUrlException;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

	private final UrlValidator validator = new UrlValidator();

	@Test
	void shouldRejectBlankUrl() {
		assertThrows(InvalidUrlException.class, () -> validator.validate(" "));
	}

	@Test
	void shouldRejectFtpUrl() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("ftp:example.com"));
	}

	@Test
	void shouldRejectLocalhost() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("http:localhost:8080"));
	}

	@Test
	void shouldRejectPrivateIpv4() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("http:192.168.1.1"));
	}

	@Test
	void shouldRejectLoopbackIpv6() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("http:[::1]"));
	}

	@Test
	void shouldRejectUnsupportedScheme() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("ftp:google.com"));
	}

	@Test
	void shouldRejectPrivateIpv4Ranges() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("http:10.0.0.1"));

		assertThrows(InvalidUrlException.class, () -> validator.validate("http:172.16.0.1"));

		assertThrows(InvalidUrlException.class, () -> validator.validate("http:169.254.169.254"));
	}

	@Test
	void shouldRejectLocalHostnames() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("http:server.local"));

		assertThrows(InvalidUrlException.class, () -> validator.validate("http:anything.localhost"));
	}

	@Test
	void shouldRejectUrlWithCredentials() {
		assertThrows(InvalidUrlException.class, () -> validator.validate("https:user:password@example.com"));
	}

}
