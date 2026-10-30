package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.vision.HiveAim;

/**
 * Drive, plate, intake, hive aiming settings and Driver Station diagnostic output live here.
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

    // Gamepad 1 button held to enable PD driving; release it once after START.
    public static final Button DRIVE_ENABLE_BUTTON = Button.A;

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

    // ==================== HIVE AIMING (LIMELIGHT) ====================
    // Hold an aim button to turn toward that alliance's nearest hive (largest tags
    // in view) as soon as any one of its AprilTags is visible. Two or more visible
    // tags are fitted together for the most accurate aim. A single tag is shifted
    // to the middle of the hive using the tag spacing measured the last time two or
    // more tags were in view, scaled by the tag's size; until then the robot centers
    // on that tag. Holding an aim button also enables PD like DRIVE_ENABLE_BUTTON:
    // the robot holds its position and the left stick still moves it, while aiming
    // replaces the right stick. The chosen hive is kept while the button is held; if
    // it stays out of view for AIM_LOST_MS, the alliance's nearest visible hive is
    // chosen instead. Blue wins if both buttons are held. Needs the Limelight
    // configured as "limelight"; without it the aim buttons do nothing.
    public static final int AIM_GAMEPAD = 1;
    public static final Button AIM_BLUE_BUTTON = Button.X;
    public static final Button AIM_RED_BUTTON = Button.B;

    // Tag IDs of each hive, one row per hive, listed LEFT to RIGHT as seen from the
    // robot facing that hive. Empty rows are ignored. Check the order during INIT:
    // each "Hive N tag tx" line must increase from left to right.
    // IDs from the BIOBUZZ game manual; tracking of all four hives tested on the robot.
    public static final int[][] HIVE_TAG_IDS = {
            {42, 43, 44, 45}, // Hive 1: BLUE
            {38, 39, 40, 41}, // Hive 2: BLUE, audience side
            {30, 31, 32, 33}, // Hive 3: RED
            {34, 35, 36, 37}, // Hive 4: RED, audience side
    };

    // Hive numbers (HIVE_TAG_IDS rows, counting from 1) tracked by each aim button.
    public static final int[] BLUE_HIVES = {1, 2};
    public static final int[] RED_HIVES = {3, 4};

    // Unit: cm. Center of each tag along its row, in HIVE_TAG_IDS order, measured
    // from the middle of the hive; left is negative. Shared by all hives. Only the
    // relative spacing matters, so these defaults fit four equally spaced tags
    // centered on the hive at any spacing. Copy the real values from the field
    // drawing if they differ.
    public static final double[] HIVE_TAG_POSITIONS_CM = {-30.0, -10.0, 10.0, 30.0};

    // Limelight pipeline index configured as an AprilTag (fiducial) pipeline.
    public static final int LIMELIGHT_PIPELINE = 0;

    // Ignore Limelight results older than this many milliseconds.
    public static final long LIMELIGHT_MAX_STALENESS_MS = 100;

    // Unit: degrees. Limelight tx value at which the shot goes into the hive.
    // Use it to correct for a camera or shooter mounted off the robot centerline.
    // Positive moves the aim point to the robot's right.
    public static final double AIM_OFFSET_DEG = 0.0;

    // Allowed values: +1 or -1. Flip to -1 only if aiming turns the robot AWAY
    // from the hive.
    public static final double AIM_TURN_SIGN = 1.0;

    // Unit: ms. How long the chosen hive may be out of view, while the button is
    // held, before the alliance's nearest visible hive is chosen instead. The robot
    // holds the last aimed heading meanwhile.
    public static final long AIM_LOST_MS = 500;

    // Camera_angle servo position (0..1) set at START so the Limelight looks at the
    // hive tags. Matches CAMERA_ANGLE_UP_POSITION in the Auto settings files.
    // Skipped if "Camera_angle" is not in the configuration.
    public static final double AIM_CAMERA_ANGLE_POSITION = 1.0;
    public static final Servo.Direction CAMERA_ANGLE_DIRECTION = Servo.Direction.FORWARD;

    public static void validateAimSettings() {
        if (AIM_GAMEPAD != 1 && AIM_GAMEPAD != 2) {
            throw new IllegalArgumentException("AIM_GAMEPAD must be 1 or 2");
        }
        if (AIM_BLUE_BUTTON == AIM_RED_BUTTON || (AIM_GAMEPAD == 1
                && (AIM_BLUE_BUTTON == DRIVE_ENABLE_BUTTON || AIM_RED_BUTTON == DRIVE_ENABLE_BUTTON))) {
            throw new IllegalArgumentException("Drive and aim buttons must all be different");
        }
        for (int i = 1; i < HIVE_TAG_POSITIONS_CM.length; i++) {
            if (!(HIVE_TAG_POSITIONS_CM[i] > HIVE_TAG_POSITIONS_CM[i - 1])) {
                throw new IllegalArgumentException("HIVE_TAG_POSITIONS_CM must increase left to right");
            }
        }
        for (int[] hive : HIVE_TAG_IDS) {
            if (hive.length != 0 && hive.length != HIVE_TAG_POSITIONS_CM.length) {
                throw new IllegalArgumentException("Each HIVE_TAG_IDS row needs one ID per HIVE_TAG_POSITIONS_CM entry");
            }
        }
        for (int[] alliance : new int[][] {BLUE_HIVES, RED_HIVES}) {
            for (int hive : alliance) {
                if (hive < 1 || hive > HIVE_TAG_IDS.length) {
                    throw new IllegalArgumentException("BLUE_HIVES and RED_HIVES must list hive numbers 1.."
                            + HIVE_TAG_IDS.length);
                }
            }
        }
        if (Math.abs(AIM_TURN_SIGN) != 1.0) {
            throw new IllegalArgumentException("AIM_TURN_SIGN must be +1 or -1");
        }
        if (!validNumber(AIM_OFFSET_DEG) || AIM_LOST_MS < 0) {
            throw new IllegalArgumentException("AIM_OFFSET_DEG must be finite and AIM_LOST_MS nonnegative");
        }
        if (!validNumber(AIM_CAMERA_ANGLE_POSITION)
                || AIM_CAMERA_ANGLE_POSITION < 0.0 || AIM_CAMERA_ANGLE_POSITION > 1.0) {
            throw new IllegalArgumentException("AIM_CAMERA_ANGLE_POSITION must be within 0..1");
        }
    }

    // alliance is "blue" or "red" while its aim button is held, otherwise null.
    // aimHive, nearestBlue, nearestRed are HIVE_TAG_IDS row indexes, -1 for none.
    // hiveMiddleDeg[h] is NaN when hive h cannot be aimed at; hiveTagTx[h][i] is
    // NaN when that tag is not visible. spacingKnown: a one-tag view can be shifted
    // to the middle because two or more tags have been seen.
    public static void reportAim(Telemetry telemetry, boolean limelightFound, String alliance,
                                 int aimHive, int nearestBlue, int nearestRed,
                                 double[] hiveMiddleDeg, double[][] hiveTagTx, boolean spacingKnown) {
        String status;
        if (!limelightFound) {
            status = "Limelight not found; aim buttons disabled";
        } else if (alliance == null) {
            status = "Ready: blue hive " + hiveName(nearestBlue) + ", red hive " + hiveName(nearestRed);
        } else if (aimHive < 0) {
            status = "Searching for a " + alliance + " hive tag";
        } else if (Double.isNaN(hiveMiddleDeg[aimHive])) {
            status = "Hive " + (aimHive + 1) + " out of view; holding heading";
        } else {
            int tags = HiveAim.visibleCount(hiveTagTx[aimHive]);
            status = String.format(java.util.Locale.US, "Tracking %s hive %d with %d tag%s, error %.2f deg",
                    alliance, aimHive + 1, tags, tags == 1 ? "" : "s",
                    hiveMiddleDeg[aimHive] - AIM_OFFSET_DEG);
            if (tags == 1 && !spacingKnown) status += "; centering on the tag until 2 are seen";
        }
        telemetry.addData("Aim (hold " + AIM_BLUE_BUTTON + " blue, " + AIM_RED_BUTTON + " red)", status);
        for (int h = 0; h < hiveTagTx.length; h++) {
            StringBuilder tags = new StringBuilder();
            for (int i = 0; i < HIVE_TAG_IDS[h].length; i++) {
                if (Double.isNaN(hiveTagTx[h][i])) continue;
                if (tags.length() > 0) tags.append("  ");
                tags.append(String.format(java.util.Locale.US, "%d: %.1f",
                        HIVE_TAG_IDS[h][i], hiveTagTx[h][i]));
            }
            if (tags.length() == 0) continue;
            if (!HiveAim.inOrder(hiveTagTx[h])) tags.append("  OUT OF ORDER");
            telemetry.addData("Hive " + (h + 1) + " tag tx (deg), left to right", tags.toString());
        }
    }

    private static String hiveName(int hive) {
        return hive < 0 ? "none" : String.valueOf(hive + 1);
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

    // Unit: cm. Larger measured tracking errors stop power and require releasing
    // the drive button. This is a fault threshold, NOT a speed control or tolerance.
    // Lower stops on smaller discrepancies; higher allows larger discrepancies.
    // Keep above MAX_TARGET_LEAD_CM; investigate faults before changing it.
    public static final double STOP_ERROR_CM = 300.0;

    // Unit: radians; edit the degree number. Stops power above this heading error.
    // Lower reacts sooner; higher permits a larger heading discrepancy.
    // Keep above heading tolerance/lead limits and below 180 degrees.
    // Releasing the drive button preserves the heading target, so it cannot erase this error.
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
        telemetry.addLine("After START: release, then hold " + DRIVE_ENABLE_BUTTON + " to drive, or "
                + AIM_BLUE_BUTTON + " / " + AIM_RED_BUTTON + " to aim at a blue / red hive.");
        telemetry.addLine("Keep robot still during initial IMU calibration.");
        telemetry.update();
    }

    public static void reportSensorFault(Telemetry telemetry, String fault,
                                         GoBildaPinpointDriver.DeviceStatus status) {
        telemetry.addData("Stopped", fault);
        telemetry.addData("Pinpoint", status);
        telemetry.addLine("Resolve the cause, then release " + DRIVE_ENABLE_BUTTON + " and the aim buttons.");
        telemetry.update();
    }

    public static void reportDisabled(Telemetry telemetry, double x, double y, double heading,
                                      double targetX, double targetY, double targetHeading) {
        telemetry.addLine("DISABLED: hold " + DRIVE_ENABLE_BUTTON + " to enable PD, or "
                + AIM_BLUE_BUTTON + " / " + AIM_RED_BUTTON + " to aim.");
        reportPose(telemetry, x, y, heading, targetX, targetY, targetHeading);
        telemetry.update();
    }

    public static void reportReleaseRequired(Telemetry telemetry, String fault) {
        telemetry.addData("Stopped", fault.isEmpty()
                ? "Release " + DRIVE_ENABLE_BUTTON + " and the aim buttons first" : fault);
        telemetry.update();
    }

    public static void reportTrackingFault(Telemetry telemetry, String fault) {
        telemetry.addData("Stopped", fault);
        telemetry.addLine("Check the robot; release " + DRIVE_ENABLE_BUTTON
                + " and the aim buttons before retrying.");
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
