package gui.dialogs;

import gui.components.AssetButton;
import general.CurrentData;
import components.BButton;
import components.OKButton;
import dialogs.BasicDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * A Dialog, used to select an Asset
 *
 */
public class AssetChooserDialog extends BasicDialog
{

    private JFileChooser assetChooser = new JFileChooser();
    private AssetButton.AssetType type;
    private BButton upButton = new BButton(new ImageIcon("dat//img//other//upButtonIcon.png"));
    private OKButton finishButton = new OKButton("Ok");
    private String selectedAssetName;
    private File selectedFile;

    /**
     *
     * @param t
     * @param showUp
     */
    public AssetChooserDialog(AssetButton.AssetType t, boolean showUp)
    {
        CurrentData.updateAssetRegister();
        type = t;
        setModal(true);
        assetChooser.setCurrentDirectory(CurrentData.getProject().getAssetsFolder());
        assetChooser.setControlButtonsAreShown(false);
        assetChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        assetChooser.remove(assetChooser.getComponent(0));
        assetChooser.removeChoosableFileFilter(assetChooser.getAcceptAllFileFilter());
        assetChooser.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() > 1)
                {
                    if (assetChooser.getSelectedFile() != null)
                    {
                        select();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
            }
        });
        assetChooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                if (type == AssetButton.AssetType.Model)
                {
                    return f.isDirectory() || f.getPath().toLowerCase().endsWith(".j3o") || f.getPath().toLowerCase().endsWith(".obj") | f.getName().endsWith(".scene") || f.getPath().toLowerCase().endsWith(".blend");
                } else if (type == AssetButton.AssetType.Sky)
                {
                    return f.isDirectory() || f.getPath().toLowerCase().endsWith(".sky");
                } else if (type == AssetButton.AssetType.Texture_DDS)
                {
                    return f.isDirectory() || f.getPath().toLowerCase().endsWith(".dds") || f.getPath().toLowerCase().endsWith(".png") || f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".gif");
                } else
                {
                    return f.isDirectory() || f.getPath().toLowerCase().endsWith(".png") || f.getPath().toLowerCase().endsWith(".jpg") || f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".gif");
                }
            }

            @Override
            public String getDescription()
            {
                if (type == AssetButton.AssetType.Model)
                {
                    return "Model File (.j3o, .obj, .blend, .scene)";
                } else if (type == AssetButton.AssetType.Sky)
                {
                    return "Sky";
                } else if (type == AssetButton.AssetType.Texture_DDS)
                {
                    return "Texture File (.jpg, .png, .gif) or .dds";
                } else
                {
                    return "Texture File (.jpg, .png, .gif)";
                }
            }
        });
        upButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!assetChooser.getCurrentDirectory().getName().equals("assets"))
                {
                    assetChooser.setCurrentDirectory(assetChooser.getCurrentDirectory().getParentFile());
                }
            }
        });
        finishButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                select();
            }
        });
        add(assetChooser, BorderLayout.CENTER);
        add(finishButton, BorderLayout.SOUTH);
        add(upButton, BorderLayout.NORTH);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setTitle("Select Asset");
        setVisible(showUp);
    }

    private void select()
    {
        if (assetChooser.getSelectedFile() != null)
        {
            selectedAssetName = assetChooser.getSelectedFile().getName();
            selectedFile = assetChooser.getSelectedFile();
            AssetChooserDialog.this.dispose();
        } else
        {
            JOptionPane.showMessageDialog(assetChooser, "Please choose an asset!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getSelectedAssetName()
    {
        return selectedAssetName;
    }

    /**
     *
     * @param selectedAssetName
     */
    public void setSelectedAssetName(String selectedAssetName)
    {
        this.selectedAssetName = selectedAssetName;
    }

    public File getSelectedFile()
    {
        return selectedFile;
    }
}