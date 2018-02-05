package org.egov.filter.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.filter.model.Parameter;
import org.egov.filter.model.Request;
import org.egov.filter.model.Service;
import org.egov.filter.model.SourceInEnum;
import org.egov.filter.util.FilterConstant;
import org.egov.filter.util.RequestParser;
import org.egov.filter.util.RequestWrapper;
import org.egov.filter.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.DocumentContext;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Translator {
	
	@Autowired
	private RestUtil restUtil;
	
	@Autowired
	private ResponseBuilder responseBuilder;
	
	private DocumentContext reqBodyDocumentcontext;
	
	private Map<String, List<String>> queryParamMap;

	/***
	 * Method To build external API request from the eGov Reqesut 
	 * @param service
	 * @param reqCtx
	 */
	public void translate(Service service, RequestContext reqCtx) {

		setRequestBodyDocumentContext(reqCtx);
		parseQueryString(reqCtx);
		List<Request> requests = service.getRequests();

		requests.forEach(request -> {

			String basePath = request.getBasePath();
			if (basePath == null || !basePath.contains("*")) {

				String url = constructReqUrl(request, reqCtx, null);
				String body = constructReqBody(request, null);
				String response = restUtil.doServiceCall(url, body);
				String jsonResponse = responseBuilder.parseResponse(request, service.getFinalResponse(), response);
				reqCtx.set(FilterConstant.RESPONSE_BODY, jsonResponse);
				log.debug("final Response : {}", service.getFinalResponse());

			} else {
				basePath = basePath.replace("*", "length()");
				int objLength = reqBodyDocumentcontext.read(basePath);
				for (int i = 0; i < objLength; i++) {
					String url = constructReqUrl(request, reqCtx, String.valueOf(i));
					String body = constructReqBody(request, String.valueOf(i));
					String response = restUtil.doServiceCall(url, body);
					responseBuilder.parseResponse(request, service.getFinalResponse(), response);
					log.debug("final Response:" + service.getFinalResponse());
				}
			}
		});
	}
	
	private void setRequestBodyDocumentContext(RequestContext reqCtx) {
		RequestWrapper requestWrapper = new RequestWrapper(reqCtx.getRequest());
		reqBodyDocumentcontext = RequestParser.getParsedRequestBody(requestWrapper.getPayload());
	}

	private void parseQueryString(RequestContext ctx) {
		queryParamMap = ctx.getRequestQueryParams();
	}
	
	private String constructReqUrl(Request request, RequestContext reqCtx, String index) {

		String finalUrl = null;
		URL defaultRouteHost = reqCtx.getRouteHost();

		try {
			URL url = new URL(defaultRouteHost.getProtocol(), defaultRouteHost.getHost(), defaultRouteHost.getPort(),
					request.getUrl());
			finalUrl = url.toString();
			log.debug("url2::" + url.toString());
		} catch (MalformedURLException e) {
			log.error(" exception occured while building new URL from default RouteHost : {}", e);
			throw new RuntimeException(e);
		}

		String queryStr = constructQueryParam(request.getQueryParams(), index);
		if (queryStr != null)
			finalUrl = finalUrl.concat(queryStr);

		log.debug("constructReqUrl url:" + finalUrl);
		return finalUrl;
	}

	private String constructQueryParam(List<Parameter> queryParams, String index) {

		if (null == queryParams) return null;

		String queryStr = "?";

		int i = 1;
		for (Parameter parameter : queryParams) {

			SourceInEnum inEnum = parameter.getIn();
			if (inEnum.toString().equals(SourceInEnum.query.toString())) {
				
				List<String> values = queryParamMap.get(parameter.getSource());
				if (null != values) {
					String strList = values.stream().collect(Collectors.joining(","));
					queryStr = queryStr.concat(parameter.getName()).concat("=").concat(strList);
				}

			} else if (inEnum.toString().equals(SourceInEnum.body.toString())) {
				String value = null;

				if (parameter.getSource() != null && parameter.getSource().contains("*") && index != null)
					value = reqBodyDocumentcontext.read(parameter.getSource().replaceFirst("*", index));
				else
					value = reqBodyDocumentcontext.read(parameter.getSource());

				if (value != null)
					queryStr = queryStr.concat(parameter.getName().concat("=").concat(value));
			}
			if (i++ < queryParams.size())  //FIXME problem here if the parameter is null check
				queryStr = queryStr.concat("&");
		}
		return queryStr;
	}

	private String constructReqBody(Request request, String index) {
		List<Parameter> bodyParameters = request.getBodyParams();

		String reqBody = request.getBody();

		if (bodyParameters == null)
			return reqBody;

		int i = 0;
		for (Parameter parameter : bodyParameters) {
			String regEx = "{" + i++ + "}";
			log.info("regEx:" + regEx);
			log.info("prepareReqBody parameter:" + parameter);
			reqBody = parseOutgoingReqBody(parameter, reqBody, regEx, index);
		}
		return reqBody;
	}

	private String parseOutgoingReqBody(Parameter parameter, String body, String regex, String index) {

		Object value = null;
		if (parameter.getIn().toString().equals(SourceInEnum.query.toString())) {
			List<String> values = queryParamMap.get(parameter.getSource());
			if (values != null && values.size() == 1)
				value = values.stream().collect(Collectors.joining(","));
			else if (values != null && values.size() > 1)
				value = values.toString();
			else
				value = null;
		} else if (parameter.getIn().toString().equals(SourceInEnum.body.toString())) {

			if (parameter.getSource().contains("*")) {
				String jsonPath = parameter.getSource().replaceFirst("*", index);
				value = reqBodyDocumentcontext.read(jsonPath);
			} else {
				value = reqBodyDocumentcontext.read(parameter.getSource());
			}
		}

		if (value != null)
			body = body.replace(regex, value.toString());
		else
			body = body.replace(regex, "null");

		return body;
	}
}
