package com.railscon.englishnotebook.retrofit.model;

import com.railscon.englishnotebook.utilities.SqliteStrHelper;

public class Sentence {

    private int id;
    private int sequence;
    private String enSentence;
    private String cnSentence;
    private int startTime;
    private int endTime;
    private int emphasis;
    private int duration;
    private int lessonId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getEnSentence() {
        return SqliteStrHelper.sqliteEscape(enSentence);
    }

    public void setEnSentence(String enSentence) {
        this.enSentence = enSentence;
    }

    public String getCnSentence() {
        return SqliteStrHelper.sqliteEscape(cnSentence);
    }

    public void setCnSentence(String cnSentence) {
        this.cnSentence = cnSentence;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(int emphasis) {
        this.emphasis = emphasis;
    }
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

}