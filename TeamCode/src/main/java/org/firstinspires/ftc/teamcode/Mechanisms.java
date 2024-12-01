package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Mechanisms {

    ElapsedTime totalTime = new ElapsedTime();

    // Servos and motors for outtake/intake.
    DcMotor outTakeLift;


    Servo outTakeLargePivotExpansion;
    Servo outTakeLargePivotControl;
    Servo outTakeClawPivot;
    Servo outTakeClaw;

    DcMotor inTakeLift;
    Servo inTakeClaw;
    Servo inTakeFlipControl;
    Servo inTakeFlipExpansion;
    Servo inTakeRotator;


    // Mechanism stuff
    double CLOSED_OT_POS = 0;
    double OPEN_OT_POS = .5;
    double clawInc = 0;
    double lastClawTime;
    double UP_OT_FLIP_POS = 0;
    double DOWN_OT_FLIP_POS = 1;
    double UP_OT_PIVOT_POS = 1;
    double DOWN_OT_PIVOT_POS = 0;

    double PERP_IT_POS = 0;
    double PAR_IT_POS = .33;
    double CLOSED_IT_POS = 1;
    double OPEN_IT_POS = .76;
    double UP_IT_FLIP_POS = .7;
    double DOWN_IT_FLIP_POS = .42;
    double MID_IT_FLIP_POS = .5;
    double OUT_IT_FLIP_POS = 0;
    double OUT_DOWN_IT_FLIP_POS = .26;


    boolean upPivotOT = true;
    double pivotTimeOT = 0;

    OpMode master;

    public void init(OpMode opMode)
    {
        //outTakeLift = opMode.hardwareMap.dcMotor.get("otl");
        //outTakeLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //outTakeClaw = opMode.hardwareMap.servo.get(("otc"));
        //outTakeFlip = opMode.hardwareMap.servo.get(("otf"));

        // Intake lift
        //inTakeLift = opMode.hardwareMap.dcMotor.get("itl");
        //inTakeLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //inTakeFlipControl.getController().pwmEnable();

        // Control hub servos
        inTakeFlipControl = opMode.hardwareMap.servo.get(("itfc")); // port 0
        inTakeFlipExpansion = opMode.hardwareMap.servo.get("itfe"); // port 1
        inTakeRotator = opMode.hardwareMap.servo.get("itr"); // port 2
        inTakeClaw = opMode.hardwareMap.servo.get(("itc")); // port 3


        // Expansion hub servos
        //outTakeLargePivotExpansion = opMode.hardwareMap.servo.get(("otse"));
        //outTakeLargePivotControl = opMode.hardwareMap.servo.get(("otsc"));
        //outTakeClawPivot = opMode.hardwareMap.servo.get(("otcp"));
        //outTakeClaw = opMode.hardwareMap.servo.get(("otc"));

        master = opMode;
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setOutTakeLift(){
        outTakeLift.setPower(master.gamepad2.left_stick_y);
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setOutTakeClawGrab(){
        if (master.gamepad2.x && lastClawTime < totalTime.milliseconds() - 500)
        {
            lastClawTime = totalTime.milliseconds();
            clawInc += .1;
            outTakeClaw.setPosition(clawInc);
            outTakeClaw.setDirection(Servo.Direction.REVERSE);
        }
        //outTakeClaw.setPosition(CLOSED_OT_POS);
        if (master.gamepad2.y && lastClawTime < totalTime.milliseconds() - 500)
        {
            lastClawTime = totalTime.milliseconds();
            clawInc -= .1;
            outTakeClaw.setPosition(clawInc);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setOutTakeFlip(){
        if (master.gamepad2.left_trigger > .1)
            outTakeLargePivotControl.setPosition(DOWN_OT_FLIP_POS);
        if (master.gamepad2.right_trigger > .1)
            outTakeLargePivotControl.setPosition(UP_OT_FLIP_POS);
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setOutTakePivot(){
        if (master.gamepad2.dpad_right && pivotTimeOT < totalTime.milliseconds() - 500)
        {
            if (upPivotOT)
            {
                outTakeClawPivot.setPosition(DOWN_OT_PIVOT_POS);
                upPivotOT = false;
            }
            else
            {
                outTakeClawPivot.setPosition(UP_OT_PIVOT_POS);
                upPivotOT = true;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setInTakeClawGrab(){
        if (master.gamepad2.a)
            inTakeClaw.setPosition(CLOSED_IT_POS);
        if (master.gamepad2.b)
            inTakeClaw.setPosition(OPEN_IT_POS);
        if (master.gamepad2.dpad_right)
            inTakeClaw.setPosition(.65);
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setInTakeFlip(){
        if (master.gamepad2.dpad_up)
            inTakeFlipExpansion.setPosition(UP_IT_FLIP_POS);
        if (master.gamepad2.dpad_down)
            inTakeFlipExpansion.setPosition(DOWN_IT_FLIP_POS);
        if (master.gamepad2.dpad_left)
        {
            inTakeFlipExpansion.setPosition(MID_IT_FLIP_POS);
        }
        if (master.gamepad2.x)
            inTakeFlipExpansion.setPosition(OUT_IT_FLIP_POS);
        if (master.gamepad2.y)
            inTakeFlipExpansion.setPosition(OUT_DOWN_IT_FLIP_POS);
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setInTakeRotator(){
        if (master.gamepad2.left_bumper)
            inTakeRotator.setPosition(PAR_IT_POS);
        if (master.gamepad2.right_bumper)
            inTakeRotator.setPosition(PERP_IT_POS);
    }

    ////////////////////////////////////////////////////////////////////////////////
    public void setInTakeLift(){
        inTakeLift.setPower(master.gamepad2.right_stick_y);
        if (master.gamepad2.left_trigger > .1)
        {
            inTakeLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }
        if (master.gamepad2.right_trigger > .1)
        {
            inTakeLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    public void runTesting()
    {
        if (master.gamepad2.a)
        {
            // up
            inTakeClaw.setPosition(.76);
        }
        if (master.gamepad2.b)
        {
            //grab
            inTakeFlipExpansion.setPosition(.3);
            //inTakeFlipControl.getController().setServoPosition();
        }
        if (master.gamepad2.dpad_up)
        {
            //put in transfer
            inTakeFlipExpansion.setPosition(.5);
        }
        if (master.gamepad2.dpad_down)
        {
            //put in transfer
            inTakeRotator.setPosition(.32);
        }
        if (master.gamepad2.dpad_left)
        {
            inTakeRotator.setPosition(0);
        }
        if (master.gamepad2.x)
        {
            inTakeFlipExpansion.setPosition(.7);
        }
        if (master.gamepad2.y)
        {
            inTakeClaw.setPosition(1);
        }
        if (master.gamepad2.left_bumper)
        {
            outTakeLift.setPower(.8);
        }
        else if (master.gamepad2.right_bumper)
        {
            outTakeLift.setPower(-.8);
        }
        else
        {
            outTakeLift.setPower(0);
        }
        if (master.gamepad2.left_trigger > .1)
        {
            inTakeLift.setPower(.8);
        }
        else if (master.gamepad2.right_trigger > .1)
        {
            inTakeLift.setPower(-.8);
        }
        else {
            inTakeLift.setPower(0);
        }
    }

}
