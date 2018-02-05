package org.egov.filter.pre;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.egov.ConfigurationLoader;
import org.egov.filter.builder.Translator;
import org.egov.filter.model.Service;
import org.egov.filter.util.FilterConstant;
import org.egov.filter.util.RequestParser;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceRequestFilter extends ZuulFilter {

	@Value("#{'${egov.request_filter.should_not_filter}'.split(',')}")
	private String[] shouldNotFilter;

	@Autowired
	private ConfigurationLoader configLoader;
	
	@Autowired
	private Translator translator;

	@Override
	public Object run() {

		RequestContext ctx = RequestContext.getCurrentContext();
		Map<String, Service> uriServiceMap = configLoader.getServiceConfigMap();
		log.debug(ctx.getRouteHost()+" is the route host");

		if (uriServiceMap.containsKey(ctx.getRequest().getRequestURI())) {

			Service service = uriServiceMap.get(ctx.getRequest().getRequestURI());
			translator.translate(service, ctx);
		} else {
			
			Map<String, String> map = new HashMap<>();
			map.put(FilterConstant.CONFIG_ERROR_KEY,
					FilterConstant.CONFIG_ERROR_MESSAGE + ctx.getRequest().getRequestURI());
			CustomException customException = new CustomException(map);
			customException.setCode("424");
			throw customException;
		}
		ctx.setRouteHost(null);
		return null;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		String reqUri = ctx.getRequest().getRequestURI();
		log.debug(" the url not allwoed : " + Arrays.asList(shouldNotFilter).contains(reqUri));
		return !Arrays.asList(shouldNotFilter).contains(reqUri);
	}

	@Override
	public int filterOrder() {
		return 6;
	}

	@Override
	public String filterType() {
		return "pre";
	}

}
