package de.redlion.qb;

import com.badlogic.gdx.backends.lwjgl.LwjglApplet;

public class AppletStarter extends LwjglApplet {

	private static final long serialVersionUID = 8811862709232787268L;

	public AppletStarter() {
        super(new DesktopStarter(), true);
    }

}
