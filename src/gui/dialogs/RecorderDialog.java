package gui.dialogs;

import general.CurrentData;
import com.jme3.app.state.VideoRecorderAppState;
import components.BButton;
import components.BTextField;
import components.BToggleButton;
import components.CancelButton;
import dialogs.BasicDialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import se.datadosen.component.RiverLayout;

public class RecorderDialog extends BasicDialog
{

    private BTextField fileField = new BTextField("String", "record " + new SimpleDateFormat("dd.MM - HH mm").format(new Date()) + ".avi");
    private JSpinner fpsSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 60, 1)), qualitySpinner = new JSpinner(new SpinnerNumberModel(80, 10, 100, 1));
    private CancelButton cancelButton = new CancelButton(this);
    private BToggleButton recordButton = new BToggleButton("Record",new ImageIcon("dat//img//menu//camera.png"), false);
    private BButton browseButton = new BButton("Browse", new ImageIcon("dat//img//menu//open.png"));
    private boolean fileChosen = false;

    public RecorderDialog()
    {
        fileField.setPreferredSize(new Dimension(210, 20));
        fileField.setEditable(false);
        fpsSpinner.setPreferredSize(new Dimension(120, 20));
        qualitySpinner.setPreferredSize(new Dimension(120, 20));
        recordButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (recordButton.isSelected())
                {
                    recordButton.setText("Stop");
                    cancelButton.setEnabled(false);
                    browseButton.setEnabled(false);
                    fpsSpinner.setEnabled(false);
                    qualitySpinner.setEnabled(false);
                    setDefaultCloseOperation(BasicDialog.DO_NOTHING_ON_CLOSE);
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            float quality = Float.parseFloat(qualitySpinner.getValue().toString()) / 100;
                            int framerate = Integer.parseInt(fpsSpinner.getValue().toString());
                            if (fileChosen)
                            {
                                CurrentData.getEditorWindow().getB3DApp().getStateManager().attach(
                                        new VideoRecorderAppState(new File(fileField.getText()),
                                        quality, framerate));
                            } else
                            {
                                CurrentData.getEditorWindow().getB3DApp().getStateManager().attach(
                                        new VideoRecorderAppState(new File(CurrentData.getProject().getMainFolder().getAbsolutePath() + "//" + fileField.getText()),
                                        quality, framerate));
                            }
                            return null;
                        }
                    });
                } else
                {
                    recordButton.setText("Record");
                    cancelButton.setEnabled(true);
                    browseButton.setEnabled(true);
                    fpsSpinner.setEnabled(true);
                    qualitySpinner.setEnabled(true);
                    CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                    {
                        @Override
                        public Void call() throws Exception
                        {
                            CurrentData.getEditorWindow().getB3DApp().getStateManager().detach(
                                    CurrentData.getEditorWindow().getB3DApp().getStateManager().getState(VideoRecorderAppState.class));
                            return null;
                        }
                    });
                    setDefaultCloseOperation(BasicDialog.DISPOSE_ON_CLOSE);
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
                        return (f.isDirectory() || f.getAbsolutePath().endsWith(".avi"));
                    }

                    @Override
                    public String getDescription()
                    {
                        return "AVI Video File";
                    }
                });
                jfc.setMultiSelectionEnabled(false);
                jfc.setDialogTitle("Save Video at...");
                jfc.setDialogType(JFileChooser.SAVE_DIALOG);
                jfc.showSaveDialog(CurrentData.getEditorWindow());
                fileChosen = (jfc.getSelectedFile().getAbsolutePath() != null);
                String absolutePath = jfc.getSelectedFile().getAbsolutePath();
                if (!absolutePath.endsWith(".avi"))
                {
                    absolutePath += ".avi";
                }
                fileField.setText(absolutePath);
            }
        });
        setTitle("Record Video");
        setLayout(new RiverLayout());
        add("left", new JLabel("File:"));
        add("tab", fileField);
        add("tab", browseButton);
        add("br", new JLabel("Framerate:"));
        add("tab", fpsSpinner);
        add("br", new JLabel("Quality:"));
        add("tab", qualitySpinner);
        add("br right", recordButton);
        add("right", cancelButton);
        pack();
        setModal(false);
        setAlwaysOnTop(true);
        setLocation(0, 0);
        setVisible(true);
    }
}
