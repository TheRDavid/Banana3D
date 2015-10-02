package gui.menu;

import gui.dialogs.ShortCutListDialog;
import dialogs.AboutDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class HelpMenu extends JMenu
{

    private JMenuItem viewShortscutsItem = new JMenuItem("List Shortcuts",new ImageIcon("dat//img//menu//shortcut.png"));
    private JMenuItem visitOnSourceForgeItem = new JMenuItem("Visit Homepage",new ImageIcon("dat//img//menu//homepage.png"));
    private JMenuItem aboutItem = new JMenuItem("About",new ImageIcon("dat//img//menu//about.png"));

    public HelpMenu()
    {
        setText("Help");
        add(viewShortscutsItem);
        add(visitOnSourceForgeItem);
        add(aboutItem);
        initActions();
    }

    private void initActions()
    {
        viewShortscutsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                new ShortCutListDialog();
            }
        });
        aboutItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new AboutDialog();
            }
        });
    }
}
