package org.firstinspires.ftc.teamcode.drivetrain;

import org.firstinspires.ftc.teamcode.main.Constants;
import org.firstinspires.ftc.teamcode.util.GamepadWrapper;
import org.firstinspires.ftc.teamcode.other.PinpointUpdater;
import org.firstinspires.ftc.teamcode.main.RobotManager;

/**
 * This class handles the control and calculations for the robot's drivetrain, including swerve drive functionality.
 */
public class Drivetrain extends RobotManager.HardwareDevices {
    private final RobotManager robotManager;
    private final GamepadWrapper.ButtonReader rXtoggle = new GamepadWrapper.ButtonReader();
    private boolean rotationWasZero = false;

    /**
     * Constructor for the Drivetrain class.
     * @param robotManager The Robot object that contains all hardware devices.
     */
    public Drivetrain(RobotManager robotManager) {
        this.robotManager = robotManager;
    }

    /**
     * This method calculates and sets the target angles and powers for each swerve module based on directional inputs.
     *
     * @param x             The x-axis translational input from -1 to 1.
     * @param y             The y-axis translational input from -1 to 1.
     * @param rX            The rotational input from -1 to 1.
     * @param fieldOriented A boolean indicating whether to use field-oriented driving.
     */
    public void drivetrainDirectionalInput(double x, double y, double rX, boolean fieldOriented) {
        // Calculate the magnitude of translational movement.
        double translationalMagnitude = Math.sqrt(x * x + y * y);
        // Calculate the angle of translational movement.
        double translationalAngle = Math.atan2(y, x);
        // Set the initial translational direction to forward.
        int translationalDirection = 1;

        double rotationalMagnitude = Math.abs(rX);
        // Determine the rotational direction based on the sign of rX.
        int rotationalDirection = rX >= 0 ? 1 : -1;

        boolean noRotationInput = rX == 0;
        rXtoggle.update(!noRotationInput);
        if (noRotationInput) {
            if (rXtoggle.wasJustReleased()) {
                robotManager.headingPID.setTargetHeading(PinpointUpdater.currentHeading);
            }
            rotationalMagnitude = robotManager.headingPID.calculate(PinpointUpdater.currentHeading);
        }

        double currentHeading = PinpointUpdater.currentHeading;
        // Adjust the translational angle for field-oriented driving if enabled.
        translationalAngle = fieldOriented ? translationalAngle - currentHeading : translationalAngle;
        translationalAngle = normalizeAngle(translationalAngle);

        double[] calculatedAngles = new double[robotManager.swerveModules.length];
        double[] calculatedPowers = new double[robotManager.swerveModules.length];

        // Iterate through each swerve module to calculate its target angle and power.
        for (int i = 0; i < robotManager.swerveModules.length; i++) {
            // Calculate the x and y components of translational movement.
            double translationalX = translationalMagnitude * Math.cos(translationalAngle) * translationalDirection;
            double translationalY = translationalMagnitude * Math.sin(translationalAngle) * translationalDirection;

            double rotationalAngle = Math.toRadians(Constants.SWERVE_ROTATION_FORMATION[i]);
            // Calculate the x and y components of rotational movement.
            double rotationalX = rotationalMagnitude * Math.cos(rotationalAngle) * rotationalDirection;
            double rotationalY = rotationalMagnitude * Math.sin(rotationalAngle) * rotationalDirection;

            // Combine the translational and rotational components into a single vector.
            double vectorX = translationalX + rotationalX;
            double vectorY = translationalY + rotationalY;

            // Calculate the magnitude and angle of the combined vector.
            double vectorMagnitude = Math.sqrt(vectorX * vectorX + vectorY * vectorY);
            double vectorAngle = Math.atan2(vectorY, vectorX);

            calculatedAngles[i] = Math.toDegrees(vectorAngle);
            calculatedPowers[i] = vectorMagnitude;
        }

        drivetrainSetTargets(calculatedAngles, scalePowers(calculatedPowers));
    }

    /**
     * This method sets the target angles and powers for each swerve module, handling motor inversion if necessary.
     * @param targetAngles An array of target angles for each swerve module.
     * @param targetPowers An array of target powers for each swerve module.
     */
    public void drivetrainSetTargets(double[] targetAngles, double[] targetPowers) {
        targetPowers = scalePowers(targetPowers);
        for (int i = 0; i < swerveServos.length; i++) {
            double currentAngle = robotManager.swerveModules[i].servo.getAngle();
            double error = targetAngles[i] - currentAngle;
            error = normalizeAngle(error);

            /**
             * if the error is greater than 90 the direction of the wheel needs to be flipped, so we add 180 to the target angle
             * and we invert the powers.
             *
             * if it is not greater then we just pass in the regular power.
             *
             */
            if (Math.abs(error) > 90) {
                targetAngles[i] = normalizeAngle(targetAngles[i] + 180);
                robotManager.swerveModules[i].motor.setPower(-targetPowers[i]);
            } else {
                robotManager.swerveModules[i].motor.setPower(targetPowers[i]);
            }

            robotManager.swerveModules[i].servo.setTargetAngle(targetAngles[i]);
        }
    }

    /**
     * This function scales all the powers to ensure they are within the -1 and 1 ranges.
     * This avoids breaking any of the robot functions.
     * @param powers the array of powers for all swerve modules.
     * @return the same array, but with the powers being scaled appropriately.
     */
    public double[] scalePowers(double[] powers) {
        double maxPower = 0;
        // Iterate through each power value in the array.
        for (int i = 0; i < powers.length; i++) {
            // if the power is greater then the current max power then we update it.
            if(powers[i] > maxPower){
                maxPower = powers[i];
            }
        }
        // if the maxPower is above one, that means that we need to scale.
        if(maxPower > 1){
            // iterate through and divide every element by the max power.
            for (int i = 0; i < powers.length; i++) {
                powers[i] /= maxPower;
            }
        }

        return powers;
    }

    /**
     * Normalizes an angle to the range [-180, 180).
     *
     * @param angle The angle to normalize.
     * @return The normalized angle.
     */
    public double normalizeAngle(double angle) {
        // Check if the angle is already in the desired range.
        if (angle >= -180 && angle < 180) {
            return angle;
        }

        // Normalize the angle to the range [-360, 360).
        double normalizedAngle = angle % 360;

        // If the result was negative, shift it to the range [0, 360).
        if (normalizedAngle < 0) {
            normalizedAngle += 360;
        }

        // If the angle is in the range [180, 360), shift it to [-180, 0).
        if (normalizedAngle >= 180) {
            normalizedAngle -= 360;
        }

        return normalizedAngle;
    }

    /**
     * This funciton scales the power of the joystick to follow a curve, so that it allows for finer adjustments.
     * It is clamped between -1 and 1 out of caution, although it doesn't need it, it allows for changes to the curve.
     * @param input
     * @return
     */
    public double joystickScaler(double input) {
        return Math.max(-1, Math.min(1, Math.pow(Math.abs(input), Constants.JOYSTICK_SCALER_EXPONENT) * input));
    }
}