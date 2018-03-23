package com.railscon.englishnotebook.utilities;

public class SqliteStrHelper {
    public static String sqliteEscape(String keyWord){
        if(keyWord!=null) {
            keyWord = keyWord.replace("/", "//");
            keyWord = keyWord.replace("'", "''");
            keyWord = keyWord.replace("[", "/[");
            keyWord = keyWord.replace("]", "/]");
            keyWord = keyWord.replace("%", "/%");
            keyWord = keyWord.replace("&", "/&");
            keyWord = keyWord.replace("_", "/_");
            keyWord = keyWord.replace("(", "/(");
            keyWord = keyWord.replace(")", "/)");
            return keyWord;
        }else
            return "";
    }
}
