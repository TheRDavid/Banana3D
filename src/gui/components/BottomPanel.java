package gui.components;

import java.awt.BorderLayout;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author David
 */
public class BottomPanel extends JPanel
{

    private boolean animateMode = false;
    private static BottomPanel panel = new BottomPanel();
    private StatusBar statusBar = new StatusBar();
    private AnimationPanel animPanel = new AnimationPanel();

    public static BottomPanel getPanel()
    {
        return panel;
    }

    private BottomPanel()
    {
        add(statusBar, BorderLayout.WEST);
    }

    class StatusBar extends JPanel
    {

        private JLabel messageLabel = new JLabel();
        private Date date = new Date();
        private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:  ");

        public StatusBar()
        {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(messageLabel);
            messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        }
    }

    public void setMessage(String message)
    {
        statusBar.messageLabel.setText(statusBar.sdf.format(statusBar.date) + "  " + message);
    }

    public boolean isAnimateMode()
    {
        return animateMode;
    }

    public void setAnimateMode(boolean animateMode)
    {
        this.animateMode = animateMode;
        if (animateMode)
        {
            remove(statusBar);
            add(animPanel);
        }else
        {
            remove(animPanel);
            add(statusBar);
        }
    }
    class AnimationPanel extends JPanel
    {
    
    }
}
