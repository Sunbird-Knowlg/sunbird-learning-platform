package com.ilimi.taxonomy.content.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.google.gson.Gson;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.ILogger;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;

public class BaseTest {

	public static final String TEST_GRAPH = "domain";
	public static File folder = new File("src/test/resources/definitions");
	private static TaxonomyManagerImpl taxonomyManager = new TaxonomyManagerImpl();
	public static Map<String, String> definitions = new HashMap<String, String>();

	@BeforeClass
	public static void beforeTest() throws Exception {
		System.out.println("Loading All Definitions...!!");
		definitions = loadAllDefinitions(folder);
		loadAllNodes();
	}

	@AfterClass
	public static void afterTest() {
		System.out.println("deleting Graph...!!");
		deleteGraph(TEST_GRAPH);
	}
 
	public static void loadAllNodes(){
		InputStream in =  csvReader("src/test/resources/literacy/literacy_concepts.csv");
		create(TEST_GRAPH, in);
		InputStream in1 =  csvReader("src/test/resources/literacy/literacy_dimensions.csv");
		create(TEST_GRAPH, in1);
		InputStream in2 =  csvReader("src/test/resources/literacy/literacy_domain.csv");
		create(TEST_GRAPH, in2);
	}
	public static InputStream csvReader(String file){
		InputStream in = null; 
		try {
			 in = new FileInputStream(new File(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return in;
	}
	public static Map<String, String> loadAllDefinitions(File folder) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				loadAllDefinitions(fileEntry);
			} else {
				String definition;
				try {
					definition = FileUtils.readFileToString(fileEntry);
					Response resp = createDefinition(TEST_GRAPH, definition);
					definitions.put(fileEntry.getName(), resp.getResponseCode().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return definitions;
	}

	public void writeStringToFile(String fileName, String data, boolean append) throws IOException {
		try {
			File file = new File(getClass().getResource("/Contents/" + fileName).getFile());
			FileUtils.writeStringToFile(file, data, append);
		} catch (IOException e) {
			throw new IOException("Error: While writing XML string to file.", e);
		}
	}

	public String getFileString(String fileName) {
		String fileString = "";
		File file = new File(getClass().getResource("/Contents/" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}

	public boolean isValidXmlString(String xml) {
		boolean isValid = false;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			document.getDocumentElement().normalize();
			isValid = true;
		} catch (ParserConfigurationException e) {
			isValid = false;
		} catch (SAXException e) {
			isValid = false;
		} catch (IOException e) {
			isValid = false;
		} finally {
			if (document != null) {
				document = null;
			}
		}
		return isValid;
	}

	public boolean isValidJsonString(String json) {
		boolean isValid = false;
		Gson gson = new Gson();
		try {
			gson.fromJson(json, Object.class);
			isValid = true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			isValid = false;
		}
		return isValid;
	}

	public static Response createDefinition(String graphId, String objectType) {
		Response resp = null;
		try {
			resp = taxonomyManager.updateDefinition(graphId, objectType);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	public static Response create(String graphId,InputStream in) {
		Response resp = null;
		try {
			resp = taxonomyManager.create(graphId, in);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public static Response getDefinition(String graphId, String objectType) {
		Response resp = null;
		try {
			resp = taxonomyManager.findDefinition(graphId, objectType);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public static Response deleteDefinition(String graphId, String objectType) {
		Response resp = null;
		try {
			resp = taxonomyManager.deleteDefinition(graphId, objectType);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public static Response deleteGraph(String graphId) {
		Response resp = null;
		try {
			resp = taxonomyManager.delete(graphId);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	protected static Response getResponse(Request request, ILogger logger) {
		ActorRef router = RequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				logger.log("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode()
						+ " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.log("Exception", e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}

	protected static Request setContext(Request request, String graphId, String manager, String operation) {
		request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	protected static Request getRequest(String graphId, String manager, String operation) {
		Request request = new Request();
		return setContext(request, graphId, manager, operation);
	}

	protected static Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	private static ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	protected static boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}
}
