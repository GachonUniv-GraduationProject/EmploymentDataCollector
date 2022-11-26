package mainPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.deploy.security.SelectableSecurityManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonReader {
    private final String excludePath = "data/exclude_list.json";
    private final String resultPath = "data/result_list.json";

    public static JSONObject jobPostJsonOutput = null;

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

            return getJobPost(jsonObject);

        } catch (Exception e) {
            // If there's any error, return false
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ArrayList<JobPost> convertJobPost(String json) {
        try {
            // Get json array with json library
            JSONParser parser = new JSONParser();

            JSONObject jsonObject = (JSONObject) parser.parse(json);
            return getJobPost(jsonObject);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private ArrayList<JobPost> getJobPost(JSONObject jsonObject) {
        ArrayList<JobPost> resultList = new ArrayList<>();
        try {
            JSONArray jsonArray = (JSONArray) ((JSONObject)jsonObject.get("jobs")).get("job");

            for(int i = 0; i < jsonArray.toArray().length; i++) {
                try {
                    JSONObject obj = (JSONObject) jsonArray.get(i);

                    int id = Integer.parseInt(obj.get("id").toString());

                    JSONObject company = (JSONObject) obj.get("company");
                    JSONObject companyDetail = (JSONObject) company.get("detail");
                    String companyName, position, field;
                    if (companyDetail.size() > 0)
                        companyName = companyDetail.get("name").toString();
                    else
                        continue;

                    JSONObject positionObj = (JSONObject) obj.get("position");
                    if (positionObj.size() > 0)
                        position = positionObj.get("title").toString();
                    else
                        continue;

                    JSONObject industryObj = (JSONObject) positionObj.get("industry");
                    if (industryObj.size() > 0)
                        field = industryObj.get("name").toString();
                    else
                        continue;

                    JSONObject keywordObj = (JSONObject) positionObj.get("job-code");
                    ArrayList<String> keywords = new ArrayList<>(Arrays.asList(splitKeywords(keywordObj.get("name").toString())));

                    JSONObject experienceLevelObj = (JSONObject) positionObj.get("experience-level");
                    int careerMin = Integer.parseInt(experienceLevelObj.get("min").toString());

                    JobPost jobPost = new JobPost(id, companyName, position, field, keywords, careerMin);
                    resultList.add(jobPost);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return resultList;

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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

    public void writeJobPostOutput(ArrayList<JobPost> jobPostArrayList, JPanel panel) {
        if(jobPostJsonOutput != null) {
            try {
                FileWriter file = new FileWriter(resultPath);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(JsonReader.jobPostJsonOutput);
                file.write(json);
                file.flush();
                file.close();

                JOptionPane.showMessageDialog(panel, "Export Complete!", "Message", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(panel, "[ERROR] " + e.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
            }

            //System.out.println(jobPostJsonOutput);
        }
        else {
            JOptionPane.showMessageDialog(panel, "Filter data first.", "Message", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void makeJobPostJson(ArrayList<JobPost> jobPostArrayList) {
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

        jobPostJsonOutput = finalObject;
    }
}
