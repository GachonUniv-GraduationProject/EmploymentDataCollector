package mainPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrawlingManager {
    private Frame mainFrame;
    private Panel mainPanel;

    private JPanel urlPanel;
    private JPanel keywordPanel;
    private JCheckBox[] keywordCheckboxes;

    private JPanel resultPanel;
    private Box resultBox;

    private String[] collectedKeywords;

    private int startIndex;

    private ArrayList<JobPost> jobPostList;
    private ArrayList<Keyword> keywords;
    private ArrayList<Keyword> excludedKeywords;

    private JsonReader jsonReader;

    public CrawlingManager(Frame frame, Panel panel)
    {
        mainFrame = frame;
        mainPanel = panel;

        jsonReader = new JsonReader();
        jobPostList = jsonReader.readJobPost("saramin_api_sample.json");
        excludedKeywords = jsonReader.readExcludeList();

        extractKeywordsFromJobPosts();

        drawURL();
        drawKeywords();
        drawResult();
    }

    private void extractKeywordsFromJobPosts() {
        keywords = new ArrayList<>();

        for (JobPost jobPost : jobPostList) {
            extractKeywords(jobPost.getKeywords());
        }

        Collections.sort(keywords);
    }

    private void extractKeywords(List<String> newKeywords) {
        for(String keyword : newKeywords) {
            if(!checkExcluded(keyword)) {
                if (!checkDuplicate(keyword)) {
                    keywords.add(new Keyword(keyword));
                }
            }
        }
    }

    private boolean checkExcluded(String newKeyword) {
        if(excludedKeywords != null) {
            for (Keyword k : excludedKeywords) {
                if (k.compare(newKeyword))
                    return true;
            }
        }
        return false;
    }

    private boolean checkDuplicate(String newKeyword) {
        for(int i = 0; i < keywords.size(); i++) {
            if(keywords.get(i).compare(newKeyword)) {
                keywords.get(i).addCount();
                return true;
            }
        }
        return false;
    }

    private void drawURL()
    {
        urlPanel = new JPanel();
        urlPanel.setBounds(0, 0, 640, 120);
        urlPanel.setBackground(new Color(150, 150, 150));
        urlPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        JLabel urlLabel = new JLabel("URL");
        JTextField urlText = new JTextField("Enter URL...", 50);

        JButton collectButton = new JButton("Collect data");
        collectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        urlPanel.add(urlLabel);
        urlPanel.add(urlText);
        urlPanel.add(collectButton);

        mainPanel.add(urlPanel);
    }

    private void drawKeywords()
    {
        keywordPanel = new JPanel();
        keywordPanel.setBounds(0, 120, 640, 525);
        keywordPanel.setBackground(new Color(255, 255, 255));

        int len = keywords.size();
        keywordCheckboxes = new JCheckBox[len];
        collectedKeywords = new String[len];

        Box box = Box.createVerticalBox();
        for(int i = 0; i < len; i++)
        {
            collectedKeywords[i] = keywords.get(i).getKeyword() + String.format("(%d)", keywords.get(i).getCount());
            keywordCheckboxes[i] = new JCheckBox(collectedKeywords[i]);
            box.add(keywordCheckboxes[i]);
        }

        JScrollPane selectScrollPane = new JScrollPane(box);
        selectScrollPane.setBounds(0, 120, 640, 525);
        selectScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        selectScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectScrollPane.getViewport().getView().setBackground(Color.white);
        selectScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        selectScrollPane.setOpaque(false);

        JPanel keywordBottomPanel = new JPanel();
        keywordBottomPanel.setBackground(new Color(225,225,225));
        keywordBottomPanel.setBounds(0, 645, 640, 75);
        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excludedKeywords.addAll(getCheckedKeywords());
                jsonReader.writeExcludeList(excludedKeywords);
                filterRemoveKeywords(excludedKeywords);
                showJobPosts();
            }
        });
        keywordBottomPanel.add(selectButton);

        selectScrollPane.getViewport().revalidate();
        selectScrollPane.getViewport().repaint();

        mainPanel.add(selectScrollPane);
        mainPanel.add(keywordBottomPanel);
    }

    private ArrayList<Keyword> getCheckedKeywords() {
        ArrayList<Keyword> resultList = new ArrayList<>();
        for(int i = 0; i < keywordCheckboxes.length; i++) {
            if(keywordCheckboxes[i].isSelected())
                resultList.add(keywords.get(i));
        }
        return resultList;
    }

    private void drawResult()
    {
        resultPanel = new JPanel();
        resultPanel.setBounds(640, 0, 640, 645);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20 ,20));

        JPanel exportPanel = new JPanel();
        exportPanel.setBounds(640, 645, 640, 75);

        resultBox = Box.createVerticalBox();

        JScrollPane scrollPane = new JScrollPane(resultBox);
        scrollPane.setBounds(0, 120, 640, 525);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().getView().setBackground(Color.white);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);

        resultPanel.add(scrollPane);

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonReader.writeJobPostOutput(jobPostList);
            }
        });
        exportPanel.add(exportButton);

        mainPanel.add(resultPanel);
        mainPanel.add(exportPanel);

        //getDataFromSaramin();
    }

    private void getDataFromSaramin() {
        String accessKey = "83CM6TDa4Wzvt8pfilNtneviEMD83gkIJZwNSxlC0UIX8YfZdzi";

        try {
            String text = URLEncoder.encode("", "UTF-8");
            String apiURL = "https://oapi.saramin.co.kr/job-search?access-key=" + accessKey + "&bbs_gb=0&job_type=1&start=" + startIndex + "&count=110&job_mid_cd=2";

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            int responseCode = con.getResponseCode();
            BufferedReader br;

            // Success
            if(responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }

            br.close();
            System.out.println(response.toString());
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private void filterRemoveKeywords(ArrayList<Keyword> removeList) {
        for(JobPost jobPost : jobPostList) {
            for(Keyword targetKeyword : removeList) {
                try {
                    jobPost.removeKeyword(targetKeyword.getKeyword());
                }
                catch (Exception e) {
                    System.out.println("Error keyword: " + targetKeyword.getKeyword());
                    e.printStackTrace();
                }
            }
        }
    }

    private void showJobPosts() {
        int companyAmount = jobPostList.size();

        JLabel[] companySpecificDataText = new JLabel[companyAmount];
        for(int i = 0; i < companyAmount; i++)
        {
            JobPost post = jobPostList.get(i);
            String jobPostStr = String.format("%s (%s): ", post.getCompanyName(), post.getPosition());
            companySpecificDataText[i] = new JLabel();
            companySpecificDataText[i].setFont(new Font("D2Coding", Font.PLAIN, 12));

            List<String> keywordList = post.getKeywords();
            for(int j = 0; j < keywordList.size(); j++)
            {
                if(j == 0)
                    jobPostStr += keywordList.get(j);
                else
                    jobPostStr += ", " + keywordList.get(j);
            }
            companySpecificDataText[i].setText(jobPostStr);

            resultBox.add(companySpecificDataText[i]);
        }
        resultBox.revalidate();
        resultBox.repaint();
    }
}
