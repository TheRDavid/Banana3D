 package gui.dialogs;

import general.CurrentData;
import components.BButton;
import components.BTextField;
import components.CancelButton;
import components.OKButton;
import dialogs.BasicDialog;
import general.Preference;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class FieldOfViewDialog extends BasicDialog
{

    private BTextField nearTextField;
    private BTextField farTextField;
    private BTextField leftTextField;
    private BTextField rightTextField;
    private BTextField topTextField;
    private BTextField bottomTextField;
    private BButton resetButton = new BButton("Default");
    private OKButton applyButton = new OKButton("Apply");
    private CancelButton cancelButton;

    public FieldOfViewDialog()
    {
        setLayout(new RiverLayout(5, 5));
        nearTextField = new BTextField("Float", Float.toString(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumNear()));
        farTextField = new BTextField("Float", Float.toString(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumFar()));
        leftTextField = new BTextField("Float", Float.toString(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumLeft()));
        rightTextField = new BTextField("Float", Float.toString(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumRight()));
        topTextField = new BTextField("Float", Float.toString(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumTop()));
        bottomTextField = new BTextField("Float", Float.toString(CurrentData.getEditorWindow().getB3DApp().getCamera().getFrustumBottom()));
        cancelButton = new CancelButton(this);
        applyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                float[] fov =
                {
                    Float.parseFloat(nearTextField.getText()),
                    Float.parseFloat(farTextField.getText()),
                    Float.parseFloat(leftTextField.getText()),
                    Float.parseFloat(rightTextField.getText()),
                    Float.parseFloat(topTextField.getText()),
                    Float.parseFloat(bottomTextField.getText())
                };
                CurrentData.getEditorWindow().getB3DApp().getCamera().setFrustum(
                        fov[0], fov[1], fov[2], fov[3], fov[4], fov[5]);
                CurrentData.getPrefs().set(Preference.FIELD_OF_VIEW, fov);
                CurrentData.getPrefs().save();
                dispose();
            }
        });
        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                bottomTextField.setText("" + CurrentData.getFieldOfView()[0]);
                farTextField.setText("" + CurrentData.getFieldOfView()[1]);
                leftTextField.setText("" + CurrentData.getFieldOfView()[2]);
                nearTextField.setText("" + CurrentData.getFieldOfView()[3]);
                rightTextField.setText("" + CurrentData.getFieldOfView()[4]);
                topTextField.setText("" + CurrentData.getFieldOfView()[5]);
            }
        });
        setTitle("Set Field Of View");
        add("br left", new JLabel("Near: "));
        add("tab hfill", nearTextField);
        add("br left", new JLabel("Far: "));
        add("tab hfill", farTextField);
        add("br left", new JLabel("Left: "));
        add("tab hfill", leftTextField);
        add("br left", new JLabel("Right: "));
        add("tab hfill", rightTextField);
        add("br left", new JLabel("Top: "));
        add("tab hfill", topTextField);
        add("br left", new JLabel("Bottom: "));
        add("tab hfill", bottomTextField);
        add("br left", resetButton);
        add("hfill", applyButton);
        add(cancelButton);
        pack();
        setSize(getWidth() + 80, getHeight());
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
