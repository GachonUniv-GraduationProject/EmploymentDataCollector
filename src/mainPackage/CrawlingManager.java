package mainPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

/**
 * A class that manage employment data collection feature
 * */
public class CrawlingManager {
    /**
     * Main frame of this program
     * */
    private JFrame mainFrame;
    /**
     * Main panel of main frame
     * */
    private JPanel mainPanel;

    /**
     * Panel containing the Data Collection Preset UI
     * */
    private JPanel collectPanel;

    /**
     * Button that start collecting data
     * */
    private JButton collectButton;

    /**
     * Panel showing keywords for collected data
     * */
    private JPanel keywordPanel;
    /**
     * Array of check boxes to select keywords to exclude
     * */
    private JCheckBox[] keywordCheckboxes;

    /**
     * Panel showing filtering results
     * */
    private JPanel resultPanel;
    /**
     * Box for vertical scrolling of filtering results
     * */
    private Box resultBox;

    /**
     * Array of collected keywords
     * */
    private String[] collectedKeywords;

    /**
     * Start index of job posting data to be requested to Saramin API
     * */
    private int startIndex;
    /**
     * Number of job postings to be requested by Saramin API
     * */
    private int count;
    /**
     * Total Number of job postings to be requested by Saramin API (not subtracted)
     * */
    private int totalCount;

    /**
     * Job Posting Data Received Through Saramin API
     * */
    private ArrayList<JobPost> jobPostList;
    /**
     * List of keywords extracted from job posting data
     * */
    private ArrayList<Keyword> keywords;
    /**
     * List of keywords that are excluded because they are not related to the development field
     * */
    private ArrayList<Keyword> excludedKeywords;

    /**
     * Reader instances that read to specific Json rules
     * */
    private JsonReader jsonReader;

    /**
     * TextField to receive start index
     * */
    private JTextField startIndexText;
    /**
     * TextField to receive the number of job posts to be requested by API
     * */
    private JTextField countText;

    /**
     * Progress bar frame to show data collecting progress
     * */
    private JProgressBarEx progressBarEx = null;

    public CrawlingManager(JFrame frame, JPanel panel)
    {
        mainFrame = frame;
        mainPanel = panel;

        jsonReader = new JsonReader();

        drawCollect();
        drawResult();
    }

    /**
     * Generate keyword lists from job posting data
     * */
    private void extractKeywordsFromJobPosts() {
        keywords = new ArrayList<>();

        for (JobPost jobPost : jobPostList) {
            extractKeywords(jobPost.getKeywords());
        }

        Collections.sort(keywords);
    }

    /**
     * Exclude keywords to be duplicated or excluded from the list of keywords for each job posts and add others to the list
     * */
    private void extractKeywords(List<String> newKeywords) {
        for(String keyword : newKeywords) {
            if(!checkExcluded(keyword)) {
                if (!checkDuplicate(keyword)) {
                    keywords.add(new Keyword(keyword));
                }
            }
        }
    }

    /**
     * Check if the keyword is to be excluded.
     * */
    private boolean checkExcluded(String newKeyword) {
        if(excludedKeywords != null) {
            for (Keyword k : excludedKeywords) {
                if (k.compare(newKeyword))
                    return true;
            }
        }
        return false;
    }

    /**
     * Check if the keyword is to be excluded.
     * */
    private boolean checkDuplicate(String newKeyword) {
        for(int i = 0; i < keywords.size(); i++) {
            if(keywords.get(i).compare(newKeyword)) {
                // Add the frequency of appearance of duplicate keywords
                keywords.get(i).addCount();
                return true;
            }
        }
        return false;
    }

    /**
     * Draw the pre-collection setup UI.
     * */
    private void drawCollect()
    {
        // Set up the panel(Size, Background color, Border, Layout)
        collectPanel = new JPanel();
        collectPanel.setBounds(0, 0, 480, 100);
        collectPanel.setBackground(new Color(120, 152, 255));
        collectPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        collectPanel.setLayout(null);

        // Create and set the title label to be created next to TextField
        JLabel startIndexLabel = new JLabel("start index");
        startIndexLabel.setFont(new Font("D2Coding", Font.PLAIN, 18));
        startIndexLabel.setForeground(Color.white);
        startIndexLabel.setBounds(35, 20, 100, 20);

        JLabel countLabel = new JLabel("count");
        countLabel.setFont(new Font("D2Coding", Font.PLAIN, 18));
        countLabel.setBounds(215, 20, 45, 20);
        countLabel.setForeground(Color.white);

        // Create and set text fields to be entered by the user
        startIndexText = new JTextField("0", 5);
        startIndexText.setFont(new Font("D2Coding", Font.PLAIN, 18));
        startIndexText.setBounds(35, 55, 100, 25);
        startIndexText.setForeground(Color.black);

        countText = new JTextField("100", 5);
        countText.setFont(new Font("D2Coding", Font.PLAIN, 18));
        countText.setBounds(190, 55, 100, 25);
        countText.setForeground(Color.black);

        // Load the collect button image resources
        ImageIcon collectImg = new ImageIcon("./res/collect_button.png");
        ImageIcon collectPressedImg = new ImageIcon("./res/collect_button_pressed.png");

        // Create and set buttons to start data collection.
        collectButton = new JButton(collectImg);
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
                totalCount = count;
                // Start request
                startRequestSaraminAPI();
                // Disable collect button
                collectButton.setEnabled(false);
            }
        });

        // The generated UI components are added to the panel
        collectPanel.add(startIndexLabel);
        collectPanel.add(countLabel);
        collectPanel.add(startIndexText);
        collectPanel.add(countText);
        collectPanel.add(collectButton);

        mainPanel.add(collectPanel);
    }

    /**
     * Start API requests in the background
     * */
    private void startRequestSaraminAPI() {
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                jobPostList = getDataFromSaramin();
                onSaraminLoaded();
                return null;
            }
        };

        worker.execute();
    }

    /**
     * Method that runs after requesting all data
     * */
    private void onSaraminLoaded() {
        excludedKeywords = jsonReader.readExcludeList();

        extractKeywordsFromJobPosts();
        drawKeywords();
        collectButton.setEnabled(true);
    }


    /**
     * Draw a UI to select keywords to exclude
     * */
    private void drawKeywords()
    {
        // Set up the panel(Size, Background color)
        keywordPanel = new JPanel();
        keywordPanel.setBounds(0, 100, 480, 545);
        keywordPanel.setBackground(new Color(235, 241, 251));
        keywordPanel.setLayout(null);

        // Initialize check box and keyword label arrangement
        int len = keywords.size();
        keywordCheckboxes = new JCheckBox[len];
        collectedKeywords = new String[len];

        // Load the checkbox image resources
        ImageIcon checkboxUncheckedImg = new ImageIcon("./res/checkbox_unchecked.png");
        ImageIcon checkboxCheckedImg = new ImageIcon("./res/checkbox_checked.png");

        // Create a box for vertical scrolling of check boxes
        Box box = Box.createVerticalBox();
        box.setBackground(new Color(235, 241, 251));
        for(int i = 0; i < len; i++)
        {
            // Set the content of keywords and visual settings
            collectedKeywords[i] = keywords.get(i).getKeyword() + String.format(" (x%d)", keywords.get(i).getCount());
            keywordCheckboxes[i] = new JCheckBox(collectedKeywords[i]);
            if(keywords.get(i).getCount() == 1)
                keywordCheckboxes[i].setSelected(true);
            keywordCheckboxes[i].setOpaque(false);
            keywordCheckboxes[i].setIcon(checkboxUncheckedImg);
            keywordCheckboxes[i].setSelectedIcon(checkboxCheckedImg);
            keywordCheckboxes[i].setFont(new Font("D2Coding", Font.PLAIN, 15));
            keywordCheckboxes[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            // Add them to the box
            box.add(keywordCheckboxes[i]);
        }

        // Creating and setting scroll components for vertical scrolling
        JScrollPane selectScrollPane = new JScrollPane(box);
        selectScrollPane.setBounds(0, 100, 480, 545);
        selectScrollPane.getViewport().setBackground(new Color(235, 241, 251));
        selectScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        selectScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        selectScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Load image resources for filter button
        ImageIcon filterButtonImg = new ImageIcon("./res/filter_button.png");
        ImageIcon filterButtonPressedImg = new ImageIcon("./res/filter_button_pressed.png");

        // Create and set the filter button's panel (separated from scroll pane)
        JPanel filterButtonPanel = new JPanel();
        filterButtonPanel.setBounds(0, 645, 480, 75);
        filterButtonPanel.setBackground(new Color(235, 241, 251));

        // Create and set the filter button
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
                // Add excluded list
                excludedKeywords.addAll(getCheckedKeywords());
                // Write exclude list file
                jsonReader.writeExcludeList(excludedKeywords);
                // Remove the excluded keywords
                filterRemoveKeywords(excludedKeywords);
                // Generate the json job posting data
                jsonReader.makeJobPostJson(jobPostList);
                // Show the result
                showJobPosts();
            }
        });

        // Redraw the scroll pane
        selectScrollPane.getViewport().revalidate();
        selectScrollPane.getViewport().repaint();
        // Add UI components to panel
        filterButtonPanel.add(filterButton);
        mainPanel.add(filterButtonPanel);
        mainPanel.add(selectScrollPane);
        // Redraw the main panel
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Collect the checked keywords
     * */
    private ArrayList<Keyword> getCheckedKeywords() {
        ArrayList<Keyword> resultList = new ArrayList<>();
        for(int i = 0; i < keywordCheckboxes.length; i++) {
            if(keywordCheckboxes[i].isSelected())
                resultList.add(keywords.get(i));
        }
        return resultList;
    }

    /**
     * Draw the filtered results
     * */
    private void drawResult()
    {
        // Set up the panel(Size, Background color, Border, Layout)
        resultPanel = new JPanel();
        resultPanel.setBounds(480, 50, 800, 595);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(00, 0, 0 ,5));
        resultPanel.setBackground(new Color(98, 100, 112));

        // Create and set panels for areas that look like file tabs
        JPanel exportPanel = new JPanel(null);
        exportPanel.setBounds(480, 0, 800, 50);
        exportPanel.setBackground(new Color(145, 148, 158));

        // Create the 'file tab' like label
        JLabel fileNameLabel = new JLabel("export.json");
        fileNameLabel.setFont(new Font("D2Coding", Font.PLAIN, 18));
        fileNameLabel.setBounds(0, 0, 175, 50);
        fileNameLabel.setForeground(Color.white);
        fileNameLabel.setBackground(new Color(98, 100, 112));
        fileNameLabel.setHorizontalAlignment(JLabel.CENTER);
        fileNameLabel.setOpaque(true);
        exportPanel.add(fileNameLabel);

        // Create the box for result
        resultBox = Box.createVerticalBox();

        // Creating and setting scroll components for vertical scrolling
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

        // Create and set the filter button's panel (separated from scroll pane)
        JPanel exportButtonPanel = new JPanel();
        exportButtonPanel.setBounds(480, 645, 800, 75);
        exportButtonPanel.setBackground(new Color(145, 148, 158));

        // Load the export button image resources
        ImageIcon exportButtonImg = new ImageIcon("./res/export_button.png");
        ImageIcon exportButtonPressedImg = new ImageIcon("./res/export_button_pressed.png");

        // Create the export(create file) button
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

        // Add UI components to the panel
        exportButtonPanel.add(exportButton);
        mainPanel.add(exportButtonPanel);
        mainPanel.add(exportPanel);
        mainPanel.add(resultPanel);

    }

    /**
     * Request job post data to Saramin Open API
     * */
    private ArrayList<JobPost> getDataFromSaramin() {
        // API Key for saramin API
        String accessKey = "83CM6TDa4Wzvt8pfilNtneviEMD83gkIJZwNSxlC0UIX8YfZdzi";

        // Initialize the result list
        ArrayList<JobPost> result = new ArrayList<>();
        progressBarEx = new JProgressBarEx(0, totalCount);

        try {
            // Repeat until all user created requests are made
            while(count > 0) {
                // Number of requests to API at a time
                int countLimit = 10;
                // Number of data that the API responded to
                int countApply;
                // Deduct total number of requests
                if(count > countLimit) {
                    countApply = countLimit;
                    count -= countLimit;
                    // Increase the progress bar value
                    progressBarEx.setValue(totalCount - count);
                }
                else {
                    countApply = count;
                    count = 0;
                    // Hide the progress bar
                    progressBarEx.setValue(totalCount);
                    progressBarEx.dismiss();
                }

                // Set the URL to request
                String text = URLEncoder.encode("", "UTF-8");
                String apiURL = "https://oapi.saramin.co.kr/job-search?access-key=" + accessKey + "&job_type=1&start=" + startIndex + "&count=" + countApply + "&job_mid_cd=2";
                System.out.println("URL : " + apiURL);

                // Create the HTTP connection to API Server
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");

                // Get the response code
                int responseCode = con.getResponseCode();
                BufferedReader br;

                // If success, Read the stream data from server
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                }
                // If failed, Read the error data
                else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                // Convert stream to String
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                // Close the stream
                br.close();
                System.out.println(response);

                // Move to next starting index
                startIndex += countApply;
                result = jsonReader.convertJobPost(response.toString());

                // Wait for next request
                Thread.sleep(1000);
            }
            return result;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Remove keywords to exclude from job posting data
     * */
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

    /**
     * Show the final result with json beautify
     * */
    private void showJobPosts() {
        // Beautify the json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(JsonReader.jobPostJsonOutput);
        json = "<html><pre style='font: Consolas;'>" + json + "</pre></html>";
        System.out.println(json);

        // Show the data to user
        JLabel jsonLabel = new JLabel(json);
        jsonLabel.setFont(new Font("D2Coding", Font.PLAIN, 15));
        jsonLabel.setForeground(Color.white);
        resultBox.add(jsonLabel);
        // Redraw the box
        resultBox.revalidate();
        resultBox.repaint();
    }
}
