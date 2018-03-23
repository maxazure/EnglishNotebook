package com.railscon.englishnotebook.model;

/**
 * Created by maxazure on 2018/3/11.
 */

public class LineInfo {
    public String content;
    public long start;
    public int duration;


    public LineInfo() {
    }
    public LineInfo(long start,String content) {
        this.content = content;
        this.start = start;
    }
    public LineInfo(long start,String content,int duration) {
        this.content = content;
        this.start = start;
    }
}