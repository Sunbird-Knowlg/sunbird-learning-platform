package org.ekstep.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CurationUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	private static List<String> badWords = Arrays.asList("abbo","abo","abortion","abuse","addict","addicts","adult","africa","african","alla","allah","alligatorbait","amateur","american","anal","analannie","analsex","angie","angry","anus","arab","arabs","areola","argie","aroused","arse","arsehole","asian","ass","assassin","assassinate","assassination","assault","assbagger","assblaster","assclown","asscowboy","asses","assfuck","assfucker","asshat","asshole","assholes","asshore","assjockey","asskiss","asskisser","assklown","asslick","asslicker","asslover","assman","assmonkey","assmunch","assmuncher","asspacker","asspirate","asspuppies","assranger","asswhore","asswipe","athletesfoot","attack","australian","babe","babies","backdoor","backdoorman","backseat","badfuck","balllicker","balls","ballsack","banging","baptist","barelylegal","barf","barface","barfface","bast","bastard","bazongas","bazooms","beaner","beast","beastality","beastial","beastiality","beatoff","beat-off","beatyourmeat","beaver","bestial","bestiality","bi","biatch","bible","bicurious","bigass","bigbastard","bigbutt","bigger","bisexual","bi-sexual","bitch","bitcher","bitches","bitchez","bitchin","bitching","bitchslap","bitchy","biteme","black","blackman","blackout","blacks","blind","blow","blowjob","boang","bogan","bohunk","bollick","bollock","bomb","bombers","bombing","bombs","bomd","bondage","boner","bong","boob","boobies","boobs","booby","boody","boom","boong","boonga","boonie","booty","bootycall","bountybar","bra","brea5t","breast","breastjob","breastlover","breastman","brothel","bugger","buggered","buggery","bullcrap","bulldike","bulldyke","bullshit","bumblefuck","bumfuck","bunga","bunghole","buried","burn","butchbabes","butchdike","butchdyke","butt","buttbang","butt-bang","buttface","buttfuck","butt-fuck","buttfucker","butt-fucker","buttfuckers","butt-fuckers","butthead","buttman","buttmunch","buttmuncher","buttpirate","buttplug","buttstain","byatch","cacker","cameljockey","cameltoe","canadian","cancer","carpetmuncher","carruth","catholic","catholics","cemetery","chav","cherrypopper","chickslick","children's","chin","chinaman","chinamen","chinese","chink","chinky","choad","chode","christ","christian","church","cigarette","cigs","clamdigger","clamdiver","clit","clitoris","clogwog","cocaine","cock","cockblock","cockblocker","cockcowboy","cockfight","cockhead","cockknob","cocklicker","cocklover","cocknob","cockqueen","cockrider","cocksman","cocksmith","cocksmoker","cocksucer","cocksuck","cocksucked","cocksucker","cocksucking","cocktail","cocktease","cocky","cohee","coitus","color","colored","coloured","commie","communist","condom","conservative","conspiracy","coolie","cooly","coon","coondog","copulate","cornhole","corruption","cra5h","crabs","crack","crackpipe","crackwhore","crack-whore","crap","crapola","crapper","crappy","crash","creamy","crime","crimes","criminal","criminals","crotch","crotchjockey","crotchmonkey","crotchrot","cum","cumbubble","cumfest","cumjockey","cumm","cummer","cumming","cumquat","cumqueen","cumshot","cunilingus","cunillingus","cunn","cunnilingus","cunntt","cunt","cunteyed","cuntfuck","cuntfucker","cuntlick","cuntlicker","cuntlicking","cuntsucker","cybersex","cyberslimer","dago","dahmer","dammit","damn","damnation","damnit","darkie","darky","datnigga","dead","deapthroat","death","deepthroat","defecate","dego","demon","deposit","desire","destroy","deth","devil","devilworshipper","dick","dickbrain","dickforbrains","dickhead","dickless","dicklick","dicklicker","dickman","dickwad","dickweed","diddle","die","died","dies","dike","dildo","dingleberry","dink","dipshit","dipstick","dirty","disease","diseases","disturbed","dive","dix","dixiedike","dixiedyke","doggiestyle","doggystyle","dong","doodoo","doo-doo","doom","dope","dragqueen","dragqween","dripdick","drug","drunk","drunken","dumb","dumbass","dumbbitch","dumbfuck","dyefly","dyke","easyslut","eatballs","eatme","eatpussy","ecstacy","ejaculate","ejaculated","ejaculating","ejaculation","enema","enemy","erect","erection","ero","escort","ethiopian","ethnic","european","evl","excrement","execute","executed","execution","executioner","explosion","facefucker","faeces","fag","fagging","faggot","fagot","failed","failure","fairies","fairy","faith","fannyfucker","fart","farted","farting","farty","fastfuck","fat","fatah","fatass","fatfuck","fatfucker","fatso","fckcum","fear","feces","felatio","felch","felcher","felching","fellatio","feltch","feltcher","feltching","fetish","fight","filipina","filipino","fingerfood","fingerfuck","fingerfucked","fingerfucker","fingerfuckers","fingerfucking","fire","firing","fister","fistfuck","fistfucked","fistfucker","fistfucking","fisting","flange","flasher","flatulence","floo","flydie","flydye","fok","fondle","footaction","footfuck","footfucker","footlicker","footstar","fore","foreskin","forni","fornicate","foursome","fourtwenty","fraud","freakfuck","freakyfucker","freefuck","fu","fubar","fuc","fucck","fuck","fucka","fuckable","fuckbag","fuckbuddy","fucked","fuckedup","fucker","fuckers","fuckface","fuckfest","fuckfreak","fuckfriend","fuckhead","fuckher","fuckin","fuckina","fucking","fuckingbitch","fuckinnuts","fuckinright","fuckit","fuckknob","fuckme","fuckmehard","fuckmonkey","fuckoff","fuckpig","fucks","fucktard","fuckwhore","fuckyou","fudgepacker","fugly","fuk","fuks","funeral","funfuck","fungus","fuuck","gangbang","gangbanged","gangbanger","gangsta","gatorbait","gay","gaymuthafuckinwhore","gaysex","geez","geezer","geni","genital","german","getiton","gin","ginzo","gipp","girls","givehead","glazeddonut","gob","god","godammit","goddamit","goddammit","goddamn","goddamned","goddamnes","goddamnit","goddamnmuthafucker","goldenshower","gonorrehea","gonzagas","gook","gotohell","goy","goyim","greaseball","gringo","groe","gross","grostulation","gubba","gummer","gun","gyp","gypo","gypp","gyppie","gyppo","gyppy","hamas","handjob","hapa","harder","hardon","harem","headfuck","headlights","hebe","heeb","hell","henhouse","heroin","herpes","heterosexual","hijack","hijacker","hijacking","hillbillies","hindoo","hiscock","hitler","hitlerism","hitlerist","hiv","ho","hobo","hodgie","hoes","hole","holestuffer","homicide","homo","homobangers","homosexual","honger","honk","honkers","honkey","honky","hook","hooker","hookers","hooters","hore","hork","horn","horney","horniest","horny","horseshit","hosejob","hoser","hostage","hotdamn","hotpussy","hottotrot","hummer","husky","hussy","hustler","hymen","hymie","iblowu","idiot","ikey","illegal","incest","insest","intercourse","interracial","intheass","inthebuff","israel","israeli","israel's","italiano","itch","jackass","jackoff","jackshit","jacktheripper","jade","jap","japanese","japcrap","jebus","jeez","jerkoff","jesus","jesuschrist","jew","jewish","jiga","jigaboo","jigg","jigga","jiggabo","jigger","jiggy","jihad","jijjiboo","jimfish","jism","jiz","jizim","jizjuice","jizm","jizz","jizzim","jizzum","joint","juggalo","jugs","junglebunny","kaffer","kaffir","kaffre","kafir","kanake","kid","kigger","kike","kill","killed","killer","killing","kills","kink","kinky","kissass","kkk","knife","knockers","kock","kondum","koon","kotex","krap","krappy","kraut","kum","kumbubble","kumbullbe","kummer","kumming","kumquat","kums","kunilingus","kunnilingus","kunt","ky","kyke","lactate","laid","lapdance","latin","lesbain","lesbayn","lesbian","lesbin","lesbo","lez","lezbe","lezbefriends","lezbo","lezz","lezzo","liberal","libido","licker","lickme","lies","limey","limpdick","limy","lingerie","liquor","livesex","loadedgun","lolita","looser","loser","lotion","lovebone","lovegoo","lovegun","lovejuice","lovemuscle","lovepistol","loverocket","lowlife","lsd","lubejob","lucifer","luckycammeltoe","lugan","lynch","macaca","mad","mafia","magicwand","mams","manhater","manpaste","marijuana","mastabate","mastabater","masterbate","masterblaster","mastrabator","masturbate","masturbating","mattressprincess","meatbeatter","meatrack","meth","mexican","mgger","mggor","mickeyfinn","mideast","milf","minority","mockey","mockie","mocky","mofo","moky","moles","molest","molestation","molester","molestor","moneyshot","mooncricket","mormon","moron","moslem","mosshead","mothafuck","mothafucka","mothafuckaz","mothafucked","mothafucker","mothafuckin","mothafucking","mothafuckings","motherfuck","motherfucked","motherfucker","motherfuckin","motherfucking","motherfuckings","motherlovebone","muff","muffdive","muffdiver","muffindiver","mufflikcer","mulatto","muncher","munt","murder","murderer","muslim","naked","narcotic","nasty","nastybitch","nastyho","nastyslut","nastywhore","nazi","necro","negro","negroes","negroid","negro's","nig","niger","nigerian","nigerians","nigg","nigga","niggah","niggaracci","niggard","niggarded","niggarding","niggardliness","niggardliness's","niggardly","niggards","niggard's","niggaz","nigger","niggerhead","niggerhole","niggers","nigger's","niggle","niggled","niggles","niggling","nigglings","niggor","niggur","niglet","nignog","nigr","nigra","nigre","nip","nipple","nipplering","nittit","nlgger","nlggor","nofuckingway","nook","nookey","nookie","noonan","nooner","nude","nudger","nuke","nutfucker","nymph","ontherag","oral","orga","orgasim","orgasm","orgies","orgy","osama","paki","palesimian","palestinian","pansies","pansy","panti","panties","payo","pearlnecklace","peck","pecker","peckerwood","pee","peehole","pee-pee","peepshow","peepshpw","pendy","penetration","peni5","penile","penis","penises","penthouse","period","perv","phonesex","phuk","phuked","phuking","phukked","phukking","phungky","phuq","pi55","picaninny","piccaninny","pickaninny","piker","pikey","piky","pimp","pimped","pimper","pimpjuic","pimpjuice","pimpsimp","pindick","piss","pissed","pisser","pisses","pisshead","pissin","pissing","pissoff","pistol","pixie","pixy","playboy","playgirl","pocha","pocho","pocketpool","pohm","polack","pom","pommie","pommy","poo","poon","poontang","poop","pooper","pooperscooper","pooping","poorwhitetrash","popimp","porchmonkey","porn","pornflick","pornking","porno","pornography","pornprincess","pot","poverty","premature","pric","prick","prickhead","primetime","propaganda","pros","prostitute","protestant","pu55i","pu55y","pube","pubic","pubiclice","pud","pudboy","pudd","puddboy","puke","puntang","purinapricness","puss","pussie","pussies","pussy","pussycat","pussyeater","pussyfucker","pussylicker","pussylips","pussylover","pussypounder","pusy","quashie","queef","queer","quickie","quim","ra8s","rabbi","racial","racist","radical","radicals","raghead","randy","rape","raped","raper","rapist","rearend","rearentry","rectum","redlight","redneck","reefer","reestie","refugee","reject","remains","rentafuck","republican","rere","retard","retarded","ribbed","rigger","rimjob","rimming","roach","robber","roundeye","rump","russki","russkie","sadis","sadom","samckdaddy","sandm","sandnigger","satan","scag","scallywag","scat","schlong","screw","screwyou","scrotum","scum","semen","seppo","servant","sex","sexed","sexfarm","sexhound","sexhouse","sexing","sexkitten","sexpot","sexslave","sextogo","sextoy","sextoys","sexual","sexually","sexwhore","sexy","sexymoma","sexy-slim","shag","shaggin","shagging","shat","shav","shawtypimp","sheeney","shhit","shinola","shit","shitcan","shitdick","shite","shiteater","shited","shitface","shitfaced","shitfit","shitforbrains","shitfuck","shitfucker","shitfull","shithapens","shithappens","shithead","shithouse","shiting","shitlist","shitola","shitoutofluck","shits","shitstain","shitted","shitter","shitting","shitty","shoot","shooting","shortfuck","showtime","sick","sissy","sixsixsix","sixtynine","sixtyniner","skank","skankbitch","skankfuck","skankwhore","skanky","skankybitch","skankywhore","skinflute","skum","skumbag","slant","slanteye","slapper","slaughter","slav","slave","slavedriver","sleezebag","sleezeball","slideitin","slime","slimeball","slimebucket","slopehead","slopey","slopy","slut","sluts","slutt","slutting","slutty","slutwear","slutwhore","smack","smackthemonkey","smut","snatch","snatchpatch","snigger","sniggered","sniggering","sniggers","snigger's","sniper","snot","snowback","snownigger","sob","sodom","sodomise","sodomite","sodomize","sodomy","sonofabitch","sonofbitch","sooty","sos","soviet","spaghettibender","spaghettinigger","spank","spankthemonkey","sperm","spermacide","spermbag","spermhearder","spermherder","spic","spick","spig","spigotty","spik","spit","spitter","splittail","spooge","spreadeagle","spunk","spunky","squaw","stagg","stiffy","strapon","stringer","stripclub","stroke","stroking","stupid","stupidfuck","stupidfucker","suck","suckdick","sucker","suckme","suckmyass","suckmydick","suckmytit","suckoff","suicide","swallow","swallower","swalow","swastika","sweetness","syphilis","taboo","taff","tampon","tang","tantra","tarbaby","tard","teat","terror","terrorist","teste","testicle","testicles","thicklips","thirdeye","thirdleg","threesome","threeway","timbernigger","tinkle","tit","titbitnipply","titfuck","titfucker","titfuckin","titjob","titlicker","titlover","tits","tittie","titties","titty","tnt","toilet","tongethruster","tongue","tonguethrust","tonguetramp","tortur","torture","tosser","towelhead","trailertrash","tramp","trannie","tranny","transexual","transsexual","transvestite","triplex","trisexual","trojan","trots","tuckahoe","tunneloflove","turd","turnon","twat","twink","twinkie","twobitwhore","uck","uk","unfuckable","upskirt","uptheass","upthebutt","urinary","urinate","urine","usama","uterus","vagina","vaginal","vatican","vibr","vibrater","vibrator","vietcong","violence","virgin","virginbreaker","vomit","vulva","wab","wank","wanker","wanking","waysted","weapon","weenie","weewee","welcher","welfare","wetb","wetback","wetspot","whacker","whash","whigger","whiskey","whiskeydick","whiskydick","whit","whitenigger","whites","whitetrash","whitey","whiz","whop","whore","whorefucker","whorehouse","wigger","willie","williewanker","willy","wn","wog","women's","wop","wtf","wuss","wuzzie","xtc","xxx","yankee","yellowman","zigabo","zipperhead");

	private static String tagMeApiUrlPrefix = "https://tagme.d4science.org/tagme/tag?lang=en&gcube-token=1e1f2881-62ec-4b3e-9036-9efe89347991-843339462&text=";
	private static String tagMeApiUrlSuffix = "&include_abstarction=True&epsilon=0.6";

	//private static  Map<String, Object> labels = new HashMap<String, Object>();
	//private static  List<String> flags = new ArrayList<String>();

	private static  List<Map<String,Object>> flagMapList = new ArrayList<>();

	public static List<String> checkProfanity(String str){
		List<String> result = new ArrayList<>();
		Set<String> wordSet = getListFromString(str);
		result = wordSet.parallelStream().filter(originalWord -> badWords.contains(originalWord)).collect(Collectors.toList());
		return result;
	}

	public static Set<String> getKeywords(String str) {
		Set<String> result = new HashSet<>();
		List<String> wordList = new ArrayList<>(getListFromString(str));
		int size = wordList.size();
		int start = 0;
		int limit = 0;
		while(limit<size) {
			start = limit;
			limit = limit+500 >= size ? size : limit + 500;
			String splitedWords = String.join("_", wordList.subList(start, limit));
			//TODO : Enable it if keywords are required
			result.addAll(makeTagCall(splitedWords));
		}
		return result;
	}

	//TODO: Need to decide, whether getKeywords() should return map or set
	public static Set<String> makeTagCall(String keyWords) {
		Set<String> keywords = new HashSet<>();
		try {
			String url = tagMeApiUrlPrefix + keyWords + tagMeApiUrlSuffix;
			Response resp = HttpRestUtil.callTagMeApi(url, null, new HashMap<String, String>());
			Map<String,Object> result = resp.getResult();
			List<Map<String,Object>> annotations = (List<Map<String, Object>>) result.get("annotations");
			for(Map<String,Object> map : annotations){
				keywords.add((String)map.get("spot"));
			}
		} catch (Exception e) {
			TelemetryManager.error("Error occured while getting kewords from tagme api ::::: " + e);
			e.printStackTrace();
		}
		return keywords;
	}

    /*public static void main(String args[]) {
    		String a = "<p>Nelson Mandela was president of&nbsp;africa. He never used to be&nbsp;angry.</p>";
    		System.out.println(getListFromString(a));
    }*/

	public static Set<String> getListFromString(String str){
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("\\<.*?>"," ");
		str = str.replaceAll("[;\\/:*?\"<>|&'\\.]", " ");
		return new HashSet(Arrays.asList(str.split(" ")));
	}

	public static  String getText (String ecarText) {
		StringBuilder builder = new StringBuilder();
		try {
			Map<String, Object> map = mapper.readValue(ecarText, Map.class);
			if (null!=map && !map.isEmpty()) {
				if((Map<String,Object>)map.get("theme") != null) {
					List<Map<String,Object>> stageList	=(List<Map<String,Object>>) ((Map<String,Object>)map.get("theme")).get("stage");
					for (Map<String,Object> stageMap : stageList) {
						Object textList = stageMap.get("org.ekstep.text");
						if(textList != null && textList instanceof List) {
							List<Map<String,Object>> textMap = (List<Map<String,Object>>)textList;
							extractText(textMap, builder);
						}
						textList = stageMap.get("org.ekstep.richtext");
						if(textList != null && textList instanceof List) {
							List<Map<String,Object>> textMap = (List<Map<String,Object>>)textList;
							extractText(textMap, builder);
						}
					}
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error occured while extracting text from ecml body::::: " + e);
			e.printStackTrace();
		}
		String str = builder.toString().replaceAll("\\<.*?>","");
		return str;
	}


	public static void extractText(List<Map<String, Object>> textMap, StringBuilder builder) {
		if (null!=textMap && !textMap.isEmpty()) {
			for (Map<String, Object> indText : textMap) {
				Map<String, Object> tempMap = (Map<String, Object>) indText.get("config");
				if (null!= tempMap && !tempMap.isEmpty()) {
					if (tempMap.get("__cdata") != null) {
						String actualText = (String) tempMap.get("__cdata");
						if (actualText != null) {
							try {
								Map<String, Object> map = mapper.readValue(actualText, Map.class);
								if (null!= map && !map.isEmpty()) {
									Object obj = map.get("text");
									if (obj != null && obj instanceof String) {
										builder.append(" "+(String) obj);
									}
								}
							} catch (Exception e) {
								TelemetryManager.error("Error occured while extracting text from ecml body::::: " + e);
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

	}

	public static Map<String,String> getImageUrls(String ecarText) {
		Map<String,String> mediaList = new HashMap<>();
		try {
			Map<String, Object> map = mapper.readValue(ecarText, Map.class);
			if (null!=map && !map.isEmpty()) {
				if ((Map<String, Object>) map.get("theme") != null) {
					Map<String, Object> stageList = (Map<String, Object>) ((Map<String, Object>) map.get("theme"))
							.get("manifest");
					if (stageList != null) {
						Object object = stageList.get("media");
						if (object != null && object instanceof List) {
							List<Map<String, Object>> mediaMapList = (List<Map<String, Object>>) object;
							if (mediaMapList != null) {
								for (Map<String, Object> mediaMap : mediaMapList) {
									Object mediaType = mediaMap.get("type");
									if (mediaType != null && "image".equalsIgnoreCase((String) mediaType)) {
										//TODO: Check for public url / youtube url . Id should not be null in that case. - it won't be null
										mediaList.put((String) mediaMap.get("id"), (String) mediaMap.get("src"));
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error occured while extracting images from ecml body::::: " + e);
			e.printStackTrace();
		}
		return mediaList;
	}

	public static List<Map<String,Object>> validateImages(String bodyText){
		Map<String,String> urls = getImageUrls(bodyText);

		for(String assetId : urls.keySet()){
			// download image and validate
			String url = "https://dev.ekstep.in"+urls.get(assetId);
			File file = HttpDownloadUtility.downloadFile(url, "/data/contentBundle/");
			if(null!=file){
				TelemetryManager.log("Calling Vision api for asset [ "+assetId+" ], url = "+url);
				callVisionApi(assetId,file);
			}
		}
		List<Map<String, Object>> list = flagMapList.stream().distinct().collect(Collectors.toList());
		return list;
	}

	public static void callVisionApi(String identifier , File file){
		try{
			// Get Vision Service
			VisionUtil vision = new VisionUtil(VisionUtil.getVisionService());

			Map<String, Object> labels = vision.getTags(file, vision);
			List<String> flags = vision.getFlags(file, vision);
			if(!flags.isEmpty() && !flags.contains("racy")){
				TelemetryManager.log("objectional content found for asset id [" + identifier +"] , flags = "+flags);
				Map<String,Object> dMap = new HashMap<String,Object>(){{
					put("identifier",identifier);
					put("flags",flags);
					put("labels",labels);
				}};
				flagMapList.add(dMap);
			}
		}catch(Exception e){
			TelemetryManager.log("Exception Occured while calling vision api..."+e);
			e.printStackTrace();
		}
	}
}
