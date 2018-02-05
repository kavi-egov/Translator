package org.egov.filter.post;

import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;

import org.egov.filter.util.FilterConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ResponseFilter extends ZuulFilter {
	
	@Autowired
	private ObjectMapper mapper;

	@Override
	public Object run() {

		String s = null;
		RequestContext reqCtx = RequestContext.getCurrentContext();
		reqCtx.setResponseStatusCode(HttpServletResponse.SC_OK);
		reqCtx.getResponse().setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json");

		if (reqCtx.get(FilterConstant.ERROR_RESPONSE) != null) {
			try {
				reqCtx.setResponseStatusCode(400);
				s = mapper.writeValueAsString(reqCtx.get(FilterConstant.ERROR_RESPONSE));
			} catch (JsonProcessingException e) {
				log.error(" exception occured while parsing ERROR_RESPONSE : {}", e);
			}
		} else if (reqCtx.get(FilterConstant.RESPONSE_BODY) != null) {
			s = (String) reqCtx.get(FilterConstant.RESPONSE_BODY);
		}
		reqCtx.setResponseBody(s);
		log.debug(" the ctx.get(\"responseBody\") :  {}", RequestContext.getCurrentContext().get("responseBody"));
		log.debug(" the ctx.get(\"errorResponse\") :  {}", RequestContext.getCurrentContext().get("errorResponse"));
		return null;

	}

	@Override
	public boolean shouldFilter() {
		System.err.println(" the post filter");
		return true;
	}

	@Override
	public int filterOrder() {
		return 999;
	}

	@Override
	public String filterType() {
		return "post";
	}

}
