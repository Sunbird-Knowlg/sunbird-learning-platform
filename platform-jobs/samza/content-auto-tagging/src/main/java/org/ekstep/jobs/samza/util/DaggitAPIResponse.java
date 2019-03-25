package org.ekstep.jobs.samza.util;

import java.util.Map;

public class DaggitAPIResponse {

    public static final String SUCCESS_RESPONSE_STATUS = "success";
    private String id;
    private String ver;
    private String ts;
    private Map<String, Object> params;
    private SubmitResult result;

    public boolean successful() {
        return params != null && SUCCESS_RESPONSE_STATUS.equals(params.get("status"));
    }

    private class SubmitResult {

        private String experiment_name;
        private String estimate_time;
        private Integer status;

        @Override
        public String toString() {
            return "SubmitResult{" +
                    "experiment_name=" + experiment_name +
                    ", estimate_time=" + estimate_time +
                    ", status=" + status +
                    '}';
        }
    }
}
