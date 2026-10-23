package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * Autonomous execution logic. Put adjustable values in auto_debug_V1.java.
 * This starting scaffold has no hardware initialization or route actions yet.
 * It waits for START, reports completion, and exits without commanding hardware.
 */
@Autonomous(name = "auto_V1", group = "Auto")
public class auto_V1 extends LinearOpMode {
    @Override
    public void runOpMode() {
        telemetry.setMsTransmissionInterval(auto_debug_V1.TELEMETRY_INTERVAL_MS);
        telemetry.addData("Status", "Initialized");
        telemetry.addLine("Autonomous scaffold: no robot actions configured.");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        // Add the autonomous route and mechanism sequence here.
        // Store their tunable values in auto_debug_V1.java.
        telemetry.addData("Status", "Complete: no autonomous actions configured");
        telemetry.update();
    }
}
