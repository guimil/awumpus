package com.burpen.awumpus;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;

public class SurfaceViewClass extends GLSurfaceView implements Renderer {

	private Circle[] circles;
	
	/** The Activity Context */
	private Context context;
	
	private long timeOfLastEvent = -1;
	
	private int width, height;
	
	private int markedX, markedY = -1;
	
	private double radius = 0.5;
	
	private float x, y, z;
	
	/**
	 * Instance the circle objects
	 */
	public SurfaceViewClass(Context context, int width, int height) {
		super(context);
		
		this.z = 2.25f;
		
		this.width = width;
		this.height = height;
		circles = new Circle[width * height];
		
		//Set this as Renderer
		this.setRenderer(this);
		
		for (int i=0; i<width * height; i++) {
			circles[i] = new Circle(radius);
		}
		
		this.context = context;
	}

	/**
	 * The Surface is created/init()
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
		
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
	}

	/**
	 * Here we do our drawing
	 */
	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();					//Reset The Current Modelview Matrix
		
		/*
		 * Minor changes to the original tutorial
		 * 
		 * Instead of drawing our objects here,
		 * we fire their own drawing methods on
		 * the current instance
		 */
//		gl.glTranslatef(0.0f, 0.0f, (float)(-1.0 * multi));
		// TODO use more dynamic values according to grid size
		if (z==5) {
			gl.glTranslatef(-10.0f, 9.5f, (float)(-5 * z));
		} else if (z==2.25) {
			gl.glTranslatef(-4.5f, 3.5f, (float)(-5 * z));
		} else if (z==1) {
			gl.glTranslatef(-4f, 3.5f, (float)(-5 * z));
		} else {
			gl.glTranslatef(-5.0f, 5.5f, (float)(-5 * z));
		}
		
		for (int y=0; y<height; y++) {
			int x=0;
			for (; x<width; x++) {
				//move over one spot horizontally
				gl.glTranslatef((float)(radius*2), 0.0f, 0.0f);
				if (markedX == x && markedY == y) {
					circles[x + y].draw(gl, 1.0f, 0.0f, 0.0f, 1.0f);
				} else {
					circles[x + y].draw(gl, 1.0f, 1.0f, 1.0f, 1.0f);
				}
				
			}
			//go back to the left and move down one spot vertically
			gl.glTranslatef(-(float)(x * radius*2), -(float)(radius*2), 0.0f);
		}
		
		//debug output
//		System.out.println("drew a frame with player at " + markedX + ", " + markedY);
//		System.out.println("drew a frame at " + System.currentTimeMillis());
	}

	/**
	 * If the surface changes, reset the view
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
	}
	
	public boolean handleTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			System.out.println("x = " + event.getX() + ", y = " + event.getY());
//			System.out.println("time of last: " + timeOfLastEvent + ", time of current: " + event.getEventTime() + ", span: " + (event.getEventTime() - timeOfLastEvent));
	//		if (timeOfLastEvent != -1) {
				if (event.getEventTime() - timeOfLastEvent <= 300) {
					System.out.println("double tap detected");
					toggleZoom();
					timeOfLastEvent = -1;
				} else {
					timeOfLastEvent = event.getEventTime();
				}
	//		} else {
	//			timeOfLastEvent = event.getEventTime();
	//		}
		}
		
		
		//We handled the event
		return true;
	}
	
	private void toggleZoom() {
		if (z == 2.25) {
			z = 1;
		} else if (z == 1) {
			z = 5;
		} else {
			z = 2.25f;
		}
		this.requestRender();
	}

	public void highlightSpot(int x, int y, int type) {
		markedX = x;
		markedY = y;
		
	}

	public void highlightSpot(int loc, int type) {
		highlightSpot(loc%width, loc/width, type);
		
	}
}
