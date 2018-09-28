package org.ekstep.content.tool.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class InputList {
    private List<Input> inputList;

    public InputList(List<Input> inputList) {
        this.inputList = inputList;
    }

    public List<Input> getInputList() {
        return inputList;
    }

    public void setInputList(List<Input> inputList) {
        this.inputList = inputList;
    }

    public boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(inputList);
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(inputList);
    }

    public void add(Input input) {
        inputList.add(input);
    }

    public int size() {
        return inputList.size();
    }

    @Override
    public String toString(){
        if(CollectionUtils.isNotEmpty(inputList)){
            StringBuilder builder = new StringBuilder();
            for(Input input : inputList) {
                builder.append(input.toString() + ",\n");
            }
            return builder.toString();
        }else{
            return "[]";
        }
    }
}
