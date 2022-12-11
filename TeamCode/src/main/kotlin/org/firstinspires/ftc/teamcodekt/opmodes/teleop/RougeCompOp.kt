package org.firstinspires.ftc.teamcodekt.opmodes.teleop

import org.firstinspires.ftc.teamcodekt.blacksmith.Scheduler
import org.firstinspires.ftc.teamcodekt.blacksmith.listeners.Listener
import org.firstinspires.ftc.teamcodekt.components.Drivetrain
import org.firstinspires.ftc.teamcodekt.components.LiftConfig

class RougeCompOp : RougeBaseTele() {
    override fun describeControls(): Unit = with(bot) {
        codriver.dpad_up   .onRise(lift::goToHigh)
        codriver.dpad_down .onRise(lift::goToZero)
        codriver.dpad_right.onRise(lift::goToMid)
        codriver.dpad_left .onRise(lift::goToLow)

        intakeChain.invokeOn(codriver.left_bumper)

        forwardsDepositChain.invokeOn(codriver.right_bumper)
        forwardsDepositChain.cancelOn(codriver.x)

        backwardsDepositChain.invokeOn(codriver.y)
        backwardsDepositChain.cancelOn(codriver.x)

        codriver.left_stick_x.whileHigh {
            if (codriver.left_stick_x() > .5) {
                claw.openForIntakeWide()
            }

            if (codriver.left_stick_x() < -.5) {
                claw.close()
            }
        }

        codriver.right_trigger.whileHigh {
            lift.height += (50 * codriver.right_trigger()).toInt()
        }

        codriver.left_trigger.whileHigh {
            lift.height -= (50 * codriver.left_trigger()).toInt()
        }

        Listener { lift.height > LiftConfig.MID * 1.01 }
            .whileHigh { powerMulti /= 2 }

        driver.right_trigger(.1).whileHigh {
            powerMulti *= 1 - (driver.right_trigger() * driver.right_trigger() * driver.right_trigger())
        }

        Listener.always {
            drivetrain.drive(driver.gamepad, powerMulti)
        }
    }
}
