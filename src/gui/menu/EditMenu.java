package gui.menu;

import general.CurrentData;
import general.UserActionManager;
import other.Wizard;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.*;

public class EditMenu extends JMenu
{

    private JMenuItem undoItem = new JMenuItem("Undo", new ImageIcon("dat//img//menu//undo.png"));
    private JMenuItem redoItem = new JMenuItem("Redo", new ImageIcon("dat//img//menu//redo.png"));
    private JMenuItem twinItem = new JMenuItem("Duplicate", new ImageIcon("dat//img//menu//duplicate.png"));
    private JMenuItem deleteItem = new JMenuItem("Delete", new ImageIcon("dat//img//menu//delete.png"));
    private JMenuItem animateItem = new JMenuItem("Edit Animations", new ImageIcon("dat//img//menu//random.png"));
    private JMenuItem viewPortColorSelectionItem = new JMenuItem("ViewPort Color", new ImageIcon("dat//img//menu//color.png"));
    private JMenuItem findItem = new JMenuItem("Find", new ImageIcon("dat//img//menu//search.png"));
    private JMenuItem resetSceneItem = new JMenuItem("Reset Scene", new ImageIcon("dat//img//menu//reset.png"));

    public EditMenu()
    {
        undoItem.setEnabled(false);
        redoItem.setEnabled(false);
        initShortcuts();
        setText("Edit");
        add(undoItem);
        add(redoItem);
        add(twinItem);
        add(deleteItem);
        add(animateItem);
        add(viewPortColorSelectionItem);
        add(findItem);
        add(new JSeparator());
        add(resetSceneItem);
        initActions();
    }

    private void initActions()
    {
        undoItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UserActionManager.undo();
            }
        });
        redoItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                UserActionManager.redo();
            }
        });
        animateItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getAnimationScriptDialog().setVisible(true);
                CurrentData.getAnimationScriptDialog().requestFocus();
            }
        });
        viewPortColorSelectionItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Color vPortColor = new JColorChooser().showDialog(EditMenu.this, "ViewPort Color", Wizard.makeColor(CurrentData.getEditorWindow().getB3DApp().getViewPort().getBackgroundColor()));
                CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        CurrentData.getEditorWindow().getB3DApp().getViewPort().setBackgroundColor(Wizard.makeColorRGBA(vPortColor));
                        return null;
                    }
                });
            }
        });
        findItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execFind();
            }
        });
        deleteItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execDelete(true);
            }
        });
        resetSceneItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.execReset(true);
            }
        });
    }

    private void initShortcuts()
    {
        twinItem.setAccelerator(KeyStroke.getKeyStroke("control T"));
        deleteItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        findItem.setAccelerator(KeyStroke.getKeyStroke("control F"));
    }

    public JMenuItem getUndoItem()
    {
        return undoItem;
    }

    public JMenuItem getRedoItem()
    {
        return redoItem;
    }
}