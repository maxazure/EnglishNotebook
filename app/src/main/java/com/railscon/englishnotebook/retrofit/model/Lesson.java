package com.railscon.englishnotebook.retrofit.model;

import com.railscon.englishnotebook.utilities.SqliteStrHelper;

import java.util.List;

public class Lesson {

    private int _id;
    private String cTitle;
    private List<Sentence> sentences = null;
    private String cVoice;
    private String cCreated;
    private int cSort;
    private int cRank;
    private int cLessonId;
    private String cTags;

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getCTitle() {
        return SqliteStrHelper.sqliteEscape(cTitle);
    }

    public void setCTitle(String cTitle) {
        this.cTitle = cTitle;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public String getCVoice() {
        return cVoice;
    }

    public void setCVoice(String cVoice) {
        this.cVoice = cVoice;
    }

    public String getCCreated() {
        return cCreated;
    }

    public void setCCreated(String cCreated) {
        this.cCreated = cCreated;
    }

    public int getCSort() {
        return cSort;
    }

    public void setCSort(int cSort) {
        this.cSort = cSort;
    }

    public int getCRank() {
        return cRank;
    }

    public void setCRank(int cRank) {
        this.cRank = cRank;
    }

    public int getCLessonId() {
        return cLessonId;
    }

    public void setCLessonId(int cLessonId) {
        this.cLessonId = cLessonId;
    }

    public Object getCTags() {
        return cTags;
    }

    public void setCTags(String cTags) {
        this.cTags = cTags;
    }

}