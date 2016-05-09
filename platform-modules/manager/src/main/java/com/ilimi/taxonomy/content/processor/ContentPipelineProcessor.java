package com.ilimi.taxonomy.content.processor;

import java.util.ArrayList;
import java.util.List;

import com.ilimi.taxonomy.content.entity.Content;

public class ContentPipelineProcessor extends AbstractProcessor{
	
	protected List<AbstractProcessor> lstProcessor = new ArrayList<AbstractProcessor>();

	@Override
	protected Content process(Content content) {
		for(AbstractProcessor processor: lstProcessor)
			content = processor.execute(content); 
		return content;
	}
	
	protected void registerProcessor(AbstractProcessor processor) {
		if (null != processor)
			lstProcessor.add(processor);
	}

}
