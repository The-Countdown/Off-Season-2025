package org.firstinspires.ftc.teamcode;

import android.os.Looper;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import java.util.List;
import android.os.Handler;

/**
 * The Robot class serves as the central manager for all robot hardware and high-level operations.
 * It initializes and provides access to key components such as the IMU, Limelight, swerve drive modules,
 * and associated control mechanisms. This class also manages the interaction with the FTC SDK, handles
 * asynchronous tasks through a Handler, and provides utility methods for common robot operations.
 * The {@link HardwareDevices} nested class contains static members that describe the different types of hardware the robot uses.
 * This class acts as the main interface for controlling the robot, offering a structured and organized
 * approach to managing complex robotic systems.
 */

@SuppressWarnings("all")
public class Robot {
    HardwareMap hardwareMap;
    OpMode opMode;
    public boolean isRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    public final SwerveModule[] swerveModules = new SwerveModule[4];
    public SwerveServoPIDF[] swerveServosPIDF = new SwerveServoPIDF[HardwareDevices.swerveServos.length];
    private ThreadedPIDF threadedPIDF;

    public static class HardwareDevices {
        public static List<LynxModule> allHubs;

        public static IMU imu;
        public static Limelight3A limelight;
        public static RevColorSensorV3 flashLight;

        /*Like a coordinate plane https://www.mathplanet.com/education/algebra-1/visualizing-linear-functions/the-coordinate-plane,
         the first quadrant is 0, the second is 1, etc. relating to the positions of the modules on the robot */
        public static DcMotorEx[] swerveMotors = new DcMotorEx[4];
            public static String[] motorNames = new String[4];

        public static CRServoImplEx[] swerveServos = new CRServoImplEx[4];
            public static String[] servoNames = new String[4];

        public static AnalogInput[] swerveAnalogs = new AnalogInput[4];
            public static String[] analogNames = new String[4];
    }

    public Robot(OpMode opMode) {
        this.opMode = opMode;
        this.hardwareMap = opMode.hardwareMap;

        HardwareDevices.allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : HardwareDevices.allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        HardwareDevices.imu = hardwareMap.get(IMU.class, HardwareDevices.imu.getDeviceName());
            BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
            parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
            parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
            parameters.calibrationDataFile = "BNO055IMUCalibration.json";
            parameters.loggingEnabled      = true;
            parameters.loggingTag          = "IMU";
            parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        HardwareDevices.limelight = hardwareMap.get(Limelight3A.class, "limeLight");
            HardwareDevices.flashLight = hardwareMap.get(RevColorSensorV3.class, "flashLight");

        for (int i = 0; i < HardwareDevices.swerveMotors.length; i++) {
            HardwareDevices.motorNames[i] = "swerveMotor" + (i);
            HardwareDevices.swerveMotors[i] = hardwareMap.get(DcMotorEx.class, HardwareDevices.motorNames[i]);
        }

        for (int i = 0; i < HardwareDevices.swerveServos.length; i++) {
            HardwareDevices.servoNames[i] = "swerveServo" + (i);
            HardwareDevices.swerveServos[i] = hardwareMap.get(CRServoImplEx.class, HardwareDevices.servoNames[i]);
        }

        for (int i = 0; i < HardwareDevices.swerveAnalogs.length; i++) {
            HardwareDevices.analogNames[i] = "swerveAnalog" + (i);
            HardwareDevices.swerveAnalogs[i] = hardwareMap.get(AnalogInput.class, HardwareDevices.analogNames[i]);
        }

        for (int i = 0; i < swerveModules.length; i++) {
            swerveModules[i] = new SwerveModule(this,
                    HardwareDevices.swerveMotors[i],
                    HardwareDevices.swerveServos[i],
                    HardwareDevices.swerveAnalogs[i],
                    i);
        }

        for (int i = 0; i < swerveServosPIDF.length; i++) {
            swerveServosPIDF[i] = new SwerveServoPIDF(this, i, HardwareDevices.swerveServos[i]);
        }

        threadedPIDF = new ThreadedPIDF(this);
        threadedPIDF.start();
    }

    /**
     * Runs a sequence of actions with specified delays between them.
     * This method takes a list of actions (Runnable objects) and a corresponding list of delays.
     * It ensures that each action is executed after the specified delay from the previous action.
     *
     * @param actions A list of Runnable actions to be executed in sequence.
     * @param delays  A list of delay times (in milliseconds) corresponding to each action.
     *                The size of this list must be the same as the 'actions' list.
     * @throws IllegalArgumentException If the 'actions' and 'delays' lists have different lengths.
     */
    public void runActionSequence(List<Runnable> actions, List<Long> delays) {
        if (actions.size() != delays.size()) {
            throw new IllegalArgumentException("Actions and delays must have the same length.");
        }
        // Start the recursive process of running actions with delays, starting at index 0.
        runActionSequenceInternal(actions, delays, 0);
    }
    /**
     * Internal recursive method to run the action sequence.
     * This method recursively executes the actions in the 'actions' list with the delays specified
     * in the 'delays' list. It uses a Handler to schedule each action to run after the specified delay.
     *
     * @param actions A list of Runnable actions to be executed.
     * @param delays  A list of delay times (in milliseconds) for each action.
     * @param index   The current index of the action and delay to be processed.
     */
    private void runActionSequenceInternal(List<Runnable> actions, List<Long> delays, int index) {
        // Base case: If the index is past the end of the list, we're done.
        if (index >= actions.size()) return;

        // Run the action at the current index.
        actions.get(index).run();

        // Schedule the next action to run after the specified delay.
        // Recursively call this method for the next action (index + 1).
        handler.postDelayed(() -> runActionSequenceInternal(actions, delays, index + 1), delays.get(index));
    }

    /**
     * Refreshes the data from all Lynx Modules (hubs) by clearing their bulk data cache.
     *
     * This method is crucial for ensuring that the robot is operating with the most up-to-date
     * sensor and motor data. The Lynx Modules use a bulk data cache to optimize data transfer.
     * However, if data in this cache becomes stale, the robot's actions might be based on
     * outdated information.
     *
     * This method should be called periodically or whenever you suspect that the data in the
     * bulk cache might be outdated. Common scenarios include:
     * - At the start of a new control loop iteration in teleop or autonomous.
     * - After a significant delay or pause in the robot's operation.
     * - Before reading critical sensor values that need to be absolutely current.
     * - If there is a change in the bulk caching mode.
     *
     * Calling this method ensures that the next time you read data from the hubs, the latest
     * information will be fetched, rather than possibly outdated cached data.
     */
    public void refreshData() {
        for (LynxModule hub : HardwareDevices.allHubs) {
            hub.clearBulkCache();
        }
    }

    public double getVoltage() {
        // Sets the voltage to -1 in case it is not set by the LynxModule
        double voltage = -1;
        for (LynxModule hub : Robot.HardwareDevices.allHubs) {
            voltage = hub.getInputVoltage(VoltageUnit.VOLTS);
        }

        return voltage;
    }

    public double getCurrent() {
        // Sets the current to -1 in case it is not set by the LynxModule
        double current = -1;
        for (LynxModule hub : Robot.HardwareDevices.allHubs) {
            current = hub.getCurrent(CurrentUnit.MILLIAMPS);
        }

        return current;
    }

    public Drivetrain drivetrain = new Drivetrain(this);
}
