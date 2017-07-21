package com.olliebown.body;


public class Body2D {

	private float width = 1f;
	private float drag;
	private float mass;
	private float diameter;
	
	private float sensorWidth;
	private float sensorDistance;
	
	private float[] pos;
	private float[] vel;
	private float[] accel;
	private float heading;
	private float angularVel;
	
	private float[] sensor1Pos;
	private float[] sensor2Pos;
	private float[] sensor3Pos;
	private float sin;
	private float cos;
	
	private float timeStep;
	
	public Body2D() {
		reset();
	}
	
	public void reset() {
		reset(new float[] {width / 2, width / 2}, (float)Math.PI / 2);
	}
	
	public void reset(float[] pos, float heading) {
		this.pos = pos;
		this.heading = heading;
		vel = new float[] {0, 0};
		accel = new float[] {0, 0};
		angularVel = 0;
		drag = 0.9f;
		timeStep = 0.01f;
		mass = 6f;
		diameter = 1f;
		sensorWidth = (float)(Math.PI / 3);
		sensorDistance = 0.02f;
		sensor1Pos = new float[2];
		sensor2Pos = new float[2];
		sensor3Pos = new float[2];
		calculateSensorPositions();
	}
	
	public void update(float[] thrust) {
//		thrust[0] += 0.3;
//		thrust[1] += 0.3;
//		System.out.println(thrust[0] + " " + thrust[1]);
		sin = (float)Math.sin(heading);
		cos = (float)Math.cos(heading);
		accel[0] = cos * (thrust[0] + thrust[1]) / mass - vel[0] * drag;
		accel[1] = sin * (thrust[0] + thrust[1]) / mass - vel[1] * drag;
		angularVel += timeStep * (thrust[1] - thrust[0]) * diameter / mass;
		angularVel *= drag;
		heading += angularVel;
		heading = heading % (float)(Math.PI * 2);
		vel[0] += timeStep * accel[0];
		vel[1] += timeStep * accel[1];
		pos[0] += timeStep * vel[0];
		pos[1] += timeStep * vel[1];
		pos[0] = wrap(pos[0]);
		pos[1] = wrap(pos[1]);
		calculateSensorPositions();
	}
	
	private void calculateSensorPositions() {
		sensor2Pos[0] = wrap(pos[0] + cos * sensorDistance);
		sensor2Pos[1] = wrap(pos[1] + sin * sensorDistance);
		sensor1Pos[0] = wrap(pos[0] + (float)Math.cos(heading - sensorWidth) * sensorDistance);
		sensor1Pos[1] = wrap(pos[1] + (float)Math.sin(heading - sensorWidth) * sensorDistance);
		sensor3Pos[0] = wrap(pos[0] + (float)Math.cos(heading + sensorWidth) * sensorDistance);
		sensor3Pos[1] = wrap(pos[1] + (float)Math.sin(heading + sensorWidth) * sensorDistance);		
	}
	
	public float[] getSensorPos(int i) {
		switch(i) {
		case 0:
			return sensor1Pos;
		case 1:
			return sensor2Pos;
		case 2:
			return sensor3Pos;
		case 3:
			return pos;
		default:
			return null;
		}
	}
	
	private float wrap(float x) {
		while(x < 0) x += width;
		while(x >= width) x -= width;
		return x;
	}
	
	public float[] getPosition() {
		return pos;
	}
	
	public float getSpeed() {
		return (float)Math.sqrt(vel[0] * vel[0] + vel[1] * vel[1]);
	}
	
	
	
	public float getDiameter() {
		return diameter;
	}

	public void setDiameter(float diameter) {
		this.diameter = diameter;
	}
	
	

	public float getSensorDistance() {
		return sensorDistance;
	}

	public void setSensorDistance(float sensorDistance) {
		this.sensorDistance = sensorDistance;
	}

	public static void main(String[] args) {
		Body2D b = new Body2D();
		for(int i = 0; i < 100; i++) {
			b.update(new float[] {0.2f, 0.1f});
			System.out.println(b.pos[0] + " " + b.pos[1]);
		}
		for(int i = 0; i < 100; i++) {
			b.update(new float[] {0.0f, 0.1f});
			System.out.println(b.pos[0] + " " + b.pos[1]);
		}
	}

}
