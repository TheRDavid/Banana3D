package gui.dialogs;

import general.CurrentData;
import components.BButton;
import components.BTextField;
import components.CancelButton;
import components.Checker;
import dialogs.BasicDialog;
import dialogs.ObserverDialog;
import other.Wizard;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import se.datadosen.component.RiverLayout;

public class ScreenshotDialog extends BasicDialog
{

    private PreviewLabel previewLabel = new PreviewLabel();
    private JPanel controlsPanel = new JPanel(new RiverLayout());
    private Checker countdownChecker = new Checker();
    private BTextField secondsTextField = new BTextField("integer", "Seconds");
    private Checker onlyCanvasChecker = new Checker();
    private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    private CancelButton cancelButton;
    private BButton goButton = new BButton("Go", new ImageIcon("dat//img//menu//screenshot.png"));
    private Robot robot;
    private JPanel canvas = CurrentData.getEditorWindow().getCanvasPanel();
    private JFrame window = CurrentData.getEditorWindow();
    private BufferedImage screenshot;

    public ScreenshotDialog()
    {
        countdownChecker.setChecked(false);
        countdownChecker.setTransitionSpeed(22);
        onlyCanvasChecker.setTransitionSpeed(22);
        setAlwaysOnTop(true);
        new Timer(1, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                previewLabel.repaint();
            }
        }).start();
        try
        {
            robot = new Robot();
        } catch (AWTException ex)
        {
            ObserverDialog.getObserverDialog().printError("Failed to create Robot for ScreenshotDialog", ex);
        }
        cancelButton = new CancelButton(this);
        goButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (countdownChecker.isChecked())
                {
                    try
                    {
                        try
                        {
                            for (int i = Integer.parseInt(secondsTextField.getText()); i > 0; i--)
                            {
                                previewLabel.setText(Integer.toString(i));
                                previewLabel.repaint();
                                Thread.sleep(Integer.parseInt(secondsTextField.getText()) * 1000);
                            }
                        } catch (InterruptedException ex)
                        {
                            ObserverDialog.getObserverDialog().printError("Thread.sleep in ScreenshotDialog interrupted", ex);
                        }
                        ScreenshotDialog.this.dispose();
                        createScreenshot();
                    } catch (NumberFormatException nfe)
                    {
                        JOptionPane.showMessageDialog(ScreenshotDialog.this, secondsTextField.getText() + " is not a legit number!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else
                {
                    ScreenshotDialog.this.dispose();
                    createScreenshot();
                }
            }
        });
        secondsTextField.setEnabled(false);
        countdownChecker.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                secondsTextField.setEnabled(countdownChecker.isChecked());
            }
        });
        controlsPanel.add(new JLabel("Countdown:"));
        controlsPanel.add("tab", countdownChecker);
        controlsPanel.add("tab hfill", secondsTextField);
        controlsPanel.add("br", new JLabel("Canvas Only"));
        controlsPanel.add("tab", onlyCanvasChecker);
        buttonPanel.add(goButton);
        buttonPanel.add(cancelButton);
        add(previewLabel, BorderLayout.CENTER);
        add(controlsPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        setModal(false);
        setTitle("Take Screenshot");
        setSize(800, 600);
        setVisible(true);
    }

    private void createScreenshot()
    {
        BufferedImage selectedScreenshot = screenshot;
        JFileChooser jfc = new JFileChooser();
        if (jfc.showSaveDialog(ScreenshotDialog.this) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                if (!jfc.getSelectedFile().getAbsolutePath().endsWith(".png"))
                {
                    ImageIO.write(selectedScreenshot, "png", new File(jfc.getSelectedFile().getAbsolutePath() + ".png"));
                } else
                {
                    ImageIO.write(selectedScreenshot, "png", jfc.getSelectedFile());
                }
            } catch (IOException ex)
            {
                ObserverDialog.getObserverDialog().printError("Error writing screenshot into file", ex);
            }
        }
    }

    private class PreviewLabel extends JLabel
    {

        private String txt = "Low Quality Preview";

        @Override
        public void paint(Graphics g)
        {
            if (onlyCanvasChecker.isChecked())
            {
                screenshot = robot.createScreenCapture(
                        new Rectangle(canvas.getLocationOnScreen(), canvas.getSize()));
            } else
            {
                screenshot = robot.createScreenCapture(
                        new Rectangle(window.getLocationOnScreen(), window.getSize()));
            }
            g.drawImage(Wizard.resizeImageI(screenshot, getWidth(), getHeight()), 0, 0, null);
            g.setColor(Color.cyan);
            g.setFont(new Font("Calibri", Font.PLAIN, 30));
            g.drawString(txt, getWidth() / 2 - g.getFontMetrics().stringWidth(txt) / 2, getHeight() - 10);
        }
    }
}
