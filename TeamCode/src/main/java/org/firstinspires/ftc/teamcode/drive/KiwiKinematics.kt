package org.firstinspires.ftc.teamcode.drive

import com.acmerobotics.roadrunner.geometry.Pose2d

/**
 * Mecanum drive kinematic equations. All wheel positions and velocities are given starting with front left and
 * proceeding counter-clockwise (i.e., front left, rear left, rear right, front right). Robot poses are specified in a
 * coordinate system with positive x pointing forward, positive y pointing left, and positive heading measured
 * counter-clockwise from the x-axis.
 *
 * [This paper](http://www.chiefdelphi.com/media/papers/download/2722) provides a motivated derivation.
 */
object KiwiKinematics {

    /**
     * Computes the wheel velocities corresponding to [robotVel]
     *
     * @param robotVel velocity of the robot in its reference frame
     */
    @JvmStatic
    @JvmOverloads
    fun robotToWheelVelocities(
        robotVel: Pose2d,
        trackWidth: Double
    ): List<Double> {
        val k = trackWidth / 2.0;
        return listOf(
            (robotVel.heading * k) - (0.866 * robotVel.x) + (0.6 * robotVel.y),
            (robotVel.heading * k) - robotVel.y,
            (robotVel.heading * k) + (0.866 * robotVel.x) + (0.6 * robotVel.y)
        )
    }

    /**
     * Computes the wheel velocities corresponding to [robotVel]
     *
     * @param robotVel velocity of the robot in its reference frame
     */
    @JvmStatic
    @JvmOverloads
    fun robotToWheelPowers(
        robotVel: Pose2d,
    ): List<Double> {
        return listOf(
            (robotVel.heading) - (0.866 * robotVel.x) + (0.6 * robotVel.y),
            (robotVel.heading) - robotVel.y,
            (robotVel.heading) + (0.866 * robotVel.x) + (0.6 * robotVel.y)
        )
    }


    /**
     * Computes the wheel accelerations corresponding to [robotAccel] given the provided [trackWidth] and
     * [wheelBase].
     *
     * @param robotAccel acceleration of the robot in its reference frame
     */
    @JvmStatic
    @JvmOverloads
    // follows from linearity of the derivative
    fun robotToWheelAccelerations(
        robotAccel: Pose2d,
        trackWidth: Double
    ) =
            robotToWheelVelocities(
                    robotAccel, trackWidth
            )

    /**
     * Computes the robot velocity corresponding to [wheelVelocities] and the given drive parameters.
     *
     * @param wheelVelocities wheel velocities (or wheel position deltas)
     */
    @JvmStatic
    @JvmOverloads
    fun wheelToRobotVelocities(
            wheelVelocities: List<Double>,
            trackWidth: Double
    ): Pose2d {
        val (left, rear, right) = wheelVelocities
        return Pose2d(
                (right - left) ,  // * 2 / 2
                (rear - ((right + left) / 0.866)) / 3.0,
                wheelVelocities.sum() / (trackWidth * 1.5),
                )
    }
}
