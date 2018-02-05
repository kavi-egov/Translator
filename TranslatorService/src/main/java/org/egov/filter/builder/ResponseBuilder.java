package org.egov.filter.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.filter.model.FinalResponse;
import org.egov.filter.model.Request;
import org.egov.filter.model.Response;
import org.egov.filter.model.ResponseParam;
import org.egov.filter.util.ResponseFieldDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Component
@Slf4j
public class ResponseBuilder {
	
	@Autowired
	private ResponseFieldDataConverter responseFieldDataConverter;

	@Autowired
	private ObjectMapper mapper;

	@SuppressWarnings("unchecked")
	public String parseResponse(Request request, FinalResponse finalResponse, String response) {

		log.debug("parseResponse.." + response);
		DocumentContext finalResDc = JsonPath.parse(finalResponse.getBody());
		JSONArray finalResArray = finalResDc.read(finalResponse.getBasePath());
		DocumentContext finalResObjDc = JsonPath.parse((LinkedHashMap<String, Object>) finalResArray.get(0));

		DocumentContext responseDc = JsonPath.parse(response);
		Response responsePath = request.getResponse();
		String resBasePath = responsePath.getBasePath();
		if (resBasePath.contains("*")) {
			int objLength = responseDc.read(resBasePath.replaceFirst("\\[\\*\\]", ".length()"));
			List<Object> list = new ArrayList<>();
			for (int i = 0; i < objLength; i++) {

				for (ResponseParam responseParam : request.getResponse().getResponseParams()) {
					responseFieldDataConverter.setResponse(finalResObjDc, responseParam, finalResponse, responseDc, i);
				}
				list.add(finalResObjDc.json());
				try {
					finalResObjDc = JsonPath.parse(mapper.writeValueAsString(finalResArray.get(0)));
				} catch (JsonProcessingException e) {
					log.error(" ResponseBuilder : exception occured while parsing Response : {}", e);
				}
			}

			String key = finalResponse.getBasePath().replace("$.", "");
			key = key.replaceFirst("\\[\\*\\]", "");
			finalResDc.put("$", key, list);
		}
		return finalResDc.jsonString();
	}

}
