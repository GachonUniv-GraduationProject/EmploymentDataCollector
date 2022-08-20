package mainPackage;

public class Keyword implements Comparable<Keyword>{
    private String keyword;
    private int count;

    public Keyword(String keyword) {
        this.keyword = keyword;
        count = 1;
    }

    public Keyword(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getCount() {
        return count;
    }

    public void addCount() {
        count++;
    }

    public boolean compare(String newKeyword) {
        return keyword.equals(newKeyword);
    }

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
