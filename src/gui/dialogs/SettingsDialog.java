package gui.dialogs;

import com.jme3.system.AppSettings;
import components.BTextField;
import components.Checker;
import components.OKButton;
import dialogs.BasicDialog;
import files.Configuration;
import general.CurrentData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public class SettingsDialog extends BasicDialog
{

    private JTabbedPane tabbedPane = new JTabbedPane();
    private ViewPanel viewPanel = new ViewPanel();
    private BehaviourPanel behaviourPanel = new BehaviourPanel();
    private OKButton saveButton = new OKButton("Apply");

    public SettingsDialog()
    {
        setLayout(new RiverLayout());
        tabbedPane.addTab("View", viewPanel);
        tabbedPane.addTab("Behaviour", behaviourPanel);
        add("hfill", tabbedPane);
        add("br right", saveButton);
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getConfiguration().setColorDepth(Integer.parseInt(viewPanel.colorDepthField.getText()));
                CurrentData.getConfiguration().setDefaultEditorSize(Integer.parseInt(viewPanel.editorWidthField.getText()), Integer.parseInt(viewPanel.editorHeightField.getText()));
                CurrentData.getConfiguration().setDepthBits(Integer.parseInt(viewPanel.depthBitsField.getText()));
                CurrentData.getConfiguration().setExitwithoutprompt(behaviourPanel.exitWithoutPromptChecker.isChecked());
                CurrentData.getConfiguration().setFramerate(Integer.parseInt(viewPanel.fpsField.getText()));
                switch (behaviourPanel.speedComboBox.getSelectedIndex())
                {
                    case 0:
                        CurrentData.getConfiguration().setGuiSPeed(Configuration.SLOW_GUI);
                        break;
                    case 1:
                        CurrentData.getConfiguration().setGuiSPeed(Configuration.DEFAULT_GUI);
                        break;
                    case 2:
                        CurrentData.getConfiguration().setGuiSPeed(Configuration.FAST_GUI);
                        break;
                }
                CurrentData.getConfiguration().setMutlisampling(Integer.parseInt(viewPanel.multisamplingField.getText()));
                CurrentData.getConfiguration().setvSync(viewPanel.vsyncChecker.isChecked());
                if (CurrentData.getEditorWindow().getB3DApp() != null)
                {
                    AppSettings settings = new AppSettings(true);
                    settings.setFrameRate(CurrentData.getConfiguration().framerate);
                    settings.setRenderer(AppSettings.LWJGL_OPENGL3);
                    settings.setVSync(CurrentData.getConfiguration().vSync);
                    settings.setBitsPerPixel(CurrentData.getConfiguration().colorDepth);
                    settings.setResolution(CurrentData.getEditorWindow().getPlayPanel().getWidth(), CurrentData.getEditorWindow().getPlayPanel().getHeight());
                    settings.setSamples(CurrentData.getConfiguration().mutlisampling);
                    settings.setDepthBits(CurrentData.getConfiguration().depthBits);
                    CurrentData.getEditorWindow().getB3DApp().setSettings(settings);
                    CurrentData.getEditorWindow().getB3DApp().restart();
                }
                int w = Integer.parseInt(viewPanel.editorWidthField.getText());
                int h = Integer.parseInt(viewPanel.editorHeightField.getText());
                CurrentData.getConfiguration().setEditorSize(w, h);
                CurrentData.getEditorWindow().setSize(w, h);
                CurrentData.getEditorWindow().arrangeComponentSizes();
                dispose();
            }
        });
        setTitle("Configurate");
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class ViewPanel extends JPanel
    {

        BTextField editorWidthField = new BTextField("Integer", "" + CurrentData.getConfiguration().editorWidth), editorHeightField = new BTextField("Integer", "" + CurrentData.getConfiguration().editorHeight);
        BTextField fpsField = new BTextField("Integer", CurrentData.getConfiguration().framerate + "");
        Checker vsyncChecker = new Checker();
        BTextField colorDepthField = new BTextField("Integer", CurrentData.getConfiguration().colorDepth + "");
        BTextField multisamplingField = new BTextField("Integer", CurrentData.getConfiguration().mutlisampling + "");
        BTextField depthBitsField = new BTextField("Integer", CurrentData.getConfiguration().depthBits + "");

        public ViewPanel()
        {
            vsyncChecker.setChecked(CurrentData.getConfiguration().vSync);
            setLayout(new RiverLayout());
            add(new JLabel("Default Editor Size:"));
            add("tab", editorWidthField);
            add("tab", new JLabel("x"));
            add("tab", editorHeightField);
            add("br", new JLabel("Framerate:"));
            add("tab hfill", fpsField);
            add("br", new JLabel("VSync:"));
            add("tab", vsyncChecker);
            add("br", new JLabel("Color Depth:"));
            add("tab hfill", colorDepthField);
            add("br", new JLabel("Mutlisampling:"));
            add("tab hfill", multisamplingField);
            add("br", new JLabel("Depth Bits:"));
            add("tab hfill", depthBitsField);
        }
    }

    private class BehaviourPanel extends JPanel
    {

        JComboBox<String> speedComboBox = new JComboBox<String>(new String[]
        {
            "Slow", "Default", "Fast"
        });
        Checker xmlChecker = new Checker(), exitWithoutPromptChecker = new Checker();

        public BehaviourPanel()
        {
            xmlChecker.setChecked(CurrentData.getConfiguration().saveXML);
            exitWithoutPromptChecker.setChecked(CurrentData.getConfiguration().exitwithoutprompt);
            switch (CurrentData.getConfiguration().guiSPeed)
            {
                case Configuration.DEFAULT_GUI:
                    speedComboBox.setSelectedIndex(1);
                    break;
                case Configuration.FAST_GUI:
                    speedComboBox.setSelectedIndex(1);
                    break;
            }
            setLayout(new RiverLayout());
            add(new JLabel("UI Speed:"));
            add("tab hfill", speedComboBox);
            add("br", new JLabel("Save XML:"));
            add("tab", xmlChecker);
            add("br", new JLabel("Exit Without Prompt:"));
            add("tab", exitWithoutPromptChecker);
        }
    }
}
