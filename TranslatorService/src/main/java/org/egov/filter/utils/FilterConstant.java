package org.egov.filter.utils;

import org.springframework.stereotype.Component;

@Component
public class FilterConstant {
	
    public static final String ERROR_RESPONSE = "errorResponse";
/*	public static final String REQUEST_INFO_PASCAL_CASE = "RequestInfo";
	public static final String REQUEST_INFO_CAMEL_CASE = "requestInfo";
    public static final String AUTH_BOOLEAN_FLAG_NAME = "shouldDoAuth";
    public static final String AUTH_TOKEN_KEY = "authToken";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String USER_INFO = "userInfo";
    public static final String USER_INFO_KEY = "USER_INFO";
    public static final String REQ_TOKEN_KEY = "reqAuthToken";
    public static final String DOT = ".";
    public static final String RESPONSE_BODY = "responseBody";

    
    public static final String TENANT_MODULE_MASTER = "tenant-tenants";
    public static final String TENANT_MAP_KEY = "tenantMap";*/
    
    
    // ERROR MAPPINGS 
    public static final String AUTH_ERROR_KEY = "External_Auth_Failure";
    public static final String AUTH_ERROR_MESSAGE = "Unable to get Authorization for the external api's";
    
    public static final String CONFIG_ERROR_KEY = "CONFIG_NOT_FOUND";
    public static final String CONFIG_ERROR_MESSAGE = "Config not availabe for the given URI ";
    
    
    

}
