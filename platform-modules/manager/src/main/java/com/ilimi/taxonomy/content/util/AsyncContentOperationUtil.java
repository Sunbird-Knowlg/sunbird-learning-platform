package com.ilimi.taxonomy.content.util;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.common.ContentOperations;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;

public class AsyncContentOperationUtil {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(AsyncContentOperationUtil.class.getName());

	public static void makeAsyncOperation(ContentOperations operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Content Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		if (null == operation)
			throw new ClientException(ContentErrorCodeConstants.INVALID_OPERATION.name(),
					ContentErrorMessageConstants.INVALID_OPERATION + " | [Invalid or 'null' operation.]");

		if (null == parameterMap)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_ASYNC_OPERATION_PARAMETER_MAP
							+ " | [Invalid or 'null' Parameter Map.]");

		Runnable task = new Runnable() {

			@Override
			public void run() {
				try {
					String opt = operation.name();
					switch (opt) {
					case "upload":
					case "UPLOAD":

						break;

					case "publish":
					case "PUBLISH":

						break;

					case "bundle":
					case "BUNDLE":

						break;

					case "review":
					case "REVIEW":

						break;

					default:
						LOGGER.error("Invalid Async Operation.");
						break;
					}
				} catch (Exception e) {
					LOGGER.error("Error! While Making Async Call for Content Operation: " + operation.name(), e);
				}
			}
		};
		new Thread(task, "AsyncContentOperationThread").start();
	}

}
