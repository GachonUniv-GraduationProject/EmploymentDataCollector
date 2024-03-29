package mainPackage;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Class to respond to Windows events
 * */
public class MainEventManager  extends Frame implements WindowListener {

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) { }

    /**
     * Shut down the app when user press the closing button
     * */
    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("Window close");
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent e) { }

    @Override
    public void windowDeiconified(WindowEvent e) { }

    @Override
    public void windowIconified(WindowEvent e) { }

    @Override
    public void windowOpened(WindowEvent e) { }
}
