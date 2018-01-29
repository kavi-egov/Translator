package org.egov.filter.error;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.filter.utils.FilterConstant;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.Error;
import org.egov.tracer.model.ErrorRes;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ErrorFilter extends ZuulFilter {

	@Override
	public String filterType() {
		return "error";
	}

	@Override
	public int filterOrder() {
		return 900;
	}

	@Override
	public boolean shouldFilter() {
		return RequestContext.getCurrentContext().getThrowable() != null;
	}

	@Override
	public Object run() {

		RequestContext ctx = RequestContext.getCurrentContext();

		Throwable throwable = ctx.getThrowable();
		CustomException customException = null;

		log.info("LogErrorFilter");

		ErrorRes errorResponse = new ErrorRes();
		List<Error> errors = new ArrayList<>();

		// code for extracting message from custom exception
		if (throwable.getCause() instanceof CustomException) {
			customException = (CustomException) throwable.getCause();

			Map<String, String> map = customException.getErrors();
			map.keySet().forEach(key -> {
				Error error = new Error();
				// error.setDescription(map.get(key));
				error.setCode("424");
				error.setMessage(map.get(key));
				errors.add(error);
			});
		} else {
			Error error = new Error();
			error.setDescription(throwable.getCause().getClass().getName());
			error.setCode("500");
			error.setMessage(throwable.getCause().getMessage());
			errors.add(error);
		}

		errorResponse.setErrors(errors);
		ctx.put(FilterConstant.ERROR_RESPONSE, errorResponse);
		return null;
	}
}