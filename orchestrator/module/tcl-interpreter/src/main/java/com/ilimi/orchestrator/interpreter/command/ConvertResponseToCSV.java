package com.ilimi.orchestrator.interpreter.command;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.common.dto.Response;
import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class ConvertResponseToCSV extends BaseSystemCommand implements ICommand, Command {
    
    private ObjectMapper mapper = new ObjectMapper();
    private static final String NEW_LINE_SEPARATOR = "\n";
    
    @Override
    public String getCommandName() {
        return "convert_response_to_csv";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 3) {
            try {
                TclObject tclObject1 = argv[1];
                TclObject tclObject2 = argv[2];
                if (null == tclObject1 || null == tclObject2) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    Object obj1 = ReflectObject.get(interp, tclObject1);
                    Response response = (Response) obj1;
                    String param = tclObject2.toString();
                    if (checkError(response)) {
                        TclObject tclResp = ReflectObject.newInstance(interp, response.getClass(), response);
                        interp.setResult(tclResp);
                    } else {
                        Object obj = response.get(param);
                        String csv = "";
                        if (null != obj) {
                            List<Object> list = new ArrayList<Object>();
                            if (!(obj instanceof List)) {
                                list.add(obj);
                            } else {
                                list = (List<Object>) obj;
                            }
                            csv = getCSV(list);
                        }
                        TclObject tclResp = ReflectObject.newInstance(interp, csv.getClass(), csv);
                        interp.setResult(tclResp);
                    }
                }
            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
    }
    
    @SuppressWarnings("unchecked")
    private String getCSV(List<Object> list) {
        if (null != list && !list.isEmpty()) {
            List<String[]> allRows = new ArrayList<String[]>();
            List<String[]> dataRows = new ArrayList<String[]>();
            Object obj = list.get(0);
            Map<String,Object> objectMap = null;
            try {
                objectMap = mapper.convertValue(obj, Map.class);
            } catch (Exception e) {
            }
            if (null != objectMap) {
                List<String> headers = new ArrayList<String>();
                List<Map<String, Object>> rowMaps = new ArrayList<Map<String, Object>>();
                for (Object val : list) {
                    Map<String, Object> map = mapper.convertValue(val, Map.class);
                    if (null != map && !map.isEmpty()) {
                        Set<String> keys = map.keySet();
                        for (String key : keys) {
                            if (!headers.contains(key)) {
                                headers.add(key);
                            }
                        }
                        rowMaps.add(map);
                    }
                }
                for (Map<String, Object> map : rowMaps) {
                    String[] row = new String[headers.size()];
                    for (int i=0; i<headers.size(); i++) {
                        String header = headers.get(i);
                        Object val = map.get(header);
                        addToDataRow(val, row, i);
                    }
                    dataRows.add(row);
                }
                allRows.add(headers.toArray(new String[headers.size()]));
            } else {
                for (Object val : list) {
                    String[] row = new String[1];
                    addToDataRow(val, row, 0);
                    dataRows.add(row);
                }
            }
            allRows.addAll(dataRows);
            try {
                CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
                StringWriter sWriter = new StringWriter();
                CSVPrinter writer = new CSVPrinter(sWriter, csvFileFormat);
                writer.printRecords(allRows);
                String csv = sWriter.toString();
                sWriter.close();
                writer.close();
                return csv;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    
    @SuppressWarnings("rawtypes")
    private void addToDataRow(Object val, String[] row, int i) {
        try {
            if (null != val) {
                if (val instanceof List) {
                    List list = (List) val;
                    String str = "";
                    for (int j=0; j<list.size(); j++) {
                        str += StringEscapeUtils.escapeCsv(list.get(j).toString());
                        if (j < list.size() - 1)
                            str += ",";
                    }
                    row[i] = str;
                } else if (val instanceof Object[]) {
                    Object[] arr = (Object[]) val;
                    String str = "";
                    for (int j=0; j<arr.length; j++) {
                        str += StringEscapeUtils.escapeCsv(arr[j].toString());
                        if (j < arr.length - 1)
                            str += ",";
                    }
                    row[i] = str;
                } else {
                    Object strObject = mapper.convertValue(val, Object.class);
                    row[i] = strObject.toString();
                }
            } else {
                row[i] = "";
            }
        } catch(Exception e) {
            row[i] = "";
        }
    }
    
}
