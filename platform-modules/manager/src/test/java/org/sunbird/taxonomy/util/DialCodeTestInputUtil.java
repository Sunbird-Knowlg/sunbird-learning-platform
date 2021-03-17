package org.sunbird.taxonomy.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sunbird.graph.dac.model.Node;

import java.io.IOException;
import java.util.Map;

public class DialCodeTestInputUtil {
    public static final String CONTENT_ID = "org.sunbird.apr10.textbook.test04";

    public static final String CHANNEL_ID = "in.ekstep";

    public static ObjectMapper mapper = new ObjectMapper();


    public static final String HIERARCHY_WITH_RESERVED_DC_1 = "{  \n" +
            "   \"identifier\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "   \"children\":[  \n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641755955211\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"code\":\"textbookunit_0\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":1,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244331\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641760051214\",\n" +
            "               \"parent\":\"do_11273772641755955211\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.336+0530\",\n" +
            "               \"code\":\"textbooksubunit_0\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"questions\":[  \n" +
            "               ],\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.336+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244336\",\n" +
            "               \"depth\":2,\n" +
            "               \"concepts\":[  \n" +
            "               ],\n" +
            "               \"usesContent\":[  \n" +
            "               ],\n" +
            "               \"name\":\"U1.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.337+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U1\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      },\n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641757593612\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"code\":\"textbookunit_1\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":2,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244333\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641761689615\",\n" +
            "               \"parent\":\"do_11273772641757593612\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"code\":\"textbooksubunit_1\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"questions\":[  \n" +
            "               ],\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244338\",\n" +
            "               \"depth\":2,\n" +
            "               \"concepts\":[  \n" +
            "               ],\n" +
            "               \"usesContent\":[  \n" +
            "               ],\n" +
            "               \"name\":\"U2.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U2\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      },\n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641759232013\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"code\":\"textbookunit_2\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":3,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244335\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641763328016\",\n" +
            "               \"parent\":\"do_11273772641759232013\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"code\":\"textbooksubunit_2\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244340\",\n" +
            "               \"depth\":2,\n" +
            "               \"children\":[  \n" +
            "                  {  \n" +
            "                     \"identifier\":\"do_11273772641764147217\",\n" +
            "                     \"parent\":\"do_11273772641763328016\",\n" +
            "                     \"lastStatusChangedOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"code\":\"textbooksubsubunit_2\",\n" +
            "                     \"visibility\":\"Parent\",\n" +
            "                     \"questions\":[  \n" +
            "                     ],\n" +
            "                     \"index\":1,\n" +
            "                     \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "                     \"createdOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"objectType\":\"Content\",\n" +
            "                     \"versionKey\":\"1554898244341\",\n" +
            "                     \"depth\":3,\n" +
            "                     \"concepts\":[  \n" +
            "                     ],\n" +
            "                     \"usesContent\":[  \n" +
            "                     ],\n" +
            "                     \"name\":\"U3.1.1\",\n" +
            "                     \"lastUpdatedOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"contentType\":\"TextBookUnit\",\n" +
            "                     \"status\":\"Draft\"\n" +
            "                  }\n" +
            "               ],\n" +
            "               \"name\":\"U3.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U3\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      }\n" +
            "   ]\n" +
            "}\n" +
            "Collapse\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "7:49 AM\n" +
            "Reserved will have some \n" +
            "{  \n" +
            "   \"identifier\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "   \"children\":[  \n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641755955211\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"code\":\"textbookunit_0\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":1,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244331\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641760051214\",\n" +
            "               \"parent\":\"do_11273772641755955211\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.336+0530\",\n" +
            "               \"code\":\"textbooksubunit_0\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"questions\":[  \n" +
            "               ],\n" +
            "               \"dialcodes\":[  \n" +
            "                  \"XUDKAN\"\n" +
            "               ],\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.336+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244336\",\n" +
            "               \"depth\":2,\n" +
            "               \"concepts\":[  \n" +
            "               ],\n" +
            "               \"usesContent\":[  \n" +
            "               ],\n" +
            "               \"name\":\"U1.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.337+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U1\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      },\n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641757593612\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"code\":\"textbookunit_1\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":2,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244333\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641761689615\",\n" +
            "               \"parent\":\"do_11273772641757593612\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"code\":\"textbooksubunit_1\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"questions\":[  \n" +
            "               ],\n" +
            "               \"dialcodes\":[  \n" +
            "                  \"JAHFKA\"\n" +
            "               ],\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244338\",\n" +
            "               \"depth\":2,\n" +
            "               \"concepts\":[  \n" +
            "               ],\n" +
            "               \"usesContent\":[  \n" +
            "               ],\n" +
            "               \"name\":\"U2.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U2\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      },\n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641759232013\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"code\":\"textbookunit_2\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":3,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244335\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641763328016\",\n" +
            "               \"parent\":\"do_11273772641759232013\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"code\":\"textbooksubunit_2\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244340\",\n" +
            "               \"depth\":2,\n" +
            "               \"children\":[  \n" +
            "                  {  \n" +
            "                     \"identifier\":\"do_11273772641764147217\",\n" +
            "                     \"parent\":\"do_11273772641763328016\",\n" +
            "                     \"lastStatusChangedOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"code\":\"textbooksubsubunit_2\",\n" +
            "                     \"visibility\":\"Parent\",\n" +
            "                     \"questions\":[  \n" +
            "                     ],\n" +
            "                     \"dialcodes\":[  \n" +
            "                        \"XKJFAK\"\n" +
            "                     ],\n" +
            "                     \"index\":1,\n" +
            "                     \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "                     \"createdOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"objectType\":\"Content\",\n" +
            "                     \"versionKey\":\"1554898244341\",\n" +
            "                     \"depth\":3,\n" +
            "                     \"concepts\":[  \n" +
            "                     ],\n" +
            "                     \"usesContent\":[  \n" +
            "                     ],\n" +
            "                     \"name\":\"U3.1.1\",\n" +
            "                     \"lastUpdatedOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"contentType\":\"TextBookUnit\",\n" +
            "                     \"status\":\"Draft\"\n" +
            "                  }\n" +
            "               ],\n" +
            "               \"name\":\"U3.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U3\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      }\n" +
            "   ]\n" +
            "}";

