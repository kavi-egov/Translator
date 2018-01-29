package org.egov;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.egov.filter.model.MasterDetail;
import org.egov.filter.model.Mdms;
import org.egov.filter.model.MdmsMap;
import org.egov.filter.model.Service;
import org.egov.filter.model.ServiceMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConfigurationLoader {

	@Value("${egov.service.configfile.path}")
	private String serviceConfigLocation;

	@Value("${egov.mdms.configfile.path}")
	private String mdmsConfigLocation;

	@Value("${egov.tenant.mapfile.path}")
	private String tenantMapLocation;

	@Autowired
	public ResourceLoader resourceLoader;

	private Map<String, Map<String, String>> tenantConfigMap;

	private Map<String, Service> serviceConfigMap;

	private Map<String, Map<String, MasterDetail>> mdmsConfigMap;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	@Qualifier("YamlReader")
	private ObjectMapper yamlReader;

	/***
	 * Method to load tenant Config map which maps egov tenants to external tenant
	 * values
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void loadTenantConfigMap() {

		try {
			URL tenantConfigUrl = new URL(tenantMapLocation);
			tenantConfigMap = (Map<String, Map<String, String>>) mapper
					.readValue(new InputStreamReader(tenantConfigUrl.openStream()), Map.class);
		} catch (IOException e) {
			log.error(" Error while loading TenantConfigMap : {}", e.getCause());
		}
		log.debug(" the tenant Map : {}", tenantConfigMap);
	}

	/***
	 * To Load the service configurations to serviceConfigMap with fromUrl as key
	 * and the service object as value
	 */
	@PostConstruct
	public void loadServiceconfigMap() {

		try {
			URL serviceConfigUrl = new URL(serviceConfigLocation);
			List<Service> services = yamlReader.readValue(serviceConfigUrl.openStream(), ServiceMap.class)
					.getServices();
			serviceConfigMap = services.stream()
					.collect(Collectors.toMap(Service::getFromEndPont, Function.identity()));
		} catch (IOException e) {
			log.error(" Error while loading serviceconfigMap : {}", e);
		}
		log.debug(" the service map : {}", serviceConfigMap);
	}

	/***
	 * To Load the Mdms config to mdmsMap with moduleName as key and
	 * Map<String,ModuleDetail> as Value
	 */
	@PostConstruct
	public void loadMdmsConfigMap() {

		try {
			URL mdmsUrl = new URL(mdmsConfigLocation);
			List<Mdms> mdmsList = yamlReader.readValue(mdmsUrl.openStream(), MdmsMap.class).getMdms();
			mdmsConfigMap = getMdmsMap(mdmsList);
		} catch (IOException e) {
			log.error(" Error while loading mdmsConfigMap : {}", e);
		}
		log.debug(" the mdms map : {}", mdmsConfigMap);
	}

	/***
	 * to convert List<Mdms> to proper mdms Map with moulde and master details in a
	 * Map<String, Map<String,ModuleDetail>>
	 * 
	 * @param mdmsList
	 * @return
	 */
	private Map<String, Map<String, MasterDetail>> getMdmsMap(List<Mdms> mdmsList) {

		Map<String, Map<String, MasterDetail>> moduleMap = new HashMap<>();
		for (Mdms mdms : mdmsList) {
			Map<String, MasterDetail> masterMap = new HashMap<>();
			List<MasterDetail> masterDetails = mdms.getMasterDetails();

			for (MasterDetail masterDetail : masterDetails) {
				masterMap.put(masterDetail.getMasterName(), masterDetail);
			}

			moduleMap.put(mdms.getModuleName(), masterMap);
		}
		return moduleMap;
	}

	/***
	 * Getter for mdmsConfigMap
	 * 
	 * @return Map<String, Map<String, MasterDetail>>
	 */
	public Map<String, Map<String, MasterDetail>> getMdmsConfigMap() {
		return mdmsConfigMap;
	}

	/***
	 * Getter for tenantConfigMap
	 * 
	 * @return Map<String, Map<String, String>>
	 */
	public Map<String, Map<String, String>> getTenantConfigMap() {
		return tenantConfigMap;
	}

	/***
	 * Getter for serviceConfigMap
	 * 
	 * @return Map<String, Service>
	 */
	public Map<String, Service> getServiceConfigMap() {
		return serviceConfigMap;
	}

}
