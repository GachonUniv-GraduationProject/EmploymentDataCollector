package mainPackage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonReader {
    private final String excludePath = "data/exclude_list.json";
    private final String resultPath = "data/result_list.json";

    // Read json data from json file of asset directory
    private String getJsonString() {
        String json = "";

        try {
            InputStream is = null;// 나중에 채우기

            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return json;
    }

    // Parse json string to tip array list
    public ArrayList<JobPost> readJobPost(String fileName) {
        ArrayList<JobPost> resultList = new ArrayList<>();
        try {
            // Get json array with json library
            JSONParser parser = new JSONParser();

            Reader reader = new FileReader(fileName);
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            JSONArray jsonArray = (JSONArray) ((JSONObject)jsonObject.get("jobs")).get("job");

            for(int i = 0; i < jsonArray.toArray().length; i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);

                int id = Integer.parseInt(obj.get("id").toString());

                JSONObject company = (JSONObject) obj.get("company");
                JSONObject companyDetail = (JSONObject) company.get("detail");
                String companyName = companyDetail.get("name").toString(), position, field;

                JSONObject positionObj = (JSONObject) obj.get("position");
                position = positionObj.get("title").toString();

                JSONObject industryObj = (JSONObject) positionObj.get("industry");
                field = industryObj.get("name").toString();

                JSONObject keywordObj = (JSONObject) positionObj.get("job-code");
                ArrayList<String> keywords = new ArrayList<>(Arrays.asList(splitKeywords(keywordObj.get("name").toString())));

                JSONObject experienceLevelObj = (JSONObject) positionObj.get("experience-level");
                int careerMin = Integer.parseInt(experienceLevelObj.get("min").toString());

                JobPost jobPost = new JobPost(id, companyName, position, field, keywords, careerMin);
                resultList.add(jobPost);
            }

            return resultList;

        } catch (Exception e) {
            // If there's any error, return false
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String[] splitKeywords(String keywords) {
        return keywords.split(",");
    }

    public ArrayList<Keyword> readExcludeList() {
        ArrayList<Keyword> resultList = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(excludePath);

            JSONObject mainObject = (JSONObject) parser.parse(reader);
            JSONArray jsonArray = (JSONArray) mainObject.get("exclude");
            for(int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);
                String keyword = obj.get("keyword").toString();
                int count = Integer.parseInt(obj.get("count").toString());
                resultList.add(new Keyword(keyword, count));
            }

            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeExcludeList(ArrayList<Keyword> keywordArrayList) {
        JSONArray jsonArray = new JSONArray();
        for(Keyword keyword : keywordArrayList) {
            JSONObject newObj = new JSONObject();
            newObj.put("count", keyword.getCount());
            newObj.put("keyword", keyword.getKeyword());
            jsonArray.add(newObj);
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put("exclude", jsonArray);

        try {
            FileWriter file = new FileWriter(excludePath);
            file.write(finalObject.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(finalObject);
    }

    public void writeJobPostOutput(ArrayList<JobPost> jobPostArrayList) {
        JSONArray jsonArray = new JSONArray();
        for(JobPost post : jobPostArrayList) {
            JSONObject newObj = new JSONObject();
            newObj.put("id", post.getId());
            newObj.put("companyName", post.getCompanyName());
            newObj.put("field", post.getField());
            newObj.put("position", post.getPosition());
            newObj.put("careerMin", post.getCareerMin());

            JSONArray keywordArr = new JSONArray();
            for(String keyword : post.getKeywords()) {
                JSONObject keywordObj = new JSONObject();
                keywordObj.put("keyword", keyword);
                keywordArr.add(keywordObj);
            }
            newObj.put("keywordList", keywordArr);
            jsonArray.add(newObj);
        }

        JSONObject finalObject = new JSONObject();
        finalObject.put("jobPosts", jsonArray);

        try {
            FileWriter file = new FileWriter(resultPath);
            file.write(finalObject.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(finalObject);
    }
}
