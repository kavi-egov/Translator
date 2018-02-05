package org.egov.filter.util;

import java.util.HashMap;
import java.util.Map;

import org.egov.filter.pre.AuthFilter;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RestUtil {
	
	@Value("${innowave.api_auth.retry}")
	private Integer maxInnowaveAuthRetry;
	
	@Autowired
	private RestTemplate restTemplate;
	
	ResponseEntity<String> res = null;
	
	private Integer retryCount = 0;
	
	@Autowired
	private APIAuthTokenService apiAuthTokenService;

	public String doServiceCall(String url, String body) {
		
		log.debug(" url to be posted : " + url + "  ,  " + "body:" + body);

		String authToken = AuthFilter.getAuthToken();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + authToken);
		HttpEntity<String> entity = null;
		if (body != null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
			entity = new HttpEntity<>(body, headers);
		} else {
			entity = new HttpEntity<>(headers);
		}
		
		HttpStatus httpStatus = null;
		try {
			res = restTemplate.postForEntity(url, entity, String.class);
		} catch (HttpClientErrorException ex) {
			httpStatus = ex.getStatusCode();
			log.error(" the http status code : {}",httpStatus);
		} catch (Exception ex) {
			log.error(" RestUtil : Exception occured while :" + ex);
		}
	
		// retyr logic if auth token expires
		if (null != res)
			httpStatus = res.getStatusCode();
		if (null != httpStatus && httpStatus.equals(HttpStatus.UNAUTHORIZED) && retryCount++< maxInnowaveAuthRetry) {
			log.info(" the retryCount : {} ",retryCount);
			String token = apiAuthTokenService.getAuthToken();
			AuthFilter.setAuthToken(token);
			doServiceCall(url, body);
		}
		retryCount=0;
		
		// throw exception if no Response from external api
		if(res == null) {
			Map<String,String> map = new HashMap<>();
			map.put(FilterConstant.AUTH_ERROR_KEY, FilterConstant.AUTH_ERROR_MESSAGE);
			CustomException customException = new CustomException(map);
			customException.setCode("424");
			throw customException;
		}
		return res.getBody();
	}

}
