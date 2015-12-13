package gui.dialogs;

import com.jme3.system.AppSettings;
import components.BTextField;
import components.Checker;
import components.OKButton;
import dialogs.BasicDialog;
import files.Configuration;
import general.CurrentData;
import general.Preference;
import java.awt.Dimension;
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
                int w = Integer.parseInt(viewPanel.editorWidthField.getText());
                int h = Integer.parseInt(viewPanel.editorHeightField.getText());
                CurrentData.getPrefs().set(Preference.COLOR_DEPTH, Integer.parseInt(viewPanel.colorDepthField.getText()));
                CurrentData.getPrefs().set(Preference.EDITOR_WINDOW_SIZE, new Dimension(w, h));
                CurrentData.getPrefs().set(Preference.DEPTH_BITS, Integer.parseInt(viewPanel.depthBitsField.getText()));
                CurrentData.getPrefs().set(Preference.EXIT_WITHOUT_PROMPT, behaviourPanel.exitWithoutPromptChecker.isChecked());
                CurrentData.getPrefs().set(Preference.FRAMERATE, Integer.parseInt(viewPanel.fpsField.getText()));
                switch (behaviourPanel.speedComboBox.getSelectedIndex())
                {
                    case 0:
                        CurrentData.getPrefs().set(Preference.GUI_SPEED, CurrentData.GUI_SLOW);
                        break;
                    case 1:
                        CurrentData.getPrefs().set(Preference.GUI_SPEED, CurrentData.GUI_DEFAULT);
                        break;
                    case 2:
                        CurrentData.getPrefs().set(Preference.GUI_SPEED, CurrentData.GUI_FAST);
                        break;
                }
                CurrentData.getPrefs().set(Preference.MULTISAMPLING, Integer.parseInt(viewPanel.multisamplingField.getText()));
                CurrentData.getPrefs().set(Preference.VSYNC, viewPanel.vsyncChecker.isChecked());
                if (CurrentData.getEditorWindow().getB3DApp() != null)
                {
                    AppSettings settings = new AppSettings(true);
                    settings.setFrameRate((Integer) CurrentData.getPrefs().get(Preference.FRAMERATE));
                    settings.setRenderer(AppSettings.LWJGL_OPENGL3);
                    settings.setVSync((Boolean) CurrentData.getPrefs().get(Preference.VSYNC));
                    settings.setBitsPerPixel((Integer) CurrentData.getPrefs().get(Preference.COLOR_DEPTH));
                    settings.setResolution(CurrentData.getEditorWindow().getCanvasPanel().getWidth(), CurrentData.getEditorWindow().getCanvasPanel().getHeight());
                    settings.setSamples((Integer) CurrentData.getPrefs().get(Preference.MULTISAMPLING));
                    settings.setDepthBits((Integer) CurrentData.getPrefs().get(Preference.DEPTH_BITS));
                    CurrentData.getEditorWindow().getB3DApp().setSettings(settings);
                    CurrentData.getEditorWindow().getB3DApp().restart();
                }
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

        BTextField editorWidthField = new BTextField(
                "Integer", "" + ((Dimension) CurrentData.getPrefs().get(Preference.EDITOR_WINDOW_SIZE)).width),
                editorHeightField = new BTextField(
                "Integer", "" + ((Dimension) CurrentData.getPrefs().get(Preference.EDITOR_WINDOW_SIZE)).height);
        BTextField fpsField = new BTextField("Integer", CurrentData.getPrefs().get(Preference.FRAMERATE) + "");
        Checker vsyncChecker = new Checker();
        BTextField colorDepthField = new BTextField("Integer", CurrentData.getPrefs().get(Preference.COLOR_DEPTH) + "");
        BTextField multisamplingField = new BTextField("Integer", CurrentData.getPrefs().get(Preference.MULTISAMPLING) + "");
        BTextField depthBitsField = new BTextField("Integer", CurrentData.getPrefs().get(Preference.DEPTH_BITS) + "");

        public ViewPanel()
        {
            vsyncChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.VSYNC));
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
            xmlChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.SAVE_XML));
            exitWithoutPromptChecker.setChecked((Boolean) CurrentData.getPrefs().get(Preference.EXIT_WITHOUT_PROMPT));
            switch ((Integer) CurrentData.getPrefs().get(Preference.GUI_SPEED))
            {
                case CurrentData.GUI_DEFAULT:
                    speedComboBox.setSelectedIndex(1);
                    break;
                case CurrentData.GUI_FAST:
                    speedComboBox.setSelectedIndex(2);
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
