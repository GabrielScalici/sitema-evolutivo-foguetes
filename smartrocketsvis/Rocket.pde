class Rocket {

  // physics
  PVector position;
  PVector velocity;
  PVector acceleration;

  // size
  float r;

  // color
  color c = colors[(int)random(colors.length)];

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
  Rocket(PVector l, DNA dna_, int totalRockets, color c_) {
    acceleration = new PVector();
    velocity = new PVector();
    position = l.get();
    r = 2;
    dna = dna_;
    finishTime = 0;
    recordDist = 10000;
    c = c_;
  }

  // FITNESS FUNCTION 
  // distance = distance from target
  // finish = what order did i finish (first, second, etc. . .)
  // f(distance,finish) =   (1.0f / finish^1.5) * (1.0f / distance^6);
  // a lower finish is rewarded (exponentially) and/or shorter distance to target (exponetially)
  void fitness() {
    if (recordDist < 1) recordDist = 1;

    // Reward finishing faster and getting close
    fitness = (1/(finishTime*recordDist));

    // Make the function exponential
    fitness = pow(fitness, 4);

    if (hitObstacle) fitness *= 0.1; // lose 90% of fitness hitting an obstacle
    if (hitTarget) fitness *= 2; // twice the fitness for finishing!
  }

  // Run in relation to all the obstacles
  void run(ArrayList<Obstacle> os) {
    if (!hitObstacle && !hitTarget) {
      applyForce(dna.genes[geneCounter]);
      geneCounter = (geneCounter + 1) % dna.genes.length;
      update();
      // If I hit an edge or an obstacle
      obstacles(os);
    }
    // Draw me!
    if (!hitObstacle) {
      display();
    }
  }

  void checkTarget() {
    float d = dist(position.x, position.y, target.position.x, target.position.y);
    if (d < recordDist) recordDist = d;

    if (target.contains(position) && !hitTarget) {
      hitTarget = true;
    } 
    else if (!hitTarget) {
      finishTime++;
    }
  }

  void obstacles(ArrayList<Obstacle> os) {
    for (Obstacle obs : os) {
      if (obs.contains(position)) {
        hitObstacle = true;
      }
    }
  }

  void applyForce(PVector f) {
    acceleration.add(f);
  }

  void update() {
    velocity.add(acceleration);
    position.add(velocity);
    acceleration.mult(0);
  }

  void display() {
    float theta = velocity.heading2D() + PI/2;
    pushMatrix();

    translate(position.x, position.y);
    rotate(theta);

    fill(c);
    noStroke();
    beginShape(TRIANGLES);
    vertex(0, -r*2);
    vertex(-r, r*2);
    vertex(r, r*2);
    endShape();

    popMatrix();
  }

  float getFitness() {
    return fitness;
  }

  DNA getDNA() {
    return dna;
  }

}

