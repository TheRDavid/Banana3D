package gui.components;

import gui.elementTree.ElementTree;
import components.RoundBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.search.TreeSearchable;

/**
 * Textfield that searches through an ElementTree
 *
 * @author David
 */
public class SearchTextField extends JXTextField
{

    /**
     * New SearchTextField.
     *
     * @param tree that contains the elements the user can search for.
     */
    public SearchTextField(final ElementTree tree)
    {
        setText("Search...");
        setFont(new Font("TimesNewRoman", Font.ITALIC, 14));
        //Default text is light gray
        setForeground(Color.LIGHT_GRAY);
        addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                //Highlight text, select all, change font
                setForeground(Color.WHITE);
                SearchTextField.this.selectAll();
                setFont(new Font("Tahoma", Font.PLAIN, 14));
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                //Back to default color, text & font
                setForeground(Color.LIGHT_GRAY);
                setFont(new Font("TimesNewRoman", Font.ITALIC, 14));
                setText("Search...");
            }
        });
        addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    //Search
                    new TreeSearchable(tree).search(SearchTextField.this.getText());
                }
            }
        });
        setBorder(new RoundBorder());
    }
}
