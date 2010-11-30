package com.burpen.awumpus;

import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

/*
 * This is the text-based prototype of the game. It's probably buggy.
 */

public class Wumpus {

	//TODO generate these?
	//TODO would be nice to have a user-specified cave size
	static final String[] data = new String[] { "-1", "2", "5", "8", "1", "3",
			"10", "2", "4", "12", "3", "5", "14", "1", "4", "6", "5", "7",
			"15", "6", "8", "17", "1", "7", "9", "8", "10", "18", "2", "9",
			"11", "10", "12", "19", "3", "11", "13", "12", "14", "20", "4",
			"13", "15", "6", "14", "16", "15", "17", "20", "7", "16", "18",
			"9", "17", "19", "11", "18", "20", "13", "16", "19" };
	static int[] location = new int[7];
	
	static HashMap<Integer, int[]> coords = new HashMap<Integer, int[]>();
	
	// initial starting locations
	// for new games with same setup
	static int[] m = new int[7];

	static int arrows;
	static int currentLocation;

	// game status
	static int f;

	// arrow path (rooms in sequence)
	static int[] p = new int[6];
	static int range;

	static final int MAX_ENTITIES = 6;

	static final int PLAYER = 1;
	static final int WUMPUS = 2;
	static final int PIT1 = 3;
	static final int PIT2 = 4;
	static final int BAT1 = 5;
	static final int BAT2 = 6;

	static final int MAX_ROOMS = 20;
	static final int MAX_EDGES_PER_ROOM = 3;

	static final int MAX_ARROWS = 5;

	static Room[] rooms;

	static boolean firstRun;
	static boolean requestInit;
	
	static boolean wumpusSmellTracking = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		mapCoords();
		
		firstRun = true;
		requestInit = false;
		
		// instruct();

		String tmp;

