import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class smartrocketsseq extends PApplet {

// inspired by Jer Thorp's Smart Rockets
// http://www.blprnt.com/smartrockets/
// Reference:
// The Nature of Code by Daniel Shiffman

// motion blur settings
int samplesPerFrame = 5;
float shutterAngle = 1.5f;
int[][] result;

boolean recording = false;
int numFrames = 1500;
PFont font;

public void push() {
  pushMatrix();
  pushStyle();
}

public void pop() {
  popStyle();
  popMatrix();
}

// motion blur helper
public void draw() {
    for (int i=0; i<width*height; i++)
      for (int a=0; a<3; a++)
        result[i][a] = 0;

    for (int sa=0; sa<samplesPerFrame; sa++) {
      draw_();
      loadPixels();
      for (int i=0; i<pixels.length; i++) {
        result[i][0] += pixels[i] >> 16 & 0xff;
        result[i][1] += pixels[i] >> 8 & 0xff;
        result[i][2] += pixels[i] & 0xff;
      }
    }

    loadPixels();
    for (int i=0; i<pixels.length; i++)
      pixels[i] = 0xff << 24 |
        PApplet.parseInt(result[i][0]*1.0f/samplesPerFrame) << 16 |
        PApplet.parseInt(result[i][1]*1.0f/samplesPerFrame) << 8 |
        PApplet.parseInt(result[i][2]*1.0f/samplesPerFrame);
    updatePixels();

    if (frameCount <= numFrames && recording) {
      TImage frame = new TImage(width,height,RGB,sketchPath("frames/frame_"+nf(frameCount,3)+".png"));
      frame.set(0,0,get());
      frame.saveThreaded();
      println(frameCount,"/",numFrames);
    }
}

class TImage extends PImage implements Runnable {
  // separate thread for saving images
  String filename;

  TImage(int w,int h,int format,String filename){
    this.filename = filename;
    init(w,h,format);
  }

  public void saveThreaded(){
    new Thread(this).start();
  }

  public void run(){
    this.save(filename);
  }
}

int lifetime = 300; // duration of a generation

int num_populations = 1000; // number of populations
int population_size = 90; // individuals in each population

int finished_counter = 0;

// one color per population
Population[] populations = new Population[num_populations];

class Obstacle {

  PVector position;
  float w,h;
  
  Obstacle(float x, float y, float w_, float h_) {
    position = new PVector(x,y);
    w = w_;
    h = h_;
  }

  public void display() {
    strokeWeight(1);
    stroke(0xff3a3a59);
    fill(0xff3a3a59);
    rectMode(CORNER);
    rect(position.x,position.y,w,h);
  }

  public boolean contains(PVector spot) {
    if (spot.x > position.x && spot.x < position.x + w && spot.y > position.y && spot.y < position.y + h) {
      return true;
    } else {
      return false;
    }
  }

}

class DNA {

  // The genetic sequence
  PVector[] genes;

  // The maximum strength of the forces
  float maxforce = 0.1f;

  // Constructor (makes a DNA of random PVectors)
  DNA() {
    genes = new PVector[lifetime];
    for (int i = 0; i < genes.length; i++) {
      float angle = random(TWO_PI);
      genes[i] = new PVector(cos(angle), sin(angle));
      genes[i].mult(random(0, maxforce));
    }

    // Let's give each Rocket an extra boost of strength for its first frame
    genes[0].normalize();
  }

  // Constructor #2, creates the instance based on an existing array
  DNA(PVector[] newgenes) {
    // We could make a copy if necessary
    // genes = (PVector []) newgenes.clone();
    genes = newgenes;
  }

  // CROSSOVER
  // Creates new DNA sequence from two (this & and a partner)
  public DNA crossover(DNA partner) {
    PVector[] child = new PVector[genes.length];
    // Pick a midpoint
    int crossover = PApplet.parseInt(random(genes.length));
    // Take "half" from one and "half" from the other
    for (int i = 0; i < genes.length; i++) {
      if (i > crossover) child[i] = genes[i];
      else               child[i] = partner.genes[i];
    }    
    DNA newgenes = new DNA(child);
    return newgenes;
  }

  // Based on a mutation probability, picks a new random Vector
  public void mutate(float m) {
    for (int i = 0; i < genes.length; i++) {
      if (random(1) < m) {
        float angle = random(TWO_PI);
        genes[i] = new PVector(cos(angle), sin(angle));
        genes[i].mult(random(0, maxforce));
        //        float angle = random(-0.1,0.1);
        //        genes[i].rotate(angle);
        //        float factor = random(0.9,1.1);
        //        genes[i].mult(factor);
        if (i ==0) genes[i].normalize();
      }
    }
  }
}


class Rocket {

  Obstacle target; // target
  ArrayList<Obstacle> obstacles; // obstacles

  // physics
  PVector position;
  PVector velocity;
  PVector acceleration;

  // size
  float r;

  // distance to target
  float recordDist;

  // fitness
  float fitness;
  DNA dna;
  int geneCounter = 0;

  boolean hitObstacle = false;
  boolean hitTarget = false;
  int finishTime;

  // constructor
  Rocket(PVector l, DNA dna_, int totalRockets) {

    obstacles = new ArrayList<Obstacle>();
    obstacles.add(new Obstacle(width/2-100, height/2 + 200, 200, 10));
    obstacles.add(new Obstacle(width/2+100, height/2 -100, 200, 10));
    obstacles.add(new Obstacle(width/2-300, height/2 -100, 200, 10));

    target = new Obstacle(width/2-12, 24, 24, 24);

    acceleration = new PVector();
    velocity = new PVector();
    position = l.get();
    r = 4;
    dna = dna_;
    finishTime = 0;
    recordDist = 10000;
  }

  // FITNESS FUNCTION 
  // distance = distance from target
  // finish = what order did i finish (first, second, etc. . .)
  // f(distance,finish) =   (1.0f / finish^1.5) * (1.0f / distance^6);
  // a lower finish is rewarded (exponentially) and/or shorter distance to target (exponetially)
  public void fitness() {
    if (recordDist < 1) recordDist = 1;

    // Reward finishing faster and getting close
    fitness = (1/(finishTime*recordDist));

    // Make the function exponential
    fitness = pow(fitness, 4);

    if (hitObstacle) fitness *= 0.1f; // lose 90% of fitness hitting an obstacle
    if (hitTarget) fitness *= 2; // twice the fitness for finishing!
  }

  public boolean checkTarget() {
    float d = dist(position.x, position.y, target.position.x, target.position.y);
    if (d < recordDist) recordDist = d;

    if (target.contains(position) && !hitTarget) {
      hitTarget = true;
    } 
    else if (!hitTarget) {
      finishTime++;
    }

    if (hitTarget) {
      return true;
    } else {
      return false;
    }

  }

  public void obstacles() {
    for (Obstacle obs : obstacles) {
      if (obs.contains(position)) {
        hitObstacle = true;
      }
    }
  }

  public void applyForce(PVector f) {
    acceleration.add(f);
  }

  public void run() {
    if (!hitObstacle && !hitTarget) {
      // walk on gene and apply current force
      applyForce(dna.genes[geneCounter]);
      geneCounter = (geneCounter + 1) % dna.genes.length;
      // calculate new vectors
      update();
      // check for collisions
      obstacles();
    }
  }

  public void update() {
    velocity.add(acceleration);
    position.add(velocity);
    acceleration.mult(0);
  }

  public void display() {
    float theta = velocity.heading2D() + PI/2;
    pushMatrix();

    translate(position.x, position.y);
    rotate(theta);

    strokeWeight(2);
    stroke(255);
    fill(0);
    noStroke();
    beginShape(TRIANGLES);
    vertex(0, -r*2);
    vertex(-r, r*2);
    vertex(r, r*2);
    endShape();

    popMatrix();
  }

  public float getFitness() {
    return fitness;
  }

  public DNA getDNA() {
    return dna;
  }
}


class Population {

  float mutationRate;          // Mutation rate
  Rocket[] population;         // Array to hold the current population
  ArrayList<Rocket> matingPool;    // ArrayList which we will use for our "mating pool"
  int generations;             // Number of generations

  int id;

  int cycles = 0;

  int finished_count = 0;
  boolean criteria_met = false;

   // Initialize the population
   Population(float m, int num, int id_) {
    mutationRate = m;
    population = new Rocket[num]; // initialize population of 'num' rockets
    matingPool = new ArrayList<Rocket>(); // initialize mating pool as array of rockets
    generations = 0; 
    id = id_;
    // make a new set of rockets
    for (int i = 0; i < population.length; i++) {
      PVector position = new PVector(width/2,height+20);
      population[i] = new Rocket(position, new DNA(),population.length);
    }
  }

  public void fitness() {
    for (int i = 0; i < population.length; i++) {
      population[i].fitness();
    }
  }

  public void selection() {

    matingPool.clear();

    // Calculate total fitness of whole population
    float maxFitness = getMaxFitness();

    // Calculate fitness for each member of the population (scaled to value between 0 and 1)
    for (int i = 0; i < population.length; i++) {
      float fitnessNormal = map(population[i].getFitness(),0,maxFitness,0,1);
      int n = (int) (fitnessNormal * 100);  // Arbitrary multiplier
      for (int j = 0; j < n; j++) {
        matingPool.add(population[i]);
      }
    }
  }

  // Making the next generation
  public void reproduction() {
    // Refill the population with children from the mating pool
    for (int i = 0; i < population.length; i++) {
      // Sping the wheel of fortune to pick two parents
      int m = PApplet.parseInt(random(matingPool.size()));
      int d = PApplet.parseInt(random(matingPool.size()));
      // Pick two parents
      Rocket mom = matingPool.get(m);
      Rocket dad = matingPool.get(d);
      // Get their genes
      DNA momgenes = mom.getDNA();
      DNA dadgenes = dad.getDNA();
      // Mate their genes
      DNA child = momgenes.crossover(dadgenes);
      // Mutate their genes
      child.mutate(mutationRate);
      // Fill the new population with the new child
      PVector position = new PVector(width/2,height+20);
      population[i] = new Rocket(position, child,population.length);
    }
    generations++;
  }

  public int getGenerations() {
    return generations;
  }

  // Find highest fintess for the population
  public float getMaxFitness() {
    float record = 0;
    for (int i = 0; i < population.length; i++) {
       if(population[i].getFitness() > record) {
         record = population[i].getFitness();
       }
    }
    return record;
  }

  public void run() {
    while (!criteria_met) {
      if (cycles < lifetime) {
        for (int i = 0; i < population.length; i++) {
          if (population[i].checkTarget()) {
            finished_count++;
            if (finished_count >= 0.5f * population.length) {
              criteria_met = true;
            }
          }
          population[i].run();
        }
        cycles++;
      } else {
        // new generation
        cycles = 0;
        fitness();
        selection();
        reproduction();
        // println("POPULAÇÃO [" + t.getId() + "] - Nova Geração: " + generations);
      }
    }
    println("POPULAÇÃO " + id + " - OBJETIVO CONCLUÍDO. Geração: " + generations);
    finished_counter++;
  }
}

int start;
int timer;
boolean timer_finished = false;

public void setup() {
  // 720p
  
  

  // results csv
  Table results = new Table();

  results.addColumn("test_id");
  results.addColumn("time");

  // assets
  font = loadFont("CMUSerif-BoldItalic-64.vlw");
  textFont(font, 18);

  // motion blur matrix
  result = new int[width*height][3];

  
  // run 100 tests
  for (int i = 0; i < 100; i++) {
    // write results csv
    TableRow newRow = results.addRow();
    newRow.setInt("test_id", i+1);
    // initialize populations
    populations = new Population[num_populations];
    finished_counter = 0;
    for (int j = 0; j < populations.length; j++) {
      populations[j] = new Population(0.01f, population_size, j);
    }
    // run all populations sequentially until goal is met for each one
    start = millis();
    while (finished_counter < populations.length) {
      for (int k = 0; k < populations.length; k++) {
        populations[k].run();
      }
    }
    // finished, save time elapsed
    timer = millis() - start;
    println("TIME ELAPSED: " + timer);
    newRow.setInt("time", timer);
  }

  saveTable(results, "data/sequential_ " + num_populations + "_" + population_size + ".csv");

}

// drawing loop
public void draw_() {

  background(0xfff9f8eb);

  ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
  obstacles.add(new Obstacle(width/2-100, height/2 + 200, 200, 10));
  obstacles.add(new Obstacle(width/2+100, height/2 -100, 200, 10));
  obstacles.add(new Obstacle(width/2-300, height/2 -100, 200, 10));

  Obstacle target = new Obstacle(width/2-12, 24, 24, 24);

  // display info on window title bar
  String window = "Smart Rockets (Sequential) | FPS: " + str((int)frameRate) + " | Population Size: " + populations.length + " populations of " + population_size + ": " + populations.length * population_size;
  surface.setTitle(window);

  for (int i = 0; i < populations.length; i++) {
    for (int j = 0; j < populations[i].population.length; j++) {
      populations[i].population[j].display();
    }
  }

  target.display();

  for (Obstacle obs : obstacles) {
    obs.display();
  }

  text("Time elapsed: " + timer + "ms\nPopulations finished: " + finished_counter + "/" + populations.length, 20, 30);

}
  public void settings() {  size(1280, 720, P2D);  smooth(8); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "smartrocketsseq" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
