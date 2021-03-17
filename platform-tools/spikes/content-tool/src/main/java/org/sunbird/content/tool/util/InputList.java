package org.sunbird.content.tool.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class InputList {
    private List<Input> inputList;
    private int count;

    public InputList(List<Input> inputList, int count) {
        this.count = count;
        this.inputList = inputList;
    }

    public InputList(List<Input> inputList) {
        this.count = inputList.size();
        this.inputList = inputList;
    }

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    public List<Input> getInputList() { return inputList; }

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

    public void addAll(List<Input> list) { this.inputList.addAll(list); }

    public int size() {
        return inputList.size();
    }

    @Override
    public String toString(){
        if(CollectionUtils.isNotEmpty(inputList)){
            StringBuilder builder = new StringBuilder();
            int count = 1;
            for(Input input : inputList) {
                builder.append(count + ") " + input.toString() + "\n");
                count++;
            }
            return builder.toString();
        }else{
            return "[]";
        }
    }
}
