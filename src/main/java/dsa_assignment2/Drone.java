package dsa_assignment2;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * A Drone class to simulate the decisions and information collected by a drone
 * on exploring an underground maze.
 * 
 */
public class Drone implements DroneInterface
{
	private static final Logger logger     = Logger.getLogger(Drone.class);
	
	public String getStudentID()
	{
		//change this return value to return your student id number
		return "2025695";
	}

	public String getStudentName()
	{
		//change this return value to return your name
		return "Ethan Paterson-Barker";
	}

	/**
	 * The Maze that the Drone is in
	 */
	private Maze                maze;

	/**
	 * The stack containing the portals to backtrack through when all other
	 * doorways of the current chamber have been explored (see assignment
	 * handout). Note that in Java, the standard collection class for both
	 * Stacks and Queues are Deques
	 */
	private Deque<Portal>       visitStack = new ArrayDeque<>();

	/**
	 * The set of portals that have been explored so far.
	 */
	private Set<Portal>         visited    = new HashSet<>();

	/**
	 * The Queue that contains the sequence of portals that the Drone has
	 * followed from the start
	 */
	private Deque<Portal>       visitQueue = new ArrayDeque<>();

	/**
	 * This constructor should never be used. It is private to make it
	 * uncallable by any other class and has the assert(false) to ensure that if
	 * it is ever called it will throw an exception.
	 */
	@SuppressWarnings("unused")
	private Drone()
	{
		assert (false);
	}

	/**
	 * Create a new Drone object and place it in chamber 0 of the given Maze
	 * 
	 * @param maze
	 *            the maze to put the Drone in.
	 */
	public Drone(Maze maze)
	{
		this.maze = maze;
	}

	/* 
	 * @see dsa_assignment2.DroneInterface#searchStep()
	 */
	@Override
	public Portal searchStep()
	{
		logger.debug("Search step. Current chamber: " + maze.getCurrentChamber());

		for (int i = 0;i < maze.getNumDoors();i++) {
			if (visited.contains(new Portal(maze.getCurrentChamber(), i))) {
				//The portal has been visited, skip.
				logger.debug("Portal has been visited. Portal: chamber = " + maze.getCurrentChamber() + ", door = " + i);
				continue;
			} else {
				//Portal hasn't been visited. visit.
				visited.add(new Portal(maze.getCurrentChamber(), i));
				visitQueue.addLast(new Portal(maze.getCurrentChamber(), i));
				logger.debug("Portal hasn't been visited. Leaving portal: chamber = " + maze.getCurrentChamber() + ", door = " + i);
				Portal portal = maze.traverse(i);
				visited.add(portal);
				visitQueue.addLast(portal);
				visitStack.addLast(portal);
				logger.debug("Resultant portal: chamber = " + portal.getChamber() + ", door = "  + portal.getDoor());
				return portal;
			}
		}

		//All doors have been visited, backtrack.
		logger.debug("All doors have been visited. Backtracking.");
		try {
			Portal last = visitStack.removeLast();
			Portal to = maze.traverse(last.getDoor());
			visitQueue.addLast(last);
			visitQueue.addLast(to);
			return to;
		} catch (NoSuchElementException e) {
			//There is nothing left to backtrack. Return null.
			return null;
		}
	}

	/* 
	 * @see dsa_assignment2.DroneInterface#getVisitOrder()
	 */
	@Override
	public Portal[] getVisitOrder()
	{
		Portal[] portals = new Portal[visitQueue.size()];
		Deque<Portal> queue = new ArrayDeque<>(visitQueue);
		logger.debug("Length of queue: " + visitQueue.size());
		for (int i = 0;i < visitQueue.size();i++) {
			portals[i] = queue.removeFirst();
		}
		return portals;
	}

	/*
	 * @see dsa_assignment2.DroneInterface#findPathBack()
	 */
	@Override
	public Portal[] findPathBack()
	{
		logger.debug("Finding path back. Current chamber: " + maze.getCurrentChamber());
		Portal[] order = new Portal[0];
		if (visitStack.size() == 0) {
			return new Portal[0];
		}
		Deque<Portal> path = new ArrayDeque<>(visitStack);
		Deque<Portal> path2;
		Maze mazeCopy = new Maze(maze, false);
		while (mazeCopy.getCurrentChamber() != 0) {
			Portal[] orderCopy = new Portal[order.length + 1];
			System.arraycopy(order, 0, orderCopy, 0, order.length);
			order = orderCopy;
			logger.debug("Chamber != 0. Current chamber: " + mazeCopy.getCurrentChamber());
			try {
				Portal last = path.removeLast();
				path2 = new ArrayDeque<>(path);
				Portal to = mazeCopy.traverse(last.getDoor());

				for (Portal p : path2) {
					if (last.getChamber() == p.getChamber()) {
						logger.debug("Duplicate chamber found.");
						//There is a chamber further on that goes to this chamber. Skip until that one is found. Skipping first step.
						last = path.removeLast();
						to = mazeCopy.traverse(last.getDoor());
						while (last.getChamber() != p.getChamber()) {
							logger.debug("Skipping step: " + last.toString());
							last = path.removeLast();
							to = mazeCopy.traverse(last.getDoor());
						}

						break;
					}
				}

				order[order.length - 1] = last;
				logger.debug("Leaving, going through portal: door = " + last.getDoor() + ", chamber = " + last.getChamber());
				logger.debug("Arriving, going through portal: door = " + to.getDoor() + ", chamber = " + to.getChamber());
			} catch (NoSuchElementException e) {
				//There is nothing left to backtrack. Return order.
				return order;
			}
		}
		return order;
	}

}
