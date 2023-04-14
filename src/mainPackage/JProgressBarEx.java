package mainPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Frame containing a program bar
 * */
public class JProgressBarEx extends JFrame {
    /**
     * Progress bar to show progress
     * */
    private JProgressBar progressBar;
    /**
     * Minimum of progress bar
     * */
    private int min;
    /**
     * Maximum of progress bar
     * */
    private int max;
    /**
     * Value of progress bar
     * */
    private int value;

    public JProgressBarEx(int min, int max) {
        super("Collecting progress");
        Container con = getContentPane();

        // Create the progress bar instance
        this.min = min;
        this.max = max;
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, min, max);

        // Set the container layout
        con.setLayout(new BorderLayout());
        con.add("North", new JLabel("Job post collecting..."));
        con.add("Center", progressBar);

        // Set the progress bar string
        progressBar.setStringPainted(true);
        progressBar.setString("0%");

        // Set the frame to be created in the middle of the screen
        setLocationRelativeTo(null);

        // Set the frame size and show
        setSize(300, 100);
        setVisible(true);

        // Start progress bar sync
        runProgressBar();
    }

    /**
     * Set the value of progress bar
     * */
    public void setValue(int newVal) {
        value = newVal;
    }

    /**
     * Hide the progress bar
     * */
    public void dismiss() {
        setVisible(false);
    }

    /**
     * Start progress bar synchronization
     * */
    private void runProgressBar() {
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while(value < progressBar.getMaximum())
                {
                    progressBar.setValue(value);
                    int ratio = 100 * value / max;
                    progressBar.setString(ratio + "%");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {}
                }
                return null;
            }
        };

        worker.execute();
    }
}
