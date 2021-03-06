package general;

import java.io.Serializable;

/**
 *
 * @author David
 */
public enum Preference implements Serializable
{

    KEY_ANIMATION_EDITOR_SHOWN, KEY_ANIMATION_DIALOG_SIZE, KEY_ANIMATION_DIALOG_LOCATION, EDITOR_WINDOW_LOCATION, RECENT_PROJECT_PATHS,
    EXIT_WITHOUT_PROMPT, FULLSCREEN, ASSETBROWSER_SHOWN, SHOW_GRID, SHOW_SCENERY, SHOW_FILTERS,
    SHOW_WIREFRAME, SHOW_ALL_MOTIONPATHS, REMOND_OF_NODE_CHILDREN_AS_MOTION_EVENT_SPATIAL, ASSETBROWSER_ON_TOP,
    ANIMATIONSCRIPT_DIALOG_VISIBLE, SAVE_XML, VSYNC, CAM_SPEED, GRID_GAP, GRID_X, GRID_Y, TREESORT,
    EDITOR_WINDOW_SIZE, ANIMATION_SCRIPT_DIALOG_POSITION, FRAMERATE, GUI_SPEED,
    COLOR_DEPTH, MULTISAMPLING, DEPTH_BITS, FIELD_OF_VIEW, KEY_ANIMATION_EDITOR_ON_TOP,
    SHOW_NODE_HIERARCHY;
}
