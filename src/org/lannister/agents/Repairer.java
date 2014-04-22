package org.lannister.agents;

import java.util.List;

import org.lannister.EIManager;
import org.lannister.brain.AgentBrain;
import org.lannister.brain.RepairerBrain;
import org.lannister.messaging.Message;
import org.lannister.util.Percepts;

import eis.iilang.Percept;

/**
author = 'Oguz Demir'
 */
public class Repairer extends Agent {

	public Repairer(String name, String team, AgentBrain brain) {
		super(name, team, brain);
	}

	@Override
	protected void handlePercepts() {
		List<Percept> percepts = EIManager.getPercepts(getAgentName());
		handleCommonPercepts(percepts);
		
		for(Percept percept : percepts) {
			if(percept.getName().equals(Percepts.VISIBLEENTITY)) {
				String id  		= percept.getParameters().get(0).toString();
				String pos 		= percept.getParameters().get(1).toString();
				String team		= percept.getParameters().get(2).toString();
				String status 	= percept.getParameters().get(3).toString();
				
				if(team.equals(this.team) && status.equals("disabled")) {
					//help
					RepairerBrain repairerBrain = (RepairerBrain) brain;
					repairerBrain.setFriend(id);
				}
				
				if(!team.equals(this.team) && status.equals("normal")) {
					//parry
					RepairerBrain repairerBrain = (RepairerBrain) brain;
					repairerBrain.setEnemy(id);
				}
			}
		}
	}

	@Override
	protected void handleMessages() {
		List<Message> messages = brain.getCoordinator().popMessages(getAgentName());
		handleCommonMessages(messages);
		
		for(Message message : messages) {
			String from 	= message.getFrom();
			Percept percept = message.getPercept();
			if(percept.getName().equals(Percepts.HELP)) {
				RepairerBrain repairerBrain = (RepairerBrain) brain;
				repairerBrain.handleHelpCall(from);
			}
		}
	}

}
