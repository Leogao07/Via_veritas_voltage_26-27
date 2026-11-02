package org.firstinspires.ftc.teamcode.vision;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.teamcode.teleop.Tele_debug_v1;

/**
 * LIMELIGHT TUNING: opens the Limelight web interface to a laptop on the robot Wi-Fi.
 *
 * INIT starts LimelightPortForwarder, so a laptop connected to the robot's Wi-Fi
 * can open http://192.168.43.1:5801 and adjust pipelines while the Limelight stays
 * on the Control Hub's USB. Telemetry shows the active pipeline and every AprilTag
 * seen, left to right, with the hive it belongs to in Tele_debug_v1.HIVE_TAG_IDS.
 *
 * This OpMode never drives the robot. After START it holds Camera_angle at
 * Tele_debug_v1.AIM_CAMERA_ANGLE_POSITION so the camera sees what TeleOp sees.
 * Forwarding keeps running after STOP, until the Robot Controller app restarts.
 * TeleOP_v1 also starts it in INIT while Tele_debug_v1.LIMELIGHT_PORT_FORWARDING
 * is true, so the pipelines can be tuned during normal TeleOp as well.
 */
@TeleOp(name = "Limelight_tuning", group = "Tuning")
public class LimelightTuning extends LinearOpMode {
    @Override
    public void runOpMode() {
        LimelightPortForwarder.start();

        Limelight3A limelight = hardwareMap.tryGet(Limelight3A.class, "limelight");
        Servo cameraAngle = hardwareMap.tryGet(Servo.class, "Camera_angle");
        if (cameraAngle != null) cameraAngle.setDirection(Tele_debug_v1.CAMERA_ANGLE_DIRECTION);
        // No pipelineSwitch(): the pipeline chosen in the web interface stays active.
        if (limelight != null) limelight.start();
        telemetry.setMsTransmissionInterval(Tele_debug_v1.TELEMETRY_INTERVAL_MS);

        try {
            // Runs through INIT and, after START, until STOP.
            while (!isStopRequested()) {
                // No servo moves during INIT.
                if (isStarted() && cameraAngle != null) {
                    cameraAngle.setPosition(Tele_debug_v1.AIM_CAMERA_ANGLE_POSITION);
                }
                report(limelight);
                sleep(Tele_debug_v1.TELEMETRY_INTERVAL_MS);
            }
        } finally {
            if (limelight != null) limelight.stop();
        }
    }

    private void report(Limelight3A limelight) {
        telemetry.addLine("Laptop on the robot Wi-Fi: open http://192.168.43.1:5801");
        telemetry.addData("Ports forwarded", "%d of %d",
                LimelightPortForwarder.listeningPorts(), LimelightPortForwarder.portCount());
        if (limelight == null) {
            telemetry.addLine("No \"limelight\" in the active configuration.");
            telemetry.update();
            return;
        }
        telemetry.addData("Limelight connected", limelight.isConnected());

        LLResult result = limelight.getLatestResult();
        if (result == null) {
            telemetry.addLine("No result from the Limelight yet.");
        } else {
            telemetry.addData("Pipeline", "%d (%s)", result.getPipelineIndex(), result.getPipelineType());
            List<LLResultTypes.FiducialResult> tags = result.isValid()
                    ? new ArrayList<>(result.getFiducialResults()) : new ArrayList<>();
            // Sorted by tx: the order the camera sees them, left to right.
            tags.sort((a, b) -> Double.compare(a.getTargetXDegrees(), b.getTargetXDegrees()));
            telemetry.addData("AprilTags seen", tags.size());
            for (LLResultTypes.FiducialResult tag : tags) {
                telemetry.addData("Tag " + tag.getFiducialId() + hiveName(tag.getFiducialId()),
                        "tx %.1f  ty %.1f  area %.2f%%",
                        tag.getTargetXDegrees(), tag.getTargetYDegrees(), tag.getTargetArea());
            }
        }
        if (!isStarted()) telemetry.addLine("Press START to hold Camera_angle at the TeleOp aim position.");
        telemetry.update();
    }

    /** " (hive N)" for a tag listed in Tele_debug_v1.HIVE_TAG_IDS, otherwise "". */
    private static String hiveName(int id) {
        for (int h = 0; h < Tele_debug_v1.HIVE_TAG_IDS.length; h++) {
            for (int hiveId : Tele_debug_v1.HIVE_TAG_IDS[h]) {
                if (hiveId == id) return " (hive " + (h + 1) + ")";
            }
        }
        return "";
    }
}
