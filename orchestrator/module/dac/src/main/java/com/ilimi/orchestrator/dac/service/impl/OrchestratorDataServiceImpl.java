package com.ilimi.orchestrator.dac.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.ScriptTypes;
import com.ilimi.orchestrator.dac.service.IOrchestratorDataService;

public class OrchestratorDataServiceImpl implements IOrchestratorDataService {

    private MongoOperations mongoOps;

    public OrchestratorDataServiceImpl(MongoOperations mongoOps) {
        this.mongoOps = mongoOps;
    }

    @Override
    public OrchestratorScript getScript(String name) {
        if (StringUtils.isNotBlank(name)) {
            Query query = new Query(Criteria.where("name").is(name));
            OrchestratorScript script = this.mongoOps.findOne(query, OrchestratorScript.class);
            return script;
        }
        return null;
    }

    @Override
    public OrchestratorScript getScriptById(String id) {
        if (StringUtils.isNotBlank(id)) {
            Query query = new Query(Criteria.where("id").is(id));
            OrchestratorScript script = this.mongoOps.findOne(query, OrchestratorScript.class);
            return script;
        }
        return null;
    }

    @Override
    public String createScript(OrchestratorScript script) {
        if (null != script) {
            script.setType(ScriptTypes.SCRIPT.name());
            this.mongoOps.insert(script);
            return script.getId();
        }
        return null;
    }

    @Override
    public String createCommand(OrchestratorScript command) {
        if (null != command) {
            command.setType(ScriptTypes.COMMAND.name());
            this.mongoOps.insert(command);
            return command.getId();
        }
        return null;
    }

    @Override
    public void updateScript(OrchestratorScript script) {
        if (null != script)
            this.mongoOps.save(script);
    }

    @Override
    public List<OrchestratorScript> getAllScripts() {
        Query query = new Query(Criteria.where("type").is(ScriptTypes.SCRIPT.name()));
        return this.mongoOps.find(query, OrchestratorScript.class);
    }

    @Override
    public List<OrchestratorScript> getAllCommands() {
        Query query = new Query(Criteria.where("type").is(ScriptTypes.COMMAND.name()));
        return this.mongoOps.find(query, OrchestratorScript.class);
    }

    @Override
    public List<OrchestratorScript> getScriptsByRequestPath(String url, String type) {
        Criteria c = new Criteria();
        c.andOperator(Criteria.where("request_path.url").is(url), Criteria.where("request_path.type").is(type));
        Query query = new Query(c);
        return this.mongoOps.find(query, OrchestratorScript.class);
    }
    
    @Override
    public boolean isCollectionExist(){
    	return this.mongoOps.collectionExists("scripts");
    }
}
