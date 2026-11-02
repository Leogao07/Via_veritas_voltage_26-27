package org.firstinspires.ftc.teamcode.vision;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Forwards the Limelight 3A's network ports through the Control Hub, so a laptop
 * on the robot's Wi-Fi can open the Limelight web interface and tune pipelines at
 * http://192.168.43.1:5801 while the Limelight stays plugged into the hub's USB.
 *
 * Over USB the Limelight is reachable only from the Control Hub, at 172.29.0.1.
 * start() listens on ports 5800-5809 of every Control Hub network interface and
 * passes each connection through to the same port on the Limelight: 5800 is the
 * camera stream, 5801 the web interface, and the others carry its live data.
 *
 * Forwarding keeps running after the OpMode that started it ends, until the Robot
 * Controller app restarts; calling start() again does nothing. OpModes do not need
 * it to use the Limelight; it is only for tuning from a laptop.
 */
public final class LimelightPortForwarder {
    private LimelightPortForwarder() { }

    // Limelight 3A address on the Control Hub's USB connection.
    public static final String LIMELIGHT_ADDRESS = "172.29.0.1";
    public static final int FIRST_PORT = 5800;
    public static final int LAST_PORT = 5809;

    // Unit: ms. Give up on a connection the Limelight does not answer in this time.
    private static final int CONNECT_TIMEOUT_MS = 2000;

    private static boolean started;
    private static final AtomicInteger listeningPorts = new AtomicInteger();

    /** Starts forwarding FIRST_PORT..LAST_PORT; later calls do nothing. */
    public static synchronized void start() {
        if (started) return;
        started = true;
        for (int port = FIRST_PORT; port <= LAST_PORT; port++) {
            final int forwardedPort = port;
            startDaemon("Limelight forward " + port, () -> listen(forwardedPort));
        }
    }

    /** Number of ports currently accepting connections, for telemetry. */
    public static int listeningPorts() {
        return listeningPorts.get();
    }

    public static int portCount() {
        return LAST_PORT - FIRST_PORT + 1;
    }

    private static void listen(int port) {
        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(port));
            listeningPorts.incrementAndGet();
            try {
                while (true) {
                    Socket client = server.accept();
                    startDaemon("Limelight forward " + port + " client", () -> bridge(client, port));
                }
            } finally {
                listeningPorts.decrementAndGet();
            }
        } catch (IOException e) {
            // The port could not be opened or stopped accepting; it stays unforwarded.
        }
    }

    /** Connects one laptop connection to the same port on the Limelight. */
    private static void bridge(Socket client, int port) {
        Socket limelight = new Socket();
        try {
            client.setTcpNoDelay(true);
            limelight.setTcpNoDelay(true);
            limelight.connect(new InetSocketAddress(LIMELIGHT_ADDRESS, port), CONNECT_TIMEOUT_MS);
        } catch (IOException e) {
            close(client);
            close(limelight);
            return;
        }
        startDaemon("Limelight forward " + port + " reply", () -> pipe(limelight, client));
        pipe(client, limelight);
    }

    /** Copies bytes until either side closes, then closes both sockets. */
    private static void pipe(Socket from, Socket to) {
        byte[] buffer = new byte[16 * 1024];
        try {
            InputStream in = from.getInputStream();
            OutputStream out = to.getOutputStream();
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (IOException e) {
            // A closed socket ends the copy.
        } finally {
            close(from);
            close(to);
        }
    }

    private static void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            // Already closed.
        }
    }

    private static void startDaemon(String name, Runnable task) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }
}
