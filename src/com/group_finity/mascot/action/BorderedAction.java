package com.group_finity.mascot.action;

import java.awt.Point;
import java.util.List;
import java.util.logging.Logger;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.Border;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.environment.NotOnVisibleBorder;
import com.group_finity.mascot.environment.FloorCeiling;
import com.group_finity.mascot.environment.Wall;


/**
 * Original Author: Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/)
 * Currently developed by Shimeji-ee Group.
 */
public abstract class BorderedAction extends ActionBase {

	private static final Logger log = Logger.getLogger(BorderedAction.class.getName());

	private static final String PARAMETER_BORDERTYPE = "BorderType";

	public static final String DEFAULT_BORDERTYPE = null;

	public static final String BORDERTYPE_CEILING = "Ceiling";

	public static final String BORDERTYPE_WALL = "Wall";

	public static final String BORDERTYPE_FLOOR = "Floor";

	private Border border;

	private int cnt;

	private String borderType;

	public BorderedAction(final List<Animation> animations, final VariableMap params) {
		super(animations, params);
	}

	@Override
	public void init(final Mascot mascot) throws VariableException {
		super.init(mascot);

		borderType = getBorderType();

		if (BORDERTYPE_CEILING.equals(borderType)) {
			this.setBorder(getEnvironment().getCeiling());
		} else if (BORDERTYPE_WALL.equals(borderType)) {
			this.setBorder(getEnvironment().getWall());
		} else if (BORDERTYPE_FLOOR.equals(borderType)) {
			this.setBorder(getEnvironment().getFloor());
		}

	}

	@Override
	protected void tick() throws LostGroundException, VariableException {

		if (getBorder() != null) {
			// 枠が動いているかも
			getMascot().setAnchor(getBorder().move(getMascot().getAnchor()));
		}
	}
	
	private String getBorderType() throws VariableException {
		return eval(PARAMETER_BORDERTYPE, String.class, DEFAULT_BORDERTYPE);
	}

	private void setBorder(final Border border) {
		this.border = border;

	}
	protected Border getBorder() {
		return this.border;
	}

	@Override
	public boolean hasNext() throws VariableException {
		Point p = new Point();
		if (BORDERTYPE_CEILING.equals(borderType)) {
			p = getMascot().getAnchor();
				for (int i=2;i>1;i--) {
				int x = p.x;
				int y = p.y;
				getMascot().setAnchor(new Point(x+i,y));
				if (getEnvironment().getCeiling() instanceof NotOnVisibleBorder) return false;
				if (getMascot().isLookRight() && (getEnvironment().getWorkArea().getLeft() == getMascot().getAnchor().getX())) return false;
				//if (i == 0) continue;
				getMascot().setAnchor(new Point(x-i,y));
				if (!getMascot().isLookRight() && getEnvironment().getWorkArea().getLeft() == (getMascot().getAnchor().getX())) return false;
				if (getEnvironment().getCeiling() instanceof NotOnVisibleBorder) return false;
			}
			getMascot().setAnchor(p);
		}
		if (BORDERTYPE_WALL.equals(borderType)) {
			p = getMascot().getAnchor();
				for (int i=2;i>0;i--) {
				int x = p.x;
				int y = p.y;
				getMascot().setAnchor(new Point(x,y+i));
				if (getEnvironment().getWall() instanceof NotOnVisibleBorder) return false;
				if (i == 0) continue;
				getMascot().setAnchor(new Point(x,y-i));
				if (getEnvironment().getWall() instanceof NotOnVisibleBorder) return false;
			}
			getMascot().setAnchor(p);
		}
		if (BORDERTYPE_FLOOR.equals(borderType)) {
			p = getMascot().getAnchor();
				for (int i=2;i>0;i--) {
				int x = p.x;
				int y = p.y;
				getMascot().setAnchor(new Point(x+i,y));
				if (getEnvironment().getFloor() instanceof NotOnVisibleBorder) return false;
				if (i == 0) continue;
				getMascot().setAnchor(new Point(x-i,y));
				if (getEnvironment().getFloor() instanceof NotOnVisibleBorder) return false;
			}
			getMascot().setAnchor(p);
		}
		return super.hasNext();
	}

}
