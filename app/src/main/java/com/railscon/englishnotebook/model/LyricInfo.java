package com.railscon.englishnotebook.model;

import java.util.List;

/**
 * Created by maxazure on 2018/3/11.
 */

public class LyricInfo {
    public List<LineInfo> songLines;
    public String songArtist;
    public String songTitle;
    public String songAlbum;
    public long songOffset;

    public LyricInfo() {
    }
}