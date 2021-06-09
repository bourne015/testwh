package com.group_finity.mascot.behavior;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;

/**
 * サンプル用の単純な振る舞い.
 */
public class UserBehavior implements Behavior {
	private static final Logger log = Logger.getLogger(UserBehavior.class.getName());

	public static final String BEHAVIORNAME_FALL = "Fall";

	public static final String BEHAVIORNAME_DRAGGED = "Dragged";	

	public static final String BEHAVIORNAME_THROWN = "Thrown";

	private final String name;

	private final Configuration configuration;

	private final Action action;

	private Mascot mascot;


	public UserBehavior(final String name, final Action action, final Configuration configuration) {
		this.name = name;
		this.configuration = configuration;
		this.action = action;
	}

	@Override
	public String toString() {
		return "Behavior(" + getName() + ")";
	}

	@Override
	public synchronized void init(final Mascot mascot) throws CantBeAliveException {

		this.setMascot(mascot);

		log.log(Level.INFO, "Default Behavior({0},{1})", new Object[] { this.getMascot(), this });

		try {
			getAction().init(mascot);
			if (!getAction().hasNext()) {
				try {
					mascot.setBehavior(this.getConfiguration().buildBehavior(getName(), mascot));
				} catch (final BehaviorInstantiationException e) {
					throw new CantBeAliveException("Failed to initialize the following behavior", e);
				}
			}
		} catch (final VariableException e) {
			throw new CantBeAliveException("An error occurred in the evaluation of the variable", e);
		}

	}

	private Configuration getConfiguration() {
		return this.configuration;
	}

	private Action getAction() {
		return this.action;
	}

	private String getName() {
		return this.name;
	}

	/**
	 * マウスが押された.
	 * 左ボタンだったらドラッグ開始.
	 * @throws CantBeAliveException 
	 */
	public synchronized void mousePressed(final MouseEvent event) throws CantBeAliveException {

		if (SwingUtilities.isLeftMouseButton(event)) {
			// Begin dragging
			try {
				getMascot().setBehavior(this.getConfiguration().buildBehavior(BEHAVIORNAME_DRAGGED));
			} catch (final BehaviorInstantiationException e) {
				throw new CantBeAliveException("Failed to initialize the drag action", e);
			}
		}

	}

	/**
	 * On Mouse Release.
	 * End dragging.
	 * @ Throws CantBeAliveException
	 */
	public synchronized void mouseReleased(final MouseEvent event) throws CantBeAliveException {

		if (SwingUtilities.isLeftMouseButton(event)) {
			// Termination of drag
			try {
				getMascot().setBehavior(this.getConfiguration().buildBehavior(BEHAVIORNAME_THROWN));
			} catch (final BehaviorInstantiationException e) {
				throw new CantBeAliveException("Failed to initialize the drop action", e);
			}
		}

	}

	@Override
	public synchronized void next() throws CantBeAliveException {

		try {
			if (getAction().hasNext()) {
				getAction().next();
			}

			if (getAction().hasNext()) {

				// / IE をちゃんと持てているかチェック
				if ((getMascot().getBounds().getX() + getMascot().getBounds().getWidth() <= getEnvironment().getScreen()
						.getLeft())
						|| (getEnvironment().getScreen().getRight() <= getMascot().getBounds().getX())
						|| (getEnvironment().getScreen().getBottom() <= getMascot().getBounds().getY())) {

					log.log(Level.INFO, "Out of the screen bounds({0},{1})", new Object[] { getMascot(), this });

					getMascot().setAnchor(
							new Point((int) (Math.random() * (getEnvironment().getScreen().getRight() - getEnvironment()
									.getScreen().getLeft()))
									+ getEnvironment().getScreen().getLeft(), getEnvironment().getScreen().getTop() - 256));

					try {
						getMascot().setBehavior(this.getConfiguration().buildBehavior(BEHAVIORNAME_FALL));
					} catch (final BehaviorInstantiationException e) {
						throw new CantBeAliveException("Failed to initialize the falling action", e);
					}
				}

			} else {
				log.log(Level.INFO, "Completed Behavior ({0},{1})", new Object[] { getMascot(), this });

				try {
					getMascot().setBehavior(this.getConfiguration().buildBehavior(getName(), getMascot()));
				} catch (final BehaviorInstantiationException e) {
					throw new CantBeAliveException("Failed to initialize the following actions", e);
				}
			}
		} catch (final LostGroundException e) {
			log.log(Level.INFO, "Lost Ground ({0},{1})", new Object[] { getMascot(), this });

			try {
				getMascot().setBehavior(this.getConfiguration().buildBehavior(BEHAVIORNAME_FALL));
			} catch (final BehaviorInstantiationException ex) {
				throw new CantBeAliveException("Failed to initialize the action falls", ex);
			}
		} catch (final VariableException e) {
			throw new CantBeAliveException("An error occurred in the evaluation of the variable", e);
		}

	}

	private void setMascot(final Mascot mascot) {
		this.mascot = mascot;
	}

	private Mascot getMascot() {
		return this.mascot;
	}

	protected MascotEnvironment getEnvironment() {
		return getMascot().getEnvironment();
	}
}
