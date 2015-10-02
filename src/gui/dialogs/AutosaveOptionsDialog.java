package gui.dialogs;

import files.AutosaveOptions;
import general.CurrentData;
import components.BButton;
import components.BTextField;
import components.CancelButton;
import dialogs.BasicDialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import se.datadosen.component.RiverLayout;

public class AutosaveOptionsDialog extends BasicDialog
{

    private BTextField pathField = new BTextField("String", "");
    private JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
    private CancelButton cancelButton = new CancelButton(this);
    private BButton saveButton = new BButton("Save"), browseButton = new BButton("Browse");
    private JLabel minLabel = new JLabel();

    public AutosaveOptionsDialog()
    {
        minutesSpinner.getModel().setValue(CurrentData.getProject().getAutosaveOptions().getMinutesPerSave());
        pathField.setText(CurrentData.getProject().getAutosaveOptions().getPath());
        pathField.setEditable(false);
        pathField.setPreferredSize(new Dimension(220, 25));
        minutesSpinner.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                updateMinText();
            }
        });
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!pathField.getText().equals(""))
                {
                    CurrentData.getProject().setAutosaveOptions(new AutosaveOptions(pathField.getText(),
                            ((SpinnerNumberModel) minutesSpinner.getModel()).getNumber().intValue()));
                    CurrentData.getAutosaveRunnable().interrupt();
                    dispose();
                }
            }
        });
        browseButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser jfc = new JFileChooser(CurrentData.getProject().getMainFolder());
                jfc.setFileFilter(new FileFilter()
                {
                    @Override
                    public boolean accept(File f)
                    {
                        return (f.isDirectory() || f.getName().endsWith(".b3ds"));
                    }

                    @Override
                    public String getDescription()
                    {
                        return "B3D-Scene";
                    }
                });
                jfc.setDialogTitle("Select autosave file");
                jfc.setMultiSelectionEnabled(false);
                jfc.showSaveDialog(AutosaveOptionsDialog.this);
                String path = jfc.getSelectedFile().getAbsolutePath();
                if (!path.endsWith(".b3ds"))
                {
                    path += ".b3ds";
                }
                pathField.setText(path);
            }
        });
        setLayout(new RiverLayout());
        add("br left", new JLabel("Save to:"));
        add("tab", pathField);
        add(browseButton);
        add("br left", new JLabel("Save after"));
        add("tab", minutesSpinner);
        updateMinText();
        add(minLabel);
        add("br right", saveButton);
        add("right", cancelButton);
        setTitle("Autosave Options");
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void updateMinText()
    {
        if (Integer.parseInt(minutesSpinner.getValue().toString()) < 2)
        {
            minLabel.setText("minute");
        } else
        {
            minLabel.setText("minutes");
        }
    }
}
