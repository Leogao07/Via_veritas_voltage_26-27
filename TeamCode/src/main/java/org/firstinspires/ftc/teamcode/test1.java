package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.PwmControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * GAMEPAD -> VIRTUAL TARGET POSE -> PD CONTROLLER -> YOUR ORIGINAL WHEEL MIXER
 *
 * The sticks change targetX, targetY, and targetHeading, not motor power.
 * Odometry measures the real pose. PD feedback makes the robot follow the target.
 * With centered sticks and LEFT BUMPER held, the robot holds the last target.
 * Release LEFT BUMPER to stop applying power and reset only the X/Y target.
 * Target heading starts at 0 degrees on INIT. ONLY right-stick X changes it.
 * Translating, releasing the bumper, and actual heading drift never change it.
 *
 * Original hardware names are retained; joystick spelling is corrected.
 * Plate presets and intake controls are configured in DriveDebug.java.
 * All positions are centimeters; all internal angles are radians; time is seconds.
 *
 * TUNING: All adjustable settings and diagnostic reports are in DriveDebug.java.
 * IMPORTANT: The supplied gains are starting values, not robot-specific tuning.
 * Feedback reduces tracking errors; it cannot remove slip, sensor drift, motor
 * saturation, static friction, or all steady-state error under a constant load.
 */
@TeleOp(name = "test PD Target Drive", group = "test1")
public class test1 extends LinearOpMode {
    private DcMotor intake;
    private Servo plate;
    private double plateTargetDegrees = Double.NaN;
    private DcMotor leftfront;
    private DcMotor leftback;
    private DcMotor rightback;
    private DcMotor rightfront;
    private GoBildaPinpointDriver odometry;

    // Edit DriveDebug.java to change tuning, directions, timing, or diagnostics.
    // All original default values and drivetrain variable names are preserved.

    // The internal movement data that the gamepad modifies.
    // These are WORLD coordinates: X/Y axes stay fixed as the robot turns.
    private double targetX;
    private double targetY;
    private double targetHeading;

    // State needed to estimate and smooth actual velocity from successive poses.
    private double previousX;
    private double previousY;
    private double previousHeading;
    private double velocityX;
    private double velocityY;
    private double angularVelocity;
    private boolean headingSettled;

    @Override
    public void runOpMode() {
        // Original joystick meanings, with corrected variable spelling:
        // forward > 0: forward; left > 0: left; rotate > 0: clockwise.
        float forward;
        float left;
        float rotate;

        leftfront = hardwareMap.get(DcMotor.class, "leftfront");
        leftback = hardwareMap.get(DcMotor.class, "leftback");
        rightback = hardwareMap.get(DcMotor.class, "rightback");
        rightfront = hardwareMap.get(DcMotor.class, "rightfront");
        odometry = hardwareMap.get(GoBildaPinpointDriver.class, "odometry");

        // Match the directions in your original code explicitly.
        leftfront.setDirection(DriveDebug.LEFTFRONT_DIRECTION);
        leftback.setDirection(DriveDebug.LEFTBACK_DIRECTION);
        rightfront.setDirection(DriveDebug.RIGHTFRONT_DIRECTION);
        rightback.setDirection(DriveDebug.RIGHTBACK_DIRECTION);

        for (DcMotor motor : new DcMotor[] {leftfront, leftback, rightfront, rightback}) {
            motor.setPower(0.0);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            // This program supplies its own pose feedback. Do not run a second
            // motor-encoder position or velocity controller underneath it.
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        try {
            DriveDebug.validateMechanismSettings();
            intake = hardwareMap.get(DcMotor.class, "intake");
            intake.setPower(0.0);
            intake.setDirection(DriveDebug.INTAKE_DIRECTION);
            intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            plate = hardwareMap.get(Servo.class, "plate");
            plate.setDirection(DriveDebug.PLATE_DIRECTION);
            plate.scaleRange(0.0, 1.0);
            if (DriveDebug.PLATE_USE_CUSTOM_PWM_RANGE) {
                if (!(plate instanceof PwmControl)) {
                    throw new IllegalStateException("plate controller does not support custom PWM range");
                }
                ((PwmControl) plate).setPwmRange(new PwmControl.PwmRange(
                        DriveDebug.PLATE_PWM_MIN_US, DriveDebug.PLATE_PWM_MAX_US));
            }
            // No automatic plate move during INIT; the first preset selects its target.
            // You confirmed your odometry already reports correctly. This code
            // does not overwrite pod resolution, offsets, or encoder directions.
            // If another OpMode configures those settings, copy its EXACT setup
            // calls here so this OpMode also works after a fresh power cycle.
            // Do not substitute guessed offsets or a guessed pod type.

            // Reset only during INIT, while the robot is stationary.
            odometry.resetPosAndIMU();
            targetHeading = 0.0; // The startup facing direction is the zero reference.
            sleep(DriveDebug.IMU_RESET_WAIT_MS);
            telemetry.setMsTransmissionInterval(DriveDebug.TELEMETRY_INTERVAL_MS);
            while (!isStarted() && !isStopRequested()) {
                odometry.update();
                DriveDebug.reportInit(telemetry, odometry.getDeviceStatus(),
                        odometry.getPosX(DistanceUnit.CM), odometry.getPosY(DistanceUnit.CM),
                        odometry.getHeading(AngleUnit.DEGREES));
                sleep(DriveDebug.INIT_LOOP_SLEEP_MS);
            }
            waitForStart();
            if (isStopRequested()) return;

            long previousTime = System.nanoTime();
            boolean tracking = false;
            boolean requireRelease = true;
            String fault = "";

            while (opModeIsActive()) {
                // Always read the complete odometry pose BEFORE computing power.
                odometry.update();
                double x = odometry.getPosX(DistanceUnit.CM);
                double y = odometry.getPosY(DistanceUnit.CM);
                double heading = odometry.getHeading(AngleUnit.RADIANS);
                long now = System.nanoTime();
                double dt = (now - previousTime) / 1_000_000_000.0;
                previousTime = now;

                // A loop delay must not advance the virtual target by a large jump.
                // Status checks catch reported sensor faults, but cannot prove that
                // every plausible-looking sensor value is fresh and accurate.
                boolean poseOK = odometry.getDeviceStatus()
                        == GoBildaPinpointDriver.DeviceStatus.READY
                        && finite(x) && finite(y) && finite(heading);
                boolean timingOK = dt > 0.0 && dt <= DriveDebug.MAX_LOOP_SECONDS;
                if (!poseOK || !timingOK) {
                    intake.setPower(0.0);
                    DriveDebug.reportMechanisms(telemetry, plateTargetDegrees, intake.getPower());
                    fault = !poseOK ? "Odometry not ready / invalid pose" : "Control loop delayed";
                    requireRelease = true;
                    tracking = false;
                    stopDrive();
                    DriveDebug.reportSensorFault(telemetry, fault, odometry.getDeviceStatus());
                    sleep(DriveDebug.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }

                // Mechanisms remain usable while the drivetrain bumper is released.
                updateMechanisms();
                DriveDebug.reportMechanisms(telemetry, plateTargetDegrees, intake.getPower());

                // Release means zero motor power, not position hold. BRAKE reduces
                // coasting, but it is not a mechanical lock or an instantaneous stop.
                if (!gamepad1.left_bumper) {
                    stopDrive();
                    resetTarget(x, y, heading);
                    tracking = false;
                    requireRelease = false;
                    fault = "";
                    DriveDebug.reportDisabled(telemetry, x, y, heading,
                            targetX, targetY, targetHeading);
                    sleep(DriveDebug.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }
                if (requireRelease) {
                    stopDrive();
                    DriveDebug.reportReleaseRequired(telemetry, fault);
                    sleep(DriveDebug.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }
                if (!tracking) {
                    // Rebase translation, but keep the heading chosen by the driver.
                    resetTarget(x, y, heading);
                    tracking = true;
                    stopDrive();
                    sleep(DriveDebug.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }

                // Derivative measurement: differentiate WORLD pose, then low-pass
                // filter the velocity. Wrapped angle differences avoid a false
                // full-turn velocity spike when heading crosses +/-180 degrees.
                double alpha = dt / (DriveDebug.VELOCITY_FILTER_SECONDS + dt);
                velocityX += alpha * ((x - previousX) / dt - velocityX);
                velocityY += alpha * ((y - previousY) / dt - velocityY);
                angularVelocity += alpha * (wrap(heading - previousHeading) / dt - angularVelocity);
                previousX = x;
                previousY = y;
                previousHeading = heading;

                forward = (float) deadband(-gamepad1.left_stick_y);
                left = (float) deadband(-gamepad1.left_stick_x);
                rotate = (float) deadband(gamepad1.right_stick_x);

                // Normalize diagonal input so diagonal target speed is not faster.
                double stickLength = Math.max(1.0, Math.hypot(forward, left));
                double forwardSpeed = forward / stickLength * DriveDebug.MAX_SPEED_CM_S;
                double leftSpeed = left / stickLength * DriveDebug.MAX_SPEED_CM_S;
                double cos = Math.cos(heading);
                double sin = Math.sin(heading);

                // Use the TARGET heading for the joystick reference. Actual yaw
                // drift must not bend the intended straight-line target path.
                // At target heading 0, forward increases X and left increases Y.
                // At target heading +90 degrees, forward increases world Y.
                // Actual heading (cos/sin above) is used later to correct the wheels.
                double commandCos = Math.cos(targetHeading);
                double commandSin = Math.sin(targetHeading);
                double targetVelocityX = 2*(commandCos * forwardSpeed - commandSin * leftSpeed);
                double targetVelocityY = 2*(commandSin * forwardSpeed + commandCos * leftSpeed);
                // Pinpoint heading is CCW-positive; your rotation stick is CW-positive.
                double targetAngularVelocity = 2*(-rotate * DriveDebug.MAX_TURN_RAD_S);

                // Integrate velocity to create the internal target pose.
                // If the next step exceeds the lead limit, freeze translation
                // unless that step brings the target closer to the measured robot.
                // This discards unreachable commands instead of storing a backlog.
                double nextX = targetX + targetVelocityX * dt;
                double nextY = targetY + targetVelocityY * dt;
                double oldDistance = Math.hypot(targetX - x, targetY - y);
                double nextDistance = Math.hypot(nextX - x, nextY - y);
                boolean translationLimited = nextDistance > DriveDebug.MAX_TARGET_LEAD_CM
                        && nextDistance >= oldDistance;
                if (translationLimited) {
                    targetVelocityX = 0.0;
                    targetVelocityY = 0.0;
                } else {
                    targetX = nextX;
                    targetY = nextY;
                }

                double nextHeading = wrap(targetHeading + targetAngularVelocity * dt);
                double oldAngleError = Math.abs(wrap(targetHeading - heading));
                double nextAngleError = Math.abs(wrap(nextHeading - heading));
                boolean rotationLimited = nextAngleError > DriveDebug.MAX_TARGET_LEAD_RAD
                        && nextAngleError >= oldAngleError;
                if (rotationLimited) {
                    targetAngularVelocity = 0.0;
                } else if (rotate != 0.0f) {
                    // The only runtime assignment to targetHeading. Left-stick
                    // input and measured heading never overwrite this target.
                    targetHeading = nextHeading;
                }

                double errorX = targetX - x;
                double errorY = targetY - y;
                double errorHeading = wrap(targetHeading - heading);
                if (Math.hypot(errorX, errorY) > DriveDebug.STOP_ERROR_CM
                        || Math.abs(errorHeading) > DriveDebug.STOP_ERROR_RAD) {
                    intake.setPower(0.0);
                    DriveDebug.reportMechanisms(telemetry, plateTargetDegrees, intake.getPower());
                    fault = "Tracking error too large";
                    stopDrive();
                    requireRelease = true;
                    tracking = false;
                    DriveDebug.reportTrackingFault(telemetry, fault);
                    sleep(DriveDebug.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }

                // PD in world coordinates:
                // P corrects position/heading error.
                // D corrects target velocity minus actual velocity (error rate).
                // With centered sticks, target velocity is zero, so D damps motion.
                // No integral or feedforward term is included in this PD-only design.
                double powerX = DriveDebug.KP_POSITION * errorX
                        + DriveDebug.KD_POSITION * (targetVelocityX - velocityX);
                double powerY = DriveDebug.KP_POSITION * errorY
                        + DriveDebug.KD_POSITION * (targetVelocityY - velocityY);
                double powerCCW = DriveDebug.KP_HEADING * errorHeading
                        + DriveDebug.KD_HEADING * (targetAngularVelocity - angularVelocity);
                powerCCW = applyHeadingTolerance(powerCCW, errorHeading, rotate);

                // Convert world correction back into the robot's current axes.
                double forwardPower = cos * powerX + sin * powerY;
                double leftPower = -sin * powerX + cos * powerY;
                double clockwisePower = -powerCCW;
                drive(forwardPower, leftPower, clockwisePower);
                //  drive(forward, left, rotate);

                DriveDebug.reportRunning(telemetry, x, y, heading,
                        targetX, targetY, targetHeading, errorX, errorY, errorHeading,
                        headingSettled, translationLimited, rotationLimited);
                sleep(DriveDebug.CONTROL_LOOP_SLEEP_MS);
            }
        } finally {
            // Also stop if an exception interrupts the control loop.
            if (intake != null) intake.setPower(0.0);
            stopDrive();
        }
    }

    private Gamepad mechanismGamepad(int number) {
        return number == 1 ? gamepad1 : gamepad2;
    }

    private void updateMechanisms() {
        Gamepad plateGamepad = mechanismGamepad(DriveDebug.PLATE_GAMEPAD);
        // If several preset buttons are held, priority is preset 1, 2, 3, then 4.
        double requestedDegrees = Double.NaN;
        if (DriveDebug.isPressed(plateGamepad, DriveDebug.PLATE_PRESET_1_BUTTON)) {
            requestedDegrees = DriveDebug.PLATE_PRESET_1_DEGREES;
        } else if (DriveDebug.isPressed(plateGamepad, DriveDebug.PLATE_PRESET_2_BUTTON)) {
            requestedDegrees = DriveDebug.PLATE_PRESET_2_DEGREES;
        } else if (DriveDebug.isPressed(plateGamepad, DriveDebug.PLATE_PRESET_3_BUTTON)) {
            requestedDegrees = DriveDebug.PLATE_PRESET_3_DEGREES;
        } else if (DriveDebug.isPressed(plateGamepad, DriveDebug.PLATE_PRESET_4_BUTTON)) {
            requestedDegrees = DriveDebug.PLATE_PRESET_4_DEGREES;
        }
        if (!Double.isNaN(requestedDegrees) && requestedDegrees != plateTargetDegrees) {
            plate.setPosition(DriveDebug.platePosition(requestedDegrees));
            plateTargetDegrees = requestedDegrees;
        }

        Gamepad intakeGamepad = mechanismGamepad(DriveDebug.INTAKE_GAMEPAD);
        // Preserve original behavior: forward button wins if both are held.
        if (DriveDebug.isPressed(intakeGamepad, DriveDebug.INTAKE_FORWARD_BUTTON)) {
            intake.setPower(DriveDebug.INTAKE_FORWARD_POWER);
        } else if (DriveDebug.isPressed(intakeGamepad, DriveDebug.INTAKE_REVERSE_BUTTON)) {
            intake.setPower(DriveDebug.INTAKE_REVERSE_POWER);
        } else {
            intake.setPower(0.0);
        }
    }

    private void resetTarget(double x, double y, double heading) {
        targetX = previousX = x;
        targetY = previousY = y;
        previousHeading = heading;
        velocityX = velocityY = angularVelocity = 0.0;
        headingSettled = false;
        // Deliberately preserve targetHeading, even when the bumper is released.
    }

    private double applyHeadingTolerance(double powerCCW, double errorHeading, float rotate) {
        double angleError = Math.abs(errorHeading);
        double turnRate = Math.abs(angularVelocity);
        if (rotate != 0.0f) {
            headingSettled = false; // Always respond to deliberate turning input.
        } else if (headingSettled) {
            if (angleError >= DriveDebug.HEADING_RESUME_RAD || turnRate >= DriveDebug.RESUME_TURN_RATE_RAD_S) {
                headingSettled = false;
            }
        } else if (angleError <= DriveDebug.HEADING_TOLERANCE_RAD && turnRate <= DriveDebug.SETTLED_TURN_RATE_RAD_S) {
            headingSettled = true;
        }
        // Tolerance changes motor correction, never the stored target angle.
        return headingSettled ? 0.0 : powerCCW;
    }

    private void drive(double forwardPower, double leftPower, double clockwisePower) {
        forwardPower *= DriveDebug.FORWARD_MIX_SIGN;
        leftPower *= DriveDebug.LEFT_MIX_SIGN;
        clockwisePower *= DriveDebug.CLOCKWISE_MIX_SIGN;

        // Your original wheel equations, with PD outputs replacing joystick values.
        double leftfrontPower = forwardPower + leftPower + clockwisePower;
        double rightfrontPower = -forwardPower + leftPower + clockwisePower;
        double leftbackPower = forwardPower - leftPower + clockwisePower;
        double rightbackPower = -forwardPower - leftPower + clockwisePower;

        // Scale all wheels together to retain their ratios during saturation.
        // Independent clipping would distort the requested motion direction.
        double largest = Math.max(Math.max(Math.abs(leftfrontPower), Math.abs(rightfrontPower)),
                Math.max(Math.abs(leftbackPower), Math.abs(rightbackPower)));
        double scale = largest > DriveDebug.MAX_MOTOR_POWER ? DriveDebug.MAX_MOTOR_POWER / largest : 1.0;
        leftfront.setPower(leftfrontPower * scale);
        rightfront.setPower(rightfrontPower * scale);
        leftback.setPower(leftbackPower * scale);
        rightback.setPower(rightbackPower * scale);
    }

    private void stopDrive() {
        leftfront.setPower(0.0);
        rightfront.setPower(0.0);
        leftback.setPower(0.0);
        rightback.setPower(0.0);
    }

    private static double deadband(double value) {
        if (Math.abs(value) <= DriveDebug.STICK_DEADBAND) return 0.0;
        // Rescale the remaining range smoothly from zero to full command.
        return Math.copySign((Math.abs(value) - DriveDebug.STICK_DEADBAND) / (1.0 - DriveDebug.STICK_DEADBAND), value);
    }

    private static double wrap(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
