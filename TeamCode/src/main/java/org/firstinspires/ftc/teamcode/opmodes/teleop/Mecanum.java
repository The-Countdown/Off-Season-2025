package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.TurtleRobotTeleOp;
//  Controls:
// left stick forward and backward
// right stick left and right to strafe
// left stick left and right to turn
// a to move linear slide up
// b to move linear slide down

@TeleOp(name = "Mecanum")
public class Mecanum extends LinearOpMode {

    double frontLeftDrive, frontRightDrive, backRightDrive, backLeftDrive, armServo, clawServo;
    double driveSpeed = 1;
    static final double     COUNTS_PER_MOTOR_REV    = 537.7 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // No External Gearing.
    static final double     WHEEL_DIAMETER_INCHES   =  3.7795276;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     DRIVE_SPEED             = 0.6;
    static final double     TURN_SPEED              = 0.5;

    @Override
    public void runOpMode() {
        TurtleRobotTeleOp robot = new TurtleRobotTeleOp(this);
        robot.init(hardwareMap);
        waitForStart();
        if (opModeIsActive()) {
            while (opModeIsActive()) {
                while (gamepad2.right_bumper) {
                    robot.leftslidemotor.setPower(1);
                    robot.rightslidemotor.setPower(1);
                }
                while (gamepad2.left_bumper) {
                    robot.leftslidemotor.setPower(-0.5);
                    robot.rightslidemotor.setPower(-0.5);
                }
                robot.leftslidemotor.setPower(0);
                robot.rightslidemotor.setPower(0);
            //                     FORWARD                     TURN                       STRAFE
            frontRightDrive = (-gamepad1.left_stick_y - gamepad1.right_stick_x - gamepad1.left_stick_x)*driveSpeed;
            frontLeftDrive  = (-gamepad1.left_stick_y + gamepad1.right_stick_x + gamepad1.left_stick_x)*driveSpeed;
            backRightDrive  = (-gamepad1.left_stick_y - gamepad1.right_stick_x + gamepad1.left_stick_x)*driveSpeed;
            backLeftDrive   = (-gamepad1.left_stick_y + gamepad1.right_stick_x - gamepad1.left_stick_x)*driveSpeed;
            clawServo = (gamepad2.right_stick_y);
            armServo = (gamepad2.left_stick_y);

            robot.rightfrontmotor.setPower(frontRightDrive);
            robot.rightbackmotor.setPower(backRightDrive);
            robot.leftbackmotor.setPower(backLeftDrive);
            robot.leftfrontmotor.setPower(frontLeftDrive);
            robot.ArmServo.setPower(clawServo);
            robot.ClawMotor.setPower(armServo);
            if (frontLeftDrive>0 && frontRightDrive>0 && backLeftDrive>0 && backRightDrive>0) {
                telemetry.addLine("Going forward");
            }
            if (frontLeftDrive>0 && frontRightDrive>0 && backLeftDrive<0 && backRightDrive<0 || frontLeftDrive<0 && frontRightDrive<0 && backLeftDrive<0 && backRightDrive<0) {
                telemetry.addLine("Turning");
            }
            if (frontLeftDrive>0 && frontRightDrive<0 && backLeftDrive>0 && backRightDrive<0 || frontLeftDrive<0 && frontRightDrive>0 && backLeftDrive<0 && backRightDrive>0) {
                telemetry.addLine("Strafing");
            }


            telemetry.addLine("motor name               motor speed");
            telemetry.addLine();
            telemetry.addData("Front right drive power = ", frontRightDrive);
            telemetry.addData("Front left drive power  = ", frontLeftDrive);
            telemetry.addData("Back right drive power  = ", backRightDrive);
            telemetry.addData("Back left drive power   = ", backLeftDrive);
            telemetry.update();
            }
        }
    }
}
