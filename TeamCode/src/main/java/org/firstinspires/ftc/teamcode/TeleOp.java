package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp", group = "TeleOp")
public class TeleOp extends OpMode {
    private final RobotManager robotManager = new RobotManager(this);
    public static boolean fieldOriented = false;

    public static double CURRENT_LOOP_TIME_MS;

    @Override
    public void init() {
        robotManager.isRunning = true;
        robotManager.indicatorLight.setColor(Constants.LED_COLOR.RED);
        robotManager.refreshData();
        RobotManager.HardwareDevices.imu.resetYaw();
        RobotManager.HardwareDevices.pinpoint.resetPosAndIMU(); // run at start of auto instead
        robotManager.drivetrain.drivetrainSetTargets(Constants.SWERVE_STOP_FORMATION, Constants.SWERVE_NO_POWER);
        robotManager.opMode.telemetry.addLine("TeleOp Initialized");
        robotManager.opMode.telemetry.update();
        robotManager.indicatorLight.setColor(Constants.LED_COLOR.GREEN);
    }

    @Override
    public void init_loop() {
        robotManager.refreshData();
    }

    @Override
    public void start() {
        if (RobotManager.HardwareDevices.pinpoint.getDeviceStatus() != GoBildaPinpoint.DeviceStatus.READY) {
            robotManager.telemetryPermanent.addData("WARNING, PINPOINT STATUS:", RobotManager.HardwareDevices.pinpoint.getDeviceStatus());
        }
    }

    @Override
    public void loop() {
        resetRuntime();
        robotManager.refreshData();
        robotManager.drivetrain.drivetrainDirectionalInput(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, fieldOriented);

        robotManager.opMode.telemetry.addData("Voltage:", robotManager.getVoltage() + "V");
        robotManager.opMode.telemetry.addData("Current:", robotManager.getCurrent() + "A");
        robotManager.opMode.telemetry.addLine();
        robotManager.opMode.telemetry.addData("Pinpoint X:", PinpointUpdater.currentPose.getX(DistanceUnit.CM) + "cm");
        robotManager.opMode.telemetry.addData("Pinpoint Y:", PinpointUpdater.currentPose.getY(DistanceUnit.CM) + "cm");
        robotManager.opMode.telemetry.addData("Pinpoint Heading:", PinpointUpdater.currentHeading + "°");
        robotManager.opMode.telemetry.addLine();
        robotManager.opMode.telemetry.addData("Loop Time:", getRuntime() * 1000 + "ms");
        robotManager.opMode.telemetry.update();
        RobotManager.HardwareDevices.pinpoint.update();
        CURRENT_LOOP_TIME_MS = getRuntime() * 1000;
    }

    @Override
    public void stop() {
        robotManager.drivetrain.drivetrainSetTargets(Constants.SWERVE_STOP_FORMATION, Constants.SWERVE_NO_POWER);
        robotManager.isRunning = false;
    }
}
