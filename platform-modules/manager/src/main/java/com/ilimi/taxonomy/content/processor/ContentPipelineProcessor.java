package com.ilimi.taxonomy.content.processor;

import java.util.ArrayList;
import java.util.List;

import com.ilimi.taxonomy.content.entity.Content;

public class ContentPipelineProcessor extends AbstractProcessor{
	
	protected List<AbstractProcessor> lstProcessor = new ArrayList<AbstractProcessor>();

	@Override
	protected void process(Content content) {
		for(AbstractProcessor processor: lstProcessor)
			processor.execute(content); 
	}
	
	protected void registerProcessor(AbstractProcessor processor) {
		if (null != processor)
			lstProcessor.add(processor);
	}

}
