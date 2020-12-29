#include <AFMotor.h> // motor library 


AF_DCMotor motor1(1, MOTOR12_64KHZ); // initializes right motor and sets frequency to 64KHZ
AF_DCMotor motor3(3, MOTOR12_64KHZ); // initializes left motor and sets frequency to 64KHZ

int motorSpeed=0; 
int continousSpeed=0;

void setup() 
{
    Serial.begin(9600);
    motor1.setSpeed(0); // initializes initial speed of right motor to 0.
    motor3.setSpeed(0); // initializes initial speed of left motor to 0.
}
 
void loop() 
{
    if(Serial.available() > 0) 
    {
         int commandFromApp= Serial.read();      // reads input from app in the form of a number
         continousSpeed= commandFromApp;         // if continousSpeed 6 or greater, then it will continously loop that command 
         Serial.println(continousSpeed);
         
                if (continousSpeed == 0)         // stops robot and turns on red LED for half a second
                {
                    motor1.setSpeed(0);          // sets speed of both motors to 0
                    motor3.setSpeed(0); 
                    digitalWrite(10, HIGH);      // turns on red LED for half a second
                    delay(500);
                    digitalWrite(10, LOW);       // turns off red LED
                    Serial.println("OFF");
                 }

                 else if (continousSpeed== 1)   // stops robot, turns on blue LED for half a second, and "music"
                 {
                    motor1.setSpeed(0);          // sets speed of both motors to 0
                    motor3.setSpeed(0); 
                    Serial.println("ON"); 
                 }

                 else if (continousSpeed == 2)   // turns robot left continously 
                 {
                    motor1.setSpeed(255);        // sets speed of both motors to 150    
                    motor3.setSpeed(255); 
                    motor1.run(FORWARD);        // motor 1 goes forward  ..... backward and forward are switched 
                    motor3.run(BACKWARD);         // motor 2 goes backward ..... backward and forward are switched 
                    Serial.println("LEFT"); 
                 }

                 else if (continousSpeed == 3)   // turns robot right continously 
                 {
                    motor1.setSpeed(255);        // sets speed of both motors to 150 
                    motor3.setSpeed(255); 
                    motor1.run(BACKWARD);         // motor 1 goes backward ..... backward and forward are switched 
                    motor3.run(FORWARD);        // motor 2 goes forward ..... backward and forward are switched 
                    Serial.println("RIGHT");
                 }

                 else if (continousSpeed == 4)   // stops robot from turning left 
                 {
                    motor1.setSpeed(0);          // sets speed of both motors to 0
                    motor3.setSpeed(0); 
                    Serial.println("LEFT STOP");
                 }

                 else if (continousSpeed == 5)   // stops robot from turning right 
                 {
                    motor1.setSpeed(0);          // sets speed of both motors to 0 
                    motor3.setSpeed(0); 
                    Serial.println("RIGHT STOP");
                 }

                 else if (continousSpeed == 6)   // moves robot forward with a speed of 150 continously unless stopped or turns
                 {
                    motorSpeed=150;
                    motor1.setSpeed(motorSpeed); // sets speed of both motors to 150 
                    motor3.setSpeed(motorSpeed); 
                    motor3.run(FORWARD);        // both motors move forward... backward and forward are switched
                    motor1.run(FORWARD);
                    Serial.println("150" + motorSpeed);
                 }

                 else if (continousSpeed == 7)   // moves robot forward with a speed of 175 continously unless stopped or turns
                 {
                    motorSpeed=175;
                    motor1.setSpeed(motorSpeed); // sets speed of both motors to 175
                    motor3.setSpeed(motorSpeed); 
                    motor3.run(FORWARD);        // both motors move forward... backward and forward are switched
                    motor1.run(FORWARD);
                    Serial.println("175" + motorSpeed);
                 }

                 else if (continousSpeed == 8)   // moves robot forward with a speed of 200 continously unless stopped or turns
                 {
                    motorSpeed=200;
                    motor1.setSpeed(motorSpeed); // sets speed of both motors to 200
                    motor3.setSpeed(motorSpeed); 
                    motor3.run(FORWARD);        // both motors move forward... backward and forward are switched
                    motor1.run(FORWARD);
                    Serial.println("200" +motorSpeed);
                 }

                 else if (continousSpeed == 9)   // moves robot forward with a speed of 225 continously unless stopped or turns
                 {
                    motorSpeed=225;
                    motor1.setSpeed(motorSpeed); // sets speed of both motors to 225
                    motor3.setSpeed(motorSpeed); 
                    motor1.run(FORWARD);        // both motors move forward... backward and forward are switched
                    motor3.run(FORWARD);
                    Serial.println("225" +motorSpeed);
                 }

                 else if (continousSpeed == 10) // moves robot forward with a speed of 255 continously unless stopped or turns
                 {
                    motorSpeed=255;
                    motor1.setSpeed(motorSpeed);// sets speed of both motors to 255
                    motor3.setSpeed(motorSpeed); 
                    motor1.run(FORWARD);       // both motors move forward... backward and forward are switched
                    motor3.run(FORWARD);
                    Serial.println("255" +motorSpeed);
                 }       
    }
}

