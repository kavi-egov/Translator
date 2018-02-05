package org.egov.filter.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class APIAuthTokenService {

	@Value("${innowave.api_auth.grant_type}")
	private String grantType;

	@Value("${innowave.api_auth.username}")
	private String username;

	@Value("${innowave.api_auth.password}")
	private String password;

	@Value("${innowave.api_auth.password.basic_token}")
	private String basicToken;

	@Value("${egov.innowave.host}")
	private String innowaveHost;

	@Value("${innowave.api_auth.uri}")
	private String authTokenUri;

	@Autowired
	private RestTemplate restTemplate;

	public String getAuthToken() {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + basicToken);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String[] urlVar = innowaveHost.split(":");

		URL url = null;
		try {
			url = new URL("http", urlVar[0], Integer.parseInt(urlVar[1]), authTokenUri);
		} catch (NumberFormatException | MalformedURLException e) {
			log.error(" APIAuthTokenService : exception occured while creating URL object : {}", e);
			throw new RuntimeException(e);
		}

		log.debug("url:" + url);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", grantType);
		map.add("username", username);
		map.add("password", password);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url.toString(), request, String.class);
		String authToken = JsonPath.read(response.getBody(), "$.access_token");
		log.info("auth Service response:" + response.getStatusCode() + "," + authToken);

		return authToken;
	}

}
