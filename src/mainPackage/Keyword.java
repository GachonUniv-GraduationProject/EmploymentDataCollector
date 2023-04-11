package mainPackage;

/**
 * A class that contains the words and frequency of appearance of a particular keyword
 * */
public class Keyword implements Comparable<Keyword>{
    /**
     * Word of keyword
     * */
    private String keyword;
    /**
     * Frequency of this keyword
     * */
    private int count;

    public Keyword(String keyword) {
        this.keyword = keyword;
        count = 1;
    }

    public Keyword(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }

    /**
     * Get the word of keyword
     * */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Set the word of keyword
     * */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Get frequency of this keyword
     * */
    public int getCount() {
        return count;
    }

    /**
     * Add frequency of this keyword
     * */
    public void addCount() {
        count++;
    }

    /**
     * Check if newKeyword is same with this keyword
     * */
    public boolean compare(String newKeyword) {
        return keyword.equals(newKeyword);
    }

    /**
     * compareTo override function to sort the priority of that keyword
     * */
    @Override
    public int compareTo(Keyword other) {
        if(count > other.count)
            return 1;
        else if(count == other.count) {
            return keyword.compareTo(other.keyword);
        }
        else
            return -1;
    }
}
