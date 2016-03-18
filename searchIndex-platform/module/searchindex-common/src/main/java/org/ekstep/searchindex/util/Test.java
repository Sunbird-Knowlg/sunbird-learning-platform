package org.ekstep.searchindex.util;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class Test {

	public static void main(String[] args) {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis").object().key("filter").object()
				.key("nfkc_normalizer").object().key("type").value("icu_normalizer").key("name").value("nfkc")
				.endObject().endObject().key("analyzer").object().key("ind_normalizer").object().key("tokenizer")
				.value("icu_tokenizer").key("filter").array().value("nfkc_normalizer").endArray().endObject()
				.endObject().endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key("Word").object().key("properties").object().key("word").object().key("type")
				.value("string").key("analyzer").value("ind_normalizer").key("fields").object().key("hash").object()
				.key("type").value("murmur3").endObject().endObject().endObject().key("rootWord").object().key("type")
				.value("string").key("analyzer").value("ind_normalizer").key("fields").object().key("hash").object()
				.key("type").value("murmur3").endObject().endObject().endObject().key("date").object().key("type")
				.value("date").key("format").value("dd-MMM-yyyy HH:mm:ss").endObject().endObject().endObject()
				.endObject();
	System.out.println("Hi");
	}
}
