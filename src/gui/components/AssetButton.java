package gui.components;

import gui.dialogs.AssetChooserDialog;
import components.BButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author David
 */
public class AssetButton extends BButton
{

    private AssetChooserDialog chooser;
    private AssetType assetType;
    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem removeItem = new JMenuItem("Remove");
    private String fileName;

    public AssetButton()
    {
    }

    private void initPopupMenu()
    {
        popupMenu.add(removeItem);
        removeItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fileName = null;
                if (chooser != null)
                    chooser.setSelectedAssetName(null);
                AssetButton.this.setText("Select " + assetType.toString());
            }
        });
    }

    public enum AssetType
    {

        Texture,
        Texture_DDS,
        Sound,
        Model,
        Sky
    }

    /**
     *
     * @param type
     */
    public AssetButton(AssetType type)
    {
        this(type, null);
    }

    /**
     *
     * @param type
     * @param fName
     */
    public AssetButton(AssetType type, String fName)
    {
        fileName = fName;
        assetType = type;
        if (fName == null)
        {
            setText("Select " + assetType.toString());
        } else
        {
            setText(fName);
        }
        addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    chooser = new AssetChooserDialog(assetType, true);
                    if (chooser.getSelectedAssetName() != null)
                    {
                        setText(chooser.getSelectedAssetName());
                    }
                } else
                {
                    if (fileName != null)
                    {
                        popupMenu.show(AssetButton.this, e.getPoint().x, e.getPoint().y);
                    }
                }
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
        initPopupMenu();
    }

    public AssetType getAssetType()
    {
        return assetType;
    }

    public void setAssetType(AssetType at)
    {
        assetType = at;
        chooser = new AssetChooserDialog(assetType, false);
    }

    public AssetChooserDialog getAssetChooser()
    {
        return chooser;
    }
}
