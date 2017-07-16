package org.ekstep.content.processor;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.content.concrete.processor.AssessmentItemCreatorProcessor;
import org.ekstep.content.concrete.processor.AssetCreatorProcessor;
import org.ekstep.content.concrete.processor.AssetsValidatorProcessor;
import org.ekstep.content.concrete.processor.BaseConcreteProcessor;
import org.ekstep.content.concrete.processor.EmbedControllerProcessor;
import org.ekstep.content.concrete.processor.GlobalizeAssetProcessor;
import org.ekstep.content.concrete.processor.LocalizeAssetProcessor;
import org.ekstep.content.concrete.processor.MissingAssetValidatorProcessor;
import org.ekstep.content.concrete.processor.MissingControllerValidatorProcessor;
import org.ekstep.content.entity.Plugin;

/**
 * The Class <code>ContentPipelineProcessor</code> is also a Concrete Processor
 * which inherits the <code>AbstractProcessor</code> This Processor enables the
 * Manual registering of Concrete Processors. It has a <code>list</code> of
 * Concrete Processor as attribute which contains all the Concrete Processor who
 * will be executed in sequence.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 * 
 * @see AbstractProcessor
 */
public class ContentPipelineProcessor extends AbstractProcessor {

	/** List of Processor which are going to take part in the operation. */
	protected List<AbstractProcessor> lstProcessor = new ArrayList<AbstractProcessor>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.content.processor.AbstractProcessor#process(com.ilimi.
	 * taxonomy.content.entity.Plugin)
	 */
	@Override
	protected Plugin process(Plugin content) {
		for (AbstractProcessor processor : lstProcessor)
			content = processor.execute(content);
		return content;
	}

	/**
	 * Register the Concrete Processor to the List of Processor. All the
	 * Processor needs to be registered before in order get their functionality.
	 *
	 * @param processor
	 *            is the instance of Concrete Processor.
	 * 
	 */
	public void registerProcessor(AbstractProcessor processor) {
		if (null != processor)
			lstProcessor.add(processor);
	}

}
