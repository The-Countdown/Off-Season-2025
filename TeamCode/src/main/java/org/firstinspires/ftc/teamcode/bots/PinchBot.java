package org.firstinspires.ftc.teamcode.bots;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.*;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class PinchBot extends PivotBot{

    private boolean isOpen = false;

    public Servo pinch;

    public Servo rotate;

    private double servoPos;


    private double servoMax = 0.78;
    private double servoMin = 0.45;
    public PinchBot(LinearOpMode opMode) {
        super(opMode);
    }


    public void init(HardwareMap hardwareMap){
        super.init(hardwareMap);
        pinch = hardwareMap.get(Servo.class, "pinch");
        rotate = hardwareMap.get(Servo.class, "rotate");
    }

    protected void onTick() {
        super.onTick();

        rotate.setPosition(servoPos);
    }

    public void pinchControl(boolean open, boolean close){

        if (open) {

            isOpen = true;
            pinch.setPosition(0.5);

        }
        if (close) {

            isOpen = false;
            pinch.setPosition(1);

        }
    }
    public void rotate(double angle){ //5216 - 4706
        double maxAnglePos = 0.1;
        double minAnglePos = 0;
        if(angle>maxAnglePos){
            angle = maxAnglePos;
        }
        if(angle<minAnglePos){
            angle = minAnglePos;
        }

        double servoPos = (angle/90)*maxAnglePos;
        rotate.setPosition(servoPos);
    }

    public void rotateControl(boolean left, boolean right){


        if(left){
            servoPos -= 0.01;
        }
        if(right){
            servoPos += 0.01;
        }

        if (servoPos > servoMax) {

            servoPos = servoMax;

        }

        if (servoPos < servoMin) {

            servoPos = servoMin;

        }
    }
    public void pickUp(boolean button){
        // use horizontalDistance() to move robot and verticalDistance() to move slide
        // figure out how to find angle and use rotate(angle)

        double VERTICAL_OFFSET = 2;
        double VERTICAL_PROPORTION = 2;
        double HORIZONTAL_PROPORTION = 2;

        double[] position = detectOne();
        double x = position[0];
        double y = position[1];
        double theta = position[2];
        moveSlide((int) ((y + VERTICAL_OFFSET)*VERTICAL_PROPORTION)); //move slide vertically
        driveStraightByDistance(90, x*HORIZONTAL_PROPORTION, 2);
        rotate(theta);
        isOpen = false;
        //pinchControl();

        //slideControl(false, true);
        //rotate(CENTER_POSITION);

    }
}