		for (;;) {
			if (firstRun) {
				caveSetup();
				firstRun = false;
			}
			if (!firstRun && requestInit) {
				setLocations();
			} else if (!firstRun && !requestInit) {
				setOldLocations();
			}
			System.out.println("Enable Wumpus smell tracking? Y/N");
			Scanner sc = new Scanner(System.in);
			tmp = sc.nextLine();
			if (tmp.equalsIgnoreCase("y")) {
				wumpusSmellTracking = true;
			} else if (tmp.equalsIgnoreCase("n")) {
				wumpusSmellTracking = false;
			} else {
				while (!(tmp.equalsIgnoreCase("y"))
						&& !(tmp.equalsIgnoreCase("n"))) {
					System.out.println("Enable Wumpus smell tracking? Y/N");
					sc = new Scanner(System.in);
					tmp = sc.nextLine();
					if (tmp.equalsIgnoreCase("y")) {
						wumpusSmellTracking = true;
					} else if (tmp.equalsIgnoreCase("n")) {
						wumpusSmellTracking = false;
					}
				}
			}
			initializeGame();
			startGame();
			f = 0;
			System.out.println("Same setup? Y/N");
			Scanner scanner = new Scanner(System.in);
			tmp = scanner.nextLine();
			if (tmp.equalsIgnoreCase("y")) {
				requestInit = false;
			} else if (tmp.equalsIgnoreCase("n")) {
				requestInit = true;
			} else {
				while (!(tmp.equalsIgnoreCase("y"))
						&& !(tmp.equalsIgnoreCase("n"))) {
					System.out.println("Save setup? Y/N");
					scanner = new Scanner(System.in);
					tmp = scanner.nextLine();
					if (tmp.equalsIgnoreCase("y")) {
						requestInit = false;
					} else if (tmp.equalsIgnoreCase("n")) {
						requestInit = true;
					}
				}
			}
		}

	}

	private static void setOldLocations() {
		location = m;
	}



	private static void mapCoords() {
		coords.put(1, new int[] {293, 521});
		coords.put(2, new int[] {439, 519});
		coords.put(3, new int[] {452, 372});
		coords.put(4, new int[] {362, 369});
		coords.put(5, new int[] {296, 428});
		coords.put(6, new int[] {224, 369});
		coords.put(7, new int[] {133, 373});
		coords.put(8, new int[] {159, 518});
		coords.put(9, new int[] {71, 589});
		coords.put(10, new int[] {542, 592});
		coords.put(11, new int[] {590, 182});
		coords.put(12, new int[] {468, 218});
		coords.put(13, new int[] {385, 178});
		coords.put(14, new int[] {343, 269});
		coords.put(15, new int[] {276, 268});
		coords.put(16, new int[] {218, 188});
		coords.put(17, new int[] {147, 233});
		coords.put(18, new int[] {62, 202});
		coords.put(19, new int[] {312, 36});
		coords.put(20, new int[] {312, 125});
	}

	private static void startGame() {
		// System.out.println("starting game...");

		while (f == 0) {
			warn();
			promptAction();
		}
		if (f < 0) {
			System.out.println("HA HA HA - YOU LOSE!");
		} else if (f > 0) {
			System.out
					.println("HEE HEE HEE - THE WUMPUS'LL GET YOU NEXT TIME!!");
		}

	}

	private static void promptAction() {
		System.out.println("Shoot or move? S/M");
		Scanner sc = new Scanner(System.in);
		String tmp = sc.nextLine();

		if (tmp.equalsIgnoreCase("s")) {
			promptShoot();
			shoot();
		} else if (tmp.equalsIgnoreCase("m")) {
			promptMove();
			move();
		} else {
			promptAction();
		}
	}

	private static void promptShoot() {
		f = 0;

		promptRange();
		promptPath();
	}

	private static void shoot() {
		currentLocation = location[PLAYER];
		for (int i = 1; i < range; i++) {
			for (int j = 1; j <= 3; j++) {
				if (rooms[currentLocation].getEdge1().toString() == p[j] + "") {
					currentLocation = p[j];
				}
				int x = b_getRandomEdge();
				if (x == 1) {
					currentLocation = rooms[currentLocation].getEdge1().toInt();
				} else if (x == 2) {
					currentLocation = rooms[currentLocation].getEdge2().toInt();
				} else if (x == 3) {
					currentLocation = rooms[currentLocation].getEdge3().toInt();
				}
				checkForHit();
			}
		}
		System.out.println("Missed");
		moveWumpus();
		ammoCheck();

	}

	private static void moveWumpus() {
		//TODO reset wumpus tracking marks
		int k = c_getRandomWumpusAction();
		if (k == 4) {
			if (location[WUMPUS] != currentLocation) {
				return;
			}
			System.out.println("TSK TSK TSK - WUMPUS GOT YOU!");
			f = -1;
			return;
		} else {
			// location[WUMPUS] = cave.get(location[WUMPUS])[k];
			if (k == 1) {
				location[WUMPUS] = rooms[location[WUMPUS]].getEdge1().toInt();
			} else if (k == 2) {
				location[WUMPUS] = rooms[location[WUMPUS]].getEdge2().toInt();
			} else if (k == 3) {
				location[WUMPUS] = rooms[location[WUMPUS]].getEdge3().toInt();
			}
		}
	}

	private static void ammoCheck() {
		arrows--;
		if (arrows <= 0) {
			f = -1;
			return;
		}
	}

	private static void checkForHit() {
		if (currentLocation == location[WUMPUS]) {
			System.out.println("AHA! YOU GOT THE WUMPUS!");
			f = 1;
			return;
		} else {
			if (currentLocation != location[PLAYER]) {
				return;
			} else {
				System.out.println("OUCH! ARROW GOT YOU!");
				f = -1;
				return;
			}
		}
	}

	private static void promptPath() {
		Scanner sc = new Scanner(System.in);
		for (int i = 1; i < range; i++) {
			System.out.println("Room number:");
			p[i] = sc.nextInt();

			if (i > 2) {
				while (p[i] == p[i - 2]) {
					System.out
							.println("Arrows aren't that crooked - try another room");
					System.out.println("Room number:");
					p[i] = sc.nextInt();
				}
			}
		}
	}

	private static void promptRange() {
		System.out.println("Number of rooms (1-5):");
		Scanner sc = new Scanner(System.in);
		int range = sc.nextInt();

		while (range < 1 || range > 5) {
			System.out.println("Number of rooms (1-5):");
			range = sc.nextInt();
		}
	}

	private static void warn() {

		// for each entity
		for (int j = 2; j <= MAX_ENTITIES; j++) {

			if ((rooms[location[PLAYER]].getEdge1().toInt() != location[j])
					&& (rooms[location[PLAYER]].getEdge2().toInt() != location[j])
					&& (rooms[location[PLAYER]].getEdge3().toInt() != location[j])) {
				continue;
			} else {
				if (j == WUMPUS) {
					if (wumpusSmellTracking) {
						System.out.println("I SMELL A WUMPUS!");
					}
					continue;
				}
				if (j == PIT1 || j == PIT2) {
					System.out.println("I FEEL A DRAFT");
					continue;
				}
				if (j == BAT1 || j == BAT2) {
					System.out.println("BATS NEARBY!");
					continue;
				}
				System.err.println("warning failure");
			}
		}

		System.out.println("You are in room " + location[PLAYER]);
		System.out.println("Tunnels lead to "
		// + cave.get(currentLocation)[1]
				+ rooms[currentLocation].getEdge1().toString()
				// + getCave().get(currentLocation).getConnectedRooms()[1]
				+ ", "
				// + cave.get(currentLocation)[2]
				+ rooms[currentLocation].getEdge2().toString()
				// + getCave().get(currentLocation).getConnectedRooms()[2]
				+ ", "
				// + cave.get(currentLocation)[3]
				+ rooms[currentLocation].getEdge3().toString()
		// + getCave().get(currentLocation).getConnectedRooms()[3]
				);

	}



	private static void instruct() {
		System.out.println("Instructions? Y/N");
		String tmp = null;
		Scanner sc = new Scanner(System.in);
		tmp = sc.nextLine();
		while (!tmp.equalsIgnoreCase("y") && !tmp.equalsIgnoreCase("n")) {
			tmp = sc.nextLine();
		}
		if (tmp.equalsIgnoreCase("y")) {
			// TODO instructions
			System.out.println("instructions");
		}
	}

	private static void caveSetup() {
		rooms = new Room[21];
		for (int i = 1; i < 21; i++) {
			rooms[i] = new Room();
		}
		int x = -1;
		int index = 1;
		for (int i = 1; i < 21; i++) {
			// System.out.println("debug " + i);
			rooms[i].setData(i);

			x = Integer.parseInt(data[index]);
			rooms[i].setEdge1(rooms[x]);
			index++;

			x = Integer.parseInt(data[index]);
			rooms[i].setEdge2(rooms[x]);
			index++;

			x = Integer.parseInt(data[index]);
			rooms[i].setEdge3(rooms[x]);
			index++;

		}

		setLocations();
	}

	private static void initializeGame() {
		arrows = MAX_ARROWS;
		currentLocation = location[PLAYER];
	}

	private static void checkLocations() {
		for (int i = 1; i <= MAX_ENTITIES; i++) {
			for (int j = 1; j <= MAX_ENTITIES; j++) {
				if (i == j) {
					continue;
				} else if (location[i] == location[j]) {
					setLocations();
				}
			}
		}

	}

	private static void setLocations() {
		// locate items
		for (int i = 1; i < 7; i++) {
			location[i] = a_getRandomRoom();
			m[i] = location[i];
		}
		checkLocations();
	}

	public static int a_getRandomRoom() {
		return (int) (Math.random() * MAX_ROOMS + 1);
	}

	public static int b_getRandomEdge() {
		return (int) (Math.random() * 3 + 1);
	}

	public static int c_getRandomWumpusAction() {
		return (int) (Math.random() * 4 + 1);
	}

	public void shoot(int i) {
		// TODO fix this

	}

	public static void promptMove() {
		System.out.println("Where to?");
		Scanner sc = new Scanner(System.in);
		try {
			currentLocation = sc.nextInt();
		} catch (InputMismatchException e) {
			currentLocation = -1;
		}
	}

	public static void promptMove(boolean warn) {
		System.out.println("Invalid move destination");
		promptMove();
	}

	public static void move() {
		if (currentLocation < 1 || currentLocation > MAX_ROOMS) {
			promptMove(true);
		}

		boolean moved = false;
		// for (int j = 1; j <= MAX_EDGES_PER_ROOM; j++) {
		// if (cave.get(location[PLAYER])[j] == currentLocation) {
		// if (getCave().get(location[PLAYER]).getConnectedRooms()[j]==i) {
		if (rooms[location[PLAYER]].getEdge1().toInt() == currentLocation) {
			// debug
			// System.out.println("Moving to room " + currentLocation);
			location[PLAYER] = currentLocation;
			moved = true;
		}
		if (rooms[location[PLAYER]].getEdge2().toInt() == currentLocation) {
			// debug
			// System.out.println("Moving to room " + currentLocation);
			location[PLAYER] = currentLocation;
			moved = true;
		}
		if (rooms[location[PLAYER]].getEdge3().toInt() == currentLocation) {
			// debug
			// System.out.println("Moving to room " + currentLocation);
			location[PLAYER] = currentLocation;
			moved = true;
		}
		if (!moved) {
			// if (j == MAX_EDGES_PER_ROOM) {
			promptMove(true);
			// }
			// }
		}

		currentLocation = location[PLAYER];

		checkForHazards();

		// warn();
	}

	private static void checkForHazards() {
		location[PLAYER] = currentLocation;
		if (currentLocation == location[WUMPUS]) {
			System.out.println("... OOPS! BUMPED A WUMPUS!");
			moveWumpus();
		} else if (currentLocation == location[PIT1]
				|| currentLocation == location[PIT2]) {
			System.out.println("YYYYIIIIEEEE . . . FELL IN PIT");
			f = -1;
			return;
		} else if (currentLocation == location[BAT1]
				|| currentLocation == location[BAT2]) {
			System.out
					.println("ZAP--SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!");
			currentLocation = a_getRandomRoom();
			checkForHazards();
		}
	}

}
