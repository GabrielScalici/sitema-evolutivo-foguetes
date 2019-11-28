// inspired by Jer Thorp's Smart Rockets
// http://www.blprnt.com/smartrockets/
// Reference:
// The Nature of Code by Daniel Shiffman

// motion blur settings
int samplesPerFrame = 5;
float shutterAngle = 1.5;
int[][] result;

boolean recording = false;
int numFrames = 1500;
PFont font;

void push() {
  pushMatrix();
  pushStyle();
}

void pop() {
  popStyle();
  popMatrix();
}

// motion blur helper
void draw() {
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
        int(result[i][0]*1.0/samplesPerFrame) << 16 |
        int(result[i][1]*1.0/samplesPerFrame) << 8 |
        int(result[i][2]*1.0/samplesPerFrame);
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

// colors for populations
color[] colors = {  
                    #ff7657, 
                    #665c84, 
                    #a26ea1, 
                    #f18a9b, 
                    #ffb480,
                    #a26ea1,
                    #f18a9b,
                    #ffb480,
                    #ffba5a,
                    #45315d
                };

int lifetime;  // how long should each generation live
int population_size = 5;

// one color per population
Population[] populations = new Population[colors.length];

int lifecycle; // timer

Obstacle target; // target
ArrayList<Obstacle> obstacles; // obstacles

int start;
int timer;
boolean timer_finished = false;

void setup() {
  // 720p
  size(1280, 720, P2D);
  smooth(8);

  // assets
  font = loadFont("CMUSerif-BoldItalic-64.vlw");
  textFont(font, 18);

  // motion blur matrix
  result = new int[width*height][3];
  
  // number of cycles for a generation
  lifetime = 300;

  lifecycle = 0;
  
  // initialize target at top of screen
  target = new Obstacle(width/2-12, 24, 24, 24);
  
  // initialize populations
  for (int i = 0; i < populations.length; i++) {
    populations[i] = new Population(random(0.01, 0.1), population_size, colors[i]);
  }

  // initialize obstacles
  obstacles = new ArrayList<Obstacle>();
  obstacles.add(new Obstacle(width/2-100, height/2 + 200, 200, 10));
  obstacles.add(new Obstacle(width/2+100, height/2 -100, 200, 10));
  obstacles.add(new Obstacle(width/2-300, height/2 -100, 200, 10));

  timer = millis();
}

// drawing loop
void draw_() {
  // display info on window title bar
  String window = "FPS: " + str((int)frameRate) + " | Generation: " + populations[0].getGenerations() + " | Cycles Left: " + (lifetime-lifecycle) + "/" + lifetime + " | Population Size: " + populations.length + " populations of " + population_size + ": " + populations.length * population_size + " | Frames: " + frameCount;
  surface.setTitle(window);

  background(#f9f8eb);

  target.display();

  for (int i = 0; i < populations.length; i++) {
    if (lifecycle < lifetime) {
      populations[i].live(obstacles);
    } else {
      lifecycle = 0;
      // selection and reproduction for all populations
      for (int j = 0; j < populations.length; j++) {
        populations[j].fitness();
        populations[j].selection();
        populations[j].reproduction();
      }
    }
  }

  lifecycle++;

  // draw obstacles
  for (Obstacle obs : obstacles) {
    obs.display();
  }

  String info = "Generation: " + populations[0].getGenerations() + "\nPopulation Size: " + populations.length * population_size;
  text(info,20,35);
  
}
