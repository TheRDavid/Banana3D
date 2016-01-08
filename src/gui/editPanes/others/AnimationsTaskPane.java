package gui.editPanes.others;

import b3dElements.animations.timedAnimations.B3D_TimedAnimation;
import b3dElements.B3D_Element;
import general.CurrentData;
import components.BButton;
import components.EditTaskPane;
import components.SmallProgressbar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import other.LiveAnimation;
import other.Wizard;
import se.datadosen.component.RiverLayout;

/**
 *
 * @author David
 */
public class AnimationsTaskPane extends EditTaskPane
{

    private AnimationsPanel animationsPanel;
    private B3D_Element element;
    private ImageIcon playIcon = new ImageIcon("dat//img//menu//c_play.png"), stopIcon = new ImageIcon("dat//img//menu//c_stop.png");
    private BButton editButton = new BButton("Edit", new ImageIcon("dat//img//menu//c_edit.png"));

    public AnimationsTaskPane()
    {
        element = Wizard.getObjects().getB3D_Element(CurrentData.getEditorWindow().getB3DApp().getSelectedUUID());
        animationsPanel = new AnimationsPanel();
        taskPane.setTitle("Custom Animations");
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CurrentData.getAnimationScriptDialog().setVisible(true);
            }
        });
        taskPane.add(animationsPanel, BorderLayout.CENTER);
        taskPane.add(editButton, BorderLayout.SOUTH);
        add(taskPane, BorderLayout.CENTER);
    }

    private class AnimationsPanel extends JPanel
    {

        public AnimationsPanel()
        {
            setLayout(new RiverLayout());
            update();
        }

        public void update()
        {
            removeAll();
            for (B3D_TimedAnimation animation : element.getAnimations())
            {
                add("br", new AnimationPanel(animation));
            }
        }

        private class AnimationPanel extends JPanel
        {

            private SmallProgressbar progressbar;
            private BButton playButton = new BButton(playIcon,true);
            private B3D_TimedAnimation animation;
            private JLabel nameLabel;

            public AnimationPanel(B3D_TimedAnimation anim)
            {
                this.animation = anim;
                progressbar = new SmallProgressbar();
                progressbar.setColor(Color.orange);
                progressbar.setValue(0);
                progressbar.setDisplayValue(false);
                progressbar.setMax((int) animation.getDuration() * 1000);
                progressbar.setPreferredSize(new Dimension(250, 25));
                setLayout(new RiverLayout());
                nameLabel = new JLabel(anim.getName());
                nameLabel.setPreferredSize(new Dimension(100, 20));
                add(nameLabel);
                add("tab", playButton);
                add("tab", progressbar);
                playButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (animation.isPlaying())
                        {
                            playButton.setIcon(playIcon);
                            animation.stop();
                            progressbar.setValue(0);
                        } else
                        {
                            playButton.setIcon(stopIcon);
                            animation.play();
                            CurrentData.getEditorWindow().getB3DApp().enqueue(new Callable<Void>()
                            {
                                public Void call() throws Exception
                                {
                                    Wizard.getActiveAnimations().add(new LiveAnimation(animation));
                                    return null;
                                }
                            });
                        }
                    }
                });
            }

            public SmallProgressbar getProgressbar()
            {
                return progressbar;
            }

            public BButton getPlayButton()
            {
                return playButton;
            }

            public B3D_TimedAnimation getAnimation()
            {
                return animation;
            }
        }
    }

    @Override
    public void updateData(boolean urgent)
    {
        for (B3D_TimedAnimation animation : element.getAnimations())
        {
            if (animation.isPlaying())
            {
                for (Component comp : animationsPanel.getComponents())
                    if (((AnimationsPanel.AnimationPanel) comp).getAnimation().equals(animation))
                        ((AnimationsPanel.AnimationPanel) comp).getProgressbar().setValue((int) (animation.getTime() * 1000));
            } else
                for (Component comp : animationsPanel.getComponents())
                    if (((AnimationsPanel.AnimationPanel) comp).getAnimation().equals(animation))
                    {
                        ((AnimationsPanel.AnimationPanel) comp).getProgressbar().setValue(0);
                        ((AnimationsPanel.AnimationPanel) comp).getPlayButton().setIcon(playIcon);
                    }
        }
        if (urgent)
        {
            animationsPanel.update();
            taskPane.repaint();
            taskPane.validate();
            taskPane.revalidate();
        }
    }
}
