package com.burpen.awumpus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Circle {
		
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	
//	/** The initial vertex definition */
//	private float vertices[] = { 
//								-1.0f, -1.0f, 0.0f, //Bottom Left
//								1.0f, -1.0f, 0.0f, 	//Bottom Right
//								-1.0f, 1.0f, 0.0f, 	//Top Left
//								1.0f, 1.0f, 0.0f 	//Top Right
//												};
	private float points[] = new float[360*3];
	
	/**
	 * The Circle constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Circle(double radius) {
		//
//		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(points.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
//		vertexBuffer.put(vertices);
		
		int i = 0;
		
		for (double angle=0; angle<360; angle+=1) {
//			System.out.println(angle + ", vertex 1: " + (float) (Math.sin(angle) * radius));
			points[i] = (float) (Math.sin(angle) * radius);
//			System.out.println(angle + ", vertex 2: " + (float) (Math.cos(angle) * radius));
			points[i+1] = (float) (Math.cos(angle) * radius);
			points[i+2] = 0.0f;
			
			i+=3;
		}
		vertexBuffer.put(points);
		
		vertexBuffer.position(0);
	}

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL context
	 */
	public void draw(GL10 gl, float red, float blue, float green, float alpha) {
		//set the color
//		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glColor4f(red, blue, green, alpha);
		
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CW);
		
		//Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		//Enable vertex buffer
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		//Draw the vertices as triangle strip
//		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
//		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, points.length/3);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, points.length/3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
