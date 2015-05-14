package com.ilimi.dac.impl;

import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.dac.BaseDataAccessService;
import com.ilimi.dac.dto.Version;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.entity.VersionEntity;
import com.ilimi.dac.impl.entity.dao.VersionDao;

@Component
public class VersionDataService extends BaseDataAccessService implements IVersionDataService {

    /** The model mapper. */
    private ModelMapper modelMapper = null;

    public VersionDataService() {
        super();
        modelMapper = new ModelMapper();
    }

    @Autowired
    VersionDao dao = null;

    @Override
    @Transactional
    public Response createVersion(Request request) {
        Version version = (Version) request.get(CommonDACParams.object_version.name());
        VersionEntity entity = new VersionEntity();
        modelMapper.map(version, entity);
        dao.save(entity);
        return OK(CommonDACParams.object_version_id.name(), entity.getId());
    }

    @Override
    @Transactional
    public Response getAllVersions(Request request) {
        String objectId = (String) request.get(CommonDACParams.object_id.name());
        Search search = new Search();
        search.addFilter(new Filter("objectId", objectId));

        List<VersionEntity> commentEntities = dao.search(search);
        Type listType = new TypeToken<List<Version>>() {
        }.getType();
        List<Version> versions = modelMapper.map(commentEntities, listType);
        return OK(CommonDACParams.object_versions.name(), versions);
    }

    @Override
    public Response getVersion(Request request) {
        String objectId = (String) request.get(CommonDACParams.object_id.name());
        String version = (String) request.get(CommonDACParams.object_version_num.name());
        Search search = new Search();
        search.addFilterAnd(new Filter("objectId", objectId), new Filter("version", version));
        VersionEntity entity = dao.searchUnique(search);
        if (entity != null) {
            Version versionData = modelMapper.map(entity, Version.class);
            return OK(CommonDACParams.object_version.name(), versionData);
        } else {
            return ERROR(new ClientException("ERR_VERSION_NO_DATA", "Version does not exist."));
        }
    }

}
