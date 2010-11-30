package com.burpen.awumpus;
public class Room {
	protected int data;
	protected Room edge1;
	protected Room edge2;
	protected Room edge3;
	
	public Room() {
		edge1 = null;
		edge2 = null;
		edge3 = null;
		data = -1;
	}

	public Room(int d, Room e1, Room e2, Room e3) {
		this.edge1 = e1;
		this.edge2 = e2;
		this.edge3 = e3;
		this.data = d;
	}

	public Room getEdge1() {
		return this.edge1;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public Room getEdge2() {
		return edge2;
	}

	public void setEdge2(Room edge2) {
		this.edge2 = edge2;
	}

	public Room getEdge3() {
		return edge3;
	}

	public void setEdge3(Room edge3) {
		this.edge3 = edge3;
	}

	public void setEdge1(Room edge1) {
		this.edge1 = edge1;
	}
	
	public String toString() {
		return "" + data;
	}

	public int toInt() {
		return this.data;
	}
}
