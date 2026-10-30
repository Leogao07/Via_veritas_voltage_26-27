package org.firstinspires.ftc.teamcode.vision;

/**
 * Finds the middle of a hive from the Limelight tx angles of its AprilTags.
 * It is shared so that TeleOP_v1's aim buttons and any Auto program that aims at
 * a hive work the same way. This helper is not an OpMode and has no settings of
 * its own.
 *
 * A hive's tags sit in a straight row. Each tag's u = tan(tx) follows
 * u = (m + k1 * p) / (1 + k2 * p), where p is the tag's position along the row
 * and m = tan(angle to the middle of the hive, p = 0). Rearranged as
 * m + k1 * p - k2 * u * p = u it is linear in m, k1, k2, so a least-squares fit
 * solves it exactly from three or more tags, including the perspective of a
 * row seen at an angle. With two tags k2 is taken as 0 (a straight line in u),
 * which is slightly off when the robot is close to the row and sees it at an angle.
 */
public final class HiveAim {
    private HiveAim() { }

    /**
     * Angle in degrees to the middle of the hive, or NaN when fewer than minTags
     * tags are visible, they are out of order, or the fit fails.
     * tx[i] is tag i's Limelight tx in degrees, NaN when not seen; positions[i]
     * is its place along the row, increasing from left to right.
     */
    public static double middleDeg(double[] tx, double[] positions, int minTags) {
        int visible = visibleCount(tx);
        if (visible < Math.max(2, minTags) || !inOrder(tx)) return Double.NaN;
        int unknowns = visible >= 3 ? 3 : 2;
        double[][] normal = new double[unknowns][unknowns + 1]; // [A^T A | A^T u]
        for (int i = 0; i < tx.length; i++) {
            if (Double.isNaN(tx[i])) continue;
            double p = positions[i];
            double u = Math.tan(Math.toRadians(tx[i]));
            double[] row = {1.0, p, -u * p};
            for (int r = 0; r < unknowns; r++) {
                for (int c = 0; c < unknowns; c++) normal[r][c] += row[r] * row[c];
                normal[r][unknowns] += row[r] * u;
            }
        }
        double middleTan = solve(normal)[0];
        if (Double.isNaN(middleTan) || Double.isInfinite(middleTan)) return Double.NaN;
        return Math.toDegrees(Math.atan(middleTan));
    }

    /**
     * Angle in degrees to the middle of the hive from one or more visible tags, or
     * NaN when no tag is visible or they are out of order. Two or more tags are
     * fitted as in middleDeg(tx, positions, 2). A single tag is shifted to the
     * middle by its position times the row scale, estimated from the tag's size as
     * scalePerRootArea * sqrt(area): closer tags look bigger and lie farther apart.
     * With an unknown scale (NaN) the single tag itself is the aim point.
     * area[i] is tag i's Limelight target area in % of the image.
     */
    public static double middleDeg(double[] tx, double[] area, double[] positions,
                                   double scalePerRootArea) {
        if (visibleCount(tx) != 1) return middleDeg(tx, positions, 2);
        for (int i = 0; i < tx.length; i++) {
            if (Double.isNaN(tx[i])) continue;
            if (Double.isNaN(scalePerRootArea) || !(area[i] > 0.0)) return tx[i];
            double scale = scalePerRootArea * Math.sqrt(area[i]);
            return Math.toDegrees(Math.atan(Math.tan(Math.toRadians(tx[i])) - scale * positions[i]));
        }
        return Double.NaN; // Not reached: exactly one tag is visible.
    }

    /**
     * Row scale per square root of tag area, from two or more visible tags: the
     * slope of tan(tx) against tag position divided by the square root of the tags'
     * mean area. It barely changes with distance, so the caller keeps the latest
     * value to place the middle from a later one-tag view. NaN with fewer than two
     * tags, tags out of order, or no area.
     */
    public static double scalePerRootArea(double[] tx, double[] area, double[] positions) {
        int visible = visibleCount(tx);
        if (visible < 2 || !inOrder(tx)) return Double.NaN;
        double sumPosition = 0.0;
        double sumTan = 0.0;
        double sumArea = 0.0;
        for (int i = 0; i < tx.length; i++) {
            if (Double.isNaN(tx[i])) continue;
            sumPosition += positions[i];
            sumTan += Math.tan(Math.toRadians(tx[i]));
            sumArea += area[i];
        }
        double meanPosition = sumPosition / visible;
        double meanTan = sumTan / visible;
        double meanArea = sumArea / visible;
        if (!(meanArea > 0.0)) return Double.NaN;
        double sumPositionSquared = 0.0;
        double sumPositionTan = 0.0;
        for (int i = 0; i < tx.length; i++) {
            if (Double.isNaN(tx[i])) continue;
            double offset = positions[i] - meanPosition;
            sumPositionSquared += offset * offset;
            sumPositionTan += offset * (Math.tan(Math.toRadians(tx[i])) - meanTan);
        }
        return sumPositionTan / sumPositionSquared / Math.sqrt(meanArea);
    }

    /** Number of tags seen: the entries of tx that are not NaN. */
    public static int visibleCount(double[] tx) {
        int count = 0;
        for (double value : tx) {
            if (!Double.isNaN(value)) count++;
        }
        return count;
    }

    /** True when the visible tags' tx increase from left to right, as a row requires. */
    public static boolean inOrder(double[] tx) {
        double previous = Double.NEGATIVE_INFINITY;
        for (double value : tx) {
            if (Double.isNaN(value)) continue;
            if (value <= previous) return false;
            previous = value;
        }
        return true;
    }

    /** Solves the augmented system [A | b] by Gauss-Jordan elimination; returns x. */
    private static double[] solve(double[][] m) {
        int n = m.length;
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int r = col + 1; r < n; r++) {
                if (Math.abs(m[r][col]) > Math.abs(m[pivot][col])) pivot = r;
            }
            double[] swap = m[col];
            m[col] = m[pivot];
            m[pivot] = swap;
            for (int r = 0; r < n; r++) {
                if (r == col) continue;
                double factor = m[r][col] / m[col][col];
                for (int c = col; c <= n; c++) m[r][c] -= factor * m[col][c];
            }
        }
        double[] x = new double[n];
        for (int i = 0; i < n; i++) x[i] = m[i][n] / m[i][i];
        return x;
    }
}
