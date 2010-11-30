package com.burpen.awumpus;

public class GridNode {
	private int north, east, south, west;
	private boolean available;

	public GridNode(int north, int east, int south, int west) {
		super();
		this.north = north;
		this.east = east;
		this.south = south;
		this.west = west;
		this.available = true;
	}

	public int getNorth() {
		return north;
	}

	public void setNorth(int north) {
		this.north = north;
	}

	public int getEast() {
		return east;
	}

	public void setEast(int east) {
		this.east = east;
	}

	public int getSouth() {
		return south;
	}

	public void setSouth(int south) {
		this.south = south;
	}

	public int getWest() {
		return west;
	}

	public void setWest(int west) {
		this.west = west;
	}

	public boolean isAvailable() {
		return this.available;
	}
	
	public void collapse() {
		this.west = -1;
		this.south = -1;
		this.north = -1;
		this.east = -1;
		this.available = false;
	}
}
