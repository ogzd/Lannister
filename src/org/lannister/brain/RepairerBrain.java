package org.lannister.brain;

import org.lannister.action.ActionFactory;
import org.lannister.action.ActionResults;
import org.lannister.action.Actions;
import org.lannister.agents.AgentMode;
import org.lannister.brain.AgentPlan.PlanType;

import eis.iilang.Action;

/**
author = 'Oguz Demir'
 */
public class RepairerBrain extends AgentBrain {

	/**
	 * id of the caller agent for repair
	 */
	private String caller;
	
	/**
	 * id of the enemy agent
	 * @param name
	 */
	private String enemy = null; 
	
	/**
	 * id of a teammate
	 * @param name
	 */
	private String friend = null;
	
	public RepairerBrain(String name) {
		super(name);
	}
	
	@Override
	protected Action handleFailedAction() {
		switch (result) { 
			case ActionResults.FAILRANDOM:
				return ActionFactory.get().create(action, param);
			case ActionResults.FAILUNKNOWN:
			case ActionResults.FAILUNREACHABLE:
				abortPlan();
				return ActionFactory.get().create(Actions.SKIP);
			case ActionResults.FAILNORESOURCE: 	// recharge
				return ActionFactory.get().create(Actions.RECHARGE);
			case ActionResults.FAILATTACKED:   	// defend
				return ActionFactory.get().create(Actions.PARRY);
			case ActionResults.FAILSTATUS: 		// disabled
				return ActionFactory.get().create(Actions.SKIP);
			default: 
				return null;
		}
	}

	@Override
	protected Action handleSucceededAction() {
		Action action = null;
		switch(mode) {
			case EXPLORING:
				action = plan.isCompleted() ? ActionFactory.get().surveyOrRecharge(energy) : ActionFactory.get().gotoOrRecharge(energy, position, plan.next());
				if(!Actions.isTypeOf(action, Actions.RECHARGE)) {
					plan   = plan.isCompleted() ? AgentPlanner.newExploringPlan(position) 	 : plan;
					mode   = plan.isCompleted() ? AgentMode.SURVEYING 						 : mode;
					plan   = plan.isCompleted() ? AgentPlanner.newSurveyingPlan(position) 	 : plan;
				}
				break;
			case SURVEYING:
				action = plan.isCompleted() ? ActionFactory.get().surveyOrRecharge(energy) : ActionFactory.get().gotoOrRecharge(energy, position, plan.next());
				if(!Actions.isTypeOf(action, Actions.RECHARGE)) {
					plan   = plan.isCompleted() ? AgentPlanner.newSurveyingPlan(position) 	 	  : plan;
					mode   = plan.isCompleted() ? AgentMode.BESTSCORE 						 	  : mode;
					plan   = plan.isCompleted() ? AgentPlanner.newBestScoringPlan(position, name) : plan;
				}
				break;
			case HELPING:
				action = plan.isCompleted() ? ActionFactory.get().repairOrRecharge(energy, caller) : ActionFactory.get().gotoOrRecharge(energy, position, plan.next());
				if(!Actions.isTypeOf(action, Actions.RECHARGE)) {
					mode   = plan.isCompleted() ? nmode : mode;
					plan   = plan.isCompleted() ? (mode == AgentMode.EXPLORING ? AgentPlanner.newExploringPlan(position) 		 : (
												   mode == AgentMode.SURVEYING ? AgentPlanner.newSurveyingPlan(position) 		 : (
												   mode == AgentMode.BESTSCORE ? AgentPlanner.newBestScoringPlan(position, name) : null 
														   										 ))) : plan;
				}
				break;
			case BESTSCORE:
				action = plan.isCompleted() ? ActionFactory.get().parryOrRecharge(energy) : ActionFactory.get().gotoOrRecharge(energy, position, plan.next());
				break;
		}
		return action;
	}
	
	public boolean handleHelpCall(String caller) {
		this.caller = caller;
		AgentPlanner.abortPlan(plan);
		updateMode(AgentMode.HELPING);
		plan = AgentPlanner.newCustomPlan(position, positions.get(caller));
		return true;
	}

	@Override
	protected Action handleImmediateAction() {
		if(enemy != null) {
			enemy = null;
			return ActionFactory.get().parryOrRecharge(energy);
		}
		if(friend != null) {
			String ffriend = friend; friend = null;
			return ActionFactory.get().repairOrRecharge(energy, ffriend);
		}
		return null;
 	}
	
	@Override
	protected Action handleDisabledAction() {
		Action action = null;
		plan   = plan.type != PlanType.REPAIRING ? AgentPlanner.newRepairingPlan(position, getDisabledAgentsPositions()) : plan;
		action = plan.isCompleted() ? ActionFactory.get().repairOrRecharge(energy, plan.getTargetAgent()) : ActionFactory.get().gotoOrRecharge(energy, position, plan.next());
		if(!Actions.isTypeOf(action, Actions.RECHARGE)) {
			plan = plan.isCompleted() ? AgentPlanner.newRepairingPlan(position, getDisabledAgentsPositions()) : plan;
		}
		return action;
	}
	
	public void setEnemy(String enemy) {
		this.enemy = enemy;
	}
	
	public void setFriend(String friend) {
		this.friend = friend;
	}
}