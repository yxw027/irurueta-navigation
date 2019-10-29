package com.irurueta.navigation.frames;

import com.irurueta.algebra.DecomposerException;
import com.irurueta.algebra.Matrix;
import com.irurueta.algebra.RankDeficientMatrixException;
import com.irurueta.algebra.Utils;
import com.irurueta.algebra.WrongSizeException;
import com.irurueta.geometry.InvalidRotationMatrixException;
import com.irurueta.geometry.Quaternion;
import com.irurueta.geometry.Rotation3D;
import com.irurueta.navigation.geodesic.Constants;
import com.irurueta.statistics.UniformRandomizer;
import com.irurueta.units.Angle;
import com.irurueta.units.AngleUnit;
import com.irurueta.units.Time;
import com.irurueta.units.TimeUnit;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class CoordinateTransformationMatrixTest {

    private static final double THRESHOLD = 1e-6;
    private static final double MIN_ANGLE_DEGREES = -45.0;
    private static final double MAX_ANGLE_DEGREES = 45.0;
    private static final double LATITUDE_DEGREES = 41.3825;
    private static final double LONGITUDE_DEGREES = 2.176944;
    private static final double TIME_INTERVAL_SECONDS = 0.02;

    private static final double ABSOLUTE_ERROR = 1e-6;

    @Test
    public void testConstants() {
        assertEquals(CoordinateTransformationMatrix.ROWS, 3);
        assertEquals(CoordinateTransformationMatrix.COLS, 3);
        assertEquals(CoordinateTransformationMatrix.DEFAULT_THRESHOLD,
                Rotation3D.DEFAULT_VALID_THRESHOLD, 0.0);
        assertEquals(CoordinateTransformationMatrix.EARTH_ROTATION_RATE,
                Constants.EARTH_ROTATION_RATE, 0.0);
    }

    @Test
    public void testConstructor() throws WrongSizeException,
            InvalidRotationMatrixException {

        // test constructor with source and destination
        CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check
        assertEquals(c.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c.getMatrix(), Matrix.identity(3, 3));

        // Force NullPointerException
        c = null;
        try {
            c = new CoordinateTransformationMatrix(null,
                    FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(FrameType.LOCAL_NAVIGATION_FRAME,
                    null);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
        assertNull(c);


        // test constructor with matrix, source, destination and threshold
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        c = new CoordinateTransformationMatrix(m, FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME, THRESHOLD);

        // check
        assertEquals(c.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c.getMatrix(), m);

        // Force InvalidRotationMatrixException
        c = null;
        try {
            c = new CoordinateTransformationMatrix(new Matrix(3, 3),
                    FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.LOCAL_NAVIGATION_FRAME, THRESHOLD);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(new Matrix(1, 3),
                    FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.LOCAL_NAVIGATION_FRAME, THRESHOLD);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(new Matrix(3, 1),
                    FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.LOCAL_NAVIGATION_FRAME, THRESHOLD);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        assertNull(c);

        // Force NullPointerException
        try {
            c = new CoordinateTransformationMatrix(m, null,
                    FrameType.LOCAL_NAVIGATION_FRAME, THRESHOLD);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(m,
                    FrameType.LOCAL_NAVIGATION_FRAME, null, THRESHOLD);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
        assertNull(c);

        // Force IllegalArgumentException
        try {
            c = new CoordinateTransformationMatrix(m, FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME, -1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(c);


        // test constructor with matrix, source and destination
        c = new CoordinateTransformationMatrix(m, FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check
        assertEquals(c.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c.getMatrix(), m);

        // Force InvalidRotationMatrixException
        c = null;
        try {
            c = new CoordinateTransformationMatrix(new Matrix(3, 3),
                    FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.LOCAL_NAVIGATION_FRAME);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(new Matrix(1, 3),
                    FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.LOCAL_NAVIGATION_FRAME);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(new Matrix(3, 1),
                    FrameType.LOCAL_NAVIGATION_FRAME,
                    FrameType.LOCAL_NAVIGATION_FRAME);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        assertNull(c);

        // Force NullPointerException
        try {
            c = new CoordinateTransformationMatrix(m, null,
                    FrameType.LOCAL_NAVIGATION_FRAME);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
        try {
            c = new CoordinateTransformationMatrix(m,
                    FrameType.LOCAL_NAVIGATION_FRAME, null);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
        assertNull(c);

        // test constructor from euler angles
        c = new CoordinateTransformationMatrix(roll, pitch, yaw,
                FrameType.LOCAL_NAVIGATION_FRAME, FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check
        assertEquals(c.getRollEulerAngle(), roll, ABSOLUTE_ERROR);
        assertEquals(c.getPitchEulerAngle(), pitch, ABSOLUTE_ERROR);
        assertEquals(c.getYawEulerAngle(), yaw, ABSOLUTE_ERROR);
        assertEquals(c.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);


        // test constructor from euler angles
        final Angle rollAngle = new Angle(roll, AngleUnit.RADIANS);
        final Angle pitchAngle = new Angle(pitch, AngleUnit.RADIANS);
        final Angle yawAngle = new Angle(yaw, AngleUnit.RADIANS);

        c = new CoordinateTransformationMatrix(rollAngle, pitchAngle, yawAngle,
                FrameType.LOCAL_NAVIGATION_FRAME, FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check
        assertEquals(c.getRollEulerAngle(), roll, ABSOLUTE_ERROR);
        assertEquals(c.getPitchEulerAngle(), pitch, ABSOLUTE_ERROR);
        assertEquals(c.getYawEulerAngle(), yaw, ABSOLUTE_ERROR);
        assertEquals(c.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertTrue(c.getRollEulerAngleMeasurement().equals(rollAngle, THRESHOLD));
        assertTrue(c.getPitchEulerAngleMeasurement().equals(pitchAngle, THRESHOLD));
        assertTrue(c.getYawEulerAngleMeasurement().equals(yawAngle, THRESHOLD));


        // test constructor from another value
        c = new CoordinateTransformationMatrix(m, FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(c);

        // check
        assertEquals(c.getSourceType(), c2.getSourceType());
        assertEquals(c.getDestinationType(), c2.getDestinationType());
        assertEquals(c.getMatrix(), c2.getMatrix());
    }

    @Test
    public void testGetMatrix() throws InvalidRotationMatrixException,
            WrongSizeException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        Matrix m2 = c.getMatrix();

        Matrix m3 = new Matrix(1, 1);
        c.getMatrix(m3);

        assertEquals(m, m2);
        assertNotSame(m, m2);

        assertEquals(m, m3);
        assertNotSame(m, m3);
    }

    @Test
    public void testSetMatrixWithThreshold() throws WrongSizeException,
            InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();

        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check default value
        assertEquals(c.getMatrix(), Matrix.identity(3, 3));

        // set new value
        c.setMatrix(m, THRESHOLD);

        // check
        assertEquals(c.getMatrix(), m);
        assertNotSame(c.getMatrix(), m);

        // Force InvalidRotationMatrixException
        try {
            c.setMatrix(new Matrix(3, 3), THRESHOLD);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c.setMatrix(new Matrix(1, 3), THRESHOLD);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c.setMatrix(new Matrix(3, 1), THRESHOLD);
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }

        // Force IllegalArgumentException
        try {
            c.setMatrix(m, -1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testSetMatrixWithoutThreshold() throws WrongSizeException,
            InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();

        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check default value
        assertEquals(c.getMatrix(), Matrix.identity(3, 3));

        // set new value
        c.setMatrix(m);

        // check
        assertEquals(c.getMatrix(), m);
        assertNotSame(c.getMatrix(), m);

        // Force InvalidRotationMatrixException
        try {
            c.setMatrix(new Matrix(3, 3));
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c.setMatrix(new Matrix(1, 3));
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
        try {
            c.setMatrix(new Matrix(3, 1));
            fail("InvalidRotationMatrixException expected but not thrown");
        } catch (final InvalidRotationMatrixException ignore) {
        }
    }

    @Test
    public void testIsValidMatrixWithThreshold() throws WrongSizeException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();

        assertTrue(CoordinateTransformationMatrix.isValidMatrix(m, THRESHOLD));

        assertFalse(CoordinateTransformationMatrix.isValidMatrix(
                new Matrix(3, 3), THRESHOLD));
        assertFalse(CoordinateTransformationMatrix.isValidMatrix(
                new Matrix(1, 3), THRESHOLD));
        assertFalse(CoordinateTransformationMatrix.isValidMatrix(
                new Matrix(3, 1), THRESHOLD));

        // test with Nan matrix
        m.initialize(Double.NaN);
        assertFalse(CoordinateTransformationMatrix.isValidMatrix(m, THRESHOLD));

        // Force IllegalArgumentException
        try {
            CoordinateTransformationMatrix.isValidMatrix(m, -1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testIsValidMatrixWithoutThreshold() throws WrongSizeException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();

        assertTrue(CoordinateTransformationMatrix.isValidMatrix(m));

        assertFalse(CoordinateTransformationMatrix.isValidMatrix(
                new Matrix(3, 3)));
        assertFalse(CoordinateTransformationMatrix.isValidMatrix(
                new Matrix(1, 3)));
        assertFalse(CoordinateTransformationMatrix.isValidMatrix(
                new Matrix(3, 1)));

        // test with Nan matrix
        m.initialize(Double.NaN);
        assertFalse(CoordinateTransformationMatrix.isValidMatrix(m));
    }

    @Test
    public void testGetSetEulerAngles() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll1 = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch1 = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw1 = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));

        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        c.setEulerAngles(roll1, pitch1, yaw1);

        final double roll2 = c.getRollEulerAngle();
        final double pitch2 = c.getPitchEulerAngle();
        final double yaw2 = c.getYawEulerAngle();

        assertEquals(roll1, roll2, ABSOLUTE_ERROR);
        assertEquals(pitch1, pitch2, ABSOLUTE_ERROR);
        assertEquals(yaw1, yaw2, ABSOLUTE_ERROR);
    }

    @Test
    public void testGetSetEulerAnglesMeasurements() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));

        final Angle rollAngle1 = new Angle(roll, AngleUnit.RADIANS);
        final Angle pitchAngle1 = new Angle(pitch, AngleUnit.RADIANS);
        final Angle yawAngle1 = new Angle(yaw, AngleUnit.RADIANS);

        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        c.setEulerAngles(rollAngle1, pitchAngle1, yawAngle1);

        final Angle rollAngle2 = new Angle(0.0, AngleUnit.DEGREES);
        c.getRollEulerAngleMeasurement(rollAngle2);
        final Angle rollAngle3 = c.getRollEulerAngleMeasurement();

        final Angle pitchAngle2 = new Angle(0.0, AngleUnit.DEGREES);
        c.getPitchEulerAngleMeasurement(pitchAngle2);
        final Angle pitchAngle3 = c.getPitchEulerAngleMeasurement();

        final Angle yawAngle2 = new Angle(0.0, AngleUnit.DEGREES);
        c.getYawEulerAngleMeasurement(yawAngle2);
        final Angle yawAngle3 = c.getYawEulerAngleMeasurement();

        assertTrue(rollAngle1.equals(rollAngle2, THRESHOLD));
        assertTrue(rollAngle1.equals(rollAngle3, THRESHOLD));

        assertTrue(pitchAngle1.equals(pitchAngle2, THRESHOLD));
        assertTrue(pitchAngle1.equals(pitchAngle3, THRESHOLD));

        assertTrue(yawAngle1.equals(yawAngle2, THRESHOLD));
        assertTrue(yawAngle1.equals(yawAngle3, THRESHOLD));
    }

    @Test
    public void testGetSetSourceType() {

        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check initial value
        assertEquals(c.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);

        // set new value
        c.setSourceType(FrameType.BODY_FRAME);

        // check
        assertEquals(c.getSourceType(), FrameType.BODY_FRAME);

        // Force NullPointerException
        try {
            c.setSourceType(null);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testGetSetDestinationType() {

        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check initial value
        assertEquals(c.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // set new value
        c.setDestinationType(FrameType.BODY_FRAME);

        // check
        assertEquals(c.getDestinationType(), FrameType.BODY_FRAME);

        // Force NullPointerException
        try {
            c.setDestinationType(null);
            fail("NullPointerException expected but not thrown");
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testAsRotation() throws InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final Rotation3D rotation1 = c.asRotation();
        Quaternion rotation2 = new Quaternion();
        c.asRotation(rotation2);

        assertEquals(m, rotation1.asInhomogeneousMatrix());
        assertTrue(m.equals(rotation2.asInhomogeneousMatrix(), THRESHOLD));
    }

    @Test
    public void testCopyTo() throws InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);

        c1.copyTo(c2);

        // check
        assertEquals(c1.getSourceType(), c2.getSourceType());
        assertEquals(c1.getDestinationType(), c2.getDestinationType());
        assertEquals(c1.getMatrix(), c2.getMatrix());
    }

    @Test
    public void testCopyFrom() throws InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);

        c2.copyFrom(c1);

        // check
        assertEquals(c1.getSourceType(), c2.getSourceType());
        assertEquals(c1.getDestinationType(), c2.getDestinationType());
        assertEquals(c1.getMatrix(), c2.getMatrix());
    }

    @Test
    public void testHashCode() throws InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);

        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1.hashCode(), c3.hashCode());
    }

    @Test
    public void testEquals() throws InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);

        //noinspection ConstantConditions,SimplifiableJUnitAssertion
        assertTrue(c1.equals((Object)c1));
        assertTrue(c1.equals(c1));
        assertTrue(c1.equals(c2));
        assertFalse(c1.equals(c3));
        //noinspection ConstantConditions,SimplifiableJUnitAssertion
        assertFalse(c1.equals((Object)null));
        assertFalse(c1.equals(null));
        //noinspection SimplifiableJUnitAssertion
        assertFalse(c1.equals(new Object()));
    }

    @Test
    public void testEqualsWithThreshold() throws InvalidRotationMatrixException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);

        assertTrue(c1.equals(c1, THRESHOLD));
        assertTrue(c1.equals(c2, THRESHOLD));
        assertFalse(c1.equals(c3, THRESHOLD));
        assertFalse(c1.equals(null, THRESHOLD));
    }

    @Test
    public void testInverse() throws InvalidRotationMatrixException, WrongSizeException,
            RankDeficientMatrixException, DecomposerException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c2 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        // check that initially they are equal
        assertEquals(c1, c2);
        assertEquals(c1, c3);

        c1.inverse(c2);
        c3.inverse();

        assertEquals(c2, c3);
        assertNotEquals(c1, c2);

        final Matrix m2 = c2.getMatrix();

        assertEquals(m2, m.transposeAndReturnNew());
        assertTrue(m2.equals(Utils.inverse(m), THRESHOLD));
    }

    @Test
    public void testInverseAndReturnNew() throws InvalidRotationMatrixException,
            WrongSizeException, RankDeficientMatrixException, DecomposerException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();
        final CoordinateTransformationMatrix c = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final CoordinateTransformationMatrix invC = c.inverseAndReturnNew();

        assertNotEquals(c, invC);

        final Matrix invM = invC.getMatrix();

        assertEquals(invM, m.transposeAndReturnNew());
        assertTrue(invM.equals(Utils.inverse(m), THRESHOLD));
    }

    @Test
    public void testEcefToNedMatrix() throws WrongSizeException {
        final double cosLat = Math.cos(Math.toRadians(LATITUDE_DEGREES));
        final double sinLat = Math.sin(Math.toRadians(LATITUDE_DEGREES));
        final double cosLong = Math.cos(Math.toRadians(LONGITUDE_DEGREES));
        final double sinLong = Math.sin(Math.toRadians(LONGITUDE_DEGREES));

        final Matrix cen = new Matrix(3, 3);
        cen.setElementAt(0, 0, -sinLat * cosLong);
        cen.setElementAt(1, 0, -sinLong);
        cen.setElementAt(2, 0, -cosLat * cosLong);

        cen.setElementAt(0, 1, -sinLat * sinLong);
        cen.setElementAt(1, 1, cosLong);
        cen.setElementAt(2, 1, -cosLat * sinLong);

        cen.setElementAt(0, 2, cosLat);
        cen.setElementAt(1, 2, 0.0);
        cen.setElementAt(2, 2, -sinLat);


        final Matrix cen1 = new Matrix(1, 1);
        final Matrix cen2 = new Matrix(3, 3);
        CoordinateTransformationMatrix.ecefToNedMatrix(Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES), cen1);
        CoordinateTransformationMatrix.ecefToNedMatrix(Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES), cen2);
        final Matrix cen3 = CoordinateTransformationMatrix.ecefToNedMatrix(
                Math.toRadians(LATITUDE_DEGREES), Math.toRadians(LONGITUDE_DEGREES));

        assertEquals(cen1, cen2);
        assertEquals(cen1, cen3);
        assertEquals(cen1, cen);

        final Angle latitude = new Angle(LATITUDE_DEGREES, AngleUnit.DEGREES);
        final Angle longitude = new Angle(LONGITUDE_DEGREES, AngleUnit.DEGREES);

        final Matrix cen4 = new Matrix(3, 3);
        CoordinateTransformationMatrix.ecefToNedMatrix(latitude, longitude, cen4);
        final Matrix cen5 = CoordinateTransformationMatrix.ecefToNedMatrix(
                latitude, longitude);

        assertEquals(cen1, cen4);
        assertEquals(cen1, cen5);
    }

    @Test
    public void testEcefToNedCoordinateTransformationMatrix() {

        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix.ecefToNedCoordinateTransformationMatrix(
                Math.toRadians(LATITUDE_DEGREES), Math.toRadians(LONGITUDE_DEGREES), c1);
        final CoordinateTransformationMatrix c2 = CoordinateTransformationMatrix
                .ecefToNedCoordinateTransformationMatrix(Math.toRadians(LATITUDE_DEGREES),
                        Math.toRadians(LONGITUDE_DEGREES));

        final Matrix cen = CoordinateTransformationMatrix.ecefToNedMatrix(
                Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES));

        assertEquals(c1.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c1.getDestinationType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c1.getMatrix(), cen);

        assertEquals(c2.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c2.getDestinationType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c2.getMatrix(), cen);

        final Angle latitude = new Angle(LATITUDE_DEGREES, AngleUnit.DEGREES);
        final Angle longitude = new Angle(LONGITUDE_DEGREES, AngleUnit.DEGREES);

        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix.ecefToNedCoordinateTransformationMatrix(
                latitude, longitude, c3);
        final CoordinateTransformationMatrix c4 = CoordinateTransformationMatrix
                .ecefToNedCoordinateTransformationMatrix(latitude, longitude);

        assertEquals(c3.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c3.getDestinationType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c3.getMatrix(), cen);

        assertEquals(c4.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c4.getDestinationType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c4.getMatrix(), cen);
    }

    @Test
    public void testNedToEcefMatrix() throws WrongSizeException {
        final Matrix cen = CoordinateTransformationMatrix.ecefToNedMatrix(
                Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES));
        final Matrix cne = cen.transposeAndReturnNew();

        final Matrix cne1 = new Matrix(1, 1);
        final Matrix cne2 = new Matrix(3, 3);
        CoordinateTransformationMatrix.nedToEcefMatrix(
                Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES), cne1);
        CoordinateTransformationMatrix.nedToEcefMatrix(
                Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES), cne2);
        final Matrix cne3 = CoordinateTransformationMatrix.nedToEcefMatrix(
                Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES));

        assertEquals(cne1, cne2);
        assertEquals(cne1, cne3);
        assertEquals(cne1, cne);

        final Angle latitude = new Angle(LATITUDE_DEGREES, AngleUnit.DEGREES);
        final Angle longitude = new Angle(LONGITUDE_DEGREES, AngleUnit.DEGREES);

        final Matrix cne4 = new Matrix(3, 3);
        CoordinateTransformationMatrix.nedToEcefMatrix(latitude, longitude, cne4);
        final Matrix cne5 = CoordinateTransformationMatrix.nedToEcefMatrix(
                latitude, longitude);

        assertEquals(cne1, cne4);
        assertEquals(cne1, cne5);
    }

    @Test
    public void testNedToEcefCoordinateTransformationMatrix() {
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix.nedToEcefCoordinateTransformationMatrix(
                Math.toRadians(LATITUDE_DEGREES), Math.toRadians(LONGITUDE_DEGREES), c1);
        final CoordinateTransformationMatrix c2 = CoordinateTransformationMatrix
                .nedToEcefCoordinateTransformationMatrix(
                        Math.toRadians(LATITUDE_DEGREES),
                        Math.toRadians(LONGITUDE_DEGREES));

        final Matrix cne = CoordinateTransformationMatrix.nedToEcefMatrix(
                Math.toRadians(LATITUDE_DEGREES),
                Math.toRadians(LONGITUDE_DEGREES));

        assertEquals(c1.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c1.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c1.getMatrix(), cne);

        assertEquals(c2.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c2.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c2.getMatrix(), cne);

        final Angle latitude = new Angle(LATITUDE_DEGREES, AngleUnit.DEGREES);
        final Angle longitude = new Angle(LONGITUDE_DEGREES, AngleUnit.DEGREES);

        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix.nedToEcefCoordinateTransformationMatrix(
                latitude, longitude, c3);
        final CoordinateTransformationMatrix c4 = CoordinateTransformationMatrix
                .nedToEcefCoordinateTransformationMatrix(latitude, longitude);

        assertEquals(c3.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c3.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c3.getMatrix(), cne);

        assertEquals(c4.getSourceType(), FrameType.LOCAL_NAVIGATION_FRAME);
        assertEquals(c4.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c4.getMatrix(), cne);
    }

    @Test
    public void testEcefToEciMatrixFromTimeInterval() throws WrongSizeException {
        final Time timeInterval = new Time(TIME_INTERVAL_SECONDS, TimeUnit.SECOND);
        final Matrix cei1 = new Matrix(3, 3);
        CoordinateTransformationMatrix.ecefToEciMatrixFromTimeInterval(
                timeInterval, cei1);
        final Matrix cei2 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromTimeInterval(timeInterval);
        final Matrix cei3 = new Matrix(3, 3);
        CoordinateTransformationMatrix.ecefToEciMatrixFromTimeInterval(
                TIME_INTERVAL_SECONDS, cei3);
        final Matrix cei4 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertEquals(cei1, cei2);
        assertEquals(cei1, cei3);
        assertEquals(cei1, cei4);

        // test when zero time interval is equal to the identity
        final Matrix cei5 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromTimeInterval(0.0);
        assertEquals(cei5, Matrix.identity(3, 3));
    }

    @Test
    public void testEcefToEciMatrixFromAngle() throws WrongSizeException {
        final double alpha = CoordinateTransformationMatrix.EARTH_ROTATION_RATE
                * TIME_INTERVAL_SECONDS;
        final Angle angle = new Angle(alpha, AngleUnit.RADIANS);
        final Matrix cei1 = new Matrix(3, 3);
        CoordinateTransformationMatrix.ecefToEciMatrixFromAngle(angle, cei1);
        final Matrix cei2 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromAngle(angle);
        final Matrix cei3 = new Matrix(3, 3);
        CoordinateTransformationMatrix.ecefToEciMatrixFromAngle(alpha, cei3);
        final Matrix cei4 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromAngle(alpha);

        assertEquals(cei1, cei2);
        assertEquals(cei1, cei3);
        assertEquals(cei1, cei4);

        final Matrix cei5 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertEquals(cei1, cei5);

        // test when zero angle is equal to the identity
        final Matrix cei6 = CoordinateTransformationMatrix
                .ecefToEciMatrixFromAngle(0.0);
        assertEquals(cei6, Matrix.identity(3, 3));
    }

    @Test
    public void testEcefToEciCoordinateTransformationMatrixFromTimeInterval() {
        final Time timeInterval = new Time(TIME_INTERVAL_SECONDS, TimeUnit.SECOND);
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .ecefToEciCoordinateTransformationMatrixFromTimeInterval(
                        timeInterval, c1);
        final CoordinateTransformationMatrix c2 =
                CoordinateTransformationMatrix
                        .ecefToEciCoordinateTransformationMatrixFromTimeInterval(
                                timeInterval);
        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .ecefToEciCoordinateTransformationMatrixFromTimeInterval(
                        TIME_INTERVAL_SECONDS, c3);
        final CoordinateTransformationMatrix c4 =
                CoordinateTransformationMatrix
                        .ecefToEciCoordinateTransformationMatrixFromTimeInterval(
                                TIME_INTERVAL_SECONDS);

        final Matrix cei = CoordinateTransformationMatrix
                .ecefToEciMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertEquals(c1.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c1.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c1.getMatrix(), cei);

        assertEquals(c2.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c2.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c2.getMatrix(), cei);

        assertEquals(c3.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c3.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c3.getMatrix(), cei);

        assertEquals(c4.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c4.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c4.getMatrix(), cei);
    }

    @Test
    public void testEcefToEciCoordinateTransformationMatrixFromAngle() {
        final double alpha = CoordinateTransformationMatrix.EARTH_ROTATION_RATE
                * TIME_INTERVAL_SECONDS;
        final Angle angle = new Angle(alpha, AngleUnit.RADIANS);

        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .ecefToEciCoordinateTransformationMatrixFromAngle(angle, c1);
        final CoordinateTransformationMatrix c2 = CoordinateTransformationMatrix
                .ecefToEciCoordinateTransformationMatrixFromAngle(angle);
        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .ecefToEciCoordinateTransformationMatrixFromAngle(alpha, c3);
        final CoordinateTransformationMatrix c4 = CoordinateTransformationMatrix
                .ecefToEciCoordinateTransformationMatrixFromAngle(alpha);

        final Matrix cei = CoordinateTransformationMatrix
                .ecefToEciMatrixFromAngle(alpha);

        assertEquals(c1.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c1.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c1.getMatrix(), cei);

        assertEquals(c2.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c2.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c2.getMatrix(), cei);

        assertEquals(c3.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c3.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c3.getMatrix(), cei);

        assertEquals(c4.getSourceType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c4.getDestinationType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c4.getMatrix(), cei);
    }

    @Test
    public void testEciToEcefMatrixFromTimeInterval() throws WrongSizeException,
            RankDeficientMatrixException, DecomposerException {

        final Time timeInterval = new Time(TIME_INTERVAL_SECONDS, TimeUnit.SECOND);
        final Matrix cie1 = new Matrix(3, 3);
        CoordinateTransformationMatrix.eciToEcefMatrixFromTimeInterval(
                timeInterval, cie1);
        final Matrix cie2 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromTimeInterval(timeInterval);
        final Matrix cie3 = new Matrix(3, 3);
        CoordinateTransformationMatrix.eciToEcefMatrixFromTimeInterval(
                TIME_INTERVAL_SECONDS, cie3);
        final Matrix cie4 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertEquals(cie1, cie2);
        assertEquals(cie1, cie3);
        assertEquals(cie1, cie4);

        final Matrix cei = CoordinateTransformationMatrix
                .ecefToEciMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertTrue(cie1.equals(Utils.inverse(cei), THRESHOLD));

        // test when zero time interval is equal to the identity
        final Matrix cie5 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromTimeInterval(0.0);
        assertEquals(cie5, Matrix.identity(3, 3));
    }

    @Test
    public void testEciToEcefMatrixFromAngle() throws WrongSizeException {
        final double alpha = CoordinateTransformationMatrix.EARTH_ROTATION_RATE
                * TIME_INTERVAL_SECONDS;
        final Angle angle = new Angle(alpha, AngleUnit.RADIANS);
        final Matrix cie1 = new Matrix(3, 3);
        CoordinateTransformationMatrix.eciToEcefMatrixFromAngle(angle, cie1);
        final Matrix cie2 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromAngle(angle);
        final Matrix cie3 = new Matrix(3, 3);
        CoordinateTransformationMatrix.eciToEcefMatrixFromAngle(alpha, cie3);
        final Matrix cie4 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromAngle(alpha);

        assertEquals(cie1, cie2);
        assertEquals(cie1, cie3);
        assertEquals(cie1, cie4);

        final Matrix cie5 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertEquals(cie1, cie5);

        // test when zero angle is equal to the identity
        final Matrix cie6 = CoordinateTransformationMatrix
                .eciToEcefMatrixFromAngle(0.0);
        assertEquals(cie6, Matrix.identity(3, 3));
    }

    @Test
    public void testEciToEcefCoordinateTransformationMatrixFromTimeInterval() {
        final Time timeInterval = new Time(TIME_INTERVAL_SECONDS, TimeUnit.SECOND);
        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .eciToEcefCoordinateTransformationMatrixFromTimeInterval(
                        timeInterval, c1);
        final CoordinateTransformationMatrix c2 =
                CoordinateTransformationMatrix
                        .eciToEcefCoordinateTransformationMatrixFromTimeInterval(
                                timeInterval);
        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .eciToEcefCoordinateTransformationMatrixFromTimeInterval(
                        TIME_INTERVAL_SECONDS, c3);
        final CoordinateTransformationMatrix c4 =
                CoordinateTransformationMatrix
                        .eciToEcefCoordinateTransformationMatrixFromInterval(
                                TIME_INTERVAL_SECONDS);

        final Matrix cie = CoordinateTransformationMatrix
                .eciToEcefMatrixFromTimeInterval(TIME_INTERVAL_SECONDS);

        assertEquals(c1.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c1.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c1.getMatrix(), cie);

        assertEquals(c2.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c2.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c2.getMatrix(), cie);

        assertEquals(c3.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c3.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c3.getMatrix(), cie);

        assertEquals(c4.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c4.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c4.getMatrix(), cie);
    }

    @Test
    public void testEciToEcefCoordinateTransformationMatrixFromAngle() {
        final double alpha = CoordinateTransformationMatrix.EARTH_ROTATION_RATE
                * TIME_INTERVAL_SECONDS;
        final Angle angle = new Angle(alpha, AngleUnit.RADIANS);

        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .eciToEcefCoordinateTransformationMatrixFromAngle(angle, c1);
        final CoordinateTransformationMatrix c2 = CoordinateTransformationMatrix
                .eciToEcefCoordinateTransformationMatrixFromAngle(angle);
        final CoordinateTransformationMatrix c3 = new CoordinateTransformationMatrix(
                FrameType.BODY_FRAME, FrameType.BODY_FRAME);
        CoordinateTransformationMatrix
                .eciToEcefCoordinateTransformationMatrixFromAngle(alpha, c3);
        final CoordinateTransformationMatrix c4 = CoordinateTransformationMatrix
                .eciToEcefCoordinateTransformationMatrixFromAngle(alpha);

        final Matrix cie = CoordinateTransformationMatrix
                .eciToEcefMatrixFromAngle(alpha);

        assertEquals(c1.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c1.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c1.getMatrix(), cie);

        assertEquals(c2.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c2.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c2.getMatrix(), cie);

        assertEquals(c3.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c3.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c3.getMatrix(), cie);

        assertEquals(c4.getSourceType(), FrameType.EARTH_CENTERED_INERTIAL_FRAME);
        assertEquals(c4.getDestinationType(), FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);
        assertEquals(c4.getMatrix(), cie);
    }

    @Test
    public void testClone() throws InvalidRotationMatrixException {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final double yaw = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES, MAX_ANGLE_DEGREES));
        final Quaternion q = new Quaternion(roll, pitch, yaw);

        final Matrix m = q.asInhomogeneousMatrix();

        final CoordinateTransformationMatrix c1 = new CoordinateTransformationMatrix(m,
                FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.EARTH_CENTERED_EARTH_FIXED_FRAME);

        final Object c2 = c1.clone();

        assertEquals(c1, c2);
    }
}
