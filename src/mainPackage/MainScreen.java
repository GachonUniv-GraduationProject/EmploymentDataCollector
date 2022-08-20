package mainPackage;

import java.awt.*;

public class MainScreen {

    private static Frame mainFrame;
    private static Panel mainPanel;

    public static void main(String[] args)
    {
        mainFrame = new Frame("Employment Data Collector");
        mainPanel = new Panel();
        mainPanel.setLayout(null);

        mainFrame.setSize(1280, 720);

        mainPanel.setBackground(new Color(200, 200, 200));
        mainPanel.setSize(mainFrame.getWidth(), mainFrame.getHeight());
        mainFrame.add((mainPanel));

        CrawlingManager crawlingManager = new CrawlingManager(mainFrame, mainPanel);

        mainFrame.setVisible(true);
        mainFrame.setResizable(false);
        mainFrame.addWindowListener(new MainEventManager());
    }

}
