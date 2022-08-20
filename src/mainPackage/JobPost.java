package mainPackage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class JobPost {
    private int id;
    private String companyName;
    private String position;
    private String field;
    private ArrayList<String> keywords;
    private int careerMin;

    public JobPost(int id, String companyName, String position, String field, ArrayList<String> keywords, int careerMin) {
        this.id = id;
        this.companyName = companyName;
        this.position = position;
        this.field = field;
        this.keywords = keywords;
        this.careerMin = careerMin;
    }


    public int getId() {
        return id;
    }
    public String getCompanyName() {
        return companyName;
    }

    public String getPosition() {
        return position;
    }

    public String getField() {
        return field;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public int getCareerMin() {
        return careerMin;
    }

    public void removeKeyword(String keyword) {
        if(keywords.contains(keyword))
            keywords.remove(keyword);
    }
}
