package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
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
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;


public class ContentPublishWorkflowTests extends BaseTest{
	
	int rn = generateRandomInt(0, 9999999);
	
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"status\": \"Live\"}}}";
	String jsonGetContentList = "{\"request\": { \"search\": {\"tags\":[\"LP_functionalTest\"], \"sort\": \"contentType\",\"order\": \"asc\"}}}";
	String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";

	String invalidContentId = "LP_FT"+rn+"";
	String malformedXMLBody = "xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<theme id=\"theme\" startStage=\"splash\" ver=\"0.2\">\n    <manifest>\n        <media assetId=\"LP_FT_2222_razor\" id=\"razor\" srcheast-1.amazsetId=\"LP_FT_2222_scissors\" pe=\"image\"/>\n        <media assetId=\"LP_FT_2222_saw\" id=\"w.png\" type=\"Test_QAfe\" id=\"t-1.amazonaws.com/content/1468nife.gif\" type=\"image\"/>\n        <media assetId=\"Ton_block\" id=\"question_\" src=\"https://ublic.outheast-1.amazonaws.com/content/1468397836145question_b.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_barber_img\" id=\"barber_img\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397838507barber.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_carpenter_img\" id=\"carpenter_img\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397762865carpenter.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_tailor_img\" id=\"tailor_img\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397875338tailor.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_wife_img\" id=\"wife_img\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397760639wife.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_level_band\" id=\"level_band\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397791243level_band.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_publisher_logo\" id=\"publisher_logo\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397821936publisher_logo.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_previous\" id=\"previous\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397868260icon_previous.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_next\" id=\"next\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397830054icon_next.png\" type=\"image\"/>\n        <media id=\"play_icon\" src=\"play.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_icon_sound\" id=\"icon_sound\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397756407icon_sound.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_icon_hint\" id=\"icon_hint\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397816738icon_hint.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_icon_home\" id=\"icon_home\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397827745icon_home.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_icon_reload\" id=\"icon_reload\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397814189icon_reload.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_start\" id=\"start\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397793617start.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_speech_bubble\" id=\"speech_bubble\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397779773speech_bubble.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_sringeri\" id=\"sringeri\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397865843sringeri.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page1\" id=\"page1\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397777632image_1.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page2\" id=\"page2\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397776418image_2.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page3\" id=\"page3\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397774946image_3.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page4\" id=\"page4\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397773844image_4.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page5\" id=\"page5\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397771556image_5.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page6\" id=\"page6\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397769420image_6.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page7\" id=\"page7\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397767520image_7.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page8\" id=\"page8\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397766313image_8.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page9\" id=\"page9\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397765044image_9.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page10\" id=\"page10\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397794695image_10.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page11\" id=\"page11\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397798543image_11.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page12\" id=\"page12\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397799973image_12.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page13\" id=\"page13\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397801329image_13.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page14\" id=\"page14\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397802465image_14.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page15\" id=\"page15\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397804708image_15.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page16\" id=\"page16\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397803614image_16.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page17\" id=\"page17\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397808743image_17.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page18\" id=\"page18\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397806411image_18.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page19\" id=\"page19\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397812912image_19.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_page20\" id=\"page20\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397757577image_20.jpg\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_t-outer\" id=\"t-outer\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397792276tailor-outer.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_t-inner\" id=\"t-inner\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397796202tailor-inner.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_w-outer\" id=\"w-outer\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397752449wife-outer.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_w-inner\" id=\"w-inner\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397846616wife-inner.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_b-outer\" id=\"b-outer\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397811095barber-outer.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_b-inner\" id=\"b-inner\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397837294barber-inner.png\" type=\"image\"/>\n        <media assetId=\"LP_FT_2222_scene17_audio\" id=\"scene17_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397840644scene17.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"3944.4897959183672\" id=\"scene17_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene11_audio\" id=\"scene11_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397781428scene11.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"3996.734693877551\" id=\"scene11_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene16_audio\" id=\"scene16_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397833853scene16.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"5355.102040816327\" id=\"scene16_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_splash_audio\" id=\"splash_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397758844splash.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"7131.428571428572\" id=\"cover_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene20_audio\" id=\"scene20_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397823096scene20.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"2821.2244897959185\" id=\"learning10_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"4040.2721088435383\" id=\"learning11_sound\" startTime=\"4000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene13_audio\" id=\"scene13_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397772683scene13.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"7732.244897959184\" id=\"scene13_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene21_audio\" id=\"scene21_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397862114scene21.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"3355.283446712018\" id=\"learning12_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"3837.0975056689344\" id=\"learning13_sound\" startTime=\"5000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene10_audio\" id=\"scene10_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397818439scene10.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"8019.591836734694\" id=\"scene10_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene14_audio\" id=\"scene14_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397755217scene14.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"7000.816326530612\" id=\"scene14_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"2000\" id=\"tiger_sound\" startTime=\"9000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene19_audio\" id=\"scene19_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397786365scene19.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"12303.673469387755\" id=\"scene19_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene18_audio\" id=\"scene18_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397869244scene18.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"12512.653061224491\" id=\"scene18_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene15_audio\" id=\"scene15_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397831054scene15.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"17998.367346938776\" id=\"scene15_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene3_audio\" id=\"scene3_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397824477scene3.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"7575.510204081631\" id=\"learning1_sound\" startTime=\"10000\"/>\n                <audioSprite duration=\"5500.0\" id=\"learning2_sound\" startTime=\"19000\"/>\n                <audioSprite duration=\"8960\" id=\"scene3_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene5_audio\" id=\"scene5_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397832442scene5.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"8533.333333333332\" id=\"learning3_sound\" startTime=\"14000\"/>\n                <audioSprite duration=\"4632.380952380952\" id=\"learning4_sound\" startTime=\"24000\"/>\n                <audioSprite duration=\"12199.183673469388\" id=\"scene5_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene8_audio\" id=\"scene8_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397864491scene8.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"17266.938775510203\" id=\"scene8_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"2111.473922902494\" id=\"cow_sound\" startTime=\"19000\"/>\n                <audioSprite duration=\"7021.496598639455\" id=\"teapot_rhyme\" startTime=\"23000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene1_audio\" id=\"scene1_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397843692scene1.mp3\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"23337.95918367347\" id=\"clock_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"7000.0\" id=\"scene1_sound\" startTime=\"25000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene7_audio\" id=\"scene7_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397819846scene7.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"14872.38095238095\" id=\"learning5_sound\" startTime=\"7000\"/>\n                <audioSprite duration=\"5642.448979591837\" id=\"learning6_sound\" startTime=\"23000\"/>\n                <audioSprite duration=\"5456.689342403628\" id=\"learning7_sound\" startTime=\"30000\"/>\n                <audioSprite duration=\"5982.04081632653\" id=\"scene7_sound\" startTime=\"0\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene9_audio\" id=\"scene9_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397841841scene9.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"8695.873015873016\" id=\"learning8_sound\" startTime=\"23000\"/>\n                <audioSprite duration=\"4643.990929705218\" id=\"learning9_sound\" startTime=\"33000\"/>\n                <audioSprite duration=\"7131.428571428572\" id=\"scene9_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"12443.42403628118\" id=\"saw_sound\" startTime=\"9000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene6_audio\" id=\"scene6_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397866873scene6.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"13844.897959183674\" id=\"scene6_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"41239.38775510204\" id=\"tree_frogs_and_birds_sound\" startTime=\"15000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene4_audio\" id=\"scene4_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397848270scene4.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"18625.30612244898\" id=\"scene4_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"41239.38775510204\" id=\"tree_frogs_and_birds_sound\" startTime=\"20000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene12_audio\" id=\"scene12_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397872769scene12.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"8986.122448979591\" id=\"scene12_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"61335.464852607714\" id=\"forest_sound\" startTime=\"10000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_scene2_audio\" id=\"scene2_audio\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397783849scene2.ogg\" type=\"audiosprite\">\n            <data>\n                <audioSprite duration=\"14602.448979591836\" id=\"scene2_sound\" startTime=\"0\"/>\n                <audioSprite duration=\"41239.38775510204\" id=\"tree_frogs_and_birds_sound\" startTime=\"16000\"/>\n                <audioSprite duration=\"10997.551020408167\" id=\"water_sound\" startTime=\"59000\"/>\n                <audioSprite duration=\"1202.6984126984103\" id=\"A4_sound\" startTime=\"71000\"/>\n                <audioSprite duration=\"628.0045351473973\" id=\"A5_sound\" startTime=\"74000\"/>\n                <audioSprite duration=\"1228.8208616780025\" id=\"Ab4_sound\" startTime=\"76000\"/>\n                <audioSprite duration=\"628.0045351473973\" id=\"Ab5_sound\" startTime=\"79000\"/>\n                <audioSprite duration=\"941.4739229024889\" id=\"B4_sound\" startTime=\"81000\"/>\n                <audioSprite duration=\"497.39229024943654\" id=\"B5_sound\" startTime=\"83000\"/>\n                <audioSprite duration=\"1385.5555555555554\" id=\"Bb4_sound\" startTime=\"85000\"/>\n                <audioSprite duration=\"575.759637188213\" id=\"Bb5_sound\" startTime=\"88000\"/>\n                <audioSprite duration=\"1176.5759637188182\" id=\"C5_sound\" startTime=\"90000\"/>\n                <audioSprite duration=\"784.7392290249502\" id=\"D5_sound\" startTime=\"93000\"/>\n            </data>\n        </media>\n        <media assetId=\"LP_FT_2222_mic\" id=\"mic\" src=\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397839651mic.png\" type=\"image\"/>\n        <plugin id=\"imageExt\" src=\"ImageExtPlugin.js\"/> <!-- Registering custom plugin - example -->\n    </manifest>\n    <controller id=\"story_data\" name=\"storyData\" type=\"data\"><![CDATA[{\n\t\"model\": {\n\t\t\"scene1_body\" : \"It was the Annual Haircut Day for Sringeri Srinivas.\",\n\t\t\"scene2_body\" : \"Sringeri Srinivas left home as usual to go to the local barber.\",\n\t\t\"scene3_body\" : \"But the barber said, \"Today, I have no time to cut such long hair!\"\",\n\t\t\"scene4_body\" : \"Feeling sad, Sringeri Srinivas went back home to ask his wife for help.\",\n\t\t\"scene5_body\" : \"But his wife said, \"Today, I have no time to cut such long hair!\"\",\n\t\t\"scene6_body\" : \"Feeling a little angry, Sringeri Srinivas went to his friend, the tailor.\",\n\t\t\"scene7_body\" : \"But the tailor said, \"Today, I have no time to cut such long hair!\"\",\n\t\t\"scene8_body\" : \"Now a little worried, Sringeri Srinivas went to one more friend, the carpenter.\",\n\t\t\"scene9_body\" : \"But the carpenter said, \"Today, I have no time to cut such long hair!\"\",\n\t\t\"scene10_body\" : \"Poor Sringeri Srinivas. It seemed no one would cut his hair on this Annual Haircut Day.\",\n\t\t\"scene11_body\" : \"In tears, he walked off by himself past the village forest.\",\n\t\t\"scene12_body\" : \"He sat near a cave and cried loudly.\",\n\t\t\"scene13_body\" : \"\"The day is nearly over. How can I keep my promise to cut my hair? Oh God, help me!\"\",\n\t\t\"scene14_body\" : \"The tiger sleeping peacefully inside the cave was very disturbed by all the noise.\",\n\t\t\"scene15_body\" : \"He came out, roaring, waving his giant paws at Sringeri Srinivas.\",\n\t\t\"scene16_body\" : \"Poor Sringeri Srinivas was so scared, so scared, so scared that ...\",\n\t\t\"scene17_body\" : \"... all the hair fell off his head!\",\n\t\t\"scene18_body\" : \"Sringeri Srinivas ran all the way back to the village. The tiger went back to sleep.\",\n\t\t\"scene19_body\" : \"Now, Sringeri Srinivas will not need a haircut for a very long time.\",\n\t\t\"scene20_body\" : \"Find the Barber.\",\n\t\t\"scene21_body\" : \"Find the Carpenter.\",\n\t\t\"scene22_body\" : \"Match the tools to the people.\",\n\t\t\"scene23_body\" : \"Take me home. Take each person to their home.\",\n\t\t\"kettle_title\" : \"Kettle\",\n\t\t\"kettle_synonyms\" : \"Synonyms: Teapot\",\n\t\t\"kettle_hindi\" : \"Hindi: केटली\",\n\t\t\"kettle_usage\" : \"Kettle is used for boiling tea.\",\n\t\t\"kettle_rhyme\" : \"Rhyme: I'm a little Teapot...\",\n\t\t\"scene20_hint\" : \"Barber has Scissors to Cut hair.\",\n\t\t\"scene21_hint\" : \"Carpenter has a hammer and nail.\",\n\t\t\"scene22_hint\" : \"Carpenter uses this to cut wood.\",\n\t\t\"scene23_hint\" : \"The tailor is surrounded by clothers to stitch.\"\n\t}\n}]]></controller>\n    <controller id=\"assessment\" name=\"assessment\" type=\"items\"><![CDATA[{\n\t\"identifier\": \"haircut_story_1\",\n\t\"title\": \"Haircut Story Assessment\",\n\t\"total_items\": 4,\n\t\"shuffle\": false,\n\t\"max_score\": 9,\n\t\"subject\": \"LIT\",\n\t\"item_sets\": [\n\t\t{\n\t\t\t\"id\": \"set_1\",\n        \t\"count\": 4\n\t\t}\n\t],\n\t\"items\": {\n\t\t\"set_1\" : [\n\t\t\t{\n\t\t\t\t\"identifier\": \"hs1_set_1_1\",\n\t\t\t\t\"type\": \"mcq\",\n\t\t\t\t\"num_answers\": 1,\n\t\t\t\t\"template\": \"mcq_template_1\",\n\t\t\t\t\"qlevel\": \"MEDIUM\",\n\t\t\t\t\"title\": \"Find the Barber.\",\n\t\t\t\t\"options\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"carpenter_img\"}\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"barber_img\"},\n\t\t\t\t\t\t\"answer\": true\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"tailor_img\"}\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"wife_img\"}\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"model\" : {\n\t\t\t\t\t\"title_audio\" : {\"asset\": \"learning10_sound\", \"type\": \"audio\"}\n\t\t\t\t},\n\t\t\t\t\"hints\": [\n\t\t\t\t\t{\"asset\": \"learning11_sound\", \"type\": \"audio\"},\n\t\t\t\t\t{\"asset\": \"Barber has Scissors to Cut hair.\", \"type\": \"text\"}\n\t\t\t\t],\n\t\t\t\t\"max_score\": 1,\n\t\t\t\t\"partial_scoring\": false,\n\t\t\t\t\"feedback\": \"\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"identifier\": \"hs1_set_1_2\",\n\t\t\t\t\"type\": \"mcq\",\n\t\t\t\t\"num_answers\": 1,\n\t\t\t\t\"template\": \"mcq_template_1\",\n\t\t\t\t\"qlevel\": \"MEDIUM\",\n\t\t\t\t\"title\": \"Find the Carpenter.\",\n\t\t\t\t\"options\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"wife_img\"}\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"tailor_img\"}\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"carpenter_img\"},\n\t\t\t\t\t\t\"answer\": true\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"barber_img\"}\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"model\" : {\n\t\t\t\t\t\"title_audio\" : {\"asset\": \"learning12_sound\", \"type\": \"audio\"}\n\t\t\t\t},\n\t\t\t\t\"hints\": [\n\t\t\t\t\t{\"asset\": \"learning13_sound\", \"type\": \"audio\"},\n\t\t\t\t\t{\"asset\": \"Carpenter has a hammer and nail.\", \"type\": \"text\"}\n\t\t\t\t],\n\t\t\t\t\"max_score\": 1,\n\t\t\t\t\"partial_scoring\": false,\n\t\t\t\t\"feedback\": \"\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"identifier\": \"hs1_set_1_3\",\n\t\t\t\t\"type\": \"mtf\",\n\t\t\t\t\"template\": \"mtf_template_1\",\n\t\t\t\t\"qlevel\": \"MEDIUM\",\n\t\t\t\t\"title\": \"Match the tools to the people.\",\n\t\t\t\t\"lhs_options\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"barber_img\"},\n\t\t\t\t\t\t\"index\": 0\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"carpenter_img\"},\n\t\t\t\t\t\t\"index\": 1\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"tailor_img\"},\n\t\t\t\t\t\t\"index\": 2\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"wife_img\"},\n\t\t\t\t\t\t\"index\": 3\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"rhs_options\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"knife\"},\n\t\t\t\t\t\t\"answer\": 3\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"razor\"},\n\t\t\t\t\t\t\"answer\": 0\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"scissors\"},\n\t\t\t\t\t\t\"answer\": 2\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"saw\"},\n\t\t\t\t\t\t\"answer\": 1\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"max_score\": 4,\n\t\t\t\t\"partial_scoring\": true,\n\t\t\t\t\"feedback\": \"\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"identifier\": \"hs1_set_1_4\",\n\t\t\t\t\"type\": \"mtf\",\n\t\t\t\t\"template\": \"mtf_template_2\",\n\t\t\t\t\"qlevel\": \"MEDIUM\",\n\t\t\t\t\"title\": \"Take me home. Take each person to their home.\",\n\t\t\t\t\"lhs_options\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"b-outer\"},\n\t\t\t\t\t\t\"index\": 0\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"w-outer\"},\n\t\t\t\t\t\t\"index\": 1\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"t-outer\"},\n\t\t\t\t\t\t\"index\": 2\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"rhs_options\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"t-inner\"},\n\t\t\t\t\t\t\"answer\": 2\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"b-inner\"},\n\t\t\t\t\t\t\"answer\": 0\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"value\": {\"type\": \"image\", \"asset\": \"w-inner\"},\n\t\t\t\t\t\t\"answer\": 1\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"hints\": [\n\t\t\t\t\t{\"asset\": \"Tailor is surrounded by clothes to stitch.\", \"type\": \"text\"}\n\t\t\t\t],\n\t\t\t\t\"max_score\": 3,\n\t\t\t\t\"partial_scoring\": true,\n\t\t\t\t\"feedback\": \"\"\n\t\t\t}\n\t\t]\n\t}\n}]]></controller>\n    <stage id=\"baseStage\" preload=\"true\">\n        <image asset=\"carpenter_img\"/>\n        <image asset=\"barber_img\"/>\n        <image asset=\"tailor_img\"/>\n        <image asset=\"wife_img\"/>\n        <image asset=\"knife\"/>\n        <image asset=\"razor\"/>\n        <image asset=\"scissors\"/>\n        <image asset=\"play_icon\"/>\n        <image asset=\"saw\"/>\n        <image asset=\"b-outer\"/>\n        <image asset=\"w-outer\"/>\n        <image asset=\"t-outer\"/>\n        <image asset=\"b-inner\"/>\n        <image asset=\"w-inner\"/>\n        <image asset=\"t-inner\"/>\n        <audio asset=\"scene20_audio\"/>\n        <audio asset=\"scene21_audio\"/>\n\n        <image asset=\"page2\"/>\n        <image asset=\"page3\"/>\n        <image asset=\"page4\"/>\n        <image asset=\"page5\"/>\n        <image asset=\"page5\"/>\n        <image asset=\"sringeri\"/>\n        <image asset=\"icon_home\"/>\n        <image asset=\"publisher_logo\"/>\n        <image asset=\"level_band\"/>\n    </stage>\n    <stage id=\"storyBaseStage\" preload=\"true\">\n        <image asset=\"next\" h=\"8.3\" id=\"next\" visible=\"false\" w=\"5\" x=\"93\" y=\"3\"/>\n\n        <shape h=\"15\" hitArea=\"true\" id=\"nextContainer\" type=\"rect\" visible=\"false\" w=\"10\" x=\"90\" y=\"1\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"left\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"next\" type=\"command\"/>\n            </event>\n        </shape>\n        <image asset=\"previous\" h=\"8.3\" id=\"previous\" visible=\"false\" w=\"5\" x=\"2\" y=\"3\"/>\n        <shape h=\"15\" hitArea=\"true\" id=\"previousContainer\" type=\"rect\" visible=\"false\" w=\"10\" x=\"1\" y=\"1\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"right\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"previous\" transitionType=\"previous\" type=\"command\"/>\n            </event>\n        </shape>\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" param=\"body\" w=\"86\" x=\"9\" y=\"7\"/>\n\n        <g h=\"20\" id=\"youSpeakInfo\" visible=\"false\" w=\"20\" x=\"40\" y=\"40\">\n            <shape fill=\"green\" h=\"100\" type=\"roundrect\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"youSpeakInfo\" command=\"startRecord\" success=\"recordingInfo\" timeout=\"20000\" type=\"command\"/>\n                    <action asset=\"youSpeakInfo\" command=\"hide\" type=\"command\"/>\n                    <action asset=\"recordingInfo\" command=\"show\" type=\"command\"/>\n                    \n                </event>\n            </shape>\n           <!--  <shape type=\"rect\" x=\"2\" y=\"62\" w=\"20\" h=\"36\" hitArea=\"true\">\n                <event type=\"click\">\n                    <action type=\"command\" command=\"play\" asset=\"learning7_sound\" />\n                    <action type=\"command\" command=\"show\" asset=\"sewingMachineInfo\" />\n                </event>\n            </shape> -->\n            <text align=\"center\" font=\"Georgia\" fontsize=\"150\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                You Speak\n            </text>\n            <image asset=\"mic\" h=\"30\" w=\"30\" x=\"35\" y=\"40\"/>\n        </g>\n\n    <!--      <image asset=\"next\" x=\"93\" y=\"50\" w=\"2\" h=\"2.3\" id=\"play_icon\" visible=\"false\">\n            <event type=\"enter\">\n                <action type=\"command\" command=\"play\" asset=\"current_rec\" loop=\"1\" />\n            </event>\n        </image> -->\n\n\n         <image asset=\"play_icon\" h=\"7\" visible=\"false\" w=\"6\" x=\"2.8\" y=\"89.4\">\n            <event type=\"click\">\n                <action asset=\"current_rec\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n        </image>\n\n        <!-- <shape  type=\"roundrect\" x=\"93\" y=\"50\" w=\"20\" h=\"20.3\"  hitArea=\"true\" visible=\"false\">\n            <event type=\"click\">\n                <action type=\"command\" command=\"play\" asset=\"current_rec\" loop=\"1\" />\n            </event>\n        </shape> -->\n\n        <g h=\"20\" id=\"recordingInfo\" visible=\"false\" w=\"20\" x=\"40\" y=\"40\">\n            <shape fill=\"red\" h=\"100\" type=\"roundrect\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n\n                    <action asset=\"recordingInfo\" command=\"stopRecord\" type=\"command\"/>\n                    <action asset=\"play_icon\" command=\"show\" type=\"command\"/>\n                    <action asset=\"youSpeakInfo\" command=\"show\" type=\"command\"/>\n                    <action asset=\"recordingInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </shape>\n            <text align=\"center\" font=\"Georgia\" fontsize=\"150\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Recording...\n            </text>\n            <image asset=\"mic\" h=\"30\" w=\"30\" x=\"35\" y=\"40\"/>\n        </g>\n\n        <image asset=\"mic\" h=\"9\" w=\"5\" x=\"93\" y=\"78\">\n            <event type=\"click\">\n                <action asset=\"youSpeakInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </image>\n    </stage>\n    <stage h=\"100\" id=\"splash\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene1\"/>\n        <param name=\"recorderScene\" value=\"recorderScene\"/>\n        <audio asset=\"splash_audio\"/>\n        <!-- Custom Plugin Example -->\n        <imageExt asset=\"page1\" h=\"70\" x=\"2\" y=\"2\"/>\n        <image asset=\"publisher_logo\" h=\"17\" w=\"13\" x=\"2\" y=\"75\">\n            <event type=\"click\">\n                <!-- Custom Command Example -->\n                <action asset=\"page1\" command=\"custom\" invoke=\"doSomething\" message=\"Hey there! Test command works\" type=\"command\"/>\n            </event>\n        </image>\n        <image asset=\"level_band\" h=\"5\" w=\"13\" x=\"2\" y=\"94\"/>\n        <image asset=\"next\" h=\"10\" id=\"next\" w=\"6\" x=\"92\" y=\"80\"/>\n        <shape h=\"20\" hitArea=\"true\" id=\"nextContainer\" type=\"rect\" visible=\"false\" w=\"13\" x=\"87\" y=\"75\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"left\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"next\" type=\"command\"/>\n            </event>\n        </shape>\n        <image asset=\"next\" h=\"10\" id=\"nextRecord\" w=\"6\" x=\"72\" y=\"80\"/>\n        <shape h=\"20\" hitArea=\"true\" id=\"recordContainer\" type=\"rect\" visible=\"true\" w=\"13\" x=\"67\" y=\"75\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"left\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"recorderScene\" type=\"command\"/>\n            </event>\n        </shape>\n        <text font=\"Arial\" fontsize=\"28\" h=\"5\" w=\"10\" x=\"70.5\" y=\"91\">Auto transition</text>\n        <text font=\"Arial\" fontsize=\"48\" h=\"5\" w=\"25\" x=\"35\" y=\"80\">\n            Annual Haircut Day\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" weight=\"bold\" x=\"35\" y=\"86\">\n            Author:\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" x=\"41\" y=\"86\">\n            Rohini Nilekani\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" weight=\"bold\" x=\"35\" y=\"90\">\n            Illustrator:\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" x=\"43.5\" y=\"90\">\n            Angie &amp; Upesh\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" x=\"43.5\" y=\"90\">\n            Angie &amp; Upesh\n        </text>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"cover_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"cover_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene1\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene1_audio\"/>\n        <param name=\"next\" value=\"scene2\"/>\n        <param name=\"previous\" value=\"splash\"/>\n        <param model=\"storyData.scene1_body\" name=\"body\"/>\n        <shape fill=\"#F3CCDE\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page2\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene1_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"48\" hitArea=\"true\" type=\"rect\" w=\"12\" x=\"2\" y=\"2\">\n            <event type=\"click\">\n                <action asset=\"clock_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene1_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene1_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene2\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene2_audio\"/>\n        <param name=\"next\" value=\"scene3\"/>\n        <param name=\"previous\" value=\"scene1\"/>\n        <param model=\"storyData.scene2_body\" name=\"body\"/>\n        <shape fill=\"#FFF16E\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page3\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"sringeri\" h=\"36\" id=\"sringeri\" w=\"20\" x=\"11.5\" y=\"19\">\n            <event type=\"click\">\n                <action type=\"animation\">\n                    <tween id=\"sringeriWalking\">\n                        <to duration=\"500\" ease=\"linear\">\n                            <![CDATA[{\"x\":20,\"y\":20}]]>\n                        </to>\n                        <to duration=\"2000\" ease=\"quadOut\">\n                            <![CDATA[{\"x\":55,\"y\":0}]]>\n                        </to>\n                        <to duration=\"1\" ease=\"linear\">\n                            <![CDATA[{\"x\":75,\"y\":0, \"scaleX\": -1}]]>\n                        </to>\n                        <to duration=\"2000\" ease=\"linear\">\n                            <![CDATA[{\"x\":40,\"y\":55}]]>\n                        </to>\n                        <to duration=\"1\" ease=\"linear\">\n                            <![CDATA[{\"x\":18,\"y\": 55, \"scaleX\": 1}]]>\n                        </to>\n                        <to duration=\"2000\" ease=\"linear\">\n                            <![CDATA[{\"x\":57,\"y\":55}]]>\n                        </to>\n                    </tween>\n                </action>\n            </event>\n        </image>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene2_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"44\" hitArea=\"true\" type=\"rect\" w=\"15\" x=\"5\" y=\"50\">\n            <event type=\"click\">\n                <action asset=\"tree_frogs_and_birds_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"3.5\" hitArea=\"true\" type=\"circle\" w=\"3.5\" x=\"58\" y=\"37\">\n            <event type=\"click\">\n                <action asset=\"water_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"5\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"41\" y=\"40\">\n            <event type=\"click\">\n                <action asset=\"A4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"5\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"44\" y=\"36\">\n            <event type=\"click\">\n                <action asset=\"A5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"4\" hitArea=\"true\" type=\"rect\" w=\"2\" x=\"46.5\" y=\"32\">\n            <event type=\"click\">\n                <action asset=\"Ab4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"7\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"49\" y=\"27\">\n            <event type=\"click\">\n                <action asset=\"Ab5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"11\" hitArea=\"true\" type=\"rect\" w=\"3.5\" x=\"52.5\" y=\"21\">\n            <event type=\"click\">\n                <action asset=\"B4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"11\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"57\" y=\"20\">\n            <event type=\"click\">\n                <action asset=\"B5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"10\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"60.5\" y=\"20\">\n            <event type=\"click\">\n                <action asset=\"Bb4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"8\" hitArea=\"true\" type=\"rect\" w=\"2.3\" x=\"64\" y=\"22\">\n            <event type=\"click\">\n                <action asset=\"Bb5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"10\" hitArea=\"true\" type=\"rect\" w=\"4\" x=\"66.5\" y=\"25\">\n            <event type=\"click\">\n                <action asset=\"C5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"4\" hitArea=\"true\" type=\"rect\" w=\"7\" x=\"64.5\" y=\"36\">\n            <event type=\"click\">\n                <action asset=\"D5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"7\" hitArea=\"true\" type=\"rect\" w=\"5\" x=\"61.5\" y=\"40.5\">\n            <event type=\"click\">\n                <action asset=\"A4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"7\" hitArea=\"true\" type=\"rect\" w=\"4.5\" x=\"57.5\" y=\"44\">\n            <event type=\"click\">\n                <action asset=\"A5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"9\" hitArea=\"true\" type=\"rect\" w=\"4\" x=\"53\" y=\"45\">\n            <event type=\"click\">\n                <action asset=\"Ab4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"15\" hitArea=\"true\" type=\"rect\" w=\"6\" x=\"46.5\" y=\"47\">\n            <event type=\"click\">\n                <action asset=\"B4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"15\" hitArea=\"true\" type=\"rect\" w=\"6.5\" x=\"39.5\" y=\"52\">\n            <event type=\"click\">\n                <action asset=\"B5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"11\" hitArea=\"true\" type=\"rect\" w=\"4\" x=\"35\" y=\"59\">\n            <event type=\"click\">\n                <action asset=\"Bb4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"13\" hitArea=\"true\" type=\"rect\" w=\"6\" x=\"28.5\" y=\"65\">\n            <event type=\"click\">\n                <action asset=\"Bb5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"11\" hitArea=\"true\" type=\"rect\" w=\"7.5\" x=\"28.5\" y=\"79\">\n            <event type=\"click\">\n                <action asset=\"C5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"9\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"36.5\" y=\"81\">\n            <event type=\"click\">\n                <action asset=\"A5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"13\" hitArea=\"true\" type=\"rect\" w=\"3.5\" x=\"40.5\" y=\"78\">\n            <event type=\"click\">\n                <action asset=\"D5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"13\" hitArea=\"true\" type=\"rect\" w=\"3.5\" x=\"46\" y=\"78\">\n            <event type=\"click\">\n                <action asset=\"A4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"10\" hitArea=\"true\" type=\"rect\" w=\"3\" x=\"50\" y=\"80\">\n            <event type=\"click\">\n                <action asset=\"A5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"8\" hitArea=\"true\" type=\"rect\" w=\"2\" x=\"54\" y=\"81\">\n            <event type=\"click\">\n                <action asset=\"Ab4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"7\" hitArea=\"true\" type=\"rect\" w=\"1.2\" x=\"57\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"B4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"7\" hitArea=\"true\" type=\"rect\" w=\"1\" x=\"59\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"B5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene2_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene2_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene3\" w=\"100\" x=\"0\" y=\"0\">\n        <audio asset=\"scene3_audio\"/>\n        <shape fill=\"#E5F4FB\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page4\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <param name=\"next\" value=\"scene4\"/>\n        <param name=\"previous\" value=\"scene2\"/>\n        <param model=\"storyData.scene3_body\" name=\"body\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene3_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"20\" id=\"razorInfo\" visible=\"false\" w=\"20\" x=\"63\" y=\"15\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning2_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"razorInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"150\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Razor\n            </text>\n            <text font=\"Georgia\" fontsize=\"120\" h=\"80\" w=\"80\" x=\"10\" y=\"40\">\n                Razor is used to cut hair.\n            </text>\n        </g>\n        <g h=\"27\" id=\"barberInfo\" visible=\"false\" w=\"30\" x=\"65\" y=\"20\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning1_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"barberInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"125\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Barber\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" h=\"80\" w=\"80\" x=\"10\" y=\"40\">\n                Barber is cutting hair. Barber uses razor and scissors to cut hair.\n            </text>\n        </g>\n        <shape h=\"13\" hitArea=\"true\" type=\"rect\" w=\"5\" x=\"63\" y=\"30\">\n            <event type=\"click\">\n                <action asset=\"learning2_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"razorInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"25\" hitArea=\"true\" type=\"rect\" w=\"18\" x=\"53\" y=\"45\">\n            <event type=\"click\">\n                <action asset=\"learning1_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"barberInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene3_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene3_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene4\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene5\"/>\n        <param name=\"previous\" value=\"scene3\"/>\n        <param model=\"storyData.scene4_body\" name=\"body\"/>\n        <audio asset=\"scene4_audio\"/>\n        <shape fill=\"#86CFF2\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page5\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene4_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"58\" hitArea=\"true\" type=\"rect\" w=\"23\" x=\"0\" y=\"40\">\n            <event type=\"click\">\n                <action asset=\"tree_frogs_and_birds_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene4_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene4_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene5\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene6\"/>\n        <param name=\"previous\" value=\"scene4\"/>\n        <param model=\"storyData.scene5_body\" name=\"body\"/>\n        <audio asset=\"scene5_audio\"/>\n        <shape fill=\"#DEA6C9\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page6\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene5_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"20\" id=\"knifeInfo\" visible=\"false\" w=\"20\" x=\"15\" y=\"8\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning4_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"knifeInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"150\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Knife\n            </text>\n            <text font=\"Georgia\" fontsize=\"120\" h=\"80\" w=\"80\" x=\"10\" y=\"40\">\n                Knife is used to cut vegetables.\n            </text>\n        </g>\n        <g h=\"27\" id=\"wifeInfo\" visible=\"false\" w=\"30\" x=\"28\" y=\"12\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning3_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"wifeInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"125\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Sringeri Srinivas's Wife\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" h=\"80\" w=\"80\" x=\"10\" y=\"40\">\n                Wife is cooking food. Wife uses a knife to Cut vegetables.\n            </text>\n        </g>\n        <shape h=\"22\" hitArea=\"true\" type=\"rect\" w=\"12\" x=\"20\" y=\"28\">\n            <event type=\"click\">\n                <action asset=\"learning3_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"wifeInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"22\" hitArea=\"true\" type=\"rect\" w=\"6\" x=\"13\" y=\"20\">\n            <event type=\"click\">\n                <action asset=\"learning4_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"knifeInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene5_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene5_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene6\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene7\"/>\n        <param name=\"previous\" value=\"scene5\"/>\n        <param model=\"storyData.scene6_body\" name=\"body\"/>\n        <audio asset=\"scene6_audio\"/>\n        <shape fill=\"#FAB800\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page7\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene6_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"38\" hitArea=\"true\" type=\"rect\" w=\"16\" x=\"82\" y=\"25\">\n            <event type=\"click\">\n                <action asset=\"tree_frogs_and_birds_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene6_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene6_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene7\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene8\"/>\n        <param name=\"previous\" value=\"scene6\"/>\n        <param model=\"storyData.scene7_body\" name=\"body\"/>\n        <audio asset=\"scene7_audio\"/>\n        <shape fill=\"#86C8E8\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page8\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene7_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"27\" id=\"tailorInfo\" visible=\"false\" w=\"30\" x=\"40\" y=\"15\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning5_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"tailorInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"128\" h=\"4\" w=\"86\" weight=\"bold\" x=\"8\" y=\"17\">\n                Tailor\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" w=\"86\" x=\"8\" y=\"32\">\n                Tailor stitches clothes. Tailor uses tape to measure cloth. Tailor uses scissors to cut cloth. Tailor uses sewing machine to stitch.\n            </text>\n        </g>\n        <shape h=\"34\" hitArea=\"true\" type=\"rect\" w=\"20\" x=\"30\" y=\"40\">\n            <event type=\"click\">\n                <action asset=\"learning5_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"tailorInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"27\" id=\"scissorInfo\" visible=\"false\" w=\"30\" x=\"50\" y=\"5\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning6_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"scissorInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"128\" h=\"4\" w=\"86\" weight=\"bold\" x=\"8\" y=\"17\">\n                Scissor\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" w=\"86\" x=\"8\" y=\"32\">\n                Scissor is used to cut cloth.\n            </text>\n        </g>\n        <shape h=\"32\" hitArea=\"true\" type=\"rect\" w=\"6\" x=\"50\" y=\"30\">\n            <event type=\"click\">\n                <action asset=\"learning6_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"scissorInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"27\" id=\"sewingMachineInfo\" visible=\"false\" w=\"30\" x=\"3\" y=\"40\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning7_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"sewingMachineInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"128\" h=\"4\" w=\"86\" weight=\"bold\" x=\"8\" y=\"17\">\n                Sewing Machine\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" w=\"86\" x=\"8\" y=\"32\">\n                Sewing Machine is used to stitch cloth.\n            </text>\n        </g>\n        <shape h=\"36\" hitArea=\"true\" type=\"rect\" w=\"20\" x=\"2\" y=\"62\">\n            <event type=\"click\">\n                <action asset=\"learning7_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"sewingMachineInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene7_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene7_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene8\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene9\"/>\n        <param name=\"previous\" value=\"scene7\"/>\n        <param model=\"storyData.scene8_body\" name=\"body\"/>\n        <audio asset=\"scene8_audio\"/>\n        <shape fill=\"#95B1D9\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page9\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene8_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"27\" id=\"teapotInfo\" visible=\"false\" w=\"30\" x=\"18\" y=\"20\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n            <text font=\"Georgia\" fontsize=\"128\" h=\"4\" model=\"storyData.kettle_title\" w=\"86\" weight=\"bold\" x=\"8\" y=\"17\"/>\n            <text font=\"Georgia\" fontsize=\"90\" model=\"storyData.kettle_synonyms\" w=\"86\" x=\"8\" y=\"32\"/>\n            <text font=\"Georgia\" fontsize=\"90\" model=\"storyData.kettle_hindi\" w=\"86\" x=\"8\" y=\"44\"/>\n            <text font=\"Georgia\" fontsize=\"90\" model=\"storyData.kettle_usage\" w=\"86\" x=\"8\" y=\"56\"/>\n            <text color=\"#004389\" font=\"Georgia\" fontsize=\"72\" model=\"storyData.kettle_rhyme\" w=\"86\" x=\"8\" y=\"72\"/>\n            <shape h=\"30\" hitArea=\"true\" type=\"rect\" w=\"90\" x=\"2\" y=\"60\">\n                <event type=\"click\">\n                    <action asset=\"teapot_rhyme\" command=\"togglePlay\" type=\"command\"/>\n                </event>\n            </shape>\n        </g>\n        <shape h=\"12\" hitArea=\"true\" type=\"rect\" w=\"12\" x=\"11\" y=\"42\">\n            <event type=\"click\">\n                <action asset=\"teapotInfo\" command=\"toggleShow\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"15\" hitArea=\"true\" type=\"rect\" w=\"14\" x=\"84\" y=\"65\">\n            <event type=\"click\">\n                <action asset=\"cow_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene8_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene8_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene9\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene10\"/>\n        <param name=\"previous\" value=\"scene8\"/>\n        <param model=\"storyData.scene9_body\" name=\"body\"/>\n        <audio asset=\"scene9_audio\"/>\n        <shape fill=\"#D1E6EB\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page10\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene9_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"27\" id=\"carpenterInfo\" visible=\"false\" w=\"28\" x=\"72\" y=\"8\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning8_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"carpenterInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"128\" h=\"4\" w=\"86\" weight=\"bold\" x=\"8\" y=\"17\">\n                Carpenter\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" w=\"86\" x=\"8\" y=\"32\">\n                Carpenter makes furniture from wood. Carpenter uses a saw to cut wood.\n            </text>\n        </g>\n        <shape h=\"44\" hitArea=\"true\" type=\"rect\" w=\"25\" x=\"70\" y=\"37\">\n            <event type=\"click\">\n                <action asset=\"learning8_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"carpenterInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <g h=\"27\" id=\"sawInfo\" visible=\"false\" w=\"28\" x=\"56\" y=\"3\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset=\"learning9_sound\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"sawInfo\" command=\"hide\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"128\" h=\"4\" w=\"86\" weight=\"bold\" x=\"8\" y=\"17\">\n                Saw\n            </text>\n            <text font=\"Georgia\" fontsize=\"90\" w=\"86\" x=\"8\" y=\"32\">\n                Saw is used to cut wood.\n            </text>\n            <image asset=\"icon_sound\" h=\"25\" w=\"15\" x=\"8\" y=\"50\">\n                <event type=\"click\">\n                    <action asset=\"saw_sound\" command=\"togglePlay\" type=\"command\"/>\n                </event>\n            </image>\n        </g>\n        <shape h=\"42\" hitArea=\"true\" type=\"rect\" w=\"8\" x=\"60\" y=\"17\">\n            <event type=\"click\">\n                <action asset=\"learning9_sound\" command=\"play\" type=\"command\"/>\n                <action asset=\"sawInfo\" command=\"show\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene9_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene9_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene10\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene11\"/>\n        <param name=\"previous\" value=\"scene9\"/>\n        <param model=\"storyData.scene10_body\" name=\"body\"/>\n        <audio asset=\"scene10_audio\"/>\n        <shape fill=\"#DEA6C9\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page11\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene10_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene10_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene10_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene11\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene12\"/>\n        <param name=\"previous\" value=\"scene10\"/>\n        <param model=\"storyData.scene11_body\" name=\"body\"/>\n        <audio asset=\"scene11_audio\"/>\n        <shape fill=\"#8AACDA\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page12\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene11_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene11_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene11_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene12\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene13\"/>\n        <param name=\"previous\" value=\"scene11\"/>\n        <param model=\"storyData.scene12_body\" name=\"body\"/>\n        <audio asset=\"scene12_audio\"/>\n        <shape fill=\"#4CA332\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page13\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene12_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"59\" hitArea=\"true\" type=\"rect\" w=\"28\" x=\"70\" y=\"12\">\n            <event type=\"click\">\n                <action asset=\"forest_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"48\" hitArea=\"true\" type=\"rect\" w=\"23\" x=\"2\" y=\"50\">\n            <event type=\"click\">\n                <action asset=\"forest_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene12_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene12_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene13\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene14\"/>\n        <param name=\"previous\" value=\"scene12\"/>\n        <param model=\"storyData.scene13_body\" name=\"body\"/>\n        <audio asset=\"scene13_audio\"/>\n        <shape fill=\"#A0A4A7\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page14\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene13_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene13_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene13_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene14\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene15\"/>\n        <param name=\"previous\" value=\"scene13\"/>\n        <param model=\"storyData.scene14_body\" name=\"body\"/>\n        <audio asset=\"scene14_audio\"/>\n        <shape fill=\"#A4978F\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page15\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene14_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <shape h=\"52\" hitArea=\"true\" type=\"rect\" w=\"62\" x=\"36\" y=\"30\">\n            <event type=\"click\">\n                <action asset=\"tiger_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene14_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene14_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene15\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene16\"/>\n        <param name=\"previous\" value=\"scene14\"/>\n        <param model=\"storyData.scene15_body\" name=\"body\"/>\n        <audio asset=\"scene15_audio\"/>\n        <shape fill=\"#A4978F\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page16\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene15_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene15_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene15_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene16\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene17\"/>\n        <param name=\"previous\" value=\"scene15\"/>\n        <param model=\"storyData.scene16_body\" name=\"body\"/>\n        <audio asset=\"scene16_audio\"/>\n        <shape fill=\"#A4978F\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page17\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene16_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene16_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene16_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene17\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene18\"/>\n        <param name=\"previous\" value=\"scene16\"/>\n        <param model=\"storyData.scene17_body\" name=\"body\"/>\n        <audio asset=\"scene17_audio\"/>\n        <shape fill=\"#FBF9E0\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page18\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene17_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene17_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene17_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene18\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene19\"/>\n        <param name=\"previous\" value=\"scene17\"/>\n        <param model=\"storyData.scene18_body\" name=\"body\"/>\n        <audio asset=\"scene18_audio\"/>\n        <shape fill=\"#F19264\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page19\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene18_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene18_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene18_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage extends=\"storyBaseStage\" h=\"100\" id=\"scene19\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene20\"/>\n        <param name=\"previous\" value=\"scene18\"/>\n        <param model=\"storyData.scene19_body\" name=\"body\"/>\n        <audio asset=\"scene19_audio\"/>\n        <shape fill=\"#BB6D59\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <image asset=\"page20\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset=\"scene19_sound\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <events>\n            <event type=\"enter\">\n                <action asset=\"scene19_sound\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset=\"scene19_sound\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <template id=\"mcq_template_1\">\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" model=\"item.title\" w=\"86\" x=\"9\" y=\"7\"/>\n        <mcq model=\"item\" multi_select=\"false\">\n            <options cols=\"2\" h=\"85\" layout=\"table\" marginX=\"10\" marginY=\"5\" options=\"options\" w=\"70\" x=\"20\" y=\"15\"/>\n        </mcq>\n        <g h=\"20\" id=\"hint\" visible=\"false\" w=\"20\" x=\"9\" y=\"17\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\">\n                <event type=\"click\">\n                    <action asset_model=\"item.hints[0].asset\" command=\"stop\" type=\"command\"/>\n                    <action asset=\"hint\" command=\"toggleShow\" type=\"command\"/>\n                </event>\n            </image>\n            <text font=\"Georgia\" fontsize=\"150\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Hint\n            </text>\n            <text font=\"Georgia\" fontsize=\"120\" h=\"80\" model=\"item.hints[1].asset\" w=\"80\" x=\"10\" y=\"40\"/>\n        </g>\n        <image asset=\"icon_hint\" x=\"5\" y=\"35\">\n            <event type=\"click\">\n                <action asset_model=\"item.hints[0].asset\" command=\"togglePlay\" type=\"command\"/>\n                <action asset=\"hint\" command=\"toggleShow\" type=\"command\"/>\n            </event>\n        </image>\n    </template>\n    <template id=\"mtf_template_1\">\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" model=\"item.title\" w=\"86\" x=\"9\" y=\"7\"/>\n        <mtf model=\"item\">\n            <options cols=\"2\" h=\"85\" layout=\"table\" marginX=\"15\" marginY=\"5\" options=\"lhs_options\" snapX=\"45\" snapY=\"35\" w=\"75\" x=\"15\" y=\"15\"/>\n\n            <option h=\"20\" option=\"rhs_options[0]\" padX=\"20\" padY=\"10\" w=\"15\" x=\"45\" y=\"20\"/>\n            <option h=\"20\" option=\"rhs_options[1]\" padX=\"20\" padY=\"10\" w=\"15\" x=\"45\" y=\"37\"/>\n            <option h=\"20\" option=\"rhs_options[2]\" padX=\"20\" padY=\"10\" w=\"15\" x=\"45\" y=\"59\"/>\n            <option h=\"20\" option=\"rhs_options[3]\" padX=\"20\" padY=\"10\" w=\"15\" x=\"45\" y=\"80\"/>\n        </mtf>\n    </template>\n    <template id=\"mtf_template_2\">\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" model=\"item.title\" w=\"86\" x=\"9\" y=\"7\"/>\n        <mtf force=\"true\" model=\"item\">\n            <option h=\"25\" option=\"lhs_options[0]\" snapX=\"42\" snapY=\"9\" w=\"30\" x=\"20\" y=\"15\"/>\n            <option h=\"25\" option=\"lhs_options[1]\" snapX=\"10.5\" snapY=\"5\" w=\"30\" x=\"20\" y=\"43\"/>\n            <option h=\"25\" option=\"lhs_options[2]\" snapX=\"27.5\" snapY=\"8.5\" w=\"30\" x=\"20\" y=\"71\"/>\n\n            <option h=\"16\" option=\"rhs_options[0]\" w=\"10\" x=\"70\" y=\"15\"/>\n            <option h=\"19.5\" option=\"rhs_options[1]\" w=\"10\" x=\"70\" y=\"43\"/>\n            <option h=\"22\" option=\"rhs_options[2]\" w=\"12\" x=\"70\" y=\"71\"/>\n        </mtf>\n        <g h=\"20\" id=\"hint\" visible=\"false\" w=\"20\" x=\"9\" y=\"17\">\n            <image asset=\"speech_bubble\" h=\"100\" w=\"100\" x=\"0\" y=\"0\"/>\n            <text font=\"Georgia\" fontsize=\"150\" h=\"80\" w=\"80\" weight=\"bold\" x=\"10\" y=\"20\">\n                Hint\n            </text>\n            <text font=\"Georgia\" fontsize=\"120\" h=\"80\" model=\"item.hints[0].asset\" w=\"80\" x=\"10\" y=\"40\"/>\n        </g>\n        <image asset=\"icon_hint\" x=\"5\" y=\"35\">\n            <event type=\"click\">\n                <action asset=\"hint\" command=\"toggleShow\" type=\"command\"/>\n            </event>\n        </image>\n    </template>\n    <stage h=\"100\" id=\"scene20\" iterate=\"assessment\" var=\"item\" w=\"100\" x=\"0\" y=\"0\">\n        <param name=\"next\" value=\"scene21\"/>\n        <param name=\"previous\" value=\"scene19\"/>\n        <shape fill=\"#FFF16E\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <shape fill=\"#FFF16E\" h=\"89.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"next\" h=\"8.3\" id=\"next\" visible=\"false\" w=\"5\" x=\"93\" y=\"3\"/>\n        <shape h=\"15\" hitArea=\"true\" id=\"nextContainer\" type=\"rect\" visible=\"false\" w=\"10\" x=\"90\" y=\"1\">\n            <event disableTelemetry=\"true\" type=\"click\">\n                <action asset=\"scene20\" command=\"eval\" failure=\"next_item\" success=\"next_item\" type=\"command\"/>\n            </event>\n        </shape>\n        <image asset=\"previous\" h=\"8.3\" id=\"previous\" visible=\"false\" w=\"5\" x=\"2\" y=\"3\"/>\n        <shape h=\"15\" hitArea=\"true\" id=\"previousContainer\" type=\"rect\" visible=\"false\" w=\"10\" x=\"1\" y=\"1\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"right\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"previous\" transitionType=\"previous\" type=\"command\"/>\n            </event>\n        </shape>\n        <embed template=\"item\" var-item=\"item\"/>\n        <image asset=\"question_block\" h=\"16\" w=\"9\" x=\"5\" y=\"15\"/>\n        <image asset=\"icon_sound\" h=\"9\" w=\"5\" x=\"93\" y=\"88\"/>\n        <shape h=\"18\" hitArea=\"true\" type=\"rect\" w=\"13\" x=\"87\" y=\"82\">\n            <event type=\"click\">\n                <action asset_model=\"item.title_audio.asset\" command=\"togglePlay\" type=\"command\"/>\n            </event>\n        </shape>\n        <appEvents list=\"next_item\"/>\n        <events>\n            <event type=\"enter\">\n                <action asset_model=\"item.title_audio.asset\" command=\"play\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"exit\">\n                <action asset_model=\"item.title_audio.asset\" audio=\"true\" command=\"stop\" loop=\"1\" type=\"command\"/>\n            </event>\n            <event type=\"next_item\">\n                <action asset=\"theme\" command=\"transitionTo\" direction=\"left\" duration=\"500\" ease=\"linear\" effect=\"scroll\" param=\"next\" reset=\"true\" type=\"command\"/>\n            </event>\n        </events>\n    </stage>\n    <stage h=\"100\" id=\"scene21\" w=\"100\" x=\"0\" y=\"0\">\n        <shape fill=\"#FFF16E\" h=\"10.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <shape fill=\"#FFF16E\" h=\"89.5\" type=\"rect\" w=\"96\" x=\"2\" y=\"12\"/>\n        <text font=\"Georgia\" fontsize=\"96\" h=\"8\" w=\"30\" x=\"38\" y=\"15\">\n            Thank you\n        </text>\n        \n        <image asset=\"icon_reload\" h=\"20\" w=\"15\" x=\"30\" y=\"35\"/>\n        <text font=\"Arial\" fontsize=\"48\" h=\"5\" w=\"15\" x=\"33\" y=\"57\">\n            Replay\n        </text>\n        <shape h=\"30\" hitArea=\"true\" type=\"rect\" w=\"15\" x=\"30\" y=\"35\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"restart\" type=\"command\"/>\n            </event>\n        </shape>\n\n        <image asset=\"icon_home\" h=\"20\" w=\"15\" x=\"55\" y=\"35\"/>\n        <text font=\"Arial\" fontsize=\"48\" h=\"5\" w=\"15\" x=\"58\" y=\"57\">\n            Home\n        </text>\n        <shape h=\"30\" hitArea=\"true\" type=\"rect\" w=\"15\" x=\"55\" y=\"35\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"windowEvent\" href=\"#/content/list\" type=\"command\"/>\n            </event>\n        </shape>\n        \n        <image asset=\"publisher_logo\" h=\"17\" w=\"13\" x=\"5\" y=\"75\"/>\n        <image asset=\"level_band\" h=\"5\" w=\"13\" x=\"5\" y=\"94\"/>\n        <text font=\"Arial\" fontsize=\"48\" h=\"5\" w=\"25\" x=\"35\" y=\"80\">\n            Annual Haircut Day\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" weight=\"bold\" x=\"35\" y=\"86\">\n            Author:\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" x=\"41\" y=\"86\">\n            Rohini Nilekani\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" weight=\"bold\" x=\"35\" y=\"90\">\n            Illustrator:\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" x=\"43.5\" y=\"90\">\n            Angie &amp; Upesh\n        </text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" w=\"20\" x=\"43.5\" y=\"90\">\n            Angie &amp; Upesh\n        </text>\n    </stage>\n    <stage h=\"100\" id=\"recorderScene\" w=\"100\" x=\"0\" y=\"0\">\n        <!-- scene1 start -->\n        <param model=\"storyData.scene1_body\" name=\"scene1_body\"/>\n        <shape fill=\"#F3CCDE\" h=\"10.5\" id=\"scene1_body_bg\" type=\"rect\" w=\"96\" x=\"2\" y=\"2\"/>\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" id=\"scene1_body\" param=\"scene1_body\" w=\"86\" x=\"9\" y=\"7\"/>\n        <image asset=\"page2\" h=\"86\" w=\"96\" x=\"2\" y=\"12\"/>\n        <!-- scene1 end -->\n        <!-- scene2 start -->\n        <shape fill=\"#FFF16E\" h=\"10.5\" id=\"scene2_body_bg\" type=\"rect\" visible=\"false\" w=\"96\" x=\"2\" y=\"2\"/>\n        <param id=\"scene2_body\" model=\"storyData.scene2_body\" name=\"scene2_body\"/>\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" id=\"scene2_body\" param=\"scene2_body\" visible=\"false\" w=\"86\" x=\"9\" y=\"7\"/>\n        <image asset=\"page3\" h=\"86\" visible=\"false\" w=\"96\" x=\"2\" y=\"12\"/>\n        <image asset=\"sringeri\" h=\"36\" id=\"sringeri\" visible=\"false\" w=\"20\" x=\"11.5\" y=\"19\">\n            <event type=\"click\">\n                <action type=\"animation\">\n                    <tween id=\"sringeriWalking\">\n                        <to duration=\"500\" ease=\"linear\">\n                            <![CDATA[{\"x\":20,\"y\":20}]]>\n                        </to>\n                        <to duration=\"2000\" ease=\"quadOut\">\n                            <![CDATA[{\"x\":55,\"y\":0}]]>\n                        </to>\n                        <to duration=\"1\" ease=\"linear\">\n                            <![CDATA[{\"x\":75,\"y\":0, \"scaleX\": -1}]]>\n                        </to>\n                        <to duration=\"2000\" ease=\"linear\">\n                            <![CDATA[{\"x\":40,\"y\":55}]]>\n                        </to>\n                        <to duration=\"1\" ease=\"linear\">\n                            <![CDATA[{\"x\":18,\"y\": 55, \"scaleX\": 1}]]>\n                        </to>\n                        <to duration=\"2000\" ease=\"linear\">\n                            <![CDATA[{\"x\":57,\"y\":55}]]>\n                        </to>\n                    </tween>\n                </action>\n            </event>\n        </image>\n        <!-- scene2 end -->\n        <!-- scene3 start -->\n        <param model=\"storyData.scene3_body\" name=\"scene3_body\"/>\n        <shape fill=\"#E5F4FB\" h=\"10.5\" id=\"scene3_body_bg\" type=\"rect\" visible=\"false\" w=\"96\" x=\"2\" y=\"2\"/>\n        <text font=\"Georgia\" fontsize=\"42\" h=\"4\" id=\"scene3_body\" param=\"scene3_body\" visible=\"false\" w=\"86\" x=\"9\" y=\"7\"/>\n        <image asset=\"page4\" h=\"86\" visible=\"false\" w=\"96\" x=\"2\" y=\"12\"/>\n        <!-- scene3 end -->\n        <!-- sceneEnd start -->\n        <shape fill=\"#FFF16E\" h=\"10.5\" id=\"sceneEnd_shape_1\" type=\"rect\" visible=\"false\" w=\"96\" x=\"2\" y=\"2\"/>\n        <shape fill=\"#FFF16E\" h=\"89.5\" id=\"sceneEnd_shape_2\" type=\"rect\" visible=\"false\" w=\"96\" x=\"2\" y=\"12\"/>\n        <text font=\"Georgia\" fontsize=\"96\" h=\"8\" id=\"sceneEnd_txt1\" visible=\"false\" w=\"30\" x=\"38\" y=\"15\">Thank you</text>\n\n        \n        <image asset=\"icon_home\" h=\"20\" id=\"sceneEnd_img2\" visible=\"false\" w=\"15\" x=\"42\" y=\"35\"/>\n        <text font=\"Arial\" fontsize=\"48\" h=\"5\" id=\"sceneEnd_txt3\" visible=\"false\" w=\"15\" x=\"45\" y=\"57\">Home</text>\n        <shape h=\"30\" hitArea=\"true\" id=\"sceneEnd_shape_3\" type=\"rect\" visible=\"false\" w=\"15\" x=\"42\" y=\"35\">\n            <event type=\"click\">\n                <action asset=\"theme\" command=\"windowEvent\" href=\"#/content/list\" type=\"command\"/>\n            </event>\n        </shape>\n        \n        <image asset=\"publisher_logo\" h=\"17\" id=\"sceneEnd_img3\" visible=\"false\" w=\"13\" x=\"5\" y=\"75\"/>\n        <image asset=\"level_band\" h=\"5\" id=\"sceneEnd_img4\" visible=\"false\" w=\"13\" x=\"5\" y=\"94\"/>\n        <text font=\"Arial\" fontsize=\"48\" h=\"5\" id=\"sceneEnd_txt4\" visible=\"false\" w=\"25\" x=\"35\" y=\"80\">Annual Haircut Day</text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" id=\"sceneEnd_txt5\" visible=\"false\" w=\"20\" weight=\"bold\" x=\"35\" y=\"86\">Author:</text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" id=\"sceneEnd_txt6\" visible=\"false\" w=\"20\" x=\"41\" y=\"86\">Rohini Nilekani</text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" id=\"sceneEnd_txt7\" visible=\"false\" w=\"20\" weight=\"bold\" x=\"35\" y=\"90\">Illustrator:</text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" id=\"sceneEnd_txt8\" visible=\"false\" w=\"20\" x=\"43.5\" y=\"90\">Angie &amp; Upesh</text>\n        <text font=\"Georgia\" fontsize=\"28\" h=\"5\" id=\"sceneEnd_txt9\" visible=\"false\" w=\"20\" x=\"43.5\" y=\"90\">Angie &amp; Upesh</text>\n        <!-- sceneEnd end -->\n        <events>\n            <event type=\"enter\">\n                <!-- scene1 start -->\n                <action asset=\"scene1_body\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <action asset=\"scene1_body_bg\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <action asset=\"page2\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <action asset=\"scene2_body\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <action asset=\"scene2_body_bg\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <action asset=\"page3\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <action asset=\"sringeri\" command=\"toggleShow\" delay=\"10000\" type=\"command\"/>\n                <!-- scene1 end -->\n                <!-- scene2 start -->\n                <action asset=\"scene2_body\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n                <action asset=\"scene2_body_bg\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n                <action asset=\"page3\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n                <action asset=\"sringeri\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n\n                <action asset=\"scene3_body\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n                <action asset=\"scene3_body_bg\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n                <action asset=\"page4\" command=\"toggleShow\" delay=\"20000\" type=\"command\"/>\n                <!-- scene2 end -->\n                <!-- scene3 start -->\n                <action asset=\"scene3_body\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"scene3_body_bg\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"page4\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n\n                <action asset=\"sceneEnd_shape_1\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_shape_2\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt1\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_img2\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt3\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_shape_3\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_img3\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_img4\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt4\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt5\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt6\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt7\" command=\"toggleShow\" delay=\"30000\" type=\"command\"/>\n                <action asset=\"sceneEnd_txt8  <action asset=\"sc";
	String malformedJSONBody = "{\"theme\":{\"manifes77\",\"type\":\"image\",\"srity.ekstep.in/assets/public/content/orcharrc\":\"ekstep.in/as\",\":\"htmun6.png\",\"type\":\"image\",\"src\":\"https://sandbox/public/content/PopupTint_1460636175572.png\",\"assetId\":\"domain_38606\"},\"https://sandbox-commt/goodjobBg_1460727428389.png\",ain_38939\"},{\"id\":\"retryBg\",\"c\":\"https:/public/contentg_1460727370746.png\",\"type\":\"image\",\"assetId\":\"domain_38938\"},{\"id\":\"//sandbox-community.ekstep.in/assets/636610607.mp3\",\"type\":\"sound\",\"assetId\":\"domain_38624\"},{\"id\":\"goodjob_audio\",\"src\":\"https://sandbox-community.ekstep.i0636677521.mp3\",\"type\":\"sound\",\"assetId\":\"domain_38625\"},{\"id\":\"next\",\"src\":\"https://sandbox-community.ekstep.in/assets/public/content/btn_next_1461401649059.png\",\"type\":\"image\",\"assetId\":\"domain_40358\"},{\"id\":\"previous\",\"src\":\"https://sandbox-community.ekstep.in/assets/public/content/btn_back_1461401700215.png\",\"type\":\"image\",\"assetId\":\"domain_40359\"},{\"id\":\"submit\",\"src\":\"https://sandbox-community.ekstep.in/assets/public/content/icon_submit_1459243202199.png\",\"type\":\"image\",\"assetId\":\"domain_14524\"},{\"id\":\"home\",\"src\":\"https://sandbox-community.ekstep.in/aoad_1459243110661.png\",\"type\":\"image\",\"assetId\":\"domain_14522\"},{\"id\":\"icon_hkstep.in/assets/public/cont33.png\",\"type\":\"image\",\"assetId\":\"domain_799\"},{\"idy.ekstep.in/assets/public/content/backgrou98020.png\",\"type\":\"image\"}]},\"template\":[{\"text\":{\"model\":\"item.title\",\"x\":9,\"y\":7,\"w\":86,\"h\":{\"options\":{\"layout\":\"\":85,\"cols\":2,\"marginX\":10,\"marginY\":5,\"options\":\"options\"},\"multi_select\":false,\"model\":\"item\"},\"command\":\"stop\",\"asset_model\":\"item.hints[0].asset\",\"asset\":\"hint\"}],\"type\":\"click\"},\"asset\":\"speech_bubble\",\"x\":100},\"text\":[{\"x\":10,\"y\":20,\"w\":80,\"h\":\"Hint\"},{\"x\":1\":\"Georgia\",\"fontsize\":120,\"model\":\"item.hints[1].asset\"}],\"x\":9,\"y\",\"visible\":false},\"image\":{\"event\":{\"action\":[{\"\",\"asset_model\":\"item.hints[0].asset\"},{\"type\":\"command\",\"command\":\"toggleShow\",\"asset\":\"hint\"}],\"type\":35},\"id\":\"mcq_template_1\"},{\"image\":[{\"event\":{\"action\":{\"\"},\"type\":\"click\"},\"asset\":\"popupTint\",\"x\":-100,\"y\"},{\"asset\":\"retryBg\",\"x\":0,\"y\":0,\"w\":150,\"h\":150,\"visible\":true,\"id\":\"right\"}],\"shape\":[{\"event\":{\"action\":[{\"type\":\"command\",\"command\":\"\",\"command\":\"SHOWHTMLELEMENTS\",\"asset\":\"retry\"}],\"type\":\"click\"},\"type\":\"roundrect\",\",\"visible\":true,\"id\":\"retry\",\"hitArea\":true},{\"event\":{\"action\":{\"type\":\",\"asset\":\"theme\",\"param\":\"\":\"linear\",\"duration\":100},\"type\":\"click\"},\"type\":\"roundrect\",\"x\":110,\"y\":100,\"w\"::true}],\"id\":\"retry\"},{\"g\":{\"image\":[{\"asset\":\"popupTint\",\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"visible\":true,\"id\":\":9,\"visible\":true,\"editable\":true,\"model\":\"word.lemma\",\"weight\":\"normal\",\"font\":\"helvetica\",\"color\":\"rgb(0,0,0)\",\",\"w\":48.61111111111111,\"h\":5.826666666666666,\"visible\":true,\"editable\":true,\"__text\":\"stage1\",\"weight\":\"normal\",\"fontstyle\":\"\",\"fontsize\":53,\"lineHeight\":1.3,\"align\":\":[],\"hotspot\":[],\"embed\":[],\"div\":[],\"audio\":[{\"asset\":\"domain_539\"}],\"scribble\":[],\"htext\":[],\"g\":[],\"preload\":true},{\"id\":\"scene739d8151-82a6-40fb-b48d-c02c77d3e067\",\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"param\":[{\"name\":\"previous\",\"value\":\":[]},\"image\":[{\"event\":{\"action\":{\"type\":\"command\",\"command\":\"\":\"previous\",\"effect\":\"fadein\",\"direction\":\"right\",\"ease\":\"linear\",\"duration\":100},\"type\":\"click\"},\"asset\":\"previous\",\"x\":2,\"y\":3,\"w\":5,\"h\":8.3,\"id\":\"previous\",\":37.77777777777778,\"y\":26.222222222222225,\"w\":48.61111111111111,\"h\":5.826666666666666,\"visible\":true,\"editable\":true,\"__text\":\"stage2\",\"\":\"#000000\",\"fontstyle\":\"\",\"fontsize\":53,\"lineHeight\":1.3,\"align\":\"left\",\"z-index\":0}],\"shape\":[],\"hotspot\":[],\"embed\":[],\"div\":[],\"audio\":[],\"scribble\":[],\"htext\":[],\"g\":[]}";
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
			
