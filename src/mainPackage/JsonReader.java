package mainPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Helper class to help manipulate data covered by Json
 * */
public class JsonReader {
    /**
     * Path of keywords to exclude
     * */
    private final String excludePath = "data/exclude_list.json";
    /**
     * Path to store final results
     * */
    private final String resultPathPart = "data/result_list";

    private final String jsonExtension = ".json";

    /**
     * Final result of data collection Json Object
     * */
    public static JSONObject jobPostJsonOutput = null;

    /**
     * Read json data from json file of asset directory
     * */
    private String getJsonString() {
        String json = "";

        try {
            InputStream is = null;

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

    /**
     * Parse json string to job post array list
     * */
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

    /**
     * Convert job post json string to array list
     * */
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

    /**
     * Parse Job post, which is written as Json Object, to an Array List of JobPost objects.
     * */
    private ArrayList<JobPost> getJobPost(JSONObject jsonObject) {
        ArrayList<JobPost> resultList = new ArrayList<>();
        try {
            // Read the root object
            JSONArray jsonArray = (JSONArray) ((JSONObject)jsonObject.get("jobs")).get("job");

            // Parsing is done for each job announcement.
            for(int i = 0; i < jsonArray.toArray().length; i++) {
                try {
                    JSONObject obj = (JSONObject) jsonArray.get(i);

                    // Get ID of job post
                    int id = Integer.parseInt(obj.get("id").toString());

                    // Get the name of company
                    JSONObject company = (JSONObject) obj.get("company");
                    JSONObject companyDetail = (JSONObject) company.get("detail");
                    String companyName, position, field;
                    if (companyDetail.size() > 0)
                        companyName = companyDetail.get("name").toString();
                    else
                        continue;

                    // Get the position/job of job post
                    JSONObject positionObj = (JSONObject) obj.get("position");
                    if (positionObj.size() > 0)
                        position = positionObj.get("title").toString();
                    else
                        continue;

                    // Get the industry of the company
                    JSONObject industryObj = (JSONObject) positionObj.get("industry");
                    if (industryObj.size() > 0)
                        field = industryObj.get("name").toString();
                    else
                        continue;

                    // Get the job code of this job post
                    JSONObject keywordObj = (JSONObject) positionObj.get("job-code");
                    ArrayList<String> keywords = new ArrayList<>(Arrays.asList(splitKeywords(keywordObj.get("name").toString())));

                    // Get the required minimum experience level
                    JSONObject experienceLevelObj = (JSONObject) positionObj.get("experience-level");
                    int careerMin = Integer.parseInt(experienceLevelObj.get("min").toString());

                    // Create the JobPost instance and add to list
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

    /**
     * Split the keywords by comma
     * */
    private String[] splitKeywords(String keywords) {
        return keywords.split(",");
    }

    /**
     * Read exclude keyword list from file
     * */
    public ArrayList<Keyword> readExcludeList() {
        ArrayList<Keyword> resultList = new ArrayList<>();
        try {
            // Read file by exclude path
            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(excludePath);

            // Read the root object
            JSONObject mainObject = (JSONObject) parser.parse(reader);
            JSONArray jsonArray = (JSONArray) mainObject.get("exclude");
            // Read one keyword at a time and add it to the list
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

    /**
     * Save the list of keywords to be excluded as a file.
     * */
    public void writeExcludeList(ArrayList<Keyword> keywordArrayList) {
        // Create json array object
        JSONArray jsonArray = new JSONArray();
        // Add keywords to json array object
        for(Keyword keyword : keywordArrayList) {
            JSONObject newObj = new JSONObject();
            newObj.put("count", keyword.getCount());
            newObj.put("keyword", keyword.getKeyword());
            jsonArray.add(newObj);
        }
        // Add json array object to root object
        JSONObject finalObject = new JSONObject();
        finalObject.put("exclude", jsonArray);

        // Save the json object to file
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

    /**
     * Save the filtering final result to a file
     * */
    public void writeJobPostOutput(ArrayList<JobPost> jobPostArrayList, JPanel panel) {
        // When there is a final result
        if(jobPostJsonOutput != null) {
            try {
                // Make the result path
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
                StringBuilder resultPath = new StringBuilder();
                resultPath.append(resultPathPart);
                resultPath.append("(");
                resultPath.append(LocalDate.now().format(formatter));
                resultPath.append("-");
                resultPath.append(jobPostArrayList.size());
                resultPath.append(")");
                resultPath.append(jsonExtension);

                // Create the file and write result content
                FileWriter file = new FileWriter(resultPath.toString());
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
        }
        else {
            JOptionPane.showMessageDialog(panel, "Filter data first.", "Message", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generate the final result as Json Object.
     * */
    public void makeJobPostJson(ArrayList<JobPost> jobPostArrayList) {
        // Create json array object
        JSONArray jsonArray = new JSONArray();
        // Add each data to the json array
        for(JobPost post : jobPostArrayList) {
            // Add ID, company name, field, position, and required minimum career.
            JSONObject newObj = new JSONObject();
            newObj.put("id", post.getId());
            newObj.put("companyName", post.getCompanyName());
            newObj.put("field", post.getField());
            newObj.put("position", post.getPosition());
            newObj.put("careerMin", post.getCareerMin());

            // Create the keyword json array and add it to the json object
            JSONArray keywordArr = new JSONArray();
            for(String keyword : post.getKeywords()) {
                JSONObject keywordObj = new JSONObject();
                keywordObj.put("keyword", keyword);
                keywordArr.add(keywordObj);
            }
            newObj.put("keywordList", keywordArr);
            // Add json object to json array
            jsonArray.add(newObj);
        }

        // Add json array to root json object
        JSONObject finalObject = new JSONObject();
        finalObject.put("jobPosts", jsonArray);

        jobPostJsonOutput = finalObject;
    }
}
