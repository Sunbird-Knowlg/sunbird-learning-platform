package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.framework.enums.ChannelEnum;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;


@Component
public class ChannelManagerImpl extends BaseFrameworkManager implements IChannelManager {

	private static final String CHANNEL_OBJECT_TYPE = "Channel";
	private SearchProcessor processor = null;
	
	@PostConstruct
	public void init() {
		processor = new SearchProcessor();
	}
	
	@Override
	public Response createChannel(Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get(ChannelEnum.code.name()) || StringUtils.isBlank((String)request.get(ChannelEnum.code.name())))
			return ERROR("ERR_CHANNEL_CODE_REQUIRED", "Unique code is mandatory for Channel", ResponseCode.CLIENT_ERROR);
		request.put(ChannelEnum.identifier.name(), (String)request.get(ChannelEnum.code.name()));
        Response res = validateLicense(request);
        if(checkError(res)){
            return res;
        }
		return create(request, CHANNEL_OBJECT_TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response readChannel(String channelId) throws Exception{
		Response response = read(channelId, CHANNEL_OBJECT_TYPE, ChannelEnum.channel.name());
		if (Platform.config.hasPath("channel.fetch.suggested_frameworks") && Platform.config.getBoolean("channel.fetch.suggested_frameworks")) {
			Map<String, Object> responseMap = (Map<String, Object>) response.get(ChannelEnum.channel.name());
			List<Object> frameworkList = (List<Object>) responseMap.get(ChannelEnum.frameworks.name());
			if(null == frameworkList || frameworkList.isEmpty()) {
				List<Object> searchedFrameworkList = getAllFrameworkList();
				if (null != searchedFrameworkList && !searchedFrameworkList.isEmpty()) {
					responseMap.put("suggested_frameworks",searchedFrameworkList);
				}
			}
		}
		return response;
	}

	@Override
	public Response updateChannel(String channelId, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
        Response res = validateLicense(map);
        if(checkError(res)){
            return res;
        }
		return update(channelId, CHANNEL_OBJECT_TYPE, map);
	}

	@Override
	public Response listChannel(Map<String, Object> map) {
		return search(map, CHANNEL_OBJECT_TYPE, "channels", null);
	}

	@Override
	public Response retireChannel(String channelId) {
		return retire(channelId, CHANNEL_OBJECT_TYPE);
	}

	private List<Object> getAllFrameworkList() throws Exception {
		List<Object> searchResult = new ArrayList<Object>();
		SearchDTO searchDto = new SearchDTO();
		searchDto.setFuzzySearch(false);
		searchDto.setProperties(setSearchProperties());
		searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchDto.setFields(getFields());
		searchResult = Await.result(
				processor.processSearchQuery(searchDto, false, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false),
				RequestRouterPool.WAIT_TIMEOUT.duration());

		return searchResult;
	}

	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add(ChannelEnum.identifier.name());
		fields.add(ChannelEnum.name.name());
		fields.add(ChannelEnum.code.name());
		return fields;
	}

	private List<Map> setSearchProperties() {
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<>();
		property.put(ChannelEnum.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put(ChannelEnum.propertyName.name(), ChannelEnum.objectType.name());
		property.put(ChannelEnum.values.name(), ChannelEnum.Framework.name());
		properties.add(property);
		
		property = new HashMap<>();
		property.put(ChannelEnum.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put(ChannelEnum.propertyName.name(), ChannelEnum.status.name());
		property.put(ChannelEnum.values.name(), ChannelEnum.Live.name());
		properties.add(property);

		return properties;
	}

    private Response validateLicense(Map<String, Object> request) {
        if (request.containsKey("defaultLicense")) {
            List<Object> licenseList = RedisStoreUtil.getList("license");
            if (CollectionUtils.isNotEmpty(licenseList)) {
                if (!licenseList.contains(request.get("defaultLicense"))) {
                    return ERROR("ERR_INVALID_LICENSE", "License Not Found With Name: [" + (String)request.get("defaultLicense") + "]", ResponseCode.CLIENT_ERROR);
                }
            } else {
                List<Node> resultList = null;
                Request searchReq = getRequest(GRAPH_ID, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType",
                        GraphDACParams.object_type.name(), "license");
                Response response = getResponse(searchReq);
                if (!checkError(response)) {
                    if (null != response && response.getResponseCode() == ResponseCode.OK) {
                        Map<String, Object> result = response.getResult();
                        resultList = (List<Node>) result.get("node_list");
                        if ( resultList.size() > 0) {
                            for (Node obj : resultList) {
                                if(obj.getMetadata().get("status").equals("Live")){
                                    String licenseName = (String) obj.getMetadata().get("name");
                                    licenseList.add(licenseName);
                                }
                            }
                            RedisStoreUtil.saveList("license", licenseList);
                            if (!licenseList.contains(request.get("defaultLicense"))) {
                                return ERROR("ERR_INVALID_LICENSE", "License Not Found With Name: [" + (String)request.get("defaultLicense") + "]", ResponseCode.CLIENT_ERROR);
                            }
                        }
                    }
                }
                else {
                    return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Something Went Wrong While Processing Your Request. Please Try Again After Sometime!", ResponseCode.SERVER_ERROR);
                }
            }
        }
        return OK();
    }
}
