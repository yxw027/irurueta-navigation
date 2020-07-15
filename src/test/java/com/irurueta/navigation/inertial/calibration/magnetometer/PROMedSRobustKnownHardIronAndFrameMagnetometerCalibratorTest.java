/*
 * Copyright (C) 2020 Alberto Irurueta Carro (alberto@irurueta.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.irurueta.navigation.inertial.calibration.magnetometer;

import com.irurueta.algebra.Matrix;
import com.irurueta.algebra.WrongSizeException;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.navigation.frames.CoordinateTransformation;
import com.irurueta.navigation.frames.FrameType;
import com.irurueta.navigation.frames.InvalidSourceAndDestinationFrameTypeException;
import com.irurueta.navigation.frames.NEDFrame;
import com.irurueta.navigation.geodesic.wmm.WMMEarthMagneticFluxDensityEstimator;
import com.irurueta.navigation.geodesic.wmm.WorldMagneticModel;
import com.irurueta.navigation.inertial.BodyMagneticFluxDensity;
import com.irurueta.navigation.inertial.NEDMagneticFluxDensity;
import com.irurueta.navigation.inertial.NEDPosition;
import com.irurueta.navigation.inertial.calibration.BodyMagneticFluxDensityGenerator;
import com.irurueta.navigation.inertial.calibration.CalibrationException;
import com.irurueta.navigation.inertial.calibration.StandardDeviationFrameBodyMagneticFluxDensity;
import com.irurueta.navigation.inertial.estimators.BodyMagneticFluxDensityEstimator;
import com.irurueta.numerical.robust.RobustEstimatorMethod;
import com.irurueta.statistics.GaussianRandomizer;
import com.irurueta.statistics.UniformRandomizer;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class PROMedSRobustKnownHardIronAndFrameMagnetometerCalibratorTest implements
        RobustKnownHardIronAndFrameMagnetometerCalibratorListener {

    private static final double MIN_HARD_IRON = -1e-5;
    private static final double MAX_HARD_IRON = 1e-5;

    private static final double MIN_SOFT_IRON = -1e-6;
    private static final double MAX_SOFT_IRON = 1e-6;

    private static final double MIN_ANGLE_DEGREES = -45.0;
    private static final double MAX_ANGLE_DEGREES = 45.0;

    private static final double MIN_LATITUDE_DEGREES = -90.0;
    private static final double MAX_LATITUDE_DEGREES = 90.0;

    private static final double MIN_LONGITUDE_DEGREES = -180.0;
    private static final double MAX_LONGITUDE_DEGREES = 180.0;

    private static final double MIN_HEIGHT_METERS = -500.0;
    private static final double MAX_HEIGHT_METERS = 10000.0;

    private static final double MAGNETOMETER_NOISE_STD = 200e-9;

    private static final double ABSOLUTE_ERROR = 1e-9;
    private static final double VERY_LARGE_ABSOLUTE_ERROR = 1e-2;

    private static final int MEASUREMENT_NUMBER = 1000;

    private static final int OUTLIER_PERCENTAGE = 20;

    private static final double THRESHOLD = 10e-9;
    private static final double LARGE_THRESHOLD = 500e-9;

    private static final double OUTLIER_ERROR_FACTOR = 100.0;

    private static final int TIMES = 100;

    private static final Calendar START_CALENDAR = Calendar.getInstance();
    private static final Calendar END_CALENDAR = Calendar.getInstance();

    private static final long START_TIMESTAMP_MILLIS;
    private static final long END_TIMESTAMP_MILLIS;

    static {
        START_CALENDAR.set(2020, Calendar.JANUARY, 1,
                0, 0, 0);
        END_CALENDAR.set(2025, Calendar.DECEMBER, 31,
                23, 59, 59);

        START_TIMESTAMP_MILLIS = START_CALENDAR.getTimeInMillis();
        END_TIMESTAMP_MILLIS = END_CALENDAR.getTimeInMillis();
    }

    private int mCalibrateStart;
    private int mCalibrateEnd;
    private int mCalibrateNextIteration;
    private int mCalibrateProgressChange;

    @Test
    public void testConstructor1() throws WrongSizeException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertFalse(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor2() throws WrongSizeException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertFalse(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor3() throws WrongSizeException {
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        measurements);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertFalse(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor4() throws WrongSizeException {
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        measurements, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertFalse(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor5() throws WrongSizeException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        true);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor6() throws WrongSizeException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        true, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor7() throws WrongSizeException {
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        measurements, true);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertTrue(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor8() throws WrongSizeException {
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        measurements, true, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertNull(calibrator.getQualityScores());
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);
    }

    @Test
    public void testConstructor9() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(qualityScores);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertFalse(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor10() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        qualityScores, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertFalse(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor11() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(qualityScores,
                        measurements);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertFalse(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], measurements);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor12() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        qualityScores, measurements, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertFalse(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], measurements, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor13() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        qualityScores, true);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], true);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor14() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        qualityScores, true, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertNull(calibrator.getMeasurements());
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], true, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor15() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        qualityScores, measurements, true);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertTrue(calibrator.isCommonAxisUsed());
        assertNull(calibrator.getListener());
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], measurements, true);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testConstructor16() throws WrongSizeException {
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();

        PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(qualityScores,
                        measurements, true, this);

        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);
        assertEquals(calibrator.getMethod(), RobustEstimatorMethod.PROMedS);
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] hardIron = new double[3];
        calibrator.getHardIron(hardIron);
        assertArrayEquals(hardIron, new double[3], 0.0);
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix b = new Matrix(3, 1);
        calibrator.getHardIronMatrix(b);
        assertEquals(b, new Matrix(3, 1));
        assertEquals(calibrator.getInitialMm(),
                new Matrix(3, 3));
        final Matrix mm = new Matrix(3, 3);
        calibrator.getInitialMm(mm);
        assertEquals(mm, new Matrix(3, 3));
        assertSame(calibrator.getMeasurements(), measurements);
        assertTrue(calibrator.isCommonAxisUsed());
        assertSame(calibrator.getListener(), this);
        assertFalse(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertNull(calibrator.getMagneticModel());
        assertTrue(calibrator.isLinearCalibratorUsed());
        assertFalse(calibrator.isPreliminarySolutionRefined());
        assertEquals(calibrator.getProgressDelta(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_PROGRESS_DELTA,
                0.0f);
        assertEquals(calibrator.getConfidence(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_CONFIDENCE,
                0.0);
        assertEquals(calibrator.getMaxIterations(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_MAX_ITERATIONS);
        assertNull(calibrator.getInliersData());
        assertTrue(calibrator.isResultRefined());
        assertTrue(calibrator.isCovarianceKept());
        assertSame(calibrator.getQualityScores(), qualityScores);
        assertNull(calibrator.getEstimatedMm());
        assertNull(calibrator.getEstimatedSx());
        assertNull(calibrator.getEstimatedSy());
        assertNull(calibrator.getEstimatedSz());
        assertNull(calibrator.getEstimatedMxy());
        assertNull(calibrator.getEstimatedMxz());
        assertNull(calibrator.getEstimatedMyx());
        assertNull(calibrator.getEstimatedMyz());
        assertNull(calibrator.getEstimatedMzx());
        assertNull(calibrator.getEstimatedMzy());
        assertNull(calibrator.getEstimatedCovariance());
        assertEquals(calibrator.getPreliminarySubsetSize(),
                RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // Force IllegalArgumentException
        calibrator = null;
        try {
            calibrator = new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                    new double[1], measurements, true,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(calibrator);
    }

    @Test
    public void testGetSetStopThreshold() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getStopThreshold(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.DEFAULT_STOP_THRESHOLD,
                0.0);

        // set new value
        calibrator.setStopThreshold(1.0);

        // check
        assertEquals(calibrator.getStopThreshold(), 1.0, 0.0);
    }

    @Test
    public void testGetSetHardIronX() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getHardIronX(), 0.0,
                0.0);

        // set new value
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] mb = generateHardIron(randomizer);
        final double hardIronX = mb[0];

        calibrator.setHardIronX(hardIronX);

        // check
        assertEquals(calibrator.getHardIronX(), hardIronX, 0.0);
    }

    @Test
    public void testGetSetHardIronY() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getHardIronY(), 0.0,
                0.0);

        // set new value
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] mb = generateHardIron(randomizer);
        final double hardIronY = mb[1];

        calibrator.setHardIronY(hardIronY);

        // check
        assertEquals(calibrator.getHardIronY(), hardIronY, 0.0);
    }

    @Test
    public void testGetSetHardIronZ() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getHardIronZ(), 0.0,
                0.0);

        // set new value
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] mb = generateHardIron(randomizer);
        final double hardIronZ = mb[2];

        calibrator.setHardIronZ(hardIronZ);

        // check
        assertEquals(calibrator.getHardIronZ(), hardIronZ, 0.0);
    }

    @Test
    public void testSetHardIronCoordinates() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default values
        assertEquals(calibrator.getHardIronX(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronY(), 0.0, 0.0);
        assertEquals(calibrator.getHardIronZ(), 0.0, 0.0);

        // set new value
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] mb = generateHardIron(randomizer);
        final double hardIronX = mb[0];
        final double hardIronY = mb[1];
        final double hardIronZ = mb[2];

        calibrator.setHardIronCoordinates(hardIronX, hardIronY, hardIronZ);

        // check
        assertEquals(calibrator.getHardIronX(), hardIronX, 0.0);
        assertEquals(calibrator.getHardIronY(), hardIronY, 0.0);
        assertEquals(calibrator.getHardIronZ(), hardIronZ, 0.0);
    }

    @Test
    public void testGetSetInitialSx() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double sx = mm.getElementAt(0, 0);

        calibrator.setInitialSx(sx);

        // check
        assertEquals(calibrator.getInitialSx(), sx, 0.0);
    }

    @Test
    public void testGetSetInitialSy() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double sy = mm.getElementAt(1, 1);

        calibrator.setInitialSy(sy);

        // check
        assertEquals(calibrator.getInitialSy(), sy, 0.0);
    }

    @Test
    public void testGetSetInitialSz() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double sz = mm.getElementAt(2, 2);

        calibrator.setInitialSz(sz);

        // check
        assertEquals(calibrator.getInitialSz(), sz, 0.0);
    }

    @Test
    public void testGetSetInitialMxy() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double mxy = mm.getElementAt(0, 1);

        calibrator.setInitialMxy(mxy);

        // check
        assertEquals(calibrator.getInitialMxy(), mxy, 0.0);
    }

    @Test
    public void testGetSetInitialMxz() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double mxz = mm.getElementAt(0, 2);

        calibrator.setInitialMxz(mxz);

        // check
        assertEquals(calibrator.getInitialMxz(), mxz, 0.0);
    }

    @Test
    public void testGetSetInitialMyx() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMyx(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double myx = mm.getElementAt(1, 0);

        calibrator.setInitialMyx(myx);

        // check
        assertEquals(calibrator.getInitialMyx(), myx, 0.0);
    }

    @Test
    public void testGetSetInitialMyz() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double myz = mm.getElementAt(1, 2);

        calibrator.setInitialMyz(myz);

        // check
        assertEquals(calibrator.getInitialMyz(), myz, 0.0);
    }

    @Test
    public void testGetSetInitialMzx() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double mzx = mm.getElementAt(2, 0);

        calibrator.setInitialMzx(mzx);

        // check
        assertEquals(calibrator.getInitialMzx(), mzx, 0.0);
    }

    @Test
    public void testGetSetInitialMzy() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double mzy = mm.getElementAt(2, 1);

        calibrator.setInitialMzy(mzy);

        // check
        assertEquals(calibrator.getInitialMzy(), mzy, 0.0);
    }

    @Test
    public void testSetInitialScalingFactors() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default values
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);

        // set new values
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double sx = mm.getElementAt(0, 0);
        final double sy = mm.getElementAt(1, 1);
        final double sz = mm.getElementAt(2, 2);

        calibrator.setInitialScalingFactors(sx, sy, sz);

        // check
        assertEquals(calibrator.getInitialSx(), sx, 0.0);
        assertEquals(calibrator.getInitialSy(), sy, 0.0);
        assertEquals(calibrator.getInitialSz(), sz, 0.0);
    }

    @Test
    public void testSetInitialCrossCouplingErrors() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default values
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double mxy = mm.getElementAt(0, 1);
        final double mxz = mm.getElementAt(0, 2);
        final double myx = mm.getElementAt(1, 0);
        final double myz = mm.getElementAt(1, 2);
        final double mzx = mm.getElementAt(2, 0);
        final double mzy = mm.getElementAt(2, 1);

        calibrator.setInitialCrossCouplingErrors(mxy, mxz, myx,
                myz, mzx, mzy);

        // check
        assertEquals(calibrator.getInitialMxy(), mxy, 0.0);
        assertEquals(calibrator.getInitialMxz(), mxz, 0.0);
        assertEquals(calibrator.getInitialMyx(), myx, 0.0);
        assertEquals(calibrator.getInitialMyz(), myz, 0.0);
        assertEquals(calibrator.getInitialMzx(), mzx, 0.0);
        assertEquals(calibrator.getInitialMzy(), mzy, 0.0);
    }

    @Test
    public void testSetInitialScalingFactorsAndCrossCouplingErrors()
            throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default values
        assertEquals(calibrator.getInitialSx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialSz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxy(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMxz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMyz(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzx(), 0.0, 0.0);
        assertEquals(calibrator.getInitialMzy(), 0.0, 0.0);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        final double sx = mm.getElementAt(0, 0);
        final double sy = mm.getElementAt(1, 1);
        final double sz = mm.getElementAt(2, 2);
        final double mxy = mm.getElementAt(0, 1);
        final double mxz = mm.getElementAt(0, 2);
        final double myx = mm.getElementAt(1, 0);
        final double myz = mm.getElementAt(1, 2);
        final double mzx = mm.getElementAt(2, 0);
        final double mzy = mm.getElementAt(2, 1);

        calibrator.setInitialScalingFactorsAndCrossCouplingErrors(
                sx, sy, sz, mxy, mxz, myx, myz, mzx, mzy);

        // check
        assertEquals(calibrator.getInitialSx(), sx, 0.0);
        assertEquals(calibrator.getInitialSy(), sy, 0.0);
        assertEquals(calibrator.getInitialSz(), sz, 0.0);
        assertEquals(calibrator.getInitialMxy(), mxy, 0.0);
        assertEquals(calibrator.getInitialMxz(), mxz, 0.0);
        assertEquals(calibrator.getInitialMyx(), myx, 0.0);
        assertEquals(calibrator.getInitialMyz(), myz, 0.0);
        assertEquals(calibrator.getInitialMzx(), mzx, 0.0);
        assertEquals(calibrator.getInitialMzy(), mzy, 0.0);
    }

    @Test
    public void testGetHardIronAsArray() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertArrayEquals(calibrator.getHardIron(),
                new double[3], 0.0);
        final double[] result1 = new double[3];
        calibrator.getHardIron(result1);
        assertArrayEquals(result1, new double[3], 0.0);

        // set new value
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] bm = generateHardIron(randomizer);
        calibrator.setHardIron(bm);

        // check
        assertArrayEquals(calibrator.getHardIron(), bm, 0.0);
        final double[] result2 = new double[3];
        calibrator.getHardIron(result2);
        assertArrayEquals(result2, bm, 0.0);

        // Force IllegalArgumentException
        try {
            calibrator.getHardIron(new double[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            calibrator.setHardIron(new double[1]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testGetHardIronAsMatrix() throws LockedException,
            WrongSizeException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getHardIronMatrix(),
                new Matrix(3, 1));
        final Matrix result1 = new Matrix(3, 1);
        calibrator.getHardIronMatrix(result1);
        assertEquals(result1, new Matrix(3, 1));

        // set new value
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] bm = generateHardIron(randomizer);
        final Matrix b = Matrix.newFromArray(bm);
        calibrator.setHardIron(b);

        // check
        assertEquals(calibrator.getHardIronMatrix(), b);
        final Matrix result2 = new Matrix(3, 1);
        calibrator.getHardIronMatrix(result2);
        assertEquals(result2, b);

        // Force IllegalArgumentException
        try {
            calibrator.getHardIronMatrix(new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            calibrator.getHardIronMatrix(new Matrix(3, 3));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            calibrator.setHardIron(new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            calibrator.setHardIron(new Matrix(1, 3));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testGetSetInitialMm() throws WrongSizeException,
            LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getInitialMm(), new Matrix(3, 3));
        final Matrix result1 = new Matrix(3, 3);
        calibrator.getInitialMm(result1);

        // set new value
        final Matrix mm = generateSoftIronGeneral();
        calibrator.setInitialMm(mm);

        // check
        assertEquals(calibrator.getInitialMm(), mm);
        final Matrix result2 = new Matrix(3, 3);
        calibrator.getInitialMm(result2);
        assertEquals(mm, result2);
    }

    @Test
    public void testGetSetMeasurements() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertNull(calibrator.getMeasurements());

        // set new value
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                Collections.emptyList();
        calibrator.setMeasurements(measurements);

        // check
        assertSame(calibrator.getMeasurements(), measurements);
    }

    @Test
    public void testIsSetCommonAxisUsed() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertFalse(calibrator.isCommonAxisUsed());

        // set new value
        calibrator.setCommonAxisUsed(true);

        // check
        assertTrue(calibrator.isCommonAxisUsed());
    }

    @Test
    public void testGetListener() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertNull(calibrator.getListener());

        // set new value
        calibrator.setListener(this);

        // check
        assertSame(calibrator.getListener(), this);
    }

    @Test
    public void testIsReady() throws LockedException, IOException, InvalidSourceAndDestinationFrameTypeException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check initial value
        assertFalse(calibrator.isReady());

        // set not enough measurements
        calibrator.setMeasurements(Collections
                .<StandardDeviationFrameBodyMagneticFluxDensity>emptyList());

        // check
        assertFalse(calibrator.isReady());

        // set enough measurements
        final UniformRandomizer randomizer = new UniformRandomizer(
                new Random());
        final double[] hardIron = generateHardIron(randomizer);
        final Matrix softIron = generateSoftIronGeneral();
        final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                new WMMEarthMagneticFluxDensityEstimator();
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                generateMeasurementsMultipleOrientationswithSamePosition(
                        hardIron, softIron,
                        wmmEstimator, randomizer);

        calibrator.setMeasurements(measurements);

        // check
        assertFalse(calibrator.isReady());

        // add quality scores with invalid size
        double[] qualityScores = new double[measurements.size() + 1];
        calibrator.setQualityScores(qualityScores);

        assertFalse(calibrator.isReady());

        // add quality scores with valid size
        qualityScores = new double[measurements.size()];
        calibrator.setQualityScores(qualityScores);

        assertTrue(calibrator.isReady());
    }

    @Test
    public void testGetSetMagneticModel() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertNull(calibrator.getMagneticModel());

        // set new value
        final WorldMagneticModel magneticModel = new WorldMagneticModel();
        calibrator.setMagneticModel(magneticModel);

        // check
        assertSame(calibrator.getMagneticModel(), magneticModel);
    }

    @Test
    public void testIsSetLinearCalibratorUsed() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertTrue(calibrator.isLinearCalibratorUsed());

        // set new value
        calibrator.setLinearCalibratorUsed(false);

        // check
        assertFalse(calibrator.isLinearCalibratorUsed());
    }

    @Test
    public void testIsSetPreliminarySolutionRefined() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertFalse(calibrator.isPreliminarySolutionRefined());

        // set new value
        calibrator.setPreliminarySolutionRefined(true);

        // check
        assertTrue(calibrator.isPreliminarySolutionRefined());
    }

    @Test
    public void testGetSetProgressDelta() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getProgressDelta(), 0.05f, 0.0);

        // set new value
        calibrator.setProgressDelta(0.01f);

        // check
        assertEquals(calibrator.getProgressDelta(), 0.01f, 0.0);

        // force IllegalArgumentException
        try {
            calibrator.setProgressDelta(-1.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            calibrator.setProgressDelta(2.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testGetSetConfidence() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getConfidence(), 0.99, 0.0);

        // set new value
        calibrator.setConfidence(0.5);

        // check
        assertEquals(calibrator.getConfidence(), 0.5, 0.0);

        // Force IllegalArgumentException
        try {
            calibrator.setConfidence(-1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            calibrator.setConfidence(2.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testGetSetMaxIterations() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getMaxIterations(), 5000);

        // set new value
        calibrator.setMaxIterations(100);

        assertEquals(calibrator.getMaxIterations(), 100);

        // Force IllegalArgumentException
        try {
            calibrator.setMaxIterations(0);
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testIsSetResultRefined() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertTrue(calibrator.isResultRefined());

        // set new value
        calibrator.setResultRefined(false);

        // check
        assertFalse(calibrator.isResultRefined());
    }

    @Test
    public void testIsSetCovarianceKept() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertTrue(calibrator.isCovarianceKept());

        // set new value
        calibrator.setCovarianceKept(false);

        // check
        assertFalse(calibrator.isCovarianceKept());
    }

    @Test
    public void testGetSetQualityScores() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertNull(calibrator.getQualityScores());

        // set new value
        final double[] qualityScores = new double[
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS];
        calibrator.setQualityScores(qualityScores);

        // check
        assertSame(calibrator.getQualityScores(), qualityScores);

        // Force IllegalArgumentException
        try {
            calibrator.setQualityScores(new double[2]);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testGetSetPreliminarySubsetSize() throws LockedException {
        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator();

        // check default value
        assertEquals(calibrator.getPreliminarySubsetSize(),
                PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS);

        // set new value
        calibrator.setPreliminarySubsetSize(4);

        // check
        assertEquals(calibrator.getPreliminarySubsetSize(), 4);

        // Force IllegalArgumentException
        try {
            calibrator.setPreliminarySubsetSize(2);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testCalibrateGeneralNoNoiseInlier()
            throws IOException,
            InvalidSourceAndDestinationFrameTypeException,
            LockedException, CalibrationException, NotReadyException,
            WrongSizeException {

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            final UniformRandomizer randomizer = new UniformRandomizer(new Random());
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                    new WMMEarthMagneticFluxDensityEstimator();

            final double[] hardIron = generateHardIron(randomizer);
            final Matrix mm = generateSoftIronGeneral();
            assertNotNull(mm);

            final GaussianRandomizer noiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0, MAGNETOMETER_NOISE_STD);


            final NEDPosition position = createPosition(randomizer);
            final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                    new ArrayList<>();
            final double[] qualityScores = new double[MEASUREMENT_NUMBER];
            double error;
            for (int i = 0; i < MEASUREMENT_NUMBER; i++) {

                final StandardDeviationFrameBodyMagneticFluxDensity b;
                if (randomizer.nextInt(0, 100) < OUTLIER_PERCENTAGE) {
                    // outlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, noiseRandomizer, position);
                    error = Math.abs(noiseRandomizer.nextDouble());
                } else {
                    // inlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, null, position);
                    error = 0.0;
                }
                measurements.add(b);

                qualityScores[i] = 1.0 / (1.0 + error);
            }

            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                    new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                            qualityScores, measurements,
                            false, this);
            calibrator.setHardIron(hardIron);
            calibrator.setStopThreshold(THRESHOLD);

            // estimate
            reset();
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 0);
            assertEquals(mCalibrateEnd, 0);
            assertEquals(mCalibrateNextIteration, 0);
            assertEquals(mCalibrateProgressChange, 0);

            calibrator.calibrate();

            // check
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 1);
            assertEquals(mCalibrateEnd, 1);
            assertTrue(mCalibrateNextIteration > 0);
            assertTrue(mCalibrateProgressChange >= 0);

            final Matrix estimatedMm = calibrator.getEstimatedMm();

            if (!mm.equals(estimatedMm, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(mm.equals(estimatedMm, ABSOLUTE_ERROR));

            assertEstimatedResult(estimatedMm, calibrator);

            assertNotNull(calibrator.getEstimatedCovariance());

            numValid++;
            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testCalibrateCommonAxisNoNoiseInlier()
            throws IOException,
            InvalidSourceAndDestinationFrameTypeException,
            LockedException, CalibrationException, NotReadyException,
            WrongSizeException {

        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                new WMMEarthMagneticFluxDensityEstimator();

        final double[] hardIron = generateHardIron(randomizer);
        final Matrix mm = generateSoftIronCommonAxis();
        assertNotNull(mm);

        final GaussianRandomizer noiseRandomizer = new GaussianRandomizer(
                new Random(), 0.0, MAGNETOMETER_NOISE_STD);


        final NEDPosition position = createPosition(randomizer);
        final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                new ArrayList<>();
        final double[] qualityScores = new double[MEASUREMENT_NUMBER];
        double error;
        for (int i = 0; i < MEASUREMENT_NUMBER; i++) {

            final StandardDeviationFrameBodyMagneticFluxDensity b;
            if (randomizer.nextInt(0, 100) < OUTLIER_PERCENTAGE) {
                // outlier
                b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                        randomizer, noiseRandomizer, position);
                error = Math.abs(noiseRandomizer.nextDouble());
            } else {
                // inlier
                b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                        randomizer, null, position);
                error = 0.0;
            }
            measurements.add(b);

            qualityScores[i] = 1.0 / (1.0 + error);
        }

        final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                        qualityScores, measurements, true,
                        this);
        calibrator.setHardIron(hardIron);
        calibrator.setStopThreshold(THRESHOLD);

        // estimate
        reset();
        assertTrue(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertEquals(mCalibrateStart, 0);
        assertEquals(mCalibrateEnd, 0);
        assertEquals(mCalibrateNextIteration, 0);
        assertEquals(mCalibrateProgressChange, 0);

        calibrator.calibrate();

        // check
        assertTrue(calibrator.isReady());
        assertFalse(calibrator.isRunning());
        assertEquals(mCalibrateStart, 1);
        assertEquals(mCalibrateEnd, 1);
        assertTrue(mCalibrateNextIteration > 0);
        assertTrue(mCalibrateProgressChange >= 0);

        final Matrix estimatedMm = calibrator.getEstimatedMm();

        assertTrue(mm.equals(estimatedMm, ABSOLUTE_ERROR));

        assertEstimatedResult(estimatedMm, calibrator);

        assertNotNull(calibrator.getEstimatedCovariance());
    }

    @Test
    public void testCalibrateGeneralWithInlierNoise()
            throws IOException,
            InvalidSourceAndDestinationFrameTypeException,
            LockedException, CalibrationException, NotReadyException,
            WrongSizeException {

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            final UniformRandomizer randomizer = new UniformRandomizer(new Random());
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                    new WMMEarthMagneticFluxDensityEstimator();

            final double[] hardIron = generateHardIron(randomizer);
            final Matrix mm = generateSoftIronGeneral();
            assertNotNull(mm);

            final GaussianRandomizer inlierNoiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0, MAGNETOMETER_NOISE_STD);
            final GaussianRandomizer outlierNoiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0,
                    OUTLIER_ERROR_FACTOR * MAGNETOMETER_NOISE_STD);


            final NEDPosition position = createPosition(randomizer);
            final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                    new ArrayList<>();
            final double[] qualityScores = new double[MEASUREMENT_NUMBER];
            double error;
            for (int i = 0; i < MEASUREMENT_NUMBER; i++) {

                final StandardDeviationFrameBodyMagneticFluxDensity b;
                if (randomizer.nextInt(0, 100) < OUTLIER_PERCENTAGE) {
                    // outlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, outlierNoiseRandomizer, position);
                    error = Math.abs(outlierNoiseRandomizer.nextDouble());
                } else {
                    // inlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, inlierNoiseRandomizer, position);
                    error = 0.0;
                }
                measurements.add(b);

                qualityScores[i] = 1.0 / (1.0 + error);
            }

            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                    new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                            qualityScores, measurements,
                            false, this);
            calibrator.setHardIron(hardIron);
            calibrator.setStopThreshold(LARGE_THRESHOLD);

            // estimate
            reset();
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 0);
            assertEquals(mCalibrateEnd, 0);
            assertEquals(mCalibrateNextIteration, 0);
            assertEquals(mCalibrateProgressChange, 0);

            calibrator.calibrate();

            // check
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 1);
            assertEquals(mCalibrateEnd, 1);
            assertTrue(mCalibrateNextIteration > 0);
            assertTrue(mCalibrateProgressChange >= 0);

            final Matrix estimatedMm = calibrator.getEstimatedMm();

            if (!mm.equals(estimatedMm, VERY_LARGE_ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(mm.equals(estimatedMm, VERY_LARGE_ABSOLUTE_ERROR));

            assertEstimatedResult(estimatedMm, calibrator);

            assertNotNull(calibrator.getEstimatedCovariance());

            numValid++;
            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testCalibrateCommonAxisWithInlierNoise()
            throws IOException,
            InvalidSourceAndDestinationFrameTypeException,
            LockedException, CalibrationException, NotReadyException,
            WrongSizeException {

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            final UniformRandomizer randomizer = new UniformRandomizer(new Random());
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                    new WMMEarthMagneticFluxDensityEstimator();

            final double[] hardIron = generateHardIron(randomizer);
            final Matrix mm = generateSoftIronCommonAxis();
            assertNotNull(mm);

            final GaussianRandomizer inlierNoiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0, MAGNETOMETER_NOISE_STD);
            final GaussianRandomizer outlierNoiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0,
                    OUTLIER_ERROR_FACTOR * MAGNETOMETER_NOISE_STD);


            final NEDPosition position = createPosition(randomizer);
            final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                    new ArrayList<>();
            final double[] qualityScores = new double[MEASUREMENT_NUMBER];
            double error;
            for (int i = 0; i < MEASUREMENT_NUMBER; i++) {

                final StandardDeviationFrameBodyMagneticFluxDensity b;
                if (randomizer.nextInt(0, 100) < OUTLIER_PERCENTAGE) {
                    // outlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, outlierNoiseRandomizer, position);
                    error = Math.abs(outlierNoiseRandomizer.nextDouble());
                } else {
                    // inlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, inlierNoiseRandomizer, position);
                    error = 0.0;
                }
                measurements.add(b);

                qualityScores[i] = 1.0 / (1.0 + error);
            }

            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                    new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                            qualityScores, measurements,
                            true, this);
            calibrator.setHardIron(hardIron);
            calibrator.setStopThreshold(LARGE_THRESHOLD);

            // estimate
            reset();
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 0);
            assertEquals(mCalibrateEnd, 0);
            assertEquals(mCalibrateNextIteration, 0);
            assertEquals(mCalibrateProgressChange, 0);

            calibrator.calibrate();

            // check
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 1);
            assertEquals(mCalibrateEnd, 1);
            assertTrue(mCalibrateNextIteration > 0);
            assertTrue(mCalibrateProgressChange >= 0);

            final Matrix estimatedMm = calibrator.getEstimatedMm();

            if (!mm.equals(estimatedMm, VERY_LARGE_ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(mm.equals(estimatedMm, VERY_LARGE_ABSOLUTE_ERROR));

            assertEstimatedResult(estimatedMm, calibrator);

            assertNotNull(calibrator.getEstimatedCovariance());

            numValid++;
            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testCalibrateGeneralNoRefinement()
            throws IOException,
            InvalidSourceAndDestinationFrameTypeException,
            LockedException, CalibrationException, NotReadyException,
            WrongSizeException {

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            final UniformRandomizer randomizer = new UniformRandomizer(new Random());
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                    new WMMEarthMagneticFluxDensityEstimator();

            final double[] hardIron = generateHardIron(randomizer);
            final Matrix mm = generateSoftIronGeneral();
            assertNotNull(mm);

            final GaussianRandomizer noiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0, MAGNETOMETER_NOISE_STD);


            final NEDPosition position = createPosition(randomizer);
            final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                    new ArrayList<>();
            final double[] qualityScores = new double[MEASUREMENT_NUMBER];
            double error;
            for (int i = 0; i < MEASUREMENT_NUMBER; i++) {

                final StandardDeviationFrameBodyMagneticFluxDensity b;
                if (randomizer.nextInt(0, 100) < OUTLIER_PERCENTAGE) {
                    // outlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, noiseRandomizer, position);
                    error = Math.abs(noiseRandomizer.nextDouble());
                } else {
                    // inlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, null, position);
                    error = 0.0;
                }
                measurements.add(b);

                qualityScores[i] = 1.0 / (1.0 + error);
            }

            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                    new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                            qualityScores, measurements,
                            false, this);
            calibrator.setHardIron(hardIron);
            calibrator.setStopThreshold(THRESHOLD);
            calibrator.setResultRefined(false);
            calibrator.setPreliminarySolutionRefined(false);

            // estimate
            reset();
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 0);
            assertEquals(mCalibrateEnd, 0);
            assertEquals(mCalibrateNextIteration, 0);
            assertEquals(mCalibrateProgressChange, 0);

            calibrator.calibrate();

            // check
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 1);
            assertEquals(mCalibrateEnd, 1);
            assertTrue(mCalibrateNextIteration > 0);
            assertTrue(mCalibrateProgressChange >= 0);

            final Matrix estimatedMm = calibrator.getEstimatedMm();

            if (!mm.equals(estimatedMm, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(mm.equals(estimatedMm, ABSOLUTE_ERROR));

            assertEstimatedResult(estimatedMm, calibrator);

            assertNull(calibrator.getEstimatedCovariance());

            numValid++;
            break;
        }

        assertTrue(numValid > 0);
    }

    @Test
    public void testCalibrateGeneralNonLinearWithInitialValue()
            throws IOException,
            InvalidSourceAndDestinationFrameTypeException,
            LockedException, CalibrationException, NotReadyException,
            WrongSizeException {

        int numValid = 0;
        for (int t = 0; t < TIMES; t++) {
            final UniformRandomizer randomizer = new UniformRandomizer(new Random());
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator =
                    new WMMEarthMagneticFluxDensityEstimator();

            final double[] hardIron = generateHardIron(randomizer);
            final Matrix mm = generateSoftIronGeneral();
            assertNotNull(mm);

            final GaussianRandomizer noiseRandomizer = new GaussianRandomizer(
                    new Random(), 0.0, MAGNETOMETER_NOISE_STD);


            final NEDPosition position = createPosition(randomizer);
            final List<StandardDeviationFrameBodyMagneticFluxDensity> measurements =
                    new ArrayList<>();
            final double[] qualityScores = new double[MEASUREMENT_NUMBER];
            double error;
            for (int i = 0; i < MEASUREMENT_NUMBER; i++) {

                final StandardDeviationFrameBodyMagneticFluxDensity b;
                if (randomizer.nextInt(0, 100) < OUTLIER_PERCENTAGE) {
                    // outlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, noiseRandomizer, position);
                    error = Math.abs(noiseRandomizer.nextDouble());
                } else {
                    // inlier
                    b = generateMeasureAtPosition(hardIron, mm, wmmEstimator,
                            randomizer, null, position);
                    error = 0.0;
                }
                measurements.add(b);

                qualityScores[i] = 1.0 / (1.0 + error);
            }

            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator =
                    new PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator(
                            qualityScores, measurements,
                            false, this);
            calibrator.setHardIron(hardIron);
            calibrator.setStopThreshold(THRESHOLD);
            calibrator.setInitialMm(mm);
            calibrator.setLinearCalibratorUsed(false);
            calibrator.setPreliminarySolutionRefined(true);

            // estimate
            reset();
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 0);
            assertEquals(mCalibrateEnd, 0);
            assertEquals(mCalibrateNextIteration, 0);
            assertEquals(mCalibrateProgressChange, 0);

            calibrator.calibrate();

            // check
            assertTrue(calibrator.isReady());
            assertFalse(calibrator.isRunning());
            assertEquals(mCalibrateStart, 1);
            assertEquals(mCalibrateEnd, 1);
            assertTrue(mCalibrateNextIteration > 0);
            assertTrue(mCalibrateProgressChange >= 0);

            final Matrix estimatedMm = calibrator.getEstimatedMm();

            if (!mm.equals(estimatedMm, ABSOLUTE_ERROR)) {
                continue;
            }
            assertTrue(mm.equals(estimatedMm, ABSOLUTE_ERROR));

            assertEstimatedResult(estimatedMm, calibrator);

            assertNotNull(calibrator.getEstimatedCovariance());

            numValid++;
            break;
        }

        assertTrue(numValid > 0);
    }

    @Override
    public void onCalibrateStart(
            final RobustKnownHardIronAndFrameMagnetometerCalibrator calibrator) {
        checkLocked((PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator) calibrator);
        mCalibrateStart++;
    }

    @Override
    public void onCalibrateEnd(
            final RobustKnownHardIronAndFrameMagnetometerCalibrator calibrator) {
        checkLocked((PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator) calibrator);
        mCalibrateEnd++;
    }

    @Override
    public void onCalibrateNextIteration(
            final RobustKnownHardIronAndFrameMagnetometerCalibrator calibrator,
            final int iteration) {
        checkLocked((PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator) calibrator);
        mCalibrateNextIteration++;
    }

    @Override
    public void onCalibrateProgressChange(
            final RobustKnownHardIronAndFrameMagnetometerCalibrator calibrator,
            final float progress) {
        checkLocked((PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator) calibrator);
        mCalibrateProgressChange++;
    }

    private void reset() {
        mCalibrateStart = 0;
        mCalibrateEnd = 0;
        mCalibrateNextIteration = 0;
        mCalibrateProgressChange = 0;
    }

    private void checkLocked(
            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator) {
        assertTrue(calibrator.isRunning());
        try {
            calibrator.setStopThreshold(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setHardIronX(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setHardIronY(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setHardIronZ(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setHardIronCoordinates(
                    0.0, 0.0, 0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialSx(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialSz(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMxy(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMxz(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMyx(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMyz(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMzx(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMzy(0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialScalingFactors(
                    0.0, 0.0, 0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialCrossCouplingErrors(
                    0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialScalingFactorsAndCrossCouplingErrors(
                    0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setHardIron((double[]) null);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setHardIron((Matrix) null);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setInitialMm(null);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setMeasurements(null);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setCommonAxisUsed(true);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setListener(this);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setMagneticModel(null);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setLinearCalibratorUsed(false);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setPreliminarySolutionRefined(false);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setProgressDelta(0.5f);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setConfidence(0.5);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setMaxIterations(100);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setResultRefined(true);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.setCovarianceKept(true);
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        }
        try {
            calibrator.calibrate();
            fail("LockedException expected but not thrown");
        } catch (final LockedException ignore) {
        } catch (final Exception e) {
            fail("LockedException expected but not thrown");
        }
    }

    private void assertEstimatedResult(
            final Matrix mm,
            final PROMedSRobustKnownHardIronAndFrameMagnetometerCalibrator calibrator) {

        assertEquals(mm.getElementAt(0, 0), calibrator.getEstimatedSx(),
                0.0);
        assertEquals(mm.getElementAt(1, 1), calibrator.getEstimatedSy(),
                0.0);
        assertEquals(mm.getElementAt(2, 2), calibrator.getEstimatedSz(),
                0.0);
        assertEquals(mm.getElementAt(0, 1), calibrator.getEstimatedMxy(),
                0.0);
        assertEquals(mm.getElementAt(0, 2), calibrator.getEstimatedMxz(),
                0.0);
        assertEquals(mm.getElementAt(1, 0), calibrator.getEstimatedMyx(),
                0.0);
        assertEquals(mm.getElementAt(1, 2), calibrator.getEstimatedMyz(),
                0.0);
        assertEquals(mm.getElementAt(2, 0), calibrator.getEstimatedMzx(),
                0.0);
        assertEquals(mm.getElementAt(2, 1), calibrator.getEstimatedMzy(),
                0.0);
    }

    private static List<StandardDeviationFrameBodyMagneticFluxDensity>
    generateMeasurementsMultipleOrientationswithSamePosition(
            final double[] hardIron, final Matrix softIron,
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator,
            final UniformRandomizer randomizer)
            throws InvalidSourceAndDestinationFrameTypeException {
        final NEDPosition position = createPosition(randomizer);
        final List<StandardDeviationFrameBodyMagneticFluxDensity> result =
                new ArrayList<>();
        for (int i = 0; i < RobustKnownHardIronAndFrameMagnetometerCalibrator.MINIMUM_MEASUREMENTS; i++) {
            result.add(generateMeasureAtPosition(hardIron, softIron,
                    wmmEstimator, randomizer, null, position));
        }
        return result;
    }

    private static StandardDeviationFrameBodyMagneticFluxDensity generateMeasureAtPosition(
            final double[] hardIron, final Matrix softIron,
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator,
            final UniformRandomizer randomizer,
            final GaussianRandomizer noiseRandomizer,
            final NEDPosition position)
            throws InvalidSourceAndDestinationFrameTypeException {
        final CoordinateTransformation cnb = generateBodyC(randomizer);
        return generateMeasure(hardIron, softIron, wmmEstimator,
                randomizer, noiseRandomizer, position, cnb);
    }

    private static StandardDeviationFrameBodyMagneticFluxDensity generateMeasure(
            final double[] hardIron, final Matrix softIron,
            final WMMEarthMagneticFluxDensityEstimator wmmEstimator,
            final UniformRandomizer randomizer,
            final GaussianRandomizer noiseRandomizer,
            final NEDPosition position,
            final CoordinateTransformation cnb)
            throws InvalidSourceAndDestinationFrameTypeException {

        final Date timestamp = new Date(createTimestamp(randomizer));
        final NEDMagneticFluxDensity earthB = wmmEstimator.estimate(
                position, timestamp);

        final BodyMagneticFluxDensity truthMagnetic =
                BodyMagneticFluxDensityEstimator.estimate(earthB, cnb);
        final BodyMagneticFluxDensity measuredMagnetic =
                generateMeasuredMagneticFluxDensity(truthMagnetic,
                        hardIron, softIron);

        if (noiseRandomizer != null) {
            measuredMagnetic.setBx(measuredMagnetic.getBx()
                    + noiseRandomizer.nextDouble());
            measuredMagnetic.setBy(measuredMagnetic.getBy()
                    + noiseRandomizer.nextDouble());
            measuredMagnetic.setBz(measuredMagnetic.getBz()
                    + noiseRandomizer.nextDouble());
        }

        final CoordinateTransformation cbn = cnb.inverseAndReturnNew();
        final NEDFrame frame = new NEDFrame(position, cbn);

        final double std = noiseRandomizer != null ?
                noiseRandomizer.getStandardDeviation() :
                MAGNETOMETER_NOISE_STD;
        return new StandardDeviationFrameBodyMagneticFluxDensity(
                measuredMagnetic, frame, timestamp, std);
    }

    private static CoordinateTransformation generateBodyC(
            final UniformRandomizer randomizer) {

        final double roll = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES,
                        MAX_ANGLE_DEGREES));
        final double pitch = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES,
                        MAX_ANGLE_DEGREES));
        final double yaw1 = Math.toRadians(
                randomizer.nextDouble(MIN_ANGLE_DEGREES,
                        MAX_ANGLE_DEGREES));

        return new CoordinateTransformation(
                roll, pitch, yaw1, FrameType.LOCAL_NAVIGATION_FRAME,
                FrameType.BODY_FRAME);
    }

    private static BodyMagneticFluxDensity generateMeasuredMagneticFluxDensity(
            final BodyMagneticFluxDensity input, final double[] hardIron,
            final Matrix softIron) {
        return BodyMagneticFluxDensityGenerator.generate(input, hardIron,
                softIron);
    }

    private static double[] generateHardIron(
            final UniformRandomizer randomizer) {
        final double[] result = new double[BodyMagneticFluxDensity.COMPONENTS];
        randomizer.fill(result, MIN_HARD_IRON, MAX_HARD_IRON);
        return result;
    }

    private static Matrix generateSoftIronGeneral() {
        try {
            return Matrix.createWithUniformRandomValues(
                    BodyMagneticFluxDensity.COMPONENTS,
                    BodyMagneticFluxDensity.COMPONENTS, MIN_SOFT_IRON, MAX_SOFT_IRON);
        } catch (final WrongSizeException ignore) {
            // never happens
            return null;
        }
    }

    private static Matrix generateSoftIronCommonAxis() {
        final Matrix mm = generateSoftIronGeneral();
        assertNotNull(mm);

        for (int col = 0; col < mm.getColumns(); col++) {
            for (int row = 0; row < mm.getRows(); row++) {
                if (row > col) {
                    mm.setElementAt(row, col, 0.0);
                }
            }
        }
        return mm;
    }

    private static NEDPosition createPosition(
            final UniformRandomizer randomizer) {
        final double latitude = Math.toRadians(randomizer.nextDouble(
                MIN_LATITUDE_DEGREES, MAX_LATITUDE_DEGREES));
        final double longitude = Math.toRadians(randomizer.nextDouble(
                MIN_LONGITUDE_DEGREES, MAX_LONGITUDE_DEGREES));
        final double height = randomizer.nextDouble(
                MIN_HEIGHT_METERS, MAX_HEIGHT_METERS);

        return new NEDPosition(latitude, longitude, height);
    }

    private static long createTimestamp(final UniformRandomizer randomizer) {
        return randomizer.nextLong(
                START_TIMESTAMP_MILLIS, END_TIMESTAMP_MILLIS);
    }
}
