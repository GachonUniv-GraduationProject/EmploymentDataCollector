package mainPackage;

import java.awt.*;

/**
 * The class that starts the program
 * */
public class MainScreen {

    /**
     * Main frame of this program
     * */
    private static Frame mainFrame;
    /**
     * Main panel of main frame
     * */
    private static Panel mainPanel;

    public static void main(String[] args)
    {
        // Create the main frame and panel
        mainFrame = new Frame("Employment Data Collector");
        mainPanel = new Panel();
        mainPanel.setLayout(null);

        // Set the frame size
        mainFrame.setSize(1280, 745);

        // Set the background and size of panel and add to frame
        mainPanel.setBackground(new Color(200, 200, 200));
        mainPanel.setSize(mainFrame.getWidth(), mainFrame.getHeight());
        mainFrame.add((mainPanel));

        // create crawling manager
        CrawlingManager crawlingManager = new CrawlingManager(mainFrame, mainPanel);

        // Show it to user
        mainFrame.setVisible(true);
        // Disable window size changes
        mainFrame.setResizable(false);
        // Add windows event listener
        mainFrame.addWindowListener(new MainEventManager());
    }

}