	// Create and get ECML Content
	@Test
	public void createValidEcmlContentExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+ecmlNode).
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		Assert.assertEquals(ecmlNode, identifier);
	}

	// Create and get valid HTML
	@Test
	public void createValidHTMLContentExpectSuccess200(){
		contentCleanUp();
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentHtml).
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
		String htmlNode = jp.get("result.node_id");
		
		// Get content and check
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+htmlNode).
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		Assert.assertEquals(status, "Draft");
	}

	// Create and get valid APK
		@Test
		public void createValidAPKContentExpectSuccess200(){
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
		String apkNode = jp.get("result.node_id");
		
		// Get content and check
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+apkNode).
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		Assert.assertEquals(apkNode, identifier);
	}
		
	// Create and get new collection
	@Test
	public void createValidCollectionExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
		setURI();
		int rn = generateRandomInt(2000, 29999);
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
		}
		if(count==2){
			node2 = nodeId;
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
		
		// Get collection
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+collectionNode).
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		ArrayList<String> identifiers = jP2.get("result.content.children.identifier");
		Assert.assertTrue(identifiers.contains(node1)&&identifiers.contains(node2));		
	}
	
	// Create collection with invalid content
	
	// Create content
	@Test
	public void createInvalidCollectionExpect400(){
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
		String ecmlNode = jp.get("result.node_id");
		
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", ecmlNode).replace("id2", invalidContentId);
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentCollection).
		with().
			contentType(JSON).
		when().
			post("/learning/v2/content").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}		
	// Update and get list
	@Test
	public void updateValidContentExpectSuccess200(){
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
			//log().all().
			extract().
			response();	
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Update content status to live
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("/learning/v2/content/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec());
		
		// Get content list and check for content
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("/learning/v2/content/list").
			then().
				extract().
				response();
			
		// Validate the response
		JsonPath jp1 = R1.jsonPath();
		ArrayList<String> identifier = jp1.get("result.content.identifier");
		//System.out.println(identifier);
		Assert.assertTrue((identifier).contains(nodeId));

		// Update status as Retired
		setURI();
		jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("/learning/v2/content/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec());
		
		// Get content list and check for content
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("/learning/v2/content/list").
			then().
				extract().
				response();
			
		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> identifier2 = jp2.get("result.content.identifier");
		Assert.assertFalse((identifier2).contains(nodeId));
				
		// Update content with Review status
		setURI();
		jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Review");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("/learning/v2/content/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec());
		
		// Get content list and check for content
		setURI();
		Response R3 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("/learning/v2/content/list").
			then().
				 //log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
		// Validate the response
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifier3 = jp3.get("result.content.identifier");
		Assert.assertFalse((identifier3).contains(nodeId));
	}
	
	// Upload valid content expect success
	
	//Create content
	@Test
	public void uploadandPublishContentExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Verbs_test.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}

	// Upload content with valid Ecml (With external JSON for item data, another controller with __cdata item data )
	
	//Create content
	@Test
	public void uploadandPublishContentWithExternaJSONItemDataCDataExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}
	
	// Upload content with valid Ecml containing JSON item data
	
	//Create content
	@Test
	public void uploadandPublishContentWithJSONItemDataExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Item_json.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}

	// Upload content with valid ECML containing data JSONs
		
	//Create content
	@Test
	public void uploadandPublishContentWithDataJSONExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Data_json_ecml.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}

	// Upload Content with valid ECML containing custom plugin
	
	//Create content
	@Test
	public void uploadandPublishContentWithCustomPluginExpectSuccess200(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Custom_Plugin.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}

	// Upload Files with Tween animation, audio sprites and Image sprite
	
	//Create content
	@Test
	public void uploadandPublishContentWithAudioImageSpriteTweenAnimationExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/the_moon_and_the_cap.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}
		
	// Upload file without index
	
	//Create content
	@Test
	public void uploadContentWithoutIndexExpect400(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_withoutindex.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	// Upload file with invalid ecml
	
	//Create content
	@Test
	public void uploadContentWithInvalidEcmlExpect400(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_invalidEcml.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get400ResponseSpec());
	}

	// Upload file with Empty zip

	//Create content
	@Test
	public void uploadContentWithEmptyZipExpect400(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_empty.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Upload file more than 50 MB and assets above 20 MB
	
	//Create content
	@Test
	public void uploadContentAboveLimitExpect400(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/contentAbove50MB.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get400ResponseSpec());
	}

	
	// Upload File with missing assets
	//Create content
	@Test
	public void uploadandPublishContentWithMissingAssetsExpect400(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_withoutAssets.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get400ResponseSpec());
		}

	// Upload File with valid JSON ecml
		//Create content
		@Test
		public void uploadandPublishContentWithJSONEcmlExpectSuccess200(){
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
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/ecml_with_json.zip")).
			when().
				post("/learning/v2/content/upload/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec());
			
			// Get body and validate
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("/learning/v2/content/"+nodeId+"?fields=body").
			then().
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload File with valid JSON ecml
		//Create content
		@Test
		public void uploadandPublishContentWithoutAssetsExpectSuccess200(){
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
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/Ecml_without_asset.zip")).
			when().
				post("/learning/v2/content/upload/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec());
			
			// Get body and validate
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("/learning/v2/content/"+nodeId+"?fields=body").
			then().
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

	
	// Upload invalid file
	
	//Create content
	@Test
	public void uploadContentInvalidFileExpect400(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/carpenter.png")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	// Upload multiple files
	
	//Create content
	@Test
	public void uploadandPublishContentMultipleExpectSuccess200(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Verbs_test.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}
	
	// Create, upload, publish and validate ECML content
	
	//Create content
	@Test
	public void publishContentExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}
		
	// Create, upload and publish worksheet
	
	//Create content
	@Test
	public void publishWorksheetExpectSuccess200(){
		contentCleanUp();
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "Worksheet");
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Akshara_worksheet.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}
	
	// Create, upload, publish and validate HTML Content
	
	//Create content
	@Test
	public void publishHTMLContentExpectSuccess200(){
		contentCleanUp();
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentHtml).
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/Build-a-sentence.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
				
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}
	
	// Create, upload, publish and validate APK Content
	
	//Create content
	@Test
	public void publishAPKContentExpectSuccess200(){
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
	
		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)){
			Assert.assertTrue(accessURL(nodeId));
		}
	}	

	// Create, upload, publish and validate valid collection
	@Test
	public void publishValidCollectionExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
		setURI();
		int rn = generateRandomInt(999, 1999);
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
		String nodeId = jP1.get("result.node_id");
		
		// Publish collection
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/publish/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		//String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		Assert.assertTrue(status.equals("Live")&&c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2));
	}
	
	// Create, upload, publish and validate valid collection with contents created from authoring tool
		@Test
		public void publishValidCollectionWithATContentsExpectSuccess200(){
			contentCleanUp();
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
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
				multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
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
			String nodeId = jP1.get("result.node_id");
			
			// Publish collection
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("/learning/v2/content/publish/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec());
			
			// Get content and validate
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("/learning/v2/content/"+nodeId).
			then().
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			Assert.assertTrue(status.equals("Live")&&c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2));
		}
	
		// Create, upload and publish nested collection
		@Test
		public void publishNestedCollectionExpectSuccess200(){
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
			
			// Get content and validate
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("/learning/v2/content/"+nodeId1).
			then().
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			Assert.assertTrue(status.equals("Live")&&c_identifier.equals(nodeId1)&&identifier1.contains(node1)&&identifier1.contains(node2));
					
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
			
			// Get content and validate
			setURI();
			Response R4 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("/learning/v2/content/"+collectionId).
			then().
				//log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Validate the response
			JsonPath jp4 = R4.jsonPath();
			String n_status = jp4.get("result.content.status");
			String n_identifier = jp4.get("result.content.identifier");
			ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
			Assert.assertTrue (n_status.equals("Live")&&n_identifier.equals(collectionId)&&n_identifier1.contains(nodeId1));
		}
	
	// Publish content with malformed XML body
	
	//Create content
	@Test
	public void publishMalformedJSONContentExpect4xx(){
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

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Update the body with malformed XML
		setURI();
		JSONObject js = new JSONObject(jsonUpdateContentValid);
		js.getJSONObject("request").getJSONObject("content").put("body", malformedJSONBody).remove("status");
		jsonUpdateContentValid = js.toString();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonUpdateContentValid).
		with().
			contentType("application/json").
		when().
			patch("/learning/v2/content/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertFalse((isValidJSON(body) || isValidXML(body)));
		}
	
	// Publish content with malformed XML body
	
	//Create content
	@Test
	public void publishMalformedXMLContentExpect4xx(){
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
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/haircut_story.zip")).
		when().
			post("/learning/v2/content/upload/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Update the body with malformed XML
		setURI();
		JSONObject js = new JSONObject(jsonUpdateContentValid);
		js.getJSONObject("request").getJSONObject("content").put("body", malformedXMLBody).remove("status");
		jsonUpdateContentValid = js.toString();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonUpdateContentValid).
		with().
			contentType("application/json").
		when().
			patch("/learning/v2/content/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get body and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId+"?fields=body").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertFalse((isValidJSON(body) || isValidXML(body)));
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
			body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle\"}}").
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
	}
	
	// Bundle APK collection
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
		body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle\"}}").
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
	
	// Private Members
	private boolean isValidXML(String body) {
        boolean isValid = true;
		if (!StringUtils.isBlank(body)) {
	            try {
	                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	                dBuilder.parse(new InputSource(new StringReader(body)));
	            } catch(ParserConfigurationException | SAXException | IOException e) {
	                isValid = false;
	            }
	        }
	        return isValid;
	    }
	    
	private boolean isValidJSON(String body) {
        boolean isValid = true;
        if (!StringUtils.isBlank(body)) {
	            try {
	                ObjectMapper objectMapper = new ObjectMapper();
	                objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
	                objectMapper.readTree(body);
	            } catch (IOException e) {
	                isValid = false;
	            }
	        }
	        return isValid;
	    }

	@SuppressWarnings("unused")
	private boolean accessURL(String nodeId) throws ClassCastException{
	boolean accessURL = true;
	// Publish created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/publish/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v2/content/"+nodeId).
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		
		// Fetching metadatas from API response
		
		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		String statusActual = jP1.get("result.content.status");	
		String mimeTypeActual = jP1.get("result.content.mimeType");
		String codeActual = jP1.get("result.content.code");
		String osIdActual = jP1.get("result.content.osId");
		String contentTypeActual = jP1.get("result.content.contentType");
		String mediaTypeActual = jP1.get("result.content.mediaType");
		String descriptionActual = jP1.get("result.content.description");
		Float pkgVersionActual = jP1.get("result.content.pkgVersion");
		Float size = jP1.get("result.content.size");
		
		// Downloading the zip file from artifact url and ecar from download url and saving with different name
		try{
			
		String ecarName = "ecar_"+rn+"";
		String uploadFile = "upload_"+rn+"";
		
		FileUtils.copyURLToFile(new URL(artifactUrl), new File(downloadPath+"/"+uploadFile+".zip"));
		String uploadSource = downloadPath+"/"+uploadFile+".zip";
		
		FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath+"/"+ecarName+".zip"));		
		String source = downloadPath+"/"+ecarName+".zip";
		
		File Destination = new File(downloadPath+"/"+ecarName+"");
		String Dest = Destination.getPath();
		//System.out.println(Dest);
		try {
			
			// Extracting the uploaded file using artifact url
			ZipFile zipUploaded = new ZipFile(uploadSource);
			zipUploaded.extractAll(Dest);
			
			// Downloaded from artifact url
			File uploadAssetsPath = new File(Dest+"/assets");
			File[] uploadListFiles = uploadAssetsPath.listFiles();
			
			// Extracting the ecar file
			ZipFile zip = new ZipFile(source);
			zip.extractAll(Dest);
			
			String folderName = nodeId;
			String dirName = Dest+"/"+folderName;
			
			
			File fileName = new File(dirName);
			File[] listofFiles = fileName.listFiles();
			
			for(File file : listofFiles){
				
				// Validating the ecar file
				
				if(file.isFile()){
					String fPath = file.getAbsolutePath();
					String fName = file.getName();
					//System.out.println(fName);
					
					if (fName.endsWith(".zip")|| fName.endsWith(".rar")){
					ZipFile ecarZip = new ZipFile(fPath);
					ecarZip.extractAll(dirName);
					
					// Fetching the assets
					File assetsPath = new File(dirName+"/assets");
					File[] extractedAssets = assetsPath.listFiles();						
					if (assetsPath.exists()){
						
						int assetCount = assetsPath.listFiles().length;
						//System.out.println(assetCount);
						
						int uploadAssetsCount = uploadAssetsPath.listFiles().length;
						//System.out.println(uploadAssetsCount);
						
						// Asserting the assets count in uploaded zip file and ecar file
						Assert.assertEquals(assetCount, uploadAssetsCount);
						
						// Compare the files in both of the folders are same
						compareFiles(uploadListFiles, extractedAssets);
						}
					}
					else{
						System.out.println("No zip file found");
					}
				}
				else{
					System.out.println("No zip file exists");
				}
			}
			
			// Validating the manifest 
			
			File manifest = new File(Dest+"/manifest.json");
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(manifest));
			JSONObject js = (JSONObject) obj;
	
			JSONObject arc = (JSONObject) js.get("archive");
			JSONArray items = (JSONArray)arc.get("items");
			
	        @SuppressWarnings("rawtypes")
	        
	        // Extracting the metadata from manifest and assert with api response
	        
			Iterator i = items.iterator();
	        while(i.hasNext()) {
	        	try {
				JSONObject item = (JSONObject) i.next();
				String name = (String) item.get("name");		        
				String mimeType = (String) item.get("mimeType");
				Assert.assertEquals(mimeTypeActual, mimeType);
				String status = (String) item.get("status");
				Assert.assertEquals(statusActual, status);
				String code = (String) item.get("code");
				Assert.assertEquals(codeActual, code);
				String osID = (String) item.get("osId");
				Assert.assertEquals(osIdActual, osID);
				String contentType = (String) item.get("contentType");
				Assert.assertEquals(contentTypeActual, contentType);
				String mediaType = (String) item.get("mediaType");
				Assert.assertEquals(mediaTypeActual, mediaType);
				String description = (String) item.get("description");
				Assert.assertEquals(descriptionActual, description);
				Float pkgVersion = (Float) item.get("pkgVersion");
				Assert.assertNotSame(pkgVersionActual, pkgVersion);
				if (artifactUrl.endsWith(".zip")&&downloadUrl.endsWith(".ecar")&&downloadUrl.contains(nodeId)&&statusActual.equals("Live")&&size<=20000000){
					System.out.println("Publish Success");
				}
				else{
					System.out.println("Publish Fails");
					}
	        	}
			        	catch(JSONException jse){
			        		//jse.printStackTrace();
			        	}
		        	}
				}				
				catch (Exception x){
				//x.printStackTrace();	
			}
		}	
		catch (Exception e) {
    		accessURL = false;
			//e.printStackTrace();
		}
		return accessURL = true;
	}
	
	// Compare the files extracted from artifact URL and ECAR
	
	public String compareFiles(File[] uploadListFiles, File[] extractedAssets)
	{
	    String filesincommon = "";
	    String filesnotpresent = "";
	    boolean final_status = true;
	    for (int i = 0; i < uploadListFiles.length; i++) {
	        boolean status = false;
	        for (int k = 0; k < extractedAssets.length; k++) {
	            if (uploadListFiles[i].getName().equalsIgnoreCase(extractedAssets[k].getName())) {
	                filesincommon = uploadListFiles[i].getName() + "," + filesincommon;
	                //System.out.println("Common files are: "+filesincommon);
	                status = true;
	                break;
	            }
	        }
	        if (!status) {
	            final_status = false;
	            filesnotpresent = uploadListFiles[i].getName() + "," + filesnotpresent;
	        }
	    }
	    //Assert.assertTrue(final_status);
	    if (final_status) {
	        //System.out.println("Files are same");
	        return "success";
	    } else {
	    	System.out.println("Files are missing");
	        System.out.println(filesnotpresent);
	        return filesnotpresent;
	    }
	}
}


