package org.lannister;

import org.lannister.agents.AgentsController;
import org.lannister.agents.Explorer;

public class Main {

	private static AgentsController agentsController;
	
	private static String[] agents = { "LannisterExplorer1" }; 
	
	public static void main(String[] args) {
		
		agentsController = new AgentsController();
		
		for(String agent : agents)
			agentsController.registerAgent(agent, Explorer.class);
		
		agentsController.start();
		
		boolean running = EIManager.isRunning();
		while(running) {
			agentsController.perform();
			running = EIManager.isRunning();
		}
	}

}
