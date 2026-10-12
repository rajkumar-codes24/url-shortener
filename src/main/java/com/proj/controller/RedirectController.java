package com.proj.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.proj.service.UrlService;

@RestController
public class RedirectController {

	private final UrlService service;

	public RedirectController(UrlService service) {
         this.service = service;	
	}
	
	@GetMapping("/{shortCode}")
	public ResponseEntity<?> redirectToUrl(@PathVariable String shortCode){
		System.out.println("RedirectController.redirectToUrl()");
		String originalUrl = service.getOriginalUrl(shortCode);
		
		return ResponseEntity
		        .status(HttpStatus.FOUND)
		        .location(URI.create(originalUrl))
		        .build();
		
	}
	
}
