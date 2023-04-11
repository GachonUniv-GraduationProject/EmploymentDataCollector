package mainPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class that holds job posting data
 * */
public class JobPost {
    /**
     * ID of job post in Saramin
     * */
    private int id;
    /**
     * The name of the company that posted the job posting
     * */
    private String companyName;
    /**
     * A position/job
     * */
    private String position;
    /**
     * Field of recruitment
     * */
    private String field;
    /**
     * Keywords related to job openings
     * */
    private ArrayList<String> keywords;
    /**
     * Minimum requirements for required experience
     * */
    private int careerMin;

    public JobPost(int id, String companyName, String position, String field, ArrayList<String> keywords, int careerMin) {
        this.id = id;
        this.companyName = companyName;
        this.position = position;
        this.field = field;
        this.keywords = keywords;
        this.careerMin = careerMin;
    }


    /**
     * Get ID of job post
     * */
    public int getId() {
        return id;
    }
    /**
     * Get name of company that uploaded the job post
     * */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Get the position/job of job post
     * */
    public String getPosition() {
        return position;
    }

    /**
     * Get the field of recruitment
     * */
    public String getField() {
        return field;
    }

    /**
     * Get the keywords related to job post
     * */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Get the minimum requirement of career
     * */
    public int getCareerMin() {
        return careerMin;
    }

    /**
     * Remove a specific keyword
     * */
    public void removeKeyword(String keyword) {
        if(keywords.contains(keyword))
            keywords.remove(keyword);
    }
}
