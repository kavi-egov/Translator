package org.egov.filter.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.internal.JsonContext;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestParser {
	
	private static JsonContext jsonContext = new JsonContext();
	
	public Map<String, Object> hashMap;
	
	public static DocumentContext getParsedRequestBody(String payload) {
		// REPLACING com.jayway.jsonpath.JsonPath parse method with jsonContext.parse since json internally calls it
		return jsonContext.parse(payload);
	}
	
	public boolean hasRequestInfo() {
		return hashMap.containsKey(FilterConstant.REQUEST_INFO_PASCAL_CASE) || 
				hashMap.containsKey(FilterConstant.REQUEST_INFO_CAMEL_CASE);
	}
	
	public void setAuthToken(DocumentContext documentContext, RequestContext ctx) {
		if(isPascalCasePresent())
			ctx.set(FilterConstant.REQ_TOKEN_KEY, documentContext.read("$.".concat(FilterConstant.REQUEST_INFO_PASCAL_CASE).
					concat(FilterConstant.DOT).concat(FilterConstant.AUTH_TOKEN_KEY)));
		else  
			ctx.set(FilterConstant.REQ_TOKEN_KEY, documentContext.read("$.".concat(FilterConstant.REQUEST_INFO_CAMEL_CASE).
					concat(FilterConstant.DOT).concat(FilterConstant.AUTH_TOKEN_KEY)));
	}
	
	public boolean isCamelCasePresent() {
		return hashMap.containsKey(FilterConstant.REQUEST_INFO_CAMEL_CASE);
	}
	
	public boolean isPascalCasePresent() {
		return hashMap.containsKey(FilterConstant.REQUEST_INFO_PASCAL_CASE);
	}
	
	public void setReqAsMap(String jsonBody) {
		ObjectMapper mapper = new ObjectMapper();
         try {
        	 hashMap = mapper.readValue(jsonBody,
			    new TypeReference<HashMap<String, Object>>() { });
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
