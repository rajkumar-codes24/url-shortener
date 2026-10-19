package com.proj.urlvalidation;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.proj.exception.InvalidUrlException;

@Component
public class UrlValidator {

	public void validate(String url) {

		if (url == null || url.isBlank() || url.length() > 2048) {
			throw new InvalidUrlException("URL is required and must be at most 2048 characters");
		}

		try {
			URI uri = new URI(url.trim());

			String scheme = uri.getScheme();
			if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
				throw new InvalidUrlException("Only HTTP and HTTPS URLs are allowed");
			}

			if (uri.getRawUserInfo() != null) {
				throw new InvalidUrlException("URLs containing user information are not allowed");
			}

			String host = uri.getHost();

			if (host == null || host.isBlank()) {
				throw new InvalidUrlException("A valid hostname is required");
			}

			if (uri.getPort() != -1 && (uri.getPort() < 1 || uri.getPort() > 65535)) {
				throw new InvalidUrlException("Invalid port");
			}

			host = host.toLowerCase(Locale.ROOT);

			if (host.endsWith(".")) {
				host = host.substring(0, host.length() - 1);
			}

			if (host.equals("localhost") || host.endsWith(".localhost") || host.endsWith(".local")) {
				throw new InvalidUrlException("Local hostnames are not allowed");
			}

			 
			InetAddress[] addresses = InetAddress.getAllByName(host);

			if (addresses.length == 0) {
				throw new InvalidUrlException("Hostname has no valid address");
			}

			for (InetAddress address : addresses) {
				if (!isPublicAddress(address)) {
					throw new InvalidUrlException("Only public internet destinations are allowed");
				}
			}

		} catch (InvalidUrlException ex) {
			throw ex;
		} catch (UnknownHostException ex) {
			throw new InvalidUrlException("Hostname could not be resolved");
		} catch (Exception ex) {
			throw new InvalidUrlException("Invalid URL");
		}
	}

	private boolean isPublicAddress(InetAddress address) {

		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return false;
		}

		byte[] bytes = address.getAddress();

		if (address instanceof Inet4Address) {
			return isPublicIPv4(bytes);
		}

		if (address instanceof Inet6Address) {
			return isPublicIPv6(bytes);
		}

		return false;
	}

	private boolean isPublicIPv4(byte[] b) {

		int a = b[0] & 0xFF;
		int c = b[1] & 0xFF;
		int d = b[2] & 0xFF;

		if (a == 0 || a == 10 || a == 127 || a >= 224)
			return false;
		if (a == 100 && c >= 64 && c <= 127)
			return false;
		if (a == 169 && c == 254)
			return false;
		if (a == 172 && c >= 16 && c <= 31)
			return false;
		if (a == 192 && c == 168)
			return false;
		if (a == 192 && c == 0)
			return false;
		if (a == 192 && c == 2)
			return false;
		if (a == 198 && (c == 18 || c == 19))
			return false;
		if (a == 198 && c == 51 && d == 100)
			return false;
		if (a == 203 && c == 0 && d == 113)
			return false;

		return true;
	}

	private boolean isPublicIPv6(byte[] b) {

		int first = b[0] & 0xFF;
		int second = b[1] & 0xFF;

		
		if (first < 0x20 || first > 0x3F) {
			return false;
		}

		
		if (first == 0x20 && second == 0x01 && (b[2] & 0xFF) == 0x0D && (b[3] & 0xFF) == 0xB8) {
			return false;
		}

		
		if (first == 0x20 && second == 0x02) {
			return false;
		}

		return true;
	}
}
