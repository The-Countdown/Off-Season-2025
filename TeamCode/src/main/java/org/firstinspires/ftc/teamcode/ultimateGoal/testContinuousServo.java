//package org.firstinspires.ftc.masters;
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.hardware.CRServo;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//import com.qualcomm.robotcore.hardware.Servo;
//
//@Disabled
//public class testContinuousServo extends LinearOpMode {
//
//    private CRServo crServo;
//
//    @Override
//    public void runOpMode() throws InterruptedException {
//        crServo = hardwareMap.get(CRServo.class, "cRServo1");
//
//        waitForStart();
//
//        while(opModeIsActive()){
//            crServo.setPower(-0.8);
//        }
//
//
//    }
//}
