package com.dashpig.farmstorm.util;

/**
 * 距离度量（D-20）：陷阱与炮台判定用切比雪夫距离，增益光环用欧氏半径。
 * 单独成类是为了让「用了哪种度量」在调用点一眼可见，而不是散落成 Math.max/Math.sqrt。
 */
public final class DistanceMetrics {
    private DistanceMetrics() {}

    /** 切比雪夫距离：三维坐标差的绝对值最大值。 */
    public static double chebyshev(double dx, double dy, double dz) {
        return Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
    }

    /** 欧氏距离：三维坐标差的平方和开根。 */
    public static double euclidean(double dx, double dy, double dz) {
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
