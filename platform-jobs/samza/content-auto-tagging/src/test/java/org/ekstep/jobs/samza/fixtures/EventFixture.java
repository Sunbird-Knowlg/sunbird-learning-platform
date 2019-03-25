package org.ekstep.jobs.samza.fixtures;

public class EventFixture {

    public static final String GRAPH_UPDATE_EVENT = "{\n" +
            "    \"ets\": 1553067219552,\n" +
            "    \"channel\": \"0124784842112040965\",\n" +
            "    \"transactionData\": {\n" +
            "        \"properties\": {\n" +
            "            \"lastUpdatedOn\": {\n" +
            "                \"ov\": \"2019-03-20T07:33:38.975+0000\",\n" +
            "                \"nv\": \"2019-03-20T07:33:39.529+0000\"\n" +
            "            },\n" +
            "            \"lockKey\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"50630ca2-4dff-442d-b5c2-f64e5adcbf24\"\n" +
            "            },\n" +
            "            \"versionKey\": {\n" +
            "                \"ov\": \"1553067218975\",\n" +
            "                \"nv\": \"1553067219529\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"mid\": \"d0e38dfe-ea2f-4332-b6e0-085d45e3c887\",\n" +
            "    \"label\": \"Resource -1 \",\n" +
            "    \"nodeType\": \"DATA_NODE\",\n" +
            "    \"userId\": \"ANONYMOUS\",\n" +
            "    \"createdOn\": \"2019-03-20T07:33:39.552+0000\",\n" +
            "    \"objectType\": \"Content\",\n" +
            "    \"nodeUniqueId\": \"do_21272272665784320011044\",\n" +
            "    \"requestId\": null,\n" +
            "    \"operationType\": \"UPDATE\",\n" +
            "    \"nodeGraphId\": 516443,\n" +
            "    \"graphId\": \"domain\"\n" +
            "}";

    public static final String GRAPH_CREATE_EVENT = "{\n" +
            "    \"ets\": 1553067218979,\n" +
            "    \"channel\": \"0124784842112040965\",\n" +
            "    \"transactionData\": {\n" +
            "        \"properties\": {\n" +
            "            \"ownershipType\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": [\n" +
            "                    \"createdBy\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"code\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"org.sunbird.XTmRo5\"\n" +
            "            },\n" +
            "            \"channel\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"0124784842112040965\"\n" +
            "            },\n" +
            "            \"description\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Enter description for Resource\"\n" +
            "            },\n" +
            "            \"organisation\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": [\n" +
            "                    \"Odisha\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"language\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": [\n" +
            "                    \"English\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"mimeType\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"application/vnd.ekstep.ecml-archive\"\n" +
            "            },\n" +
            "            \"idealScreenSize\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"normal\"\n" +
            "            },\n" +
            "            \"createdOn\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"2019-03-20T07:33:38.975+0000\"\n" +
            "            },\n" +
            "            \"appId\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"staging.diksha.portal\"\n" +
            "            },\n" +
            "            \"contentDisposition\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"inline\"\n" +
            "            },\n" +
            "            \"lastUpdatedOn\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"2019-03-20T07:33:38.975+0000\"\n" +
            "            },\n" +
            "            \"contentEncoding\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"gzip\"\n" +
            "            },\n" +
            "            \"contentType\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Resource\"\n" +
            "            },\n" +
            "            \"dialcodeRequired\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"No\"\n" +
            "            },\n" +
            "            \"creator\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Qualitrix Content Creator\"\n" +
            "            },\n" +
            "            \"createdFor\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": [\n" +
            "                    \"0124784842112040965\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"audience\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": [\n" +
            "                    \"Learner\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"IL_SYS_NODE_TYPE\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"DATA_NODE\"\n" +
            "            },\n" +
            "            \"visibility\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Default\"\n" +
            "            },\n" +
            "            \"os\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": [\n" +
            "                    \"All\"\n" +
            "                ]\n" +
            "            },\n" +
            "            \"consumerId\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"a9cb3a83-a164-4bf0-aa49-b834cebf1c07\"\n" +
            "            },\n" +
            "            \"mediaType\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"content\"\n" +
            "            },\n" +
            "            \"osId\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"org.ekstep.quiz.app\"\n" +
            "            },\n" +
            "            \"versionKey\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"1553067218975\"\n" +
            "            },\n" +
            "            \"idealScreenDensity\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"hdpi\"\n" +
            "            },\n" +
            "            \"framework\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"mh_k-12_1\"\n" +
            "            },\n" +
            "            \"createdBy\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"ab467e6e-1f32-453c-b1d8-c6b5fa6c7b9e\"\n" +
            "            },\n" +
            "            \"compatibilityLevel\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": 1.0\n" +
            "            },\n" +
            "            \"IL_FUNC_OBJECT_TYPE\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Content\"\n" +
            "            },\n" +
            "            \"name\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Resource -1 \"\n" +
            "            },\n" +
            "            \"IL_UNIQUE_ID\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"do_21272272665784320011044\"\n" +
            "            },\n" +
            "            \"resourceType\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Learn\"\n" +
            "            },\n" +
            "            \"status\": {\n" +
            "                \"ov\": null,\n" +
            "                \"nv\": \"Draft\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"mid\": \"36183275-6615-4460-89da-119be83cbedf\",\n" +
            "    \"label\": \"Resource -1 \",\n" +
            "    \"nodeType\": \"DATA_NODE\",\n" +
            "    \"userId\": \"ANONYMOUS\",\n" +
            "    \"createdOn\": \"2019-03-20T07:33:38.979+0000\",\n" +
            "    \"objectType\": \"Content\",\n" +
            "    \"nodeUniqueId\": \"do_21272272665784320011044\",\n" +
            "    \"requestId\": null,\n" +
            "    \"operationType\": \"CREATE\",\n" +
            "    \"nodeGraphId\": 516443,\n" +
            "    \"graphId\": \"domain\"\n" +
            "}";
}
