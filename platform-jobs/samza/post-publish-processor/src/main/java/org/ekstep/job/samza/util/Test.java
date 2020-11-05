package org.ekstep.job.samza.util;

public class Test {

    public static void main(String[] args) {
        printEvent("do_312469689878536192218042","012361896255012864110919");
        printEvent("do_312469613767942144218017","012361896255012864110919");
        printEvent("do_312469493882134528217982","012361896255012864110919");
        printEvent("do_312469572639440896118184","012361896255012864110919");
        printEvent("do_31244193786056704013844","in.ekstep");
        printEvent("do_3123496977448878082141","in.ekstep");
        printEvent("do_312473607772315648219502","in.ekstep");
        printEvent("do_312469516571246592118169","012361896255012864110919");
        printEvent("do_312469669103853568218038","012361896255012864110919");
        printEvent("do_312469507013812224118164","012361896255012864110919");
        printEvent("do_31242467916306841612800","012361896255012864110919");
        printEvent("do_31265339563063705622756","0123249244914974727");
        printEvent("do_31265340638145740812753","0123249244914974727");
        printEvent("do_31240701637202739221913","0123249244914974727");
        printEvent("do_31265341146510131212766","0123249244914974727");
        printEvent("do_31265340796715008012754","0123249244914974727");
        printEvent("do_31240701892751360011879","0123249244914974727");
        printEvent("do_31265338675646464012718","0123249244914974727");
        printEvent("do_31265339892269056012739","0123249244914974727");
        printEvent("do_31240700999281049621910","0123249244914974727");
        printEvent("do_31265339760387686422764","0123249244914974727");
        printEvent("do_31265339372679987222752","0123249244914974727");
        printEvent("do_31265340945903616012757","0123249244914974727");
        printEvent("do_31240629765219123221774","0123249244914974727");
        printEvent("do_31265339081075097622746","0123249244914974727");
    }

    private static void printEvent(String id, String channel) {
        System.out.println("{\"eid\":\"BE_JOB_REQUEST\",\"ets\":" + System.currentTimeMillis() + ",\"mid\":\"LP." + System.currentTimeMillis() + ".b3fb188d-d6fe-431e-b528-da3780c710a8\",\"actor\":{\"id\":\"learning-service\",\"type\":\"System\"},\"context\":{\"pdata\":{\"ver\":\"1.0\",\"id\":\"org.ekstep.platform\"},\"channel\":\"" + channel + "\",\"env\":\"ntpprod\"},\"object\":{\"ver\":1.0,\"id\":\"" + id + "\"},\"edata\":{\"action\":\"link_dialcode\",\"iteration\":1,\"graphId\":\"domain\",\"contentType\":\"Course\",\"objectType\":\"Content\"}}");
    }
}
