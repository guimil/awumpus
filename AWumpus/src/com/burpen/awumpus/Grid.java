package com.burpen.awumpus;

public class Grid {
	private int width, height;
	private GridNode[] nodes;

	public Grid(int width, int height) {
		super();
		this.width = width;
		this.height = height;
		nodes = new GridNode[width * height];
		int north, south, east, west;
		for (int i=0; i<width*height; i++) {
			north = findNorthBoundary(i);
			east = findEastBoundary(i);
			south = findSouthBoundary(i);
			west = findWestBoundary(i);
			
			nodes[i] = new GridNode(north, east, south, west);
		}
	}

	public Grid() {
		
	}
	
	public GridNode getNode(int index) {
		return nodes[index];
	}
	
	public int[] getConnectedNodes(int index) {
		int allNodes[] = new int[] { nodes[index].getNorth(), nodes[index].getEast(),
				nodes[index].getSouth(), nodes[index].getWest() };
		int intact = 4;
		for (int i=0; i<4; i++) {
			if (allNodes[i] == -1) {
				intact--;
			}
		}
		
		int intactNodes[] = new int[intact];
		int pos = 0;
		
		for (int i=0; i<4; i++) {
			if (allNodes[i] == -1) {
				continue;
			} else {
				intactNodes[pos] = allNodes[i];
				pos++;
			}
		}
		
		return intactNodes;
	}
	
	private int findWestBoundary(int i) {
		if (isWestEdge(i)) {
			return i + width - 1;
		} else {
			return i - 1;
		}
	}

	private int findSouthBoundary(int i) {
		if (isSouthEdge(i)) {
			return i % width;
		} else {
			return i + width;
		}
	}

	private int findNorthBoundary(int i) {
		if (isNorthEdge(i)) {
			return (width * height - i);
		} else {
			return i - width;
		}
	}

	private int findEastBoundary(int i) {
		if (isEastEdge(i)) {
			return i - width + 1;
		} else {
			return i + 1;
		}
	}
	
	private boolean isWestEdge(int index) {
		return ((index % width) == 0);
	}
	
	private boolean isEastEdge(int index) {
		return ((index % width) == (width - 1));
	}
	
	private boolean isSouthEdge(int index) {
		return (index >= ((width * height) - width));
	}
	
	private boolean isNorthEdge(int index) {
		return (index < width);
	}
	
	public void deleteNode(int i) {
		if (isEastEdge(i)) {
			nodes[i-1].setEast(i-width + 1);
			nodes[i-width+1].setWest(i-1);
		} else if (isWestEdge(i)) {
			nodes[i+1].setWest(i + width - 1);
			nodes[i + width - 1].setEast(i+1);
		} else {
			nodes[i-1].setEast(i + 1);
			nodes[i+1].setWest(i-1);
		}
		
		if (isNorthEdge(i)) {
			nodes[i+width].setNorth((width * height) - width + i);
			nodes[(width * height) - width + i].setSouth(i+width);
		} else if (isSouthEdge(i)) {
			nodes[i-width].setSouth(i % width);
			nodes[i%width].setNorth(i-width);
		} else {
			nodes[i-width].setSouth(i + width);
			nodes[i+width].setSouth(i-width);
		}
		
		nodes[i].collapse();
	}

	public boolean isAvailable(int i) {
		return nodes[i].isAvailable();
	}
}
