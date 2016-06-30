package com.ilimi.taxonomy.content.processor;

import com.ilimi.taxonomy.content.concrete.processor.BaseConcreteProcessor;
import com.ilimi.taxonomy.content.entity.Plugin;

public abstract class AbstractProcessor extends BaseConcreteProcessor {
	
	protected String basePath;
	protected String contentId;
	
	protected AbstractProcessor nextProcessor;
	public void setNextProcessor(AbstractProcessor nextProcessor){
        this.nextProcessor = nextProcessor;
        this.isAutomaticChainExecutionEnabled = true;
    }
	
	protected boolean isAutomaticChainExecutionEnabled = false;
	public void setIsAutomaticChainExecutionEnabled(boolean isAutomaticChainExecutionEnabled) {
		this.isAutomaticChainExecutionEnabled = isAutomaticChainExecutionEnabled;
	}
	
	public Plugin execute(Plugin content){
        content = process(content);
        if(null != nextProcessor && isAutomaticChainExecutionEnabled == true){
        	content = nextProcessor.execute(content);
        }
        return content;
    }
	
	abstract protected Plugin process(Plugin content);
}
