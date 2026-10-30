package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.PwmControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.vision.HiveAim;

/**
 * GAMEPAD -> VIRTUAL TARGET POSE -> PD CONTROLLER -> YOUR ORIGINAL WHEEL MIXER
 *
 * The sticks change targetX, targetY, and targetHeading, not motor power.
 * Odometry measures the real pose. PD feedback makes the robot follow the target.
 * With centered sticks and gamepad 1 A held, the robot holds the last target.
 * Release A to stop applying power and reset only the X/Y target.
 * Target heading starts at 0 degrees on INIT. ONLY right-stick X changes it, or
 * holding an aim button (gamepad 1 X: blue hives, B: red hives), which turns it
 * toward that alliance's nearest hive seen by the Limelight and enables PD like A.
 * Translating, releasing A, and actual heading drift never change it.
 *
 * Original hardware names are retained; joystick spelling is corrected.
 * Plate presets and intake controls are configured in Tele_debug_v1.java.
 * All positions are centimeters; all internal angles are radians; time is seconds.
 *
 * TUNING: All adjustable settings and diagnostic reports are in Tele_debug_v1.java.
 * IMPORTANT: The supplied gains are starting values, not robot-specific tuning.
 * Feedback reduces tracking errors; it cannot remove slip, sensor drift, motor
 * saturation, static friction, or all steady-state error under a constant load.
 */
@TeleOp(name = "TeleOP_v1", group = "TeleOP_v1")
public class TeleOP_v1 extends LinearOpMode {
    private DcMotor intake;
    private Servo plate;
    private double plateTargetDegrees = Double.NaN;
    private DcMotor leftfront;
    private DcMotor leftback;
    private DcMotor rightback;
    private DcMotor rightfront;
    private GoBildaPinpointDriver odometry;
    private Limelight3A limelight; // Optional: null if not configured.
    private Servo cameraAngle;     // Optional: null if not configured.

    // Aim button state. Hive indexes are HIVE_TAG_IDS rows (hive number - 1); -1 means none.
    private double[][] hiveTagTx;   // [hive][tag] Limelight tx in degrees; NaN = not seen
    private double[][] hiveTagArea; // [hive][tag] Limelight target area, % of image; NaN = not seen
    private double[] hiveMiddleDeg; // angle to each hive's middle; NaN = cannot aim at it
    private double scalePerRootArea = Double.NaN; // tag spacing for one-tag views; see HiveAim
    private long lastFrameTime = -1;
    private int nearestBlue = -1;
    private int nearestRed = -1;
    private int[] aimAlliance;      // BLUE_HIVES or RED_HIVES while its button is held, else null
    private int aimHive = -1;
    private long aimHiveSeenNanos;
    private double aimGoalHeading = Double.NaN; // WORLD heading that faces the aimed hive

    // Edit Tele_debug_v1.java to change tuning, directions, timing, or diagnostics.
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
        leftfront.setDirection(Tele_debug_v1.LEFTFRONT_DIRECTION);
        leftback.setDirection(Tele_debug_v1.LEFTBACK_DIRECTION);
        rightfront.setDirection(Tele_debug_v1.RIGHTFRONT_DIRECTION);
        rightback.setDirection(Tele_debug_v1.RIGHTBACK_DIRECTION);

