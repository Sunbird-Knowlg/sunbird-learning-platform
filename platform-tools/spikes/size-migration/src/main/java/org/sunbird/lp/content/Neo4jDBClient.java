package org.sunbird.lp.content;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.Values;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author pritha
 *
 */
public class Neo4jDBClient {

	private final Driver driver;
	private StatementResult result;
	/**
	 * Holds mapping for N contentId and it's content
	 */
	private ArrayNode contentArrayNode = JsonNodeFactory.instance.arrayNode();
	private Map<String, Content> contentMap ;//= new HashMap<String, Content>();

	/**
	 * @param uri
	 * @param user
	 * @param password
	 */
	public Neo4jDBClient(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	public void close() throws Exception {
		driver.close();
	}

	/**
	 * fetches the all content's unique id, downloadUrl, artifactUrl where size is
	 * null
	 * 
	 * @param withLimit to limit the number of records gets fetched
	 */
	private void fetchContents(final int withLimit) {
		try (Session session = driver.session()) {
			result = session.readTransaction(new TransactionWork<StatementResult>() {
				@Override
				public StatementResult execute(Transaction tx) {
					return tx.run(
							"match (n:domain) where n.contentType=\"Asset\" and not exists(n.size) and (exists(n.downloadUrl) or exists(n.artifactUrl))return n.IL_UNIQUE_ID as contentId,"
									+ " n.size as contentSize, n.downloadUrl as downloadUrl, n.artifactUrl as artifactUrl limit "
									+ withLimit);
				}
			});
		}
	}

	/**
	 * Updates the size value of a given content
	 * 
	 * @param content
	 */
	public void updateContentSize(final Content content) {
		try (Session session = driver.session()) {
			String trxWork = session.writeTransaction(new TransactionWork<String>() {
				@Override
				public String execute(Transaction tx) {

					StatementResult result = tx.run(
							"MATCH (n) where n.contentType=\"Asset\" and n.IL_UNIQUE_ID= $id " + "SET n.size = $size "
									+ "RETURN n.IL_UNIQUE_ID, n.size",
							parameters("id", content.getContentId(), "size", content.getContentSize()));
					return result.single().get(0).asString();
				}
			});
			System.out.println(content.getContentId() + "content size updated to " + content.getContentSize());
		}
	}
	
	/**
	 * Updates the size value of list of contents
	 * 
	 * @param content
	 * @throws IOException
	 */
	public void updateAllContentSize() throws IOException {
		int cSize = contentMap.size();
		if (cSize > 0) {
			try (Session session = driver.session()) {
				String trxWork = session.writeTransaction(new TransactionWork<String>() {					   
					 
					@Override
					public String execute(Transaction tx) {
					
						for (Entry<String, Content> content : contentMap.entrySet()) {
							StatementResult result = tx.run(
									"match (n:domain) where n.IL_UNIQUE_ID=$id "
											+ "SET n.size = $size " /* + "RETURN n.IL_UNIQUE_ID, n.size " */,
									parameters("id", content.getValue().getContentId(), "size",
											content.getValue().getContentSize()));
							//System.out.println("print resutt "+result);
						}
						return "";

					}
				});
				System.out.println("content updated");
			}

		} else {
			System.out.println("No content to updated!!");

		}		

	}

	/**
	 * Updates the size value of list of contents
	 * 
	 * @param content
	 * @throws IOException
	 */
	public void updateBatchContentSize() throws IOException {

		int cSize = contentArrayNode.size();
		if (cSize > 0) {

			ObjectNode oNode = JsonNodeFactory.instance.objectNode();
			oNode.put("data", contentArrayNode);
			// String data = "{\"data\":[{\"contentId\":\"bg\",\"contentSize\":1111}]}";
			Map value = new ObjectMapper().readValue(oNode.toString(), Map.class);
			System.out.println("Map: " + value + " no of content to updates " + contentArrayNode.size());

			long starts = System.currentTimeMillis();

			try (Session session = driver.session()) {
				String trxWork = session.writeTransaction(new TransactionWork<String>() {

					String query = "UNWIND {data} as row" + " MATCH (n)" + " WHERE n.IL_UNIQUE_ID = row.contentId"
							+ " SET n.size = row.contentSize";

					@Override
					public String execute(Transaction tx) {

						StatementResult result = tx.run(query, Values.value(value)); // return "";

						return "";

					}
				});
				System.out.println("content updated");
			}
			long ends = System.currentTimeMillis();
			System.out.println("time taken to execute trax: " + (ends - starts) + "ms");

		} else {
			System.out.println("No content to updated!!");

		}

	}

	/**
	 * populates the content map with result fetch from db and the size.
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * 
	 */
	private void populateContents() throws JsonGenerationException, JsonMappingException, IOException {
		if (result != null) {
			while (result.hasNext()) {
				Record record = result.next();
				String cId = record.get("contentId").asString();
				String dUrl = record.get("downloadUrl").asString();
				String aUrl = record.get("artifactUrl").asString();

				Content content = new Content(cId, aUrl);
				content.setDownloadUrl(dUrl);

				String artifactUrl = !content.getArifactUrl().equals("null") ? content.getArifactUrl()
						: content.getDownloadUrl();
				// if artifactUrl present or downloadUrl is present, is use to get the size
				try {
					if (artifactUrl != null && !artifactUrl.isEmpty() && !artifactUrl.equals("null")) {
						double size = getContentSize(artifactUrl); // gets content's size from header
						content.setContentSize(size);
						contentArrayNode.add(content.asJson());
						contentMap.put(cId, content);

					} else {
						System.out.println(content.getContentId()
								+ ": artifactUrl, downloadUrl both are empty, so size could not be updated ");
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out
							.println("Exception occured for Content-" + content.getContentId() + ": " + e.getMessage());
				}

			}
		}

	}
	
	private void populateContentsSize() throws JsonGenerationException, JsonMappingException, IOException {
		if (result != null) {
			while (result.hasNext()) {
				Record record = result.next();
				String cId = record.get("contentId").asString();
				String dUrl = record.get("downloadUrl").asString();
				String aUrl = record.get("artifactUrl").asString();
				float sizeStr = record.get("contentSize").asFloat();

				Content content = new Content(cId, aUrl);
				content.setDownloadUrl(dUrl);

				try {
					double size = new Double(sizeStr); // gets content's size from header
					content.setContentSize(size);
					contentArrayNode.add(content.asJson());
					contentMap.put(cId, content);
					/*
					 * if (sizeStr . ) {
					 * 
					 * 
					 * } else { System.out.println(content.getContentId() +
					 * ": size empty, so size could not be updated "); }
					 */
				} catch (Exception e) {
					e.printStackTrace();
					System.out
							.println("Exception occured for Content-" + content.getContentId() + ": " + e.getMessage());
				}

			}
		}

	}

	/**
	 * Execute fetching the contents for DB and populating to Content model
	 * 
	 * @param withLimit
	 * @throws Exception
	 */
	public int run(final int withLimit) throws Exception {
		contentMap = new HashMap<String, Content>();
		fetchContents(withLimit);
		populateContents();
		System.out.println("Number of content's populate to model, " + contentMap.size());
		return contentMap.size();
	}
	/**
	 * no to content to update present.
	 * @return
	 */
	public int runNumber() {
		return contentMap.size();
	}

	/**
	 * to get the content length/size from the header for the given url
	 * 
	 * @param artifactUrl
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws UnirestException
	 */
	public double getContentSize(String artifactUrl)
			throws JsonGenerationException, JsonMappingException, IOException, UnirestException {
		HttpResponse<String> response = Unirest.head(artifactUrl).asString();
		System.out.println(new ObjectMapper().writeValueAsString(response));
		String contentLength = response.getHeaders().get("Content-Length").iterator().next();
		return new Double(contentLength);
	}
}
