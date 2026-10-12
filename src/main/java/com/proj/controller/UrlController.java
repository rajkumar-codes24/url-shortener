package com.proj.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proj.dto.ShortenUrlRequest;
import com.proj.dto.ShortenUrlResponse;
import com.proj.service.UrlService;

import jakarta.validation.Valid;


@CrossOrigin(value = "https://url-shortener-frontend-wine-theta.vercel.app/")
@RestController
@RequestMapping(value = "/api")
public class UrlController {

	private final UrlService service;

	UrlController(UrlService service) {
		this.service = service;
	}
	
	@PostMapping(value = "/getShortURL")
	  public ResponseEntity<ShortenUrlResponse> createShortURL(@RequestBody @Valid ShortenUrlRequest request){ 
		
		  System.out.println("UrlController.createShortURL()");
		  
		  ShortenUrlResponse shortenUrlResponse = service.registerUrl(request);
		  
		  return new ResponseEntity<ShortenUrlResponse>(shortenUrlResponse,HttpStatus.CREATED);
	  }
	
	
}
