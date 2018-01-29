package org.egov.filter.pre;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.egov.filter.model.Service;
import org.egov.filter.utils.FilterConstant;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ExecutionStatus;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.ZuulFilterResult;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.monitoring.Tracer;
import com.netflix.zuul.monitoring.TracerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceFilter extends ZuulFilter {

	   
    @Value("#{'${egov.request_filter.should_not_filter}'.split(',')}")
	private String[] shouldNotFilter;

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		String reqUri = ctx.getRequest().getRequestURI();
		System.err.println(" the url not allwoed : "+Arrays.asList(shouldNotFilter).contains(reqUri));
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
    
    @Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		log.info("RequestFilter");
		log.info(
				"Request Method : " + request.getMethod() + " Request URL : " + request.getRequestURL().toString());
		  Tracer t = TracerFactory.instance().startMicroTracer("ZUUL::" + this.getClass().getSimpleName());
		  ZuulFilterResult zr = new ZuulFilterResult();
		try {
			Map<String, String> map = new HashMap<>();
			map.put(FilterConstant.CONFIG_ERROR_KEY,FilterConstant.CONFIG_ERROR_MESSAGE+ctx.getRequest().getRequestURI());
			CustomException customException = new CustomException(map);
			customException.setCode("424");
			throw customException;
        } catch (Throwable e) {
        	  zr = new ZuulFilterResult(null, ExecutionStatus.SUCCESS);
            t.setName("ZUUL::" + this.getClass().getSimpleName() + " failed");
            zr = new ZuulFilterResult(ExecutionStatus.FAILED);
            zr.setException(e);
        }
		ctx.setRouteHost(null);
		return zr;
		/*List<Service> services = serviceMap.getServices();
		Map<String, Service> uriServiceMap = services.stream()
				.collect(Collectors.toMap(Service::getFromEndPont, s -> s));*/
/*
			if (uriServiceMap.containsKey(ctx.getRequest().getRequestURI()))
				reqResConstructor.constructRequest(uriServiceMap.get(ctx.getRequest().getRequestURI()), ctx);
			else {*/
			/*	Map<String, String> map = new HashMap<>();
				map.put(FilterConstant.CONFIG_ERROR_KEY,FilterConstant.CONFIG_ERROR_MESSAGE+ctx.getRequest().getRequestURI());
				CustomException customException = new CustomException(map);
				customException.setCode("424");
				throw customException;*/
		/*	}
		ctx.setRouteHost(null);

		return null;*/
	}

}
