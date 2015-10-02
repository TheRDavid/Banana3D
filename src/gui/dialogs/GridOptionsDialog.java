package gui.dialogs;

import general.CurrentData;
import com.jme3.scene.debug.Grid;
import components.BTextField;
import components.CancelButton;
import components.OKButton;
import dialogs.BasicDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.JLabel;
import se.datadosen.component.RiverLayout;

public class GridOptionsDialog extends BasicDialog
{

    private BTextField xLinesField = new BTextField("Integer",""+ CurrentData.getConfiguration().gridx);
    private BTextField yLinesField = new BTextField("Integer", ""+ CurrentData.getConfiguration().gridy);
    private BTextField gridGapField = new BTextField("Float", ""+ CurrentData.getConfiguration().gridgap);
    private OKButton okButton = new OKButton("Ok");
    private CancelButton cancelButton;

    public GridOptionsDialog()
    {
        setLayout(new RiverLayout(10, 6));
        cancelButton = new CancelButton(this);
        add("left", new JLabel("Gap width:"));
        add("tab hfill", gridGapField);
        add("br left", new JLabel("Lines on x:"));
        add("tab hfill", xLinesField);
        add("br left", new JLabel("Lines on y:"));
        add("tab hfill", yLinesField);
        add("br", okButton);
        add(cancelButton);
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getConfiguration().setGridx(Integer.parseInt(xLinesField.getText()));
                CurrentData.getConfiguration().setGridy(Integer.parseInt(yLinesField.getText()));
                CurrentData.getConfiguration().setGridgap(Float.parseFloat(gridGapField.getText()));
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        CurrentData.getEditorWindow().getB3DApp().getGridGeometry().setMesh(new Grid(
                                Integer.parseInt(xLinesField.getText()),
                                Integer.parseInt(yLinesField.getText()),
                                Float.parseFloat(gridGapField.getText())));
                        CurrentData.getEditorWindow().getB3DApp().correctGridLocation();
                        return null;
                    }
                });
                dispose();
            }
        });
        setTitle("Grid Options");
        setResizable(false);
        setModal(false);
        setAlwaysOnTop(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}