    public static final String HIERARCHY_WITH_RESERVED_DC_2 = "{  \n" +
            "   \"identifier\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "   \"children\":[  \n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641755955211\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"code\":\"textbookunit_0\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":1,\n" +
            "               \"dialcodes\":[  \n" +
            "                  \"DJKNAK\"\n" +
            "               ],\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244331\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641760051214\",\n" +
            "               \"parent\":\"do_11273772641755955211\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.336+0530\",\n" +
            "               \"code\":\"textbooksubunit_0\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"questions\":[  \n" +
            "               ],\n" +
            "               \"dialcodes\":[  \n" +
            "                  \"XUDKAN\"\n" +
            "               ],\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.336+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244336\",\n" +
            "               \"depth\":2,\n" +
            "               \"concepts\":[  \n" +
            "               ],\n" +
            "               \"usesContent\":[  \n" +
            "               ],\n" +
            "               \"name\":\"U1.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.337+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U1\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.331+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      },\n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641757593612\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"code\":\"textbookunit_1\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":2,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244333\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641761689615\",\n" +
            "               \"parent\":\"do_11273772641757593612\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"code\":\"textbooksubunit_1\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"questions\":[  \n" +
            "               ],\n" +
            "               \"dialcodes\":[  \n" +
            "                  \"JAHFKA\"\n" +
            "               ],\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244338\",\n" +
            "               \"depth\":2,\n" +
            "               \"concepts\":[  \n" +
            "               ],\n" +
            "               \"usesContent\":[  \n" +
            "               ],\n" +
            "               \"name\":\"U2.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.338+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U2\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.333+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      },\n" +
            "      {  \n" +
            "         \"identifier\":\"do_11273772641759232013\",\n" +
            "         \"parent\":\"org.sunbird.apr10.textbook.test04\",\n" +
            "         \"lastStatusChangedOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"code\":\"textbookunit_2\",\n" +
            "         \"visibility\":\"Parent\",\n" +
            "         \"index\":3,\n" +
            "         \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "         \"createdOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"objectType\":\"Content\",\n" +
            "         \"versionKey\":\"1554898244335\",\n" +
            "         \"depth\":1,\n" +
            "         \"children\":[  \n" +
            "            {  \n" +
            "               \"identifier\":\"do_11273772641763328016\",\n" +
            "               \"parent\":\"do_11273772641759232013\",\n" +
            "               \"lastStatusChangedOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"code\":\"textbooksubunit_2\",\n" +
            "               \"visibility\":\"Parent\",\n" +
            "               \"index\":1,\n" +
            "               \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "               \"createdOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"objectType\":\"Content\",\n" +
            "               \"versionKey\":\"1554898244340\",\n" +
            "               \"depth\":2,\n" +
            "               \"children\":[  \n" +
            "                  {  \n" +
            "                     \"identifier\":\"do_11273772641764147217\",\n" +
            "                     \"parent\":\"do_11273772641763328016\",\n" +
            "                     \"lastStatusChangedOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"code\":\"textbooksubsubunit_2\",\n" +
            "                     \"visibility\":\"Parent\",\n" +
            "                     \"questions\":[  \n" +
            "                     ],\n" +
            "                     \"dialcodes\":[  \n" +
            "                        \"XKJFAK\"\n" +
            "                     ],\n" +
            "                     \"index\":1,\n" +
            "                     \"mimeType\":\"application/vnd.ekstep.content-collection\",\n" +
            "                     \"createdOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"objectType\":\"Content\",\n" +
            "                     \"versionKey\":\"1554898244341\",\n" +
            "                     \"depth\":3,\n" +
            "                     \"concepts\":[  \n" +
            "                     ],\n" +
            "                     \"usesContent\":[  \n" +
            "                     ],\n" +
            "                     \"name\":\"U3.1.1\",\n" +
            "                     \"lastUpdatedOn\":\"2019-04-10T17:40:44.341+0530\",\n" +
            "                     \"contentType\":\"TextBookUnit\",\n" +
            "                     \"status\":\"Draft\"\n" +
            "                  }\n" +
            "               ],\n" +
            "               \"name\":\"U3.1\",\n" +
            "               \"lastUpdatedOn\":\"2019-04-10T17:40:44.340+0530\",\n" +
            "               \"contentType\":\"TextBookUnit\",\n" +
            "               \"status\":\"Draft\"\n" +
            "            }\n" +
            "         ],\n" +
            "         \"name\":\"U3\",\n" +
            "         \"lastUpdatedOn\":\"2019-04-10T17:40:44.335+0530\",\n" +
            "         \"contentType\":\"TextBookUnit\",\n" +
            "         \"status\":\"Draft\"\n" +
            "      }\n" +
            "   ]\n" +
            "}";

