package components;

import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author David
 */
public class StatusBar extends JPanel
{

    private static StatusBar statusBar = new StatusBar();

    public static StatusBar getStatusBar()
    {
        return statusBar;
    }
    private JLabel messageLabel = new JLabel();
    private Date date = new Date();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:  ");

    private StatusBar()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(messageLabel);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
    }

    public void setMessage(String message)
    {
        messageLabel.setText(sdf.format(date) + "  " + message);
    }
}