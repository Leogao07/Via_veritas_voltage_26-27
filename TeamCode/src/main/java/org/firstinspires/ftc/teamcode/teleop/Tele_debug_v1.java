package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Drive, plate, intake settings and Driver Station diagnostic output live here.
 * TeleOP_v1.java reads these values and calls the reporting methods automatically.
 * This is a helper class, NOT a second OpMode: do not add @TeleOp or runOpMode().
 *
 * INSTALL: Put this file and TeleOP_v1.java in the same package, then build BOTH.
 * TUNE: Stop the robot, edit a value, build the project, then INIT again.
 * These are compile-time settings, not live gamepad-adjustable settings.
 * All defaults below exactly match the working code you supplied.
 *
 * Suggested order:
 * 1. Verify wheel and odometry directions.
 * 2. Tune heading P, then heading D; tune position P, then position D.
 * 3. Adjust tolerances, then gradually increase speed and power.
 * Change one setting at a time and compare the displayed tracking errors.
 * Never raise a fault threshold just to hide an unexplained fault.
 */
public final class Tele_debug_v1 {
    private Tele_debug_v1() { } // No object needs to be constructed.

    // ==================== PLATE AND INTAKE  ====================
    // Change gamepad number (1/2), Button value, angle or power here, then rebuild.
    public enum Button {
        A, B, X, Y, LEFT_BUMPER, RIGHT_BUMPER,
        DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
        LEFT_STICK_BUTTON, RIGHT_STICK_BUTTON, LEFT_TRIGGER, RIGHT_TRIGGER
    }
    public static final double TRIGGER_BUTTON_THRESHOLD = 0.5;

    public static final int PLATE_GAMEPAD = 2;
    public static final Button PLATE_PRESET_1_BUTTON = Button.Y;
    public static final Button PLATE_PRESET_2_BUTTON = Button.X;
    public static final Button PLATE_PRESET_3_BUTTON = Button.A;
    public static final Button PLATE_PRESET_4_BUTTON = Button.B;
    // Defaults are examples: tune each angle to your installed mechanism.
    // Angles are measured from the servo's configured zero, not a world heading.
    public static final double PLATE_PRESET_1_DEGREES = 27.0;
    public static final double PLATE_PRESET_2_DEGREES = 117.0;
    public static final double PLATE_PRESET_3_DEGREES = 207.0;
    public static final double PLATE_PRESET_4_DEGREES = 297.0;
    public static final Servo.Direction PLATE_DIRECTION = Servo.Direction.FORWARD;

    // For a goBILDA 25-3 / 2000-0025-0003 in POSITION mode, as recorded in
    // this workspace's existing DEM1_Tuning.java. Confirm if hardware changed.
    // 500..2500 us provides 300 degrees; software cannot extend a 270-degree servo.
    // PLATE_TRAVEL_DEGREES must describe actual travel across the configured PWM.
    public static final boolean PLATE_USE_CUSTOM_PWM_RANGE = true;
    public static final double PLATE_PWM_MIN_US = 500.0;
    public static final double PLATE_PWM_MAX_US = 2500.0;
    public static final double PLATE_TRAVEL_DEGREES = 300.0;

    // Original intake controls: hold gamepad1 A/B, release to stop.
    // These are motor POWER fractions, not measured RPM. Forward wins if both held.
    public static final int INTAKE_GAMEPAD = 2;
    public static final Button INTAKE_FORWARD_BUTTON = Button.LEFT_BUMPER;
    public static final Button INTAKE_REVERSE_BUTTON = Button.RIGHT_BUMPER;
    public static final double INTAKE_FORWARD_POWER = 0.8;
    public static final double INTAKE_REVERSE_POWER = -0.8;
    public static final DcMotor.Direction INTAKE_DIRECTION = DcMotor.Direction.FORWARD;

    public static boolean isPressed(Gamepad gamepad, Button button) {
        switch (button) {
            case A: return gamepad.a;
            case B: return gamepad.b;
            case X: return gamepad.x;
            case Y: return gamepad.y;
            case LEFT_BUMPER: return gamepad.left_bumper;
            case RIGHT_BUMPER: return gamepad.right_bumper;
            case DPAD_UP: return gamepad.dpad_up;
            case DPAD_DOWN: return gamepad.dpad_down;
            case DPAD_LEFT: return gamepad.dpad_left;
            case DPAD_RIGHT: return gamepad.dpad_right;
            case LEFT_STICK_BUTTON: return gamepad.left_stick_button;
            case RIGHT_STICK_BUTTON: return gamepad.right_stick_button;
            case LEFT_TRIGGER: return gamepad.left_trigger > TRIGGER_BUTTON_THRESHOLD;
            case RIGHT_TRIGGER: return gamepad.right_trigger > TRIGGER_BUTTON_THRESHOLD;
            default: throw new IllegalArgumentException("Unsupported button: " + button);
        }
    }

    public static double platePosition(double degrees) {
        if (!validNumber(PLATE_TRAVEL_DEGREES) || PLATE_TRAVEL_DEGREES <= 0.0
                || !validNumber(degrees) || degrees < 0.0 || degrees > PLATE_TRAVEL_DEGREES) {
            throw new IllegalArgumentException("Plate angle must be within configured travel");
        }
        return degrees / PLATE_TRAVEL_DEGREES;
    }

    public static void validateMechanismSettings() {
        if ((PLATE_GAMEPAD != 1 && PLATE_GAMEPAD != 2)
                || (INTAKE_GAMEPAD != 1 && INTAKE_GAMEPAD != 2)) {
            throw new IllegalArgumentException("Mechanism gamepad must be 1 or 2");
        }
        if (!validNumber(PLATE_PWM_MIN_US) || !validNumber(PLATE_PWM_MAX_US)
                || PLATE_PWM_MIN_US <= 0 || PLATE_PWM_MAX_US <= PLATE_PWM_MIN_US) {
            throw new IllegalArgumentException("Invalid plate PWM range");
        }
        platePosition(PLATE_PRESET_1_DEGREES);
        platePosition(PLATE_PRESET_2_DEGREES);
        platePosition(PLATE_PRESET_3_DEGREES);
        platePosition(PLATE_PRESET_4_DEGREES);
        if (!validNumber(INTAKE_FORWARD_POWER) || !validNumber(INTAKE_REVERSE_POWER)
                || Math.abs(INTAKE_FORWARD_POWER) > 1.0 || Math.abs(INTAKE_REVERSE_POWER) > 1.0) {
            throw new IllegalArgumentException("Intake power must be within -1..1");
        }
        if (!validNumber(TRIGGER_BUTTON_THRESHOLD) || TRIGGER_BUTTON_THRESHOLD < 0.0
                || TRIGGER_BUTTON_THRESHOLD >= 1.0) {
            throw new IllegalArgumentException("Trigger threshold must be within 0..<1");
        }
    }

    private static boolean validNumber(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public static void reportMechanisms(Telemetry telemetry, double plateTargetDegrees, double intakePower) {
        // Servo target only: standard servo API provides no measured angle feedback.
        telemetry.addData("Plate target (deg)", Double.isNaN(plateTargetDegrees)
                ? "No preset selected" : String.format(java.util.Locale.US, "%.1f", plateTargetDegrees));
        telemetry.addData("Intake power", intakePower);
    }

    // ==================== TARGET SPEED AND POWER ====================

    // Unit: cm/s. Maximum speed of the virtual translation target at full stick.
    // Increase for faster requested travel; decrease if the robot cannot keep up.
    // This does not guarantee actual speed. Power, traction, PD gains, and the
    // target-lead limit can all prevent the robot from reaching this speed.
    // Keep positive. For a first small increase, try 20 -> 25, then test.
    public static final double MAX_SPEED_CM_S = 50.0;

    // Unit: rad/s. Edit the DEGREE number inside Math.toRadians(...).
    // Increase 45 -> 55 for a faster right-stick heading command; decrease for
    // finer turning control. This is a target rate, not guaranteed actual RPM.
    // Keep positive. Translation never changes targetHeading.
    public static final double MAX_TURN_RAD_S = Math.toRadians(55.0);

    // Unit: normalized motor-power command. Valid range: greater than 0, at most 1.
    // Increase 0.35 -> 0.40 to allow stronger output; decrease for gentler tests.
    // 0.35 is a command limit, NOT 35% of the robot's measured maximum speed.
    // Raising this only helps if the controller actually requests more power.
    public static final double MAX_MOTOR_POWER = 1;

    // Unit: fraction of full joystick travel. Valid range: 0 <= value < 1.
    // Inputs inside +/-0.06 become zero; the remaining range is rescaled.
    // Increase if stick noise moves the target; decrease for a smaller neutral zone.
    // An excessively large value makes small stick movements unresponsive.
    public static final double STICK_DEADBAND = 0.06;

    // ==================== POSITION AND HEADING PD ====================

    // Unit: power per cm of position error. Keep nonnegative.
    // Increase gradually for stronger position correction; decrease if it overshoots
    // or repeatedly rocks around the target. Example: 5 cm * 0.040 = 0.20 P output.
    // Tune with small changes, for example 0.040 -> 0.045.
    public static final double KP_POSITION = 0.04;

    // Unit: power per (cm/s) of TARGET velocity minus MEASURED velocity.
    // Keep nonnegative. Increase slightly to strengthen velocity-error correction
    // and damp motion when the target stops. Decrease if noise causes twitching.
    // This acts during acceleration too; it is not simply a permanent brake.
    public static final double KD_POSITION = 0.008;

    // Unit: power per RADIAN of heading error. Keep nonnegative.
    // Increase for stronger heading correction; decrease if the robot oscillates.
    // Example: a 10-degree error is about 0.1745 rad, giving about 0.209 P output.
    // Tune separately from position P, for example 1.20 -> 1.30.
    public static final double KP_HEADING = 1.30;

    // Unit: power per (rad/s) of TARGET turn rate minus MEASURED turn rate.
    // Keep nonnegative. Increase slightly to damp turn overshoot; decrease if
    // heading noise causes shaking. Too much D can make the response poorly behaved.
    // Try small changes, for example 0.10 -> 0.11, after tuning heading P.
    public static final double KD_HEADING = 0.10;

    // ==================== HEADING TOLERANCE ====================

    // Unit: radians. Edit the degree number inside Math.toRadians(...).
    // With no turn-stick input, correction settles when BOTH angle error is within
    // this threshold AND turn rate is within SETTLED_TURN_RATE_RAD_S.
    // Increase for looser heading accuracy and less correction of tiny errors;
    // decrease for tighter accuracy. Keep nonnegative and below HEADING_RESUME_RAD.
    public static final double HEADING_TOLERANCE_RAD = Math.toRadians(2.0);

    // Once settled, angle correction restarts at this error magnitude.
    // Keep GREATER than HEADING_TOLERANCE_RAD and below STOP_ERROR_RAD.
    // Increase the gap to reduce on/off switching; decrease it to react sooner.
    // Example pair for looser tolerance: settle at 3 degrees, resume at 4 degrees.
    public static final double HEADING_RESUME_RAD = Math.toRadians(3.0);

    // Unit: rad/s; edit the degree-per-second number inside Math.toRadians(...).
    // Maximum measured turn rate allowed when ENTERING the settled state.
    // Lower requires the robot to become more still before correction pauses.
    // Higher can pause correction while the robot is still rotating noticeably.
    // Keep nonnegative and below RESUME_TURN_RATE_RAD_S.
    public static final double SETTLED_TURN_RATE_RAD_S = Math.toRadians(3.0);

    // Unit: rad/s. If already settled, this turn rate restarts correction even if
    // the angle error is small. Lower reacts sooner to rotation disturbances;
    // higher ignores more rotation before reacting. Keep above the settled rate.
    // Neither tolerance nor rate thresholds change the stored target heading.
    public static final double RESUME_TURN_RATE_RAD_S = Math.toRadians(6.0);

    // ==================== TARGET ADVANCE LIMITS ====================

    // Unit: cm. Stop advancing the target outward when its next step would be
    // beyond this distance. Steps that reduce the existing gap remain allowed.
    // Increase to permit more tracking lag and potentially more P output;
    // decrease to reduce stored catch-up travel after releasing the sticks.
    // This is NOT an acceptable final position error, nor a bound on disturbances.
    // Keep positive and below STOP_ERROR_CM. Do not enlarge it to mask bad signs.
    public static final double MAX_TARGET_LEAD_CM = 50.0;

    // Unit: radians; edit the degree number. Equivalent advance limit for heading.
    // Increase if a correctly functioning turn controller needs more target lead;
    // decrease to reduce the remaining turn after the right stick is centered.
    // Keep positive, below STOP_ERROR_RAD, and comfortably below 180 degrees.
    public static final double MAX_TARGET_LEAD_RAD = Math.toRadians(100.0);

    // ==================== FAULT THRESHOLDS AND FILTER ====================

    // Unit: cm. Larger measured tracking errors stop power and require a bumper
    // release. This is a fault threshold, NOT a speed control or normal tolerance.
    // Lower stops on smaller discrepancies; higher allows larger discrepancies.
    // Keep above MAX_TARGET_LEAD_CM; investigate faults before changing it.
    public static final double STOP_ERROR_CM = 300.0;

    // Unit: radians; edit the degree number. Stops power above this heading error.
    // Lower reacts sooner; higher permits a larger heading discrepancy.
    // Keep above heading tolerance/lead limits and below 180 degrees.
    // Bumper release preserves the heading target, so it cannot erase this error.
    public static final double STOP_ERROR_RAD = Math.toRadians(600.0);

    // Unit: seconds. A control-loop interval above this value triggers a stop
    // once execution resumes. 0.15 seconds = 150 milliseconds.
    // Lower is stricter and may cause nuisance stops; higher tolerates longer stalls.
    // Normally leave unchanged and find the cause of delayed loops instead.
    public static final double MAX_LOOP_SECONDS = 0.15;

    // Unit: seconds. Time constant for smoothing measured velocity used by D.
    // Increase for smoother readings but more delayed feedback; decrease for
    // quicker feedback but more noise. Zero removes smoothing. Keep nonnegative.
    // Tune D first; change this only if measurement noise/lag is an actual issue.
    public static final double VELOCITY_FILTER_SECONDS = 0.05;

    // ==================== MOTOR AXIS SIGNS ====================

    // Allowed values: +1 or -1. Use -1 only if the ENTIRE forward output axis is
    // physically reversed. This is not a gain or speed setting. Verify odometry
    // signs separately; one wrong wheel requires correcting that wheel instead.
    public static final double FORWARD_MIX_SIGN = 1.0;

    // Allowed values: +1 or -1. Flip only if the ENTIRE left-strafe output axis
    // is reversed. Leave at +1 when the current working motion directions are right.
    public static final double LEFT_MIX_SIGN = 1.0;

    // Allowed values: +1 or -1. Flip only if the ENTIRE clockwise output axis
    // is reversed. An incorrect sign can turn negative feedback into positive feedback.
    public static final double CLOCKWISE_MIX_SIGN = 1.0;

    // Individual wheel directions, preserved from your working program.
    // Only change these after confirming a wiring/mechanical direction mismatch.
    // Changing a wheel direction affects translation AND rotation, not just one axis.
    // Left-front wheel: switch REVERSE to FORWARD only if this wheel needs reversing.
    public static final DcMotor.Direction LEFTFRONT_DIRECTION = DcMotor.Direction.REVERSE;
    // Left-back wheel: switch FORWARD to REVERSE only if this wheel needs reversing.
    public static final DcMotor.Direction LEFTBACK_DIRECTION = DcMotor.Direction.FORWARD;
    // Right-front wheel: switch FORWARD to REVERSE only if this wheel needs reversing.
    public static final DcMotor.Direction RIGHTFRONT_DIRECTION = DcMotor.Direction.FORWARD;
    // Right-back wheel: switch FORWARD to REVERSE only if this wheel needs reversing.
    public static final DcMotor.Direction RIGHTBACK_DIRECTION = DcMotor.Direction.FORWARD;

    // ==================== DISPLAY AND LOOP TIMING ====================

    // Unit: milliseconds. Driver Station telemetry transmission interval.
    // Increase for less frequent screen/network updates; decrease for faster display
    // refresh with more overhead. This does NOT set the PD calculation period.
    public static final int TELEMETRY_INTERVAL_MS = 100;

    // Unit: milliseconds. Pause after the stationary INIT-time IMU reset.
    // Normally leave at 350. Increase if needed for startup; do not shorten it
    // to increase drive speed. The controller also checks Pinpoint READY status.
    public static final long IMU_RESET_WAIT_MS = 350;

    // Unit: milliseconds. Pause between pre-START display updates.
    // Increase for less frequent INIT updates; decrease for more frequent updates.
    // This has no effect on driving speed. Keep positive; normally leave unchanged.
    public static final long INIT_LOOP_SLEEP_MS = 20;

    // Unit: milliseconds. Pause at the end of each active control-loop iteration.
    // Actual loop time also includes sensor reads, calculations, and other work.
    // Increasing slows feedback; reducing can increase CPU/bus load. This is NOT
    // a vehicle-speed adjustment. Keep positive and well below MAX_LOOP_SECONDS*1000.
    public static final long CONTROL_LOOP_SLEEP_MS = 10;

    // ==================== DIAGNOSTIC DISPLAY ====================
    // These methods only report information; they never change targets or motors.
    // Keep fault messages visible when customizing the display.

    public static void reportInit(Telemetry telemetry, GoBildaPinpointDriver.DeviceStatus status,
                                  double x, double y, double headingDegrees) {
        telemetry.addData("Pinpoint", status);
        telemetry.addData("x (cm)", x);
        telemetry.addData("y (cm)", y);
        telemetry.addData("angle (deg)", headingDegrees);
        telemetry.addLine("After START: release, then hold LEFT BUMPER to drive.");
        telemetry.addLine("Keep robot still during initial IMU calibration.");
        telemetry.update();
    }

    public static void reportSensorFault(Telemetry telemetry, String fault,
                                         GoBildaPinpointDriver.DeviceStatus status) {
        telemetry.addData("Stopped", fault);
        telemetry.addData("Pinpoint", status);
        telemetry.addLine("Resolve the cause, then release LEFT BUMPER.");
        telemetry.update();
    }

    public static void reportDisabled(Telemetry telemetry, double x, double y, double heading,
                                      double targetX, double targetY, double targetHeading) {
        telemetry.addLine("DISABLED: hold LEFT BUMPER to enable PD.");
        reportPose(telemetry, x, y, heading, targetX, targetY, targetHeading);
        telemetry.update();
    }

    public static void reportReleaseRequired(Telemetry telemetry, String fault) {
        telemetry.addData("Stopped", fault.isEmpty() ? "Release LEFT BUMPER first" : fault);
        telemetry.update();
    }

    public static void reportTrackingFault(Telemetry telemetry, String fault) {
        telemetry.addData("Stopped", fault);
        telemetry.addLine("Check the robot; release LEFT BUMPER before retrying.");
        telemetry.update();
    }

    public static void reportRunning(Telemetry telemetry,
                                     double x, double y, double heading,
                                     double targetX, double targetY, double targetHeading,
                                     double errorX, double errorY, double errorHeading,
                                     boolean headingSettled,
                                     boolean translationLimited, boolean rotationLimited) {
        reportPose(telemetry, x, y, heading, targetX, targetY, targetHeading);
        telemetry.addData("Position error (cm)", "%.2f", Math.hypot(errorX, errorY));
        telemetry.addData("Heading error (deg)", "%.2f", Math.toDegrees(errorHeading));
        telemetry.addData("Heading within tolerance", headingSettled);
        telemetry.addData("Target advance limited", "translation=%s rotation=%s",
                translationLimited, rotationLimited);
        telemetry.addLine("PD ENABLED: centered sticks hold position and heading.");
        telemetry.update();
    }

    private static void reportPose(Telemetry telemetry, double x, double y, double heading,
                                   double targetX, double targetY, double targetHeading) {
        telemetry.addData("Actual: x cm, y cm, heading deg", "%.2f, %.2f, %.2f",
                x, y, Math.toDegrees(heading));
        telemetry.addData("Target: x cm, y cm, heading deg", "%.2f, %.2f, %.2f",
                targetX, targetY, Math.toDegrees(targetHeading));
    }
}