        for (DcMotor motor : new DcMotor[] {leftfront, leftback, rightfront, rightback}) {
            motor.setPower(0.0);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            // This program supplies its own pose feedback. Do not run a second
            // motor-encoder position or velocity controller underneath it.
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        try {
            Tele_debug_v1.validateMechanismSettings();
            intake = hardwareMap.get(DcMotor.class, "intake");
            intake.setPower(0.0);
            intake.setDirection(Tele_debug_v1.INTAKE_DIRECTION);
            intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            plate = hardwareMap.get(Servo.class, "plate");
            plate.setDirection(Tele_debug_v1.PLATE_DIRECTION);
            plate.scaleRange(0.0, 1.0);
            if (Tele_debug_v1.PLATE_USE_CUSTOM_PWM_RANGE) {
                if (!(plate instanceof PwmControl)) {
                    throw new IllegalStateException("plate controller does not support custom PWM range");
                }
                ((PwmControl) plate).setPwmRange(new PwmControl.PwmRange(
                        Tele_debug_v1.PLATE_PWM_MIN_US, Tele_debug_v1.PLATE_PWM_MAX_US));
            }
            // No automatic plate move during INIT; the first preset selects its target.

            Tele_debug_v1.validateAimSettings();
            int hives = Tele_debug_v1.HIVE_TAG_IDS.length;
            hiveTagTx = new double[hives][Tele_debug_v1.HIVE_TAG_POSITIONS_CM.length];
            for (double[] tx : hiveTagTx) java.util.Arrays.fill(tx, Double.NaN);
            hiveTagArea = new double[hives][Tele_debug_v1.HIVE_TAG_POSITIONS_CM.length];
            hiveMiddleDeg = new double[hives];
            java.util.Arrays.fill(hiveMiddleDeg, Double.NaN);
            // The Limelight and camera servo are optional here: without them the
            // aim button does nothing and the rest of TeleOp works as before.
            limelight = hardwareMap.tryGet(Limelight3A.class, "limelight");
            if (limelight != null) {
                limelight.pipelineSwitch(Tele_debug_v1.LIMELIGHT_PIPELINE);
                limelight.start();
            }
            cameraAngle = hardwareMap.tryGet(Servo.class, "Camera_angle");
            if (cameraAngle != null) cameraAngle.setDirection(Tele_debug_v1.CAMERA_ANGLE_DIRECTION);
            // You confirmed your odometry already reports correctly. This code
            // does not overwrite pod resolution, offsets, or encoder directions.
            // If another OpMode configures those settings, copy its EXACT setup
            // calls here so this OpMode also works after a fresh power cycle.
            // Do not substitute guessed offsets or a guessed pod type.

            // Reset only during INIT, while the robot is stationary.
            odometry.resetPosAndIMU();
            targetHeading = 0.0; // The startup facing direction is the zero reference.
            sleep(Tele_debug_v1.IMU_RESET_WAIT_MS);
            telemetry.setMsTransmissionInterval(Tele_debug_v1.TELEMETRY_INTERVAL_MS);
            while (!isStarted() && !isStopRequested()) {
                odometry.update();
                // Shows which hive tags are visible so their order can be checked.
                updateAim(odometry.getHeading(AngleUnit.RADIANS), null);
                reportAim();
                Tele_debug_v1.reportInit(telemetry, odometry.getDeviceStatus(),
                        odometry.getPosX(DistanceUnit.CM), odometry.getPosY(DistanceUnit.CM),
                        odometry.getHeading(AngleUnit.DEGREES));
                sleep(Tele_debug_v1.INIT_LOOP_SLEEP_MS);
            }
            waitForStart();
            if (isStopRequested()) return;
            // Tilt the Limelight toward the hive tags; no servo moves during INIT.
            if (cameraAngle != null) cameraAngle.setPosition(Tele_debug_v1.AIM_CAMERA_ANGLE_POSITION);

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
                boolean timingOK = dt > 0.0 && dt <= Tele_debug_v1.MAX_LOOP_SECONDS;
                if (!poseOK || !timingOK) {
                    intake.setPower(0.0);
                    Tele_debug_v1.reportMechanisms(telemetry, plateTargetDegrees, intake.getPower());
                    fault = !poseOK ? "Odometry not ready / invalid pose" : "Control loop delayed";
                    requireRelease = true;
                    tracking = false;
                    stopDrive();
                    Tele_debug_v1.reportSensorFault(telemetry, fault, odometry.getDeviceStatus());
                    sleep(Tele_debug_v1.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }

                // Blue wins if both aim buttons are held.
                Gamepad aimGamepad = mechanismGamepad(Tele_debug_v1.AIM_GAMEPAD);
                int[] aimHives = Tele_debug_v1.isPressed(aimGamepad, Tele_debug_v1.AIM_BLUE_BUTTON)
                        ? Tele_debug_v1.BLUE_HIVES
                        : Tele_debug_v1.isPressed(aimGamepad, Tele_debug_v1.AIM_RED_BUTTON)
                        ? Tele_debug_v1.RED_HIVES : null;
                boolean aimHeld = aimHives != null;
                updateAim(heading, aimHives);

                // Mechanisms remain usable while the drive button is released.
                updateMechanisms();
                Tele_debug_v1.reportMechanisms(telemetry, plateTargetDegrees, intake.getPower());
                reportAim();

                // Release means zero motor power, not position hold. BRAKE reduces
                // coasting, but it is not a mechanical lock or an instantaneous stop.
                // Holding an aim button enables PD exactly like the drive button.
                if (!Tele_debug_v1.isPressed(gamepad1, Tele_debug_v1.DRIVE_ENABLE_BUTTON) && !aimHeld) {
                    stopDrive();
                    resetTarget(x, y, heading);
                    tracking = false;
                    requireRelease = false;
                    fault = "";
                    Tele_debug_v1.reportDisabled(telemetry, x, y, heading,
                            targetX, targetY, targetHeading);
                    sleep(Tele_debug_v1.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }
                if (requireRelease) {
                    stopDrive();
                    Tele_debug_v1.reportReleaseRequired(telemetry, fault);
                    sleep(Tele_debug_v1.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }
                if (!tracking) {
                    // Rebase translation, but keep the heading chosen by the driver.
                    resetTarget(x, y, heading);
                    tracking = true;
                    stopDrive();
                    sleep(Tele_debug_v1.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }

                // Derivative measurement: differentiate WORLD pose, then low-pass
                // filter the velocity. Wrapped angle differences avoid a false
                // full-turn velocity spike when heading crosses +/-180 degrees.
                double alpha = dt / (Tele_debug_v1.VELOCITY_FILTER_SECONDS + dt);
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
                double forwardSpeed = forward / stickLength * Tele_debug_v1.MAX_SPEED_CM_S;
                double leftSpeed = left / stickLength * Tele_debug_v1.MAX_SPEED_CM_S;
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
                double targetAngularVelocity = 2*(-rotate * Tele_debug_v1.MAX_TURN_RAD_S);
                // While a hive is aimed at, aiming replaces the right stick and turns the
                // target toward the hive at the same maximum rate, 2 * MAX_TURN_RAD_S.
                boolean aiming = aimHeld && !Double.isNaN(aimGoalHeading);
                if (aiming) {
                    double maxTurn = 2 * Tele_debug_v1.MAX_TURN_RAD_S;
                    double remaining = wrap(aimGoalHeading - targetHeading);
                    targetAngularVelocity = Math.max(-maxTurn, Math.min(maxTurn, remaining / dt));
                }

                // Integrate velocity to create the internal target pose.
                // If the next step exceeds the lead limit, freeze translation
                // unless that step brings the target closer to the measured robot.
                // This discards unreachable commands instead of storing a backlog.
                double nextX = targetX + targetVelocityX * dt;
                double nextY = targetY + targetVelocityY * dt;
                double oldDistance = Math.hypot(targetX - x, targetY - y);
                double nextDistance = Math.hypot(nextX - x, nextY - y);
                boolean translationLimited = nextDistance > Tele_debug_v1.MAX_TARGET_LEAD_CM
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
                boolean rotationLimited = nextAngleError > Tele_debug_v1.MAX_TARGET_LEAD_RAD
                        && nextAngleError >= oldAngleError;
                if (rotationLimited) {
                    targetAngularVelocity = 0.0;
                } else if (rotate != 0.0f || aiming) {
                    // The only runtime assignment to targetHeading: the right stick or
                    // the aim button. Left-stick input and heading drift never change it.
                    targetHeading = nextHeading;
                }

                double errorX = targetX - x;
                double errorY = targetY - y;
                double errorHeading = wrap(targetHeading - heading);
                if (Math.hypot(errorX, errorY) > Tele_debug_v1.STOP_ERROR_CM
                        || Math.abs(errorHeading) > Tele_debug_v1.STOP_ERROR_RAD) {
                    intake.setPower(0.0);
                    Tele_debug_v1.reportMechanisms(telemetry, plateTargetDegrees, intake.getPower());
                    fault = "Tracking error too large";
                    stopDrive();
                    requireRelease = true;
                    tracking = false;
                    Tele_debug_v1.reportTrackingFault(telemetry, fault);
                    sleep(Tele_debug_v1.CONTROL_LOOP_SLEEP_MS);
                    continue;
                }

                // PD in world coordinates:
                // P corrects position/heading error.
                // D corrects target velocity minus actual velocity (error rate).
                // With centered sticks, target velocity is zero, so D damps motion.
                // No integral or feedforward term is included in this PD-only design.
                double powerX = Tele_debug_v1.KP_POSITION * errorX
                        + Tele_debug_v1.KD_POSITION * (targetVelocityX - velocityX);
                double powerY = Tele_debug_v1.KP_POSITION * errorY
                        + Tele_debug_v1.KD_POSITION * (targetVelocityY - velocityY);
                double powerCCW = Tele_debug_v1.KP_HEADING * errorHeading
                        + Tele_debug_v1.KD_HEADING * (targetAngularVelocity - angularVelocity);
                powerCCW = applyHeadingTolerance(powerCCW, errorHeading, rotate != 0.0f || aiming);

                // Convert world correction back into the robot's current axes.
                double forwardPower = cos * powerX + sin * powerY;
                double leftPower = -sin * powerX + cos * powerY;
                double clockwisePower = -powerCCW;
                drive(forwardPower, leftPower, clockwisePower);
                //  drive(forward, left, rotate);

                Tele_debug_v1.reportRunning(telemetry, x, y, heading,
                        targetX, targetY, targetHeading, errorX, errorY, errorHeading,
                        headingSettled, translationLimited, rotationLimited);
                sleep(Tele_debug_v1.CONTROL_LOOP_SLEEP_MS);
            }
        } finally {
            // Also stop if an exception interrupts the control loop.
            if (intake != null) intake.setPower(0.0);
            stopDrive();
            if (limelight != null) limelight.stop();
        }
    }

    /**
     * Reads the newest Limelight frame and finds, for every hive, the angle to its
     * middle from any of its visible tags. While an aim button is held, aims at one
     * of that alliance's hives: the nearest usable one (largest average tag area),
     * kept until it has been out of view for AIM_LOST_MS. Each fresh view of it sets
     * aimGoalHeading. hives is BLUE_HIVES or RED_HIVES while its button is held,
     * otherwise null.
     */
    private void updateAim(double heading, int[] hives) {
        if (hives != aimAlliance) {
            // Released, or switched to the other alliance's button: drop the old hive.
            aimAlliance = hives;
            aimHive = -1;
            aimGoalHeading = Double.NaN;
        }
        if (limelight == null) return;
        LLResult result = limelight.getLatestResult();
        if (result == null || result.getControlHubTimeStamp() == lastFrameTime) return;
        lastFrameTime = result.getControlHubTimeStamp();

        for (int h = 0; h < hiveTagTx.length; h++) {
            java.util.Arrays.fill(hiveTagTx[h], Double.NaN);
            java.util.Arrays.fill(hiveTagArea[h], Double.NaN);
        }
        if (result.isValid() && result.getStaleness() <= Tele_debug_v1.LIMELIGHT_MAX_STALENESS_MS) {
            for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
                for (int h = 0; h < hiveTagTx.length; h++) {
                    int[] ids = Tele_debug_v1.HIVE_TAG_IDS[h];
                    for (int i = 0; i < ids.length; i++) {
                        if (ids[i] != tag.getFiducialId() || !Double.isNaN(hiveTagTx[h][i])) continue;
                        hiveTagTx[h][i] = tag.getTargetXDegrees();
                        hiveTagArea[h][i] = tag.getTargetArea();
                    }
                }
            }
        }

        double[] positions = Tele_debug_v1.HIVE_TAG_POSITIONS_CM;
        for (int h = 0; h < hiveTagTx.length; h++) {
            // Any view of two or more tags measures the tag spacing for one-tag views.
            double scale = HiveAim.scalePerRootArea(hiveTagTx[h], hiveTagArea[h], positions);
            if (!Double.isNaN(scale)) scalePerRootArea = scale;
        }
        for (int h = 0; h < hiveTagTx.length; h++) {
            hiveMiddleDeg[h] = HiveAim.middleDeg(hiveTagTx[h], hiveTagArea[h], positions, scalePerRootArea);
        }
        nearestBlue = nearestHive(Tele_debug_v1.BLUE_HIVES);
        nearestRed = nearestHive(Tele_debug_v1.RED_HIVES);
        if (hives == null) return;

        int nearest = hives == Tele_debug_v1.BLUE_HIVES ? nearestBlue : nearestRed;
        long now = System.nanoTime();
        if (aimHive >= 0 && !Double.isNaN(hiveMiddleDeg[aimHive])) {
            aimHiveSeenNanos = now;
        } else if (aimHive < 0 || now - aimHiveSeenNanos > Tele_debug_v1.AIM_LOST_MS * 1_000_000L) {
            aimHive = nearest;
            aimHiveSeenNanos = now;
        }
        if (aimHive >= 0 && !Double.isNaN(hiveMiddleDeg[aimHive])) {
            // tx > 0 means the hive is to the right, so turn clockwise (heading decreases).
            double errorDeg = hiveMiddleDeg[aimHive] - Tele_debug_v1.AIM_OFFSET_DEG;
            aimGoalHeading = wrap(heading - Tele_debug_v1.AIM_TURN_SIGN * Math.toRadians(errorDeg));
        }
    }

    /** Index of the nearest listed hive that can be aimed at, or -1 if none. */
    private int nearestHive(int[] hives) {
        int nearest = -1;
        double nearestArea = 0.0;
        for (int hive : hives) {
            int h = hive - 1;
            if (Double.isNaN(hiveMiddleDeg[h])) continue;
            // Closer tags look bigger, so the nearest hive has the largest average tag area.
            double sumArea = 0.0;
            for (double area : hiveTagArea[h]) {
                if (!Double.isNaN(area)) sumArea += area;
            }
            double meanArea = sumArea / HiveAim.visibleCount(hiveTagTx[h]);
            if (nearest < 0 || meanArea > nearestArea) {
                nearest = h;
                nearestArea = meanArea;
            }
        }
        return nearest;
    }

    private void reportAim() {
        String alliance = aimAlliance == null ? null
                : aimAlliance == Tele_debug_v1.BLUE_HIVES ? "blue" : "red";
        Tele_debug_v1.reportAim(telemetry, limelight != null, alliance, aimHive, nearestBlue, nearestRed,
                hiveMiddleDeg, hiveTagTx, !Double.isNaN(scalePerRootArea));
    }

    private Gamepad mechanismGamepad(int number) {
        return number == 1 ? gamepad1 : gamepad2;
    }

    private void updateMechanisms() {
        Gamepad plateGamepad = mechanismGamepad(Tele_debug_v1.PLATE_GAMEPAD);
        // If several preset buttons are held, priority is preset 1, 2, 3, then 4.
        double requestedDegrees = Double.NaN;
        if (Tele_debug_v1.isPressed(plateGamepad, Tele_debug_v1.PLATE_PRESET_1_BUTTON)) {
            requestedDegrees = Tele_debug_v1.PLATE_PRESET_1_DEGREES;
        } else if (Tele_debug_v1.isPressed(plateGamepad, Tele_debug_v1.PLATE_PRESET_2_BUTTON)) {
            requestedDegrees = Tele_debug_v1.PLATE_PRESET_2_DEGREES;
        } else if (Tele_debug_v1.isPressed(plateGamepad, Tele_debug_v1.PLATE_PRESET_3_BUTTON)) {
            requestedDegrees = Tele_debug_v1.PLATE_PRESET_3_DEGREES;
        } else if (Tele_debug_v1.isPressed(plateGamepad, Tele_debug_v1.PLATE_PRESET_4_BUTTON)) {
            requestedDegrees = Tele_debug_v1.PLATE_PRESET_4_DEGREES;
        }
        if (!Double.isNaN(requestedDegrees) && requestedDegrees != plateTargetDegrees) {
            plate.setPosition(Tele_debug_v1.platePosition(requestedDegrees));
            plateTargetDegrees = requestedDegrees;
        }

        Gamepad intakeGamepad = mechanismGamepad(Tele_debug_v1.INTAKE_GAMEPAD);
        // Preserve original behavior: forward button wins if both are held.
        if (Tele_debug_v1.isPressed(intakeGamepad, Tele_debug_v1.INTAKE_FORWARD_BUTTON)) {
            intake.setPower(Tele_debug_v1.INTAKE_FORWARD_POWER);
        } else if (Tele_debug_v1.isPressed(intakeGamepad, Tele_debug_v1.INTAKE_REVERSE_BUTTON)) {
            intake.setPower(Tele_debug_v1.INTAKE_REVERSE_POWER);
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
        // Deliberately preserve targetHeading, even when the drive button is released.
    }

    private double applyHeadingTolerance(double powerCCW, double errorHeading, boolean turning) {
        double angleError = Math.abs(errorHeading);
        double turnRate = Math.abs(angularVelocity);
        if (turning) {
            headingSettled = false; // Always respond to turning input and aiming.
        } else if (headingSettled) {
            if (angleError >= Tele_debug_v1.HEADING_RESUME_RAD || turnRate >= Tele_debug_v1.RESUME_TURN_RATE_RAD_S) {
                headingSettled = false;
            }
        } else if (angleError <= Tele_debug_v1.HEADING_TOLERANCE_RAD && turnRate <= Tele_debug_v1.SETTLED_TURN_RATE_RAD_S) {
            headingSettled = true;
        }
        // Tolerance changes motor correction, never the stored target angle.
        return headingSettled ? 0.0 : powerCCW;
    }

    private void drive(double forwardPower, double leftPower, double clockwisePower) {
        forwardPower *= Tele_debug_v1.FORWARD_MIX_SIGN;
        leftPower *= Tele_debug_v1.LEFT_MIX_SIGN;
        clockwisePower *= Tele_debug_v1.CLOCKWISE_MIX_SIGN;

        // Your original wheel equations, with PD outputs replacing joystick values.
        double leftfrontPower = forwardPower + leftPower + clockwisePower;
        double rightfrontPower = -forwardPower + leftPower + clockwisePower;
        double leftbackPower = forwardPower - leftPower + clockwisePower;
        double rightbackPower = -forwardPower - leftPower + clockwisePower;

        // Scale all wheels together to retain their ratios during saturation.
        // Independent clipping would distort the requested motion direction.
        double largest = Math.max(Math.max(Math.abs(leftfrontPower), Math.abs(rightfrontPower)),
                Math.max(Math.abs(leftbackPower), Math.abs(rightbackPower)));
        double scale = largest > Tele_debug_v1.MAX_MOTOR_POWER ? Tele_debug_v1.MAX_MOTOR_POWER / largest : 1.0;
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
        if (Math.abs(value) <= Tele_debug_v1.STICK_DEADBAND) return 0.0;
        // Rescale the remaining range smoothly from zero to full command.
        return Math.copySign((Math.abs(value) - Tele_debug_v1.STICK_DEADBAND) / (1.0 - Tele_debug_v1.STICK_DEADBAND), value);
    }

    private static double wrap(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
