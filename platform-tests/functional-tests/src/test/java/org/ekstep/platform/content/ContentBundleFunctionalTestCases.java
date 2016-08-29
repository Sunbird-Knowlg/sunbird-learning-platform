package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.platform.domain.BaseTest;
import org.json.JSONException;
//import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;

	
public class ContentBundleFunctionalTestCases extends BaseTest{

	int rn = generateRandomInt(0, 9999999);
	
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonCreateThreeContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}, { \"identifier\": \"id3\"}]}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"status\": \"Live\"}}}";
	String jsonGetContentList = "{\"request\": { \"search\": {\"tags\":[\"LP_functionalTest\"], \"sort\": \"contentType\",\"order\": \"asc\"}}}";
	String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.ekstep.app\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"body\": \"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<theme id=\"theme\" startStage=\"splash\" ver=\"0.2\">\n    <manifest>\n\n        <media assetId=\"org.ekstep.howtoweighanelephant_question_block\" id=\"question_block\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554624418question_b.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_level_band\" id=\"level_band\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620387level_band.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_publisher_logo\" id=\"publisher_logo\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623739publisher_logo.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_previous\" id=\"previous\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620539icon_previous.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_next\" id=\"next\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623871icon_next.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_icon_sound\" id=\"icon_sound\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554622151icon_sound.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_icon_hint\" id=\"icon_hint\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623454icon_hint.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_icon_home\" id=\"icon_home\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554626551icon_home.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_icon_reload\" id=\"icon_reload\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554621569icon_reload.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_start\" id=\"start\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554621688start.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_speech_bubble\" id=\"speech_bubble\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554626679speech_bubble.png\" type=\"image\"/>\n\n\n        <media assetId=\"org.ekstep.howtoweighanelephant_page1\" id=\"page1\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554626051image_1.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page2\" id=\"page2\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554626405image_2.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page3\" id=\"page3\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554625593image_3.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page4\" id=\"page4\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554625888image_4.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page5\" id=\"page5\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554625069image_5.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page6\" id=\"page6\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554625438image_6.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page7\" id=\"page7\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554624127image_7.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page8\" id=\"page8\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554624549image_8.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page9\" id=\"page9\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623580image_9.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page10\" id=\"page10\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554624709image_10.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page11\" id=\"page11\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554624293image_11.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page12\" id=\"page12\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554625222image_12.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page13\" id=\"page13\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554624888image_13.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page14\" id=\"page14\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554622708image_14.jpg\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_page15\" id=\"page15\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554622865image_15.jpg\" type=\"image\"/>\n\n\n        <media assetId=\"org.ekstep.howtoweighanelephant_confirmDiag\" id=\"confirmDiag\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554619929confirmBG.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_yes\" id=\"yes\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623020Yes.png\" type=\"image\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_no\" id=\"no\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620682No.png\" type=\"image\"/> \n        \n        \n        <media assetId=\"org.ekstep.howtoweighanelephant_scene2_audio\" id=\"scene2_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554621984scene2.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene3_audio\" id=\"scene3_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554621813scene3.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene4_audio\" id=\"scene4_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620242scene4.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene5_audio\" id=\"scene5_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554622565scene5.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene6_audio\" id=\"scene6_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620976scene6.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene7_audio\" id=\"scene7_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623148scene7.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene8_audio\" id=\"scene8_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620061scene8.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene9_audio\" id=\"scene9_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554626828scene9.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene10_audio\" id=\"scene10_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554620808scene10.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene11_audio\" id=\"scene11_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554623311scene11.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene12_audio\" id=\"scene12_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554621233scene12.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene13_audio\" id=\"scene13_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554626242scene13.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene14_audio\" id=\"scene14_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554621376scene14.mp3\" type=\"audio\"/>\n        <media assetId=\"org.ekstep.howtoweighanelephant_scene15_audio\" id=\"scene15_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1455554622412scene15.mp3\" type=\"audio\"/>\n\n    </manifest>\n\n\n    <controller id=\"story_data\" name=\"storyData\" type=\"data\"><![CDATA[{\n\t\"model\": {\n\t\t\"scene1_body\" : \"\",\n\t\t\"scene2_body\" : \"\",\n\t\t\"scene3_body\" : \"\",\n\t\t\"scene4_body\" : \"So the fruit sellers tried.\",\n\t\t\"scene5_body\" : \"The rope makers tried.\",\n\t\t\"scene6_body\" : \"The jewellers tried.\",\n\t\t\"scene7_body\" : \"The royal ministers tried.\",\n\t\t\"scene8_body\" : \"Even the court scientists tried. At last one of them said, “My little daughter Lilavati can help us.”\",\n\t\t\"scene9_body\" : \"So the King called Lilavati. Lilavati thought. And thought. And thought. At last she had an idea!\",\n\t\t\"scene10_body\" : \"She led the King’s roly- poly elephant to the river.\",\n\t\t\"scene11_body\" : \"And got the chubby- happy little elephant into a boat.\",\n\t\t\"scene12_body\" : \"The boat sank and sank and sank. When it sank no more, Lilavati made a mark on the boat.\",\n\t\t\"scene13_body\" : \"Then she got the elephant out of the boat. And the boat was filled up with stones. When the boat sank up to the mark, Lilavati said, “Stop!”\",\n\t\t\"scene14_body\" : \"Now weigh the stones. And subtract my weight. “And that, O King, is the weight of your elephant!”\"\n\t}\n}]]></controller>\n\n    <stage id=\"baseStage\" preload=\"true\">\n        <image asset=\"b-outer\"/>\n        <image asset=\"w-outer\"/>\n        <image asset=\"t-outer\"/>\n        <image asset=\"b-inner\"/>\n        <image asset=\"w-inner\"/>\n        <image asset=\"t-inner\"/>\n        <image asset=\"confirmDiag\"/>\n        <image asset=\"yes\"/>\n        <image asset=\"no\"/>\n        <image asset=\"icon_home\"/>\n    </stage>\n\n    <stage id=\"storyBaseStage\" preload=\"true\">\n        <param name=\"home_stage\" value=\"splash\"/>\n        <image asset=\"next\" h=\"8.3\" id=\"next\" visible=\"false\" w=\"5\" x=\"93\" y=\"3\"/>\n        <shape h=\"15\" hitArea=\"true\" id=\"nextContainer\" type=\"rect\" visible=\"false\" w=\"10\" x=\"90\" y=\"1\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"left\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"next\" type=\"command\"/>\n            </event>\n        </shape>\n        <image asset=\"previous\" h=\"8.3\" id=\"previous\" visible=\"false\" w=\"5\" x=\"2\" y=\"3\"/>\n        <shape h=\"15\" hitArea=\"true\" id=\"previousContainer\" type=\"rect\" visible=\"false\" w=\"10\" x=\"1\" y=\"1\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"right\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"previous\" transitionType=\"previous\" type=\"command\"/>\n            </event>\n        </shape>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"86\" x=\"9\" y=\"7\"/>\n        \n        <image asset=\"icon_home\" h=\"8.5\" w=\"5\" x=\"2\" y=\"88\"/>\n        <shape h=\"10\" hitArea=\"true\" type=\"rect\" w=\"8\" x=\"1\" y=\"87\">\n            <event type=\"click\">\n            <action asset=\"confirmDialog\" command=\"show\" type=\"command\"/>\n                <action asset=\"confirmDialog\" type=\"animation\">\n                   <tween id=\"confirmShowAnim\">: \n                       <to duration=\"1500\" ease=\"bounceOut\"><![CDATA[{\"x\":35,\"y\":40,\"w\":40,\"h\":30}]]></to>\n                   </tween>\n               </action>\n                <!--<action type=\"command\" command=\"transitionTo\" asset=\"theme\" param=\"home_stage\" effect=\"scroll\" direction=\"right\" ease=\"linear\" duration=\"600\" /> -->            \n            </event>\n        </shape>\n        \n        <g h=\"30\" id=\"confirmDialog\" visible=\"false\" w=\"30\" x=\"35\" y=\"-40\">\n            <image asset=\"confirmDiag\" h=\"100\" id=\"right\" visible=\"true\" w=\"100\" x=\"0\" y=\"0\"/>\n            \n            <image asset=\"yes\" h=\"30\" id=\"right\" visible=\"true\" w=\"15\" x=\"25\" y=\"53\">\n                <event type=\"click\">\n                    <action asset=\"theme\" command=\"transitionTo\" direction=\"right\" duration=\"600\" ease=\"linear\" effect=\"scroll\" param=\"home_stage\" type=\"command\"/>\n                    <action asset=\"retryDialog\" type=\"animation\">\n                       <tween id=\"retryHideAnim\">\n                           <to duration=\"500\" ease=\"bounceOut\"><![CDATA[{\"x\":30,\"y\":-40,\"w\":40,\"h\":30}]]></to>\n                       </tween>\n                   </action>\n                </event>\n            </image>\n            \n            <image asset=\"no\" h=\"30\" id=\"right\" visible=\"true\" w=\"15\" x=\"57\" y=\"53\">\n                <event type=\"click\">\n                    <action asset=\"confirmDialog\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n        </g>  \n    </stage>\n\n\n    <stage h=\"100\" id=\"splash\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene2\"/>\n        <image asset=\"page1\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        \n        <image asset=\"next\" h=\"13\" id=\"next\" w=\"8\" x=\"88\" y=\"5\"/>\n        <shape h=\"13\" hitArea=\"true\" id=\"nextContainer\" type=\"rect\" visible=\"false\" w=\"8\" x=\"88\" y=\"5\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"left\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"next\" type=\"command\"/>\n            </event>\n        </shape>\n        <image asset=\"previous\" h=\"13\" id=\"previous\" visible=\"true\" w=\"8\" x=\"2\" y=\"5\"/>\n        <shape h=\"13\" hitArea=\"true\" id=\"previousContainer\" type=\"rect\" visible=\"true\" w=\"8\" x=\"2\" y=\"5\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"windowEvent\" href=\"#/content/list\" type=\"command\"/>\n            </event>\n        </shape>\n    </stage>\n\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene2\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene2_audio\"/>\n        <param name=\"previous\" value=\"splash\"/>\n        <param name=\"next\" value=\"scene3\"/>\n        <image asset=\"page2\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"25\">Not so long ago, there was a King.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"30\">The King had a pet elephant.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"35\">A roly-poly elephant.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"40\">A chubby- happy elephant.</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene2_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene2_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene2_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene3\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene3_audio\"/>\n        <param name=\"previous\" value=\"scene2\"/>\n        <param name=\"next\" value=\"scene4\"/>\n        <image asset=\"page3\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"25\">One day the King said out aloud,</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"30\">\"I wonder how much my elephant weighs?\".</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene3_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene3_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene3_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene4\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene4_audio\"/>\n        <param name=\"previous\" value=\"scene3\"/>\n        <param name=\"next\" value=\"scene5\"/>\n        <image asset=\"page4\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"15\" y=\"20\">The King declared, “Find out the weight of my elephant.” </text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"25\">And win a Grand Prize!</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene4_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene4_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene4_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene5\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene5_audio\"/>\n        <param name=\"previous\" value=\"scene4\"/>\n        <param name=\"next\" value=\"scene6\"/>\n        <image asset=\"page5\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"25\" y=\"25\">So the fruit sellers tried.</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene5_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene5_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene5_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene6\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene6_audio\"/>\n        <param name=\"previous\" value=\"scene5\"/>\n        <param name=\"next\" value=\"scene7\"/>\n        <image asset=\"page6\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"65\" y=\"20\">The rope makers tried.</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene6_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene6_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene6_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene7\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene7_audio\"/>\n        <param name=\"previous\" value=\"scene6\"/>\n        <param name=\"next\" value=\"scene8\"/>\n        <image asset=\"page7\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"20\" y=\"30\">The jewellers tried.</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene7_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene7_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene7_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene8\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene8_audio\"/>\n        <param name=\"previous\" value=\"scene7\"/>\n        <param name=\"next\" value=\"scene9\"/>\n        <image asset=\"page8\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"20\" y=\"30\">The royal ministers tried.</text>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene8_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene8_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene8_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene9\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene9_audio\"/>\n        <param name=\"previous\" value=\"scene8\"/>\n        <param name=\"next\" value=\"scene10\"/>\n        <image asset=\"page9\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"20\" y=\"25\">Even the court scientists tried. At last one of them said,</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"80\" x=\"20\" y=\"30\">“My little daughter Lilavati can help us.”</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene9_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene9_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene9_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene10\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene10_audio\"/>\n        <param name=\"previous\" value=\"scene9\"/>\n        <param name=\"next\" value=\"scene11\"/>\n        <image asset=\"page10\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"65\" y=\"15\">So the King called Lilavati.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"65\" y=\"20\">Lilavati thought. And thought.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"65\" y=\"25\">And thought.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"65\" y=\"30\">At last she had an idea!</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene10_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene10_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene10_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene11\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene11_audio\"/>\n        <param name=\"previous\" value=\"scene10\"/>\n        <param name=\"next\" value=\"scene12\"/>\n        <image asset=\"page11\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"5\" y=\"25\">She led the King’s roly-poly</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"5\" y=\"30\">elephant to the river.</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene11_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene11_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene11_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene12\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene12_audio\"/>\n        <param name=\"previous\" value=\"scene11\"/>\n        <param name=\"next\" value=\"scene13\"/>\n        <image asset=\"page12\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"25\">And got the chubby happy</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"30\">little elephant into a boat.</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene12_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene12_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene12_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene13\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene13_audio\"/>\n        <param name=\"previous\" value=\"scene12\"/>\n        <param name=\"next\" value=\"scene14\"/>\n        <image asset=\"page13\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"60\" x=\"15\" y=\"30\">The boat sank and sank and sank. When it sank no more,</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"15\" y=\"35\">Lilavati made a mark on the boat.</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene13_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene13_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene13_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene14\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene14_audio\"/>\n        <param name=\"previous\" value=\"scene13\"/>\n        <param name=\"next\" value=\"scene15\"/>\n        <image asset=\"page14\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"60\" x=\"20\" y=\"20\">Then she got the elephant out of the boat. </text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"60\" x=\"20\" y=\"25\">And the boat was filled up with stones.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"60\" x=\"20\" y=\"30\">When the boat sank up to the mark, Lilavati said, “Stop!”</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene14_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene14_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene14_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene15\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene15_audio\"/>\n        <param name=\"previous\" value=\"scene14\"/>\n        <param name=\"next\" value=\"scene16\"/>\n        <image asset=\"page15\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"25\" y=\"20\">Now weigh the stones. And subtract my weight.</text>\n        <text font=\"Calibri\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"50\" x=\"25\" y=\"25\">“And that, O King, is the weight of your elephant!”</text>\n        \n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene15_audio\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene15_audio\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene15_audio\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n\n    <stage h=\"100\" id=\"scene16\" w=\"100\" x=\"0\" y=\"0\">\n        <shape fill=\"#FFF16E\" h=\"100\" type=\"rect\" w=\"100\" x=\"0\" y=\"0\"/>\n        <text font=\"Calibri\" fontsize=\"96\" h=\"8\" w=\"30\" x=\"38\" y=\"15\">\n            Thank you\n        </text>\n        \n        <image asset=\"icon_reload\" h=\"20\" w=\"12\" x=\"30\" y=\"35\"/>\n        <text font=\"Calibri\" fontsize=\"48\" h=\"20\" w=\"15\" x=\"32\" y=\"60\">\n            Replay\n        </text>\n        <shape h=\"20\" hitArea=\"true\" type=\"rect\" w=\"12\" x=\"30\" y=\"35\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"restart\" type=\"command\"/>\n            </event>\n        </shape>\n\n        <image asset=\"icon_home\" h=\"20\" w=\"12\" x=\"55\" y=\"35\"/>\n        <text font=\"Calibri\" fontsize=\"48\" h=\"5\" w=\"15\" x=\"58\" y=\"60\">\n            Home\n        </text>\n        <shape h=\"20\" hitArea=\"true\" type=\"rect\" w=\"12\" x=\"55\" y=\"35\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"windowEvent\" href=\"#/content/list\" type=\"command\"/>\n            </event>\n        </shape>\n    </stage>\n\n</theme\"}}}";

	
	String invalidContentId = "LP_FT"+rn+"";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("UploadFiles/DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	
	@BeforeClass
	public static void setup() throws URISyntaxException{
		downloadPath = new File(url.toURI().getPath());		
	}	
	
	@AfterClass
	public static void end() throws IOException{
		FileUtils.cleanDirectory(downloadPath);
		
	}

	// Create and bundle content

	// Create content
	@Test
	public void createAndBundleECMLContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
		post("/learning/v2/content/upload/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Bundle content without upload file
	@Test
	public void createAndBundleWithoutUploadExpect4xx(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				extract().response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}

	// Create and Bundle APK
	@Test
	public void createAndBundleAPKContentExpectSuccess200() {
		contentCleanUp();
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContentAPK).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
		post("/learning/v2/content/upload/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Create and Bundle collection
	@Test
	public void createAndBundleCollectionExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/Verbs_test.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+collectionNode+"\"],\"file_name\": \"Testqa_bundle_Collection\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Bundle AT content
	@Test
	public void bundleAuthoringToolContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Update content body
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonUpdateATContentBody).
		with().
		contentType("application/json").
		when().
		patch("/learning/v2/content/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle AT content and Uploaded content
	@Test
	public void bundleATAndUploadedContentExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Update content body
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateATContentBody).
				with().
				contentType("application/json").
				when().
				patch("/learning/v2/content/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());
				
				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		
		//Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);		
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle existing and non-existing content
	@Test
	public void bundleExistingAndNonExistingContentExpect4xx(){
		contentCleanUp();
		setURI();
		Response R =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		then().
		post("/learning/v2/content/upload/"+nodeId);

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\",\""+invalidContentId+"\"],\"file_name\": \"Testqa_bundle_invalid\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		log().all().
		spec(get404ResponseSpec());
	}

	// Bundle ECML and APK Contents
	@Test
	public void bundleECMLAndAPKContentsExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if(count==1){
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			}
			if(count==2){
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"").put("mimeType", "application/vnd.android.package-archive");
			}
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/Verbs_test.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		//Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);	
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle Live and Draft contents
	public void bundleLiveAndDraftContentsExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/Verbs_test.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}	
			count++;
		}
		//Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);		
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle Live and Retired Content
	@Test
	public void bundleLiveAndRetiredContentExpect4xx(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/Verbs_test.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Update status as Retired
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired");
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				patch("/learning/v2/content/"+node2);

			}	
			count++;
		}

		//Bundle both the contents
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}

	// Bundle collection with Live, Draft and Review contents
	@Test
	public void bundleCollectionWithLDRContentsExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		String node3 = null;
		int count = 1;
		while(count<=3){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/Verbs_test.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==3){
				node3 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/the_moon_and_the_cap.zip")).
				when().
				post("/learning/v2/content/upload/"+node3).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Update status as Review
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Review");
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				patch("/learning/v2/content/"+node3);
			}
			count++;
		}

		// Create collection
		setURI();
		jsonCreateThreeContentCollection = jsonCreateThreeContentCollection.replace("id1", node1).replace("id2", node2).replace("id3", node3);
		Response R1 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateThreeContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+collectionNode+"\"],\"file_name\": \"Testqa_bundle_LDRContentCollection\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Bundle collection with live and retired contents
	@Test
	public void bundleCollectionWithLiveAndRetiredContentsExpect400(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/Verbs_test")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
				
				// Update status as Retired
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired");
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				patch("/learning/v2/content/"+node2);

			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+collectionNode+"\"],\"file_name\": \"Testqa_bundle_CollectionLiveAndRetired\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}
	
	// Bundle nested collection
	@Test
	public void bundleNestedCollectionExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(900, 1999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/haircut_story.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/the_moon_and_the_cap.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId1 = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("/learning/v2/content/publish/"+nodeId1).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Create nested collection
		setURI();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId1);
		Response R3 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateNestedCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP3 = R3.jsonPath();
		String collectionId = jP3.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("/learning/v2/content/publish/"+collectionId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+collectionId+"\"],\"file_name\": \"Testqa_bundle_nestedCollection\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Content clean up	
	public void contentCleanUp(){
		setURI();
		given().
		body(jsonContentClean).
		with().
		contentType(JSON).
		when().
		post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	@SuppressWarnings({ "unused" })
	private boolean bundleValidation(String ecarUrl) throws ClassCastException{
		double manifestVesionActual = 1.1;
		boolean bundleValidation = true;
		try{
			String bundleName = "bundle_"+rn+"";
			
			// Downloading the Ecar from ecar url
			FileUtils.copyURLToFile(new URL(ecarUrl), new File(downloadPath+"/"+bundleName+".zip"));		
			String bundlePath = downloadPath+"/"+bundleName+".zip";
			
			// Setting up extract path
			File bundleExtract = new File(downloadPath+"/"+bundleName);
			String bundleExtractPath = bundleExtract.getPath();
			
			try{
				// Unzip the file
				ZipFile bundleZip = new ZipFile(bundlePath);
				bundleZip.extractAll(bundleExtractPath);

				File fileName = new File(bundleExtractPath);
				File[] listofFiles = fileName.listFiles();

				for(File file : listofFiles){
					//System.out.println(file.getName());
					if (file.isDirectory()){
						File[] listofsubFiles = file.listFiles();
						for(File newfile : listofsubFiles){
							//String fName = newfile.getName();
							if (fName.endsWith(".zip")|| fName.endsWith(".rar")){
								//System.out.println(fName);
							}
						}
					}
				}				
				// Reading the manifest		
				File manifest = new File(bundleExtractPath+"/manifest.json");
				Gson gson = new Gson();
				JsonParser parser = new JsonParser();
	            		JsonElement jsonElement = parser.parse(new FileReader(manifest));
				JsonObject obj = jsonElement.getAsJsonObject();
				JsonElement manifestVersionElement = obj.get("ver"); 
				Double manifestVersion = manifestVersionElement.getAsDouble();
				//System.out.println(manifestVersion);
				Assert.assertTrue(manifestVersion.equals(manifestVesionActual));
				
				// Validating expiry and items
				JsonObject arc = obj.getAsJsonObject("archive");
				if (arc.has("expires") && arc.has("items")){
					JsonArray items = arc.getAsJsonArray("items");
					
			        @SuppressWarnings("rawtypes")
					Iterator i = items.iterator();
			        while(i.hasNext()) {
						try {
							
							// Validating download url, status and package version
							JsonObject item = (JsonObject) i.next();
							String downloadUrl = getStringValue(item, "downloadUrl");
							String status = getStringValue(item, "status");
							JsonElement pkgVersionElement = item.get("pkgVersion");
							Float pkgVersion = pkgVersionElement.getAsFloat();
							if (status.equals("draft")||status.equals("review")){
								Assert.assertTrue(pkgVersion.equals("0"));
							}
							if(contentType.equals("Collection")){
										Assert.assertThat(downloadUrl, null);
									}
									Assert.assertTrue(downloadUrl!=null);
							}			
							 catch (Exception classCastException){
						        	classCastException.printStackTrace();
									 return false;
						        }
							}
						}
					}
		
					catch(Exception zipExtract) {
					zipExtract.printStackTrace();
					 return false;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}        
		return true;
		}
	
	private String getStringValue(JsonObject obj, String attr) {
		if (obj.has(attr)) {
			JsonElement element = obj.get(attr);
			return element.getAsString();
		}
		return null;
	}

}
