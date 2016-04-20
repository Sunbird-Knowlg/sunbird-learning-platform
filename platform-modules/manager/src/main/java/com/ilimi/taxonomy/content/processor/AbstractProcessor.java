package com.ilimi.taxonomy.content.processor;

import com.ilimi.taxonomy.content.entity.Content;

public abstract class AbstractProcessor {
	
	protected AbstractProcessor nextProcessor;
	public void setNextProcessor(AbstractProcessor nextProcessor){
        this.nextProcessor = nextProcessor;
        this.isAutomaticChainExecutionEnabled = true;
    }
	
	protected boolean isAutomaticChainExecutionEnabled = false;
	public void setIsAutomaticChainExecutionEnabled(boolean isAutomaticChainExecutionEnabled) {
		this.isAutomaticChainExecutionEnabled = isAutomaticChainExecutionEnabled;
	}
	
	public void execute(Content content){
        process(content);
        if(null != nextProcessor && isAutomaticChainExecutionEnabled == true){
        	nextProcessor.execute(content);
        }
    }
	
	abstract protected void process(Content content);
}
