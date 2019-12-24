package org.ekstep.itemset.handler;

public class QuestionPaperData {
    public static final String assessmentItemRequest = "{\n" +
            "    \"request\": {\n" +
            "        \"assessment_item\": {\n" +
            "            \"objectType\": \"AssessmentItem\",\n" +
            "            \"metadata\": {\n" +
            "            \t\"templateType\": \"Horizontal\",\n" +
            "                \"template\": \"NA\",\n" +
            "                \"copyright\": \"EKSTEP\",\n" +
            "                \"isPartialScore\": true,\n" +
            "                \"itemType\": \"UNIT\",\n" +
            "                \"code\": \"NA\",\n" +
            "                \"subject\": \"Commerce\",\n" +
            "                \"qlevel\": \"EASY\",\n" +
            "                \"evalUnordered\": false,\n" +
            "                \"channel\": \"0123221758376673287017\",\n" +
            "                \"language\": [\n" +
            "                    \"English\"\n" +
            "                ],\n" +
            "                \"medium\": \"English\",\n" +
            "                \"title\": \"What is capital of India?\\n\",\n" +
            "                \"type\": \"mcq\",\n" +
            "                \"editorState\": null,\n" +
            "                \"body\": \"{\\\"data\\\":{\\\"plugin\\\":{\\\"id\\\":\\\"org.ekstep.questionunit.mcq\\\",\\\"version\\\":\\\"1.3\\\",\\\"templateId\\\":\\\"horizontalMCQ\\\"},\\\"data\\\":{\\\"question\\\":{\\\"text\\\":\\\"<p>What is capital of India?</p>\\\\n\\\",\\\"image\\\":\\\"\\\",\\\"audio\\\":\\\"\\\",\\\"audioName\\\":\\\"\\\",\\\"hint\\\":\\\"\\\"},\\\"options\\\":[{\\\"text\\\":\\\"<p>Bangalore</p>\\\\n\\\",\\\"image\\\":\\\"\\\",\\\"audio\\\":\\\"\\\",\\\"audioName\\\":\\\"\\\",\\\"hint\\\":\\\"\\\",\\\"isCorrect\\\":false,\\\"$$hashKey\\\":\\\"object:941\\\"},{\\\"text\\\":\\\"<p>New Delhi</p>\\\\n\\\",\\\"image\\\":\\\"\\\",\\\"audio\\\":\\\"\\\",\\\"audioName\\\":\\\"\\\",\\\"hint\\\":\\\"\\\",\\\"isCorrect\\\":true,\\\"$$hashKey\\\":\\\"object:942\\\"},{\\\"text\\\":\\\"<p>Chennai</p>\\\\n\\\",\\\"image\\\":\\\"\\\",\\\"audio\\\":\\\"\\\",\\\"audioName\\\":\\\"\\\",\\\"isCorrect\\\":false,\\\"$$hashKey\\\":\\\"object:1071\\\"},{\\\"text\\\":\\\"<p>Mumbai</p>\\\\n\\\",\\\"image\\\":\\\"\\\",\\\"audio\\\":\\\"\\\",\\\"audioName\\\":\\\"\\\",\\\"isCorrect\\\":false,\\\"$$hashKey\\\":\\\"object:1076\\\"}],\\\"questionCount\\\":0,\\\"media\\\":[]},\\\"config\\\":{\\\"metadata\\\":{\\\"max_score\\\":1,\\\"isShuffleOption\\\":false,\\\"isPartialScore\\\":true,\\\"evalUnordered\\\":false,\\\"templateType\\\":\\\"Horizontal\\\",\\\"name\\\":\\\"What is capital of India?\\\\n\\\",\\\"title\\\":\\\"What is capital of India?\\\\n\\\",\\\"copyright\\\":\\\"EKSTEP\\\",\\\"medium\\\":\\\"English\\\",\\\"topic\\\":[],\\\"gradeLevel\\\":[\\\"Class 3\\\",\\\"Class 10\\\"],\\\"subject\\\":\\\"Environmental Studies\\\",\\\"learningOutcome\\\":[\\\"to identify the rules of congruence while proving the 2 triangles to be congruent,\\\"],\\\"board\\\":\\\"CBSE\\\",\\\"qlevel\\\":\\\"EASY\\\",\\\"category\\\":\\\"MCQ\\\"},\\\"max_time\\\":0,\\\"max_score\\\":1,\\\"partial_scoring\\\":true,\\\"layout\\\":\\\"Horizontal\\\",\\\"isShuffleOption\\\":false,\\\"questionCount\\\":1,\\\"evalUnordered\\\":false},\\\"media\\\":[]}}\",\n" +
            "                \"isShuffleOption\": false,\n" +
            "                \"options\": [\n" +
            "                    {\n" +
            "                        \"answer\": true,\n" +
            "                        \"value\": {\n" +
            "                            \"type\": \"text\",\n" +
            "                            \"asset\": \"1\",\n" +
            "                            \"resvalue\": 0,\n" +
            "                            \"resindex\": 0\n" +
            "                        }\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"question\": null,\n" +
            "                \"solutions\": null,\n" +
            "                \"learningOutcome\": [\n" +
            "                    \"to identify the rules of congruence while proving the 2 triangles to be congruent,\"\n" +
            "                ],\n" +
            "                \"max_score\": 1,\n" +
            "                \"name\": \"What is capital of India?\\n\",\n" +
            "                \"template_id\": \"NA\",\n" +
            "                \"category\": \"MCQ\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";


    public static final String assessmentItemSetRequest = "{\n" +
            "  \"request\": {\n" +
            "    \"assessment_item_set\": {\n" +
            "      \"objectType\": \"ItemSet\",\n" +
            "      \"metadata\": {\n" +
            "        \"title\": \"Test ItemSet\",\n" +
            "        \"type\": \"materialised\",\n" +
            "        \"max_score\": 1,\n" +
            "        \"total_items\": 1,\n" +
            "        \"description\": \"Test ItemSet\",\n" +
            "        \"code\": \"test.itemset\",\n" +
            "        \"owner\": \"Ilimi\",\n" +
            "        \"used_for\": \"assessment\",\n" +
            "        \"memberIds\": [\n" +
            "            \"do_11291254684717056013\",\n" +
            "            \"do_11291746417234739211\"\n" +
            "            \n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
