package mainPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Visibility;
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

    private JPanel collectPanel;
    private JPanel keywordPanel;
    private JCheckBox[] keywordCheckboxes;

    private JPanel resultPanel;
    private Box resultBox;

    private String[] collectedKeywords;

    private int startIndex;
    private int count;

    private ArrayList<JobPost> jobPostList;
    private ArrayList<Keyword> keywords;
    private ArrayList<Keyword> excludedKeywords;

    private JsonReader jsonReader;

    private JTextField startIndexText;
    private JTextField countText;

    public CrawlingManager(Frame frame, Panel panel)
    {
        mainFrame = frame;
        mainPanel = panel;

        jsonReader = new JsonReader();

        drawCollect();
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

    private void drawCollect()
    {
        collectPanel = new JPanel();
        collectPanel.setBounds(0, 0, 480, 100);
        collectPanel.setBackground(new Color(120, 152, 255));
        collectPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        collectPanel.setLayout(null);

        JLabel startIndexLabel = new JLabel("start index");
        startIndexLabel.setFont(new Font("D2Coding", Font.PLAIN, 18));
        startIndexLabel.setForeground(Color.white);
        startIndexLabel.setBounds(35, 20, 100, 20);

        JLabel countLabel = new JLabel("count");
        countLabel.setFont(new Font("D2Coding", Font.PLAIN, 18));
        countLabel.setBounds(215, 20, 45, 20);
        countLabel.setForeground(Color.white);

        startIndexText = new JTextField("0", 5);
        startIndexText.setFont(new Font("D2Coding", Font.PLAIN, 18));
        startIndexText.setBounds(35, 55, 100, 25);
        startIndexText.setForeground(Color.black);

        countText = new JTextField("100", 5);
        countText.setFont(new Font("D2Coding", Font.PLAIN, 18));
        countText.setBounds(190, 55, 100, 25);
        countText.setForeground(Color.black);

        ImageIcon collectImg = new ImageIcon("./res/collect_button.png");
        ImageIcon collectPressedImg = new ImageIcon("./res/collect_button_pressed.png");

        JButton collectButton = new JButton(collectImg);
        collectButton.setPressedIcon(collectPressedImg);
        collectButton.setContentAreaFilled(false);
        collectButton.setBorderPainted(false);
        collectButton.setFocusPainted(false);
        collectButton.setOpaque(false);
        collectButton.setFont(new Font("D2Coding", Font.PLAIN, 18));
        collectButton.setBounds(330, 33, 110, 45);
        collectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startIndex = Integer.parseInt(startIndexText.getText());
                count = Integer.parseInt(countText.getText());
                jobPostList = getDataFromSaramin();
                excludedKeywords = jsonReader.readExcludeList();

                extractKeywordsFromJobPosts();
                drawKeywords();
            }
        });

        collectPanel.add(startIndexLabel);
        collectPanel.add(countLabel);
        collectPanel.add(startIndexText);
        collectPanel.add(countText);
        collectPanel.add(collectButton);

        mainPanel.add(collectPanel);
    }

    private void drawKeywords()
    {
        keywordPanel = new JPanel();
        keywordPanel.setBounds(0, 100, 480, 545);
        keywordPanel.setBackground(new Color(235, 241, 251));

        int len = keywords.size();
        keywordCheckboxes = new JCheckBox[len];
        collectedKeywords = new String[len];

        keywordPanel.setLayout(null);

        ImageIcon checkboxUncheckedImg = new ImageIcon("./res/checkbox_unchecked.png");
        ImageIcon checkboxCheckedImg = new ImageIcon("./res/checkbox_checked.png");

        Box box = Box.createVerticalBox();
        box.setBackground(new Color(235, 241, 251));
        for(int i = 0; i < len; i++)
        {
            collectedKeywords[i] = keywords.get(i).getKeyword() + String.format(" (x%d)", keywords.get(i).getCount());
            keywordCheckboxes[i] = new JCheckBox(collectedKeywords[i]);
            if(keywords.get(i).getCount() == 1)
                keywordCheckboxes[i].setSelected(true);
            keywordCheckboxes[i].setOpaque(false);
            keywordCheckboxes[i].setIcon(checkboxUncheckedImg);
            keywordCheckboxes[i].setSelectedIcon(checkboxCheckedImg);
            keywordCheckboxes[i].setFont(new Font("D2Coding", Font.PLAIN, 15));
            keywordCheckboxes[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            box.add(keywordCheckboxes[i]);
        }

        JScrollPane selectScrollPane = new JScrollPane(box);
        selectScrollPane.setBounds(0, 100, 480, 545);
        selectScrollPane.getViewport().setBackground(new Color(235, 241, 251));
        selectScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        selectScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        ImageIcon filterButtonImg = new ImageIcon("./res/filter_button.png");
        ImageIcon filterButtonPressedImg = new ImageIcon("./res/filter_button_pressed.png");

        JPanel filterButtonPanel = new JPanel();
        filterButtonPanel.setBounds(0, 645, 480, 75);
        filterButtonPanel.setBackground(new Color(235, 241, 251));


        JButton filterButton = new JButton(filterButtonImg);
        filterButton.setPressedIcon(filterButtonPressedImg);
        filterButton.setContentAreaFilled(false);
        filterButton.setBorderPainted(false);
        filterButton.setFocusPainted(false);
        filterButton.setOpaque(false);
        filterButton.setFont(new Font("D2Coding", Font.PLAIN, 18));
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excludedKeywords.addAll(getCheckedKeywords());
                jsonReader.writeExcludeList(excludedKeywords);
                filterRemoveKeywords(excludedKeywords);
                jsonReader.makeJobPostJson(jobPostList);
                showJobPosts();
            }
        });

        selectScrollPane.getViewport().revalidate();
        selectScrollPane.getViewport().repaint();
        filterButtonPanel.add(filterButton);
        mainPanel.add(filterButtonPanel);
        mainPanel.add(selectScrollPane);
        mainPanel.revalidate();
        mainPanel.repaint();
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
        resultPanel.setBounds(480, 50, 800, 595);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(00, 0, 0 ,5));
        resultPanel.setBackground(new Color(98, 100, 112));

        JPanel exportPanel = new JPanel(null);
        exportPanel.setBounds(480, 0, 800, 50);
        exportPanel.setBackground(new Color(145, 148, 158));

        JLabel fileNameLabel = new JLabel("export.json");
        fileNameLabel.setFont(new Font("D2Coding", Font.PLAIN, 18));
        fileNameLabel.setBounds(0, 0, 175, 50);
        fileNameLabel.setForeground(Color.white);
        fileNameLabel.setBackground(new Color(98, 100, 112));
        fileNameLabel.setHorizontalAlignment(JLabel.CENTER);
        fileNameLabel.setOpaque(true);
        exportPanel.add(fileNameLabel);

        resultBox = Box.createVerticalBox();

        JScrollPane scrollPane = new JScrollPane(resultBox);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setBounds(0, 50, 800, 595);
        scrollPane.getViewport().setBackground(new Color(98, 100, 112));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().getView().setBackground(Color.white);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);

        resultPanel.add(scrollPane);

        JPanel exportButtonPanel = new JPanel();
        exportButtonPanel.setBounds(480, 645, 800, 75);
        exportButtonPanel.setBackground(new Color(145, 148, 158));

        ImageIcon exportButtonImg = new ImageIcon("./res/export_button.png");
        ImageIcon exportButtonPressedImg = new ImageIcon("./res/export_button_pressed.png");

        JButton exportButton = new JButton(exportButtonImg);
        exportButton.setPressedIcon(exportButtonPressedImg);
        exportButton.setContentAreaFilled(false);
        exportButton.setBorderPainted(false);
        exportButton.setFocusPainted(false);
        exportButton.setOpaque(false);
        exportButton.setFont(new Font("D2Coding", Font.PLAIN, 18));
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonReader.writeJobPostOutput(jobPostList, resultPanel);
            }
        });

        exportButtonPanel.add(exportButton);
        mainPanel.add(exportButtonPanel);
        mainPanel.add(exportPanel);
        mainPanel.add(resultPanel);

    }

    private ArrayList<JobPost> getDataFromSaramin() {
        String accessKey = "83CM6TDa4Wzvt8pfilNtneviEMD83gkIJZwNSxlC0UIX8YfZdzi";
        ArrayList<JobPost> result = new ArrayList<>();

        try {
            while(count > 0) {
                int countLimit = 10, countApply;
                if(count > countLimit) {
                    countApply = countLimit;
                    count -= countLimit;
                }
                else {
                    countApply = count;
                    count = 0;
                }

                String text = URLEncoder.encode("", "UTF-8");
                String apiURL = "https://oapi.saramin.co.kr/job-search?access-key=" + accessKey + "&job_type=1&start=" + startIndex + "&count=" + countApply + "&job_mid_cd=2";
                System.out.println("URL : " + apiURL);

                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");

                int responseCode = con.getResponseCode();
                BufferedReader br;

                // Success
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                br.close();
                System.out.println(response);

                startIndex += countApply;
                ArrayList<JobPost> postList = jsonReader.convertJobPost(response.toString());
                /*if(postList.size() < countApply) {
                    count += countApply - postList.size();
                }*/
                result.addAll(postList);
                Thread.sleep(1000);
            }
            return result;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(JsonReader.jobPostJsonOutput);
        json = "<html><pre style='font: Consolas;'>" + json + "</pre></html>";
        System.out.println(json);

        JLabel jsonLabel = new JLabel(json);
        jsonLabel.setFont(new Font("D2Coding", Font.PLAIN, 15));
        jsonLabel.setForeground(Color.white);
        resultBox.add(jsonLabel);
        resultBox.revalidate();
        resultBox.repaint();
    }
}
