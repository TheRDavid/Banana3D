package gui.dialogs;

import b3dElements.B3D_Element;
import b3dElements.animations.B3D_Animation;
import components.BButton;
import dialogs.BasicDialog;
import dialogs.ObserverDialog;
import general.CurrentData;
import general.Preference;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import javax.swing.border.EmptyBorder;
import other.AnimationTranslator;
import other.Wizard;

/**
 *
 * @author David
 */
public class AnimationScriptDialog extends BasicDialog implements ActionListener
{

    private ControlPane controlsPanel;
    private JTabbedPane scriptPane = new JTabbedPane();
    private String[][] commands =
    {
        new String[]
        {
            "move", "move : x, y, z : duration : startTime;"
        },
        new String[]
        {
            "scale", "scale : x, y, z : duration : startTime;"
        },
        new String[]
        {
            "rotate", "rotate : x, y, z : duration : startTime;"
        },
        new String[]
        {
            "call", "call : ID / animationName : startTime;"
        },
        new String[]
        {
            "fireParticles", "fireParticles : boolean : startTime;"
        },
        new String[]
        {
            "playMotion", "playMotion : play/stop/pause : startTime;"
        }
    };

    public AnimationScriptDialog()
    {
        setType(Type.NORMAL);
        setModal(false);
        controlsPanel = new ControlPane();
        add(controlsPanel, BorderLayout.NORTH);
        add(scriptPane, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentMoved(ComponentEvent e)
            {
                CurrentData.getPrefs().set(
                        Preference.ANIMATION_SCRIPT_DIALOG_POSITION,
                        AnimationScriptDialog.this.getLocation());
            }
        });
        setTitle("Animation Scripts");
        setSize(600, 400);
    }

    public void openTab(B3D_Element b3D_Element)
    {
        boolean alreadyOpened = false;
        for (int i = 0; i < scriptPane.getTabCount(); i++)
            if (((ScriptScrollPane) scriptPane.getComponentAt(i)).getElement() == b3D_Element)
            {
                alreadyOpened = true;
                break;
            }
        if (!alreadyOpened)
        {
            ScriptScrollPane pane = new ScriptScrollPane(b3D_Element);
            scriptPane.addTab(b3D_Element.getName(), pane);
            JPanel titleComponent = new JPanel(new FlowLayout(FlowLayout.LEFT));
            titleComponent.setOpaque(false);
            titleComponent.add(new JLabel(b3D_Element.getName()));
            titleComponent.add(new CloseTabButton(pane));
            scriptPane.setTabComponentAt(scriptPane.getTabCount() - 1, titleComponent);
            scriptPane.setSelectedIndex(scriptPane.getTabCount() - 1);
        }
    }

    private class CloseTabButton extends BButton
    {

        public CloseTabButton(final Component c)
        {
            setBorder(new EmptyBorder(1, 6, 2, 6));
            setText("x");
            addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    scriptPane.remove(c);
                }
            });
        }
    }

    private class ScriptScrollPane extends JScrollPane
    {

        private ScriptArea area;
        private B3D_Element element;

        public ScriptScrollPane(B3D_Element e)
        {
            element = e;
            area = new ScriptArea(element);
            setViewportView(area);
            setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
            setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
        }

        public ScriptArea getArea()
        {
            return area;
        }

        public B3D_Element getElement()
        {
            return element;
        }
    }

    private class ScriptArea extends JTextArea
    {

        private boolean brace = false;
        private JPopupMenu typesMenu = new JPopupMenu();
        private B3D_Element b3D_Element;

        public B3D_Element getB3D_Element()
        {
            return b3D_Element;
        }

        public ScriptArea(B3D_Element element)
        {
            addMouseWheelListener(new MouseWheelListener()
            {
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    setFont(new Font("Arial", Font.PLAIN, getFont().getSize() - e.getWheelRotation()));
                }
            });
            setFont(new Font("Arial", Font.PLAIN, 16));
            b3D_Element = element;
            setTabSize(3);
            for (B3D_Animation animation : element.getAnimations())
            {
                append(AnimationTranslator.translate(animation) + "\n");
                /* append(animation.getName() + "\n{");
                 for (B3D_AnimationCommand command : animation.getCommands())
                 append("\n\t" + AnimationTranslator.dec(command));
                 append("\n}\n\n");*/
            }
            for (String[] str : commands)
                typesMenu.add(new TypeItem(str[0], str[1]));
            setColumns(35);
            setBackground(Color.darkGray);
            setForeground(Color.LIGHT_GRAY);
            addKeyListener(new KeyListener()
            {
                public void keyTyped(KeyEvent e)
                {
                }

                public void keyPressed(KeyEvent e)
                {
                }

                public void keyReleased(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        setRows(getRows() + 1);
                        if (brace)
                        {
                            brace = false;
                            append("}");
                            setCaretPosition(getCaretPosition() - 1);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F4)
                        try
                        {
                            typesMenu.show(ScriptArea.this, ScriptArea.this.getCaret().getMagicCaretPosition().x, ScriptArea.this.getCaret().getMagicCaretPosition().y);
                        } catch (NullPointerException npe)
                        {
                            typesMenu.show(ScriptArea.this, 0, 0);
                            ObserverDialog.getObserverDialog().printMessage("Locating typesMenu of AnimationsTaskPane at 0,0");
                        }
                    else
                    {
                        setForeground(Color.white);
                        if (e.getKeyChar() == '{')
                            brace = true;
                    }
                }
            });
        }

        class TypeItem extends JMenuItem
        {

            public TypeItem(String text, final String command)
            {
                setText(text);
                addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        ScriptArea.this.insert(command, ScriptArea.this.getCaretPosition());
                    }
                });
            }
        }
    }

    class ControlPane extends JPanel
    {

        private BButton openButton = new BButton("Open", new ImageIcon("dat//img//menu//open.png"));
        private BButton saveButton = new BButton("Save", new ImageIcon("dat//img//menu//save.png"));

        public ControlPane()
        {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(openButton);
            add(saveButton);
            saveButton.setActionCommand("save");
            openButton.setActionCommand("open");
            saveButton.addActionListener(AnimationScriptDialog.this);
            openButton.addActionListener(AnimationScriptDialog.this);
        }
    }

    @Override
    public void setVisible(boolean b)
    {
        if (b != isVisible())
            super.setVisible(b); //To change body of generated methods, choose Tools | Templates.
        setAlwaysOnTop(true);
        setLocation((Point) CurrentData.getPrefs().get(Preference.ANIMATION_SCRIPT_DIALOG_POSITION));
        CurrentData.getPrefs().set(Preference.ANIMATIONSCRIPT_DIALOG_VISIBLE, b);
        if (CurrentData.getEditorWindow().getB3DApp().getSelectedUUID() != Wizard.NULL_SELECTION && CurrentData.getEditorWindow().getB3DApp().getSelectedUUID() != null)
        {
            openTab(Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID()));
        }
        setAlwaysOnTop(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("save") && scriptPane.getTabCount() > 0)
        {
            ScriptArea sa = (ScriptArea) ((JScrollPane) scriptPane.getSelectedComponent()).getViewport().getView();
            sa.setForeground(Color.LIGHT_GRAY);
            for (B3D_Animation b3d_anim : sa.getB3D_Element().getAnimations())
                b3d_anim.stop();
            sa.getB3D_Element().getAnimations().clear();
            sa.getB3D_Element().getAnimations().addAll(AnimationTranslator.parseToLocalAnimations(sa.getText(), sa.getB3D_Element().getUUID()));
            CurrentData.getEditorWindow().getEditPane().refresh();
        } else if (e.getActionCommand().equals("open"))
        {
            SelectElementDialog sed = new SelectElementDialog(AnimationScriptDialog.this.getLocation());
            if (sed.getSelectedElement() != null)
                openTab(sed.getSelectedElement());
        }
    }
}