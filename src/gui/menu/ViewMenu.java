package gui.menu;

import gui.dialogs.GridOptionsDialog;
import general.CurrentData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class ViewMenu extends JMenu
{

    private JCheckBoxMenuItem showAssetBrowserItem = new JCheckBoxMenuItem("Asset Browser",new ImageIcon("dat//img//menu//assets.png"));
    private JCheckBoxMenuItem fullscreenItem = new JCheckBoxMenuItem("Fullscreen",new ImageIcon("dat//img//menu//fullscreen.png"));
    private JMenuItem gridOptionsItem = new JMenuItem("Grid Options",new ImageIcon("dat//img//menu//grid.png"));
    private JMenuItem addCameraItem = new JMenuItem("Add Camera",new ImageIcon("dat//img//menu//camera.png"));
    private JMenuItem changeFPSItem = new JMenuItem("Set FPS",new ImageIcon("dat//img//menu//fps.png"));

    public ViewMenu()
    {
        initShortcuts();
        fullscreenItem.setSelected(CurrentData.getConfiguration().fullscreen);
        showAssetBrowserItem.setSelected(CurrentData.getConfiguration().assetbrowsershown);
        setText("View");
        add(showAssetBrowserItem);
        add(fullscreenItem);
        add(gridOptionsItem);
        add(changeFPSItem);
        add(addCameraItem);
        initActions();
    }

    private void initActions()
    {
        changeFPSItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            { 
                int fps = Integer.parseInt(JOptionPane.showInputDialog(null, "Set Maximum FPS", CurrentData.getEditorWindow().getB3DApp().getSettings().getFrameRate()));
                CurrentData.getEditorWindow().getB3DApp().getSettings().setFrameRate(fps);
                CurrentData.getEditorWindow().getB3DApp().restart();
            }
        });
        addCameraItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getEditorWindow().getB3DApp().createCam();
            }
        });
        gridOptionsItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new GridOptionsDialog();
            }
        });
        fullscreenItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execFullscreen();
            }
        });
        showAssetBrowserItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getAssetBrowserDialog().setVisible(showAssetBrowserItem.isSelected());
                CurrentData.getConfiguration().setAssetbrowsershown(showAssetBrowserItem.isSelected());
            }
        });
    }

    public JCheckBoxMenuItem getFullscreenItem()
    {
        return fullscreenItem;
    }

    public JCheckBoxMenuItem getShowAssetBrowserItem()
    {
        return showAssetBrowserItem;
    }

    public JMenuItem getGridOptionsItem()
    {
        return gridOptionsItem;
    }

    public JMenuItem getAddCameraItem()
    {
        return addCameraItem;
    }

    private void initShortcuts()
    {
        fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
    }
}