    public static final String testNodeMetadata ="{\"ownershipType\":[\"createdBy\"],\"code\":\"org.sunbird.apr10.textbook.test05\",\"keywords\":[\"QA_Content\"],\"channel\":\"in.ekstep\",\"description\":\"Text Book in English for Class IV\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-11T17:23:24.596+0530\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2019-04-11T17:23:24.596+0530\",\"contentEncoding\":\"gzip\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBook\",\"lastStatusChangedOn\":\"2019-04-11T17:23:24.596+0530\",\"audience\":[\"Learner\"],\"IL_SYS_NODE_TYPE\":\"DATA_NODE\",\"visibility\":\"Default\",\"os\":[\"All\"],\"mediaType\":\"content\",\"osId\":\"org.sunbird.quiz.app\",\"version\":2,\"versionKey\":\"1554983604596\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCF\",\"compatibilityLevel\":1.0,\"IL_FUNC_OBJECT_TYPE\":\"Content\",\"name\":\"Maths-4\",\"IL_UNIQUE_ID\":\"org.sunbird.apr10.textbook.test04\",\"status\":\"Draft\"}";

    public static final String reservedDialCode ="{\"XKJFAK\":0,\"ALDJBC\":1,\"JAHFKA\":3}";

    public static Map<String,Object> getMap(String input) {
        try {
            if(StringUtils.isNotBlank(input) )
                return (mapper.readValue(input, new TypeReference<Map<String,Object>>(){})) ;
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Node getTestNode() {
        Node testNode = new Node();
        testNode.setId(120489);
        testNode.setIdentifier(CONTENT_ID);
        testNode.setGraphId("domain");
        testNode.setNodeType("DATA_NODE");
        Map testMap = getMap(testNodeMetadata);
        testMap.put("reservedDialcodes",getMap(reservedDialCode));
        testNode.setMetadata(testMap);
        return testNode;
    }
}
