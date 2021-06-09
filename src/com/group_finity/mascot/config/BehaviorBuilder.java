package com.group_finity.mascot.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.behavior.UserBehavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

public class BehaviorBuilder {

	private static final Logger log = Logger.getLogger(BehaviorBuilder.class.getName());

	private final Configuration configuration;

	private final String name;

	private final String actionName;

	private int frequency;

	private final List<String> conditions;

	private final boolean nextAdditive;

	private final List<BehaviorBuilder> nextBehaviorBuilders = new ArrayList<BehaviorBuilder>();

	private final Map<String, String> params = new LinkedHashMap<String, String>();

	public BehaviorBuilder(final Configuration configuration, final Entry behaviorNode, final List<String> conditions) {
		this.configuration = configuration;
		this.name = behaviorNode.getAttribute("Name");
		this.actionName = behaviorNode.getAttribute("Action") == null ? getName() : behaviorNode.getAttribute("Action");
		this.frequency = Integer.parseInt(behaviorNode.getAttribute("Frequency"));
		this.conditions = new ArrayList<String>(conditions);
		this.getConditions().add(behaviorNode.getAttribute("Condition"));

	// Conversion to multiwindow environment checks
	// Also set IE throw frequency to 0
		if (name.contains("投げる")) frequency = 0;
		if (name.equals("Fall")) frequency = 1;
		if (!name.contains("に飛びつく")) {
			if (conditions != null) {
				for (int i=0;i<conditions.size();i++) {
					String s = conditions.get(i);
					s = s.replaceAll("environment.activeIE","curIE");
					conditions.set(i,s);
				}
			}
		}

		log.log(Level.INFO, "行動読み込み開始({0})", this);

		this.getParams().putAll(behaviorNode.getAttributes());
		this.getParams().remove("Name");
		this.getParams().remove("Action");
		this.getParams().remove("Frequency");
		this.getParams().remove("Condition");

		boolean nextAdditive = true;

		for (final Entry nextList : behaviorNode.selectChildren("NextBehaviorList")) {

			log.log(Level.INFO, "Lists the Following Behaviors...");

			nextAdditive = Boolean.parseBoolean(nextList.getAttribute("Add"));

			loadBehaviors(nextList, new ArrayList<String>());
		}

		this.nextAdditive = nextAdditive;

		log.log(Level.INFO, "Behaviors have finished loading({0})", this);

	}

	@Override
	public String toString() {
		return "Behavior(" + getName() + "," + getFrequency() + "," + getActionName() + ")";
	}

	private void loadBehaviors(final Entry list, final List<String> conditions) {

		for (final Entry node : list.getChildren()) {

			if (node.getName().equals("Condition")) {

				final List<String> newConditions = new ArrayList<String>(conditions);
				newConditions.add(node.getAttribute("Condition"));

				loadBehaviors(node, newConditions);

			} else if (node.getName().equals("BehaviorReference")) {
				final BehaviorBuilder behavior = new BehaviorBuilder(getConfiguration(), node, conditions);
				getNextBehaviorBuilders().add(behavior);
			}
		}
	}

	public void validate() throws ConfigurationException {

		if ( !getConfiguration().getActionBuilders().containsKey(getActionName()) ) {
			log.log(Level.SEVERE, "There is no corresponding action(" + this + ")");			
			throw new ConfigurationException("There is no corresponding action("+this+")");
		}
	}

	public Behavior buildBehavior() throws BehaviorInstantiationException {

		try {
			return new UserBehavior(getName(),
						getConfiguration().buildAction(getActionName(),
								getParams()), getConfiguration() );
		} catch (final ActionInstantiationException e) {
			log.log(Level.SEVERE, "Failed to initialize the corresponding action("+this+")");				
			throw new BehaviorInstantiationException("Failed to initialize the corresponding action("+this+")", e);
		}
	}


	public boolean isEffective(final VariableMap context) throws VariableException {

		for (final String condition : getConditions()) {
			if (condition != null) {
				if (!(Boolean) Variable.parse(condition).get(context)) {
					return false;
				}
			}
		}

		return true;
	}

	String getName() {
		return this.name;
	}

	int getFrequency() {
		return this.frequency;
	}

	private String getActionName() {
		return this.actionName;
	}

	private Map<String, String> getParams() {
		return this.params;
	}

	private List<String> getConditions() {
		return this.conditions;
	}

	private Configuration getConfiguration() {
		return this.configuration;
	}

	boolean isNextAdditive() {
		return this.nextAdditive;
	}

	List<BehaviorBuilder> getNextBehaviorBuilders() {
		return this.nextBehaviorBuilders;
	}
}
