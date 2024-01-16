package org.firstinspires.ftc.teamcode.CenterStageRobot.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.arcrobotics.ftclib.controller.wpilibcontroller.ElevatorFeedforward;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.hardware.motors.MotorGroup;
import com.arcrobotics.ftclib.util.MathUtils;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.inventors.ftc.robotbase.hardware.MotorExEx;

import java.util.function.DoubleSupplier;

public class ElevatorSubsystem extends SubsystemBase {
    public boolean isAuto = true;
    private final MotorExEx leftMotor;
    private final MotorExEx rightMotor;
    private final MotorGroup motors;

    private boolean isAutoEnabled = true;

    private PIDFController controller;
    private double calculation = 0.0;

    private ElevatorFeedforward feedforward;
    public double feedforwardCalc = 0;

    public double MAX_SPEED = 0.5; // TODO: Speed Value Might Change

    public enum Level {
        LOADING, LOW, MID, HIGH;
    }

    private Level level;

    private int[] levelPositions = {0, 1050, 1650, 1750}; // TODO: Level Values Might Change

    private Telemetry telemetry;
    private DoubleSupplier leftY;

    public ElevatorSubsystem(HardwareMap hm, Telemetry telemetry, DoubleSupplier leftY) {
        this.leftY = leftY;
        this.telemetry = telemetry;
        leftMotor = new MotorExEx(hm, "slider_left", 383.6, 435);
        rightMotor = new MotorExEx(hm, "slider_right", 383.6, 435);
        leftMotor.setMaxPower(MAX_SPEED);
        rightMotor.setMaxPower(MAX_SPEED);
        leftMotor.setInverted(true);
        motors = new MotorGroup(leftMotor, rightMotor);

        motors.setZeroPowerBehavior(MotorEx.ZeroPowerBehavior.BRAKE);
        motors.setPositionTolerance(30); // TODO: allowed maximum error value might change
        motors.setPositionCoefficient(0.014); // TODO: Kp Value might change

        motors.resetEncoder();

        setAuto();
        level = Level.LOADING;
        setLevel(Level.LOADING);
    }

    @Override
    public void periodic() {
//        feedforwardCalc = feedforward.calculate(motors.getVelocity());
        telemetry.addData("Height", getHeight());
        telemetry.addData("Stick", leftY.getAsDouble());
        if (isAuto) {
            telemetry.addData("Auto", "");
        } else {
            telemetry.addData("Manual", "");
        }
        telemetry.update();
    }

    public void run() {
        motors.set(MAX_SPEED);
    }

    public void stop() {
        motors.stopMotor();
    }

    public void setLevel(Level levelPicked) {
        level = levelPicked;
        int levelIdx = 0;

        if(level == Level.LOADING)
            levelIdx = 0;
        else if (level == Level.LOW)
            levelIdx = 1;
        else if (level == Level.MID)
            levelIdx = 2;
        else if (level == Level.HIGH)
            levelIdx = 3;

        motors.setTargetPosition(levelPositions[levelIdx]);
    }

    public void setManual() {
        motors.setRunMode(Motor.RunMode.RawPower);
        isAuto = false;
    }

    public void setAuto() {
        motors.setRunMode(Motor.RunMode.PositionControl);
        isAuto = true;
//        motors.setTargetPosition(getHeight().intValue());
    }

    public void setPower(double power) {
        motors.set(power);
    }

    public Double getHeight(){
        return motors.getPositions().get(0);
    }

    public Level getLevel() {
        return level;
    }

    public boolean atTargetLevel() {
        return motors.atTargetPosition();
    }
}
