/*
 * Copyright (C) 2018 Alberto Irurueta Carro (alberto@irurueta.com)
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
package com.irurueta.navigation.fingerprinting;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.algebra.SingularValueDecomposer;
import com.irurueta.geometry.InhomogeneousPoint3D;
import com.irurueta.geometry.Point3D;
import com.irurueta.navigation.LockedException;
import com.irurueta.navigation.NotReadyException;
import com.irurueta.numerical.robust.RobustEstimatorException;
import com.irurueta.numerical.robust.RobustEstimatorMethod;
import com.irurueta.statistics.GaussianRandomizer;
import com.irurueta.statistics.UniformRandomizer;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class RANSACRobustWifiAccessPointPowerAndPositionEstimator3DTest implements
        RobustWifiAccessPointPowerAndPositionEstimatorListener<Point3D>{

    private static final Logger LOGGER = Logger.getLogger(
            RANSACRobustWifiAccessPointPowerAndPositionEstimator3DTest.class.getName());

    private static final double FREQUENCY = 2.4e9; //(Hz)

    private static final int MIN_READINGS = 100;
    private static final int MAX_READINGS = 500;

    private static final double MIN_POS = -50.0;
    private static final double MAX_POS = 50.0;

    private static final double MIN_RSSI = -100;
    private static final double MAX_RSSI = -50;

    private static final double MIN_PATH_LOSS_EXPONENT = 1.6;
    private static final double MAX_PATH_LOSS_EXPONENT = 2.0;

    private static final double INLIER_ERROR_STD = 0.5;

    private static final double ABSOLUTE_ERROR = 1e-6;
    private static final double LARGE_POSITION_ERROR = 1.0;
    private static final double LARGE_POWER_ERROR = 0.5;
    private static final double PATH_LOSS_ERROR = 1.0;

    private static final double SPEED_OF_LIGHT = 299792458.0;

    private static final int TIMES = 50;

    private static final int PERCENTAGE_OUTLIERS = 20;

    private static final double STD_OUTLIER_ERROR = 10.0;


    private int estimateStart;
    private int estimateEnd;
    private int estimateNextIteration;
    private int estimateProgressChange;

    public RANSACRobustWifiAccessPointPowerAndPositionEstimator3DTest() { }

    @BeforeClass
    public static void setUpClass() { }

    @AfterClass
    public static void tearDownClass() { }

    @Before
    public void setUp() { }

    @After
    public void tearDown() { }

    @Test
    public void testConstructor() {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        //test empty constructor
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertNull(estimator.getListener());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings
        List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
        WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);
        for (int i = 0; i < 4; i++) {
            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            readings.add(new WifiRssiReadingLocated3D<>(accessPoint, 0.0, position));
        }

        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertNull(estimator.getListener());
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    (List<WifiRssiReadingLocated3D<WifiAccessPoint>>)null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>());
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertSame(estimator.getListener(), this);
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    (List<WifiRssiReadingLocated3D<WifiAccessPoint>>)null, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with readings and initial position
        InhomogeneousPoint3D initialPosition = new InhomogeneousPoint3D(
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS));
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, initialPosition);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertNull(estimator.getListener());
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    null, initialPosition);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), initialPosition);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with initial position
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                initialPosition);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertNull(estimator.getListener());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with initial position and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                initialPosition, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings, initial position and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, initialPosition, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertNull(estimator.getInitialTransmittedPowerdBm());
        assertNull(estimator.getInitialTransmittedPower());
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertSame(estimator.getListener(), this);
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    null, initialPosition, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), initialPosition, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with initial transmitted power
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                MAX_RSSI);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertNull(estimator.getListener());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings and initial transmitted power
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, MAX_RSSI);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertNull(estimator.getListener());
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    (List<WifiRssiReadingLocated3D<WifiAccessPoint>>)null, MAX_RSSI);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), MAX_RSSI);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with initial transmitted power and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                MAX_RSSI, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings, initial transmitted power and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, MAX_RSSI, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertNull(estimator.getInitialPosition());
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertSame(estimator.getListener(), this);
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    (List<WifiRssiReadingLocated3D<WifiAccessPoint>>)null, MAX_RSSI, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), MAX_RSSI, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with readings, initial position and initial transmitted power
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, initialPosition, MAX_RSSI);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertNull(estimator.getListener());
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    null, initialPosition, MAX_RSSI);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), initialPosition, MAX_RSSI);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with initial position and initial transmitted power
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                initialPosition, MAX_RSSI);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertNull(estimator.getListener());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with initial position, initial transmitted power and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                initialPosition, MAX_RSSI, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings, initial position, initial transmitted
        //power and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, initialPosition, MAX_RSSI, this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertSame(estimator.getListener(), this);
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    null, initialPosition, MAX_RSSI, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), initialPosition, MAX_RSSI, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);

        //test constructor with readings, initial position, initial transmitted power
        //and initial path loss exponent
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, initialPosition, MAX_RSSI, MIN_PATH_LOSS_EXPONENT);

        //check
        assertEquals(estimator.getThreshold(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                MIN_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertNull(estimator.getListener());
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);

        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    null, initialPosition, MAX_RSSI,
                    MIN_PATH_LOSS_EXPONENT);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), initialPosition,
                    MAX_RSSI, MIN_PATH_LOSS_EXPONENT);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);


        //test constructor with initial position, initial transmitted power and
        //initial path loss exponent
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                initialPosition, MAX_RSSI, MIN_PATH_LOSS_EXPONENT);

        //check
        assertEquals(estimator.getThreshold(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                MIN_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertNull(estimator.getListener());
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with initial position, initial transmitted power,
        // initial path loss exponent and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                initialPosition, MAX_RSSI, MIN_PATH_LOSS_EXPONENT, this);

        //check
        assertEquals(estimator.getThreshold(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                MIN_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertNull(estimator.getReadings());
        assertSame(estimator.getListener(), this);
        assertFalse(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //test constructor with readings, initial position, initial transmitted power
        //initial path loss exponent and listener
        estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                readings, initialPosition, MAX_RSSI, MIN_PATH_LOSS_EXPONENT,
                this);

        //check
        assertEquals(estimator.getThreshold(), RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
        assertEquals(estimator.getMethod(), RobustEstimatorMethod.RANSAC);
        assertEquals(estimator.getMinReadings(), 4);
        assertEquals(estimator.getNumberOfDimensions(), 3);
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
        assertEquals(estimator.getInitialTransmittedPower(), Utils.dBmToPower(MAX_RSSI), 0.0);
        assertSame(estimator.getInitialPosition(), initialPosition);
        assertEquals(estimator.getInitialPathLossExponent(),
                MIN_PATH_LOSS_EXPONENT, 0.0);
        assertFalse(estimator.isPathLossEstimationEnabled());
        assertFalse(estimator.isLocked());
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA, 0.0);
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE, 0.0);
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);
        assertNull(estimator.getInliersData());
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
        assertSame(estimator.getReadings(), readings);
        assertSame(estimator.getListener(), this);
        assertTrue(estimator.isReady());
        assertNull(estimator.getQualityScores());
        assertNull(estimator.getCovariance());
        assertNull(estimator.getEstimatedPositionCovariance());
        assertEquals(estimator.getEstimatedTransmittedPowerVariance(), 0.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPower(), 1.0, 0.0);
        assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0, 0.0);
        assertNull(estimator.getEstimatedPosition());
        assertNull(estimator.getEstimatedAccessPoint());
        assertEquals(estimator.getEstimatedPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertEquals(estimator.getEstimatedPathLossExponentVariance(), 0.0, 0.0);


        //force IllegalArgumentException
        estimator = null;
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    null, initialPosition, MAX_RSSI, this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator = new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                    new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>(), initialPosition, MAX_RSSI,
                    this);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        assertNull(estimator);
    }

    @Test
    public void testGetSetThreshold() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.getThreshold(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_THRESHOLD,
                0.0);

        //set new value
        estimator.setThreshold(50.0);

        //check
        assertEquals(estimator.getThreshold(), 50.0, 0.0);

        //force IllegalArgumentException
        try {
            estimator.setThreshold(0.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testIsSetComputeAndKeepInliersEnabled() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);

        //set new value
        estimator.setComputeAndKeepInliersEnabled(
                !RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);

        //check
        assertEquals(estimator.isComputeAndKeepInliersEnabled(),
                !RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_INLIERS);
    }

    @Test
    public void testIsSetComputeAndKeepResidualsEnabled() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);

        //set new value
        estimator.setComputeAndKeepResidualsEnabled(
                !RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);

        //check
        assertEquals(estimator.isComputeAndKeepResidualsEnabled(),
                !RANSACRobustWifiAccessPointPowerAndPositionEstimator3D.DEFAULT_COMPUTE_AND_KEEP_RESIDUALS);
    }

    @Test
    public void testGetSetInitialTransmittedPowerdBm() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertNull(estimator.getInitialTransmittedPowerdBm());

        //set new value
        estimator.setInitialTransmittedPowerdBm(MAX_RSSI);

        //check
        assertEquals(estimator.getInitialTransmittedPowerdBm(), MAX_RSSI, 0.0);
    }

    @Test
    public void testGetSetInitialTransmittedPower() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertNull(estimator.getInitialTransmittedPower());

        //set new value
        double power = Utils.dBmToPower(MAX_RSSI);
        estimator.setInitialTransmittedPower(power);

        //check
        assertEquals(estimator.getInitialTransmittedPower(), power, ABSOLUTE_ERROR);
    }

    @Test
    public void testGetSetInitialPosition() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertNull(estimator.getInitialPosition());

        //set new value
        InhomogeneousPoint3D initialPosition = new InhomogeneousPoint3D(
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS));
        estimator.setInitialPosition(initialPosition);

        //check
        assertSame(estimator.getInitialPosition(), initialPosition);
    }

    @Test
    public void testGetSetInitialPathLossExponent() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.getInitialPathLossExponent(),
                WifiAccessPointPowerAndPositionEstimator.DEFAULT_PATH_LOSS_EXPONENT,
                0.0);

        //set new value
        double pathLossExponent = randomizer.nextDouble(
                MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);
        estimator.setInitialPathLossExponent(pathLossExponent);

        //check
        assertEquals(estimator.getInitialPathLossExponent(),
                pathLossExponent, 0.0);
    }

    @Test
    public void testIsSetPathLossEstimationEnabled() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertFalse(estimator.isPathLossEstimationEnabled());

        //set new value
        estimator.setPathLossEstimationEnabled(true);

        //check
        assertTrue(estimator.isPathLossEstimationEnabled());
    }

    @Test
    public void testGetSetProgressDelta() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.getProgressDelta(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_PROGRESS_DELTA,
                0.0);

        //set new value
        estimator.setProgressDelta(0.5f);

        //check
        assertEquals(estimator.getProgressDelta(), 0.5f, 0.0);

        //force IllegalArgumentException
        try {
            estimator.setProgressDelta(-1.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator.setProgressDelta(2.0f);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetConfidence() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.getConfidence(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_CONFIDENCE,
                0.0);

        //set new value
        estimator.setConfidence(0.5);

        //check
        assertEquals(estimator.getConfidence(), 0.5, 0.0);

        //force IllegalArgumentException
        try {
            estimator.setConfidence(-1.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator.setConfidence(2.0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetMaxIterations() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.getMaxIterations(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_MAX_ITERATIONS);

        //set new value
        estimator.setMaxIterations(10);

        //check
        assertEquals(estimator.getMaxIterations(), 10);

        //force IllegalArgumentException
        try {
            estimator.setMaxIterations(0);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testIsSetResultRefined() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.isResultRefined(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);

        //set new value
        estimator.setResultRefined(
                !RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);

        //check
        assertEquals(estimator.isResultRefined(),
                !RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_REFINE_RESULT);
    }

    @Test
    public void testIsSetCovarianceKept() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertEquals(estimator.isCovarianceKept(),
                RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);

        //set new value
        estimator.setCovarianceKept(
                !RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);

        //check
        assertEquals(estimator.isCovarianceKept(),
                !RobustWifiAccessPointPowerAndPositionEstimator.DEFAULT_KEEP_COVARIANCE);
    }

    @Test
    public void testGetSetReadings() throws LockedException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());

        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertNull(estimator.getReadings());

        //set new value
        List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
        WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);
        for (int i = 0; i < 4; i++) {
            InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS),
                    randomizer.nextDouble(MIN_POS, MAX_POS));
            readings.add(new WifiRssiReadingLocated3D<>(accessPoint, 0.0, position));
        }

        estimator.setReadings(readings);

        //check
        assertSame(estimator.getReadings(), readings);

        //force IllegalArgumentException
        try {
            estimator.setReadings(null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
        try {
            estimator.setReadings(new ArrayList<WifiRssiReadingLocated3D<WifiAccessPoint>>());
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ignore) { }
    }

    @Test
    public void testGetSetListener() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertNull(estimator.getListener());

        //set new value
        estimator.setListener(this);

        //check
        assertSame(estimator.getListener(), this);
    }

    @Test
    public void testGetSetQualityScores() throws LockedException {
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();

        //check default value
        assertNull(estimator.getQualityScores());

        //set new value
        estimator.setQualityScores(new double[3]);

        //check
        assertNull(estimator.getQualityScores());
    }

    @Test
    public void testEstimateNoInlierErrorNoRefinementNoInlierDataAndNoResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(false);
            estimator.setComputeAndKeepInliersEnabled(false);
            estimator.setComputeAndKeepResidualsEnabled(false);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNull(estimator.getInliersData());
            assertNull(estimator.getCovariance());
            assertNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertEquals(powerVariance, 0.0, 0.0);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
            }

            avgPositionError += positionDistance;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
            }

            avgPowerError += powerError;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorNoRefinementNoInlierDataAndWithResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(false);
            estimator.setComputeAndKeepInliersEnabled(false);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNull(estimator.getCovariance());
            assertNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertEquals(powerVariance, 0.0, 0.0);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
            }

            avgPositionError += positionDistance;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
            }

            avgPowerError += powerError;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorNoRefinementWithInlierDataAndNoResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(false);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(false);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNull(estimator.getCovariance());
            assertNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertEquals(powerVariance, 0.0, 0.0);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
            }

            avgPositionError += positionDistance;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
            }

            avgPowerError += powerError;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorNoRefinementWithInlierDataAndWithResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(false);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNull(estimator.getCovariance());
            assertNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertEquals(powerVariance, 0.0, 0.0);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
            }

            avgPositionError += positionDistance;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
            }

            avgPowerError += powerError;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorWithRefinementNoInlierDataAndNoResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(false);
            estimator.setComputeAndKeepResidualsEnabled(false);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorWithRefinementNoInlierDataAndWithResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(false);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorWithRefinementWithInlierDataAndWithResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateWithInlierErrorWithRefinementWithInlierDataAndWithResiduals()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                error += inlierErrorRandomizer.nextDouble();

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i], INLIER_ERROR_STD));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= LARGE_POSITION_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        LARGE_POSITION_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= LARGE_POWER_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, LARGE_POWER_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorWithInitialPosition()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            InhomogeneousPoint3D initialPosition =
                    new InhomogeneousPoint3D(
                            accessPointPosition.getInhomX() + inlierErrorRandomizer.nextDouble(),
                            accessPointPosition.getInhomY() + inlierErrorRandomizer.nextDouble(),
                            accessPointPosition.getInhomZ() + inlierErrorRandomizer.nextDouble());

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setInitialPosition(initialPosition);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorWithInitialPower()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            double initialTransmittedPowerdBm = transmittedPowerdBm +
                    inlierErrorRandomizer.nextDouble();

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setInitialTransmittedPowerdBm(initialTransmittedPowerdBm);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateNoInlierErrorWithInitialPositionAndPower()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            InhomogeneousPoint3D initialPosition =
                    new InhomogeneousPoint3D(
                            accessPointPosition.getInhomX() + inlierErrorRandomizer.nextDouble(),
                            accessPointPosition.getInhomY() + inlierErrorRandomizer.nextDouble(),
                            accessPointPosition.getInhomZ() + inlierErrorRandomizer.nextDouble());
            double initialTransmittedPowerdBm = transmittedPowerdBm +
                    inlierErrorRandomizer.nextDouble();

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setInitialPosition(initialPosition);
            estimator.setInitialTransmittedPowerdBm(initialTransmittedPowerdBm);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateWithInlierErrorWithInitialPositionAndPower()
            throws LockedException, NotReadyException, RobustEstimatorException,
            AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);
        GaussianRandomizer inlierErrorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, INLIER_ERROR_STD);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        MAX_PATH_LOSS_EXPONENT));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                error += inlierErrorRandomizer.nextDouble();

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i], INLIER_ERROR_STD));
            }

            InhomogeneousPoint3D initialPosition =
                    new InhomogeneousPoint3D(
                            accessPointPosition.getInhomX() + inlierErrorRandomizer.nextDouble(),
                            accessPointPosition.getInhomY() + inlierErrorRandomizer.nextDouble(),
                            accessPointPosition.getInhomZ() + inlierErrorRandomizer.nextDouble());
            double initialTransmittedPowerdBm = transmittedPowerdBm +
                    inlierErrorRandomizer.nextDouble();

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setInitialPosition(initialPosition);
            estimator.setInitialTransmittedPowerdBm(initialTransmittedPowerdBm);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getPathLossExponent(), MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= LARGE_POSITION_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        LARGE_POSITION_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= LARGE_POWER_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, LARGE_POWER_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateWithPathLossEstimationEnabled()
            throws LockedException, NotReadyException, RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValidPathLoss = 0,
                numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPathLossError = 0.0, avgValidPathLossError = 0.0,
                avgInvalidPathLossError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        double avgPathLossStd = 0.0, avgValidPathLossStd = 0.0,
                avgInvalidPathLossStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        pathLossExponent));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);
            estimator.setPathLossEstimationEnabled(true);
            estimator.setInitialPosition(accessPointPosition);
            estimator.setInitialTransmittedPowerdBm(transmittedPowerdBm);
            estimator.setInitialPathLossExponent(pathLossExponent);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());
            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    estimatedAccessPoint.getPathLossExponent(), 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            if (powerVariance <= 0.0) {
                continue;
            }
            assertTrue(powerVariance > 0.0);
            double pathLossVariance = estimator.getEstimatedPathLossExponentVariance();
            if (pathLossVariance <= 0.0) {
                continue;
            }
            assertTrue(pathLossVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);
            double pathLossStd = Math.sqrt(pathLossVariance);

            boolean validPosition, validPower, validPathLoss;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            double pathLossError = Math.abs(
                    estimator.getEstimatedPathLossExponent() -
                            pathLossExponent);

            if (pathLossError <= PATH_LOSS_ERROR) {
                assertEquals(estimator.getEstimatedPathLossExponent(),
                        pathLossExponent, PATH_LOSS_ERROR);
                validPathLoss = true;
                numValidPathLoss++;

                avgValidPathLossError += pathLossError;
                avgValidPathLossStd += pathLossStd;
            } else {
                validPathLoss = false;

                avgInvalidPathLossError += pathLossError;
                avgInvalidPathLossStd += pathLossStd;
            }

            avgPathLossError += pathLossError;
            avgPathLossStd += pathLossStd;

            if (validPosition && validPower && validPathLoss) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValidPathLoss > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPathLossError /= numValidPathLoss;
        avgInvalidPathLossError /= (TIMES - numValidPathLoss);
        avgPathLossError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        avgValidPathLossStd /= numValidPathLoss;
        avgInvalidPathLossStd /= (TIMES - numValidPathLoss);
        avgPathLossStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid path loss: {0} %",
                (double)numValidPathLoss / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage all valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        LOGGER.log(Level.INFO, "Avg. valid path loss error: {0}",
                avgValidPathLossError);
        LOGGER.log(Level.INFO, "Avg. invalid path loss error: {0}",
                avgInvalidPathLossError);
        LOGGER.log(Level.INFO, "Avg. path loss error: {0}",
                avgPathLossError);

        LOGGER.log(Level.INFO, "Valid path loss standard deviation {0}",
                avgValidPathLossStd);
        LOGGER.log(Level.INFO, "Invalid path loss standard deviation {0}",
                avgInvalidPathLossStd);
        LOGGER.log(Level.INFO, "Path loss standard deviation {0}",
                avgPathLossStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Test
    public void testEstimateWithInitialPathLoss()
            throws LockedException, NotReadyException, RobustEstimatorException, AlgebraException {
        UniformRandomizer randomizer = new UniformRandomizer(new Random());
        GaussianRandomizer errorRandomizer = new GaussianRandomizer(
                new Random(), 0.0, STD_OUTLIER_ERROR);

        int numValidPosition = 0, numValidPower = 0, numValid = 0;
        double avgPositionError = 0.0, avgValidPositionError = 0.0,
                avgInvalidPositionError = 0.0;
        double avgPowerError = 0.0, avgValidPowerError = 0.0,
                avgInvalidPowerError = 0.0;
        double avgPositionStd = 0.0, avgValidPositionStd = 0.0,
                avgInvalidPositionStd = 0.0;
        double avgPowerStd = 0.0, avgValidPowerStd = 0.0,
                avgInvalidPowerStd = 0.0;
        for (int t = 0; t < TIMES; t++) {
            InhomogeneousPoint3D accessPointPosition =
                    new InhomogeneousPoint3D(
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS),
                            randomizer.nextDouble(MIN_POS, MAX_POS));
            double transmittedPowerdBm = randomizer.nextDouble(MIN_RSSI, MAX_RSSI);
            double transmittedPower = Utils.dBmToPower(transmittedPowerdBm);
            WifiAccessPoint accessPoint = new WifiAccessPoint("bssid", FREQUENCY);

            double pathLossExponent = randomizer.nextDouble(
                    MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);

            int numReadings = randomizer.nextInt(
                    MIN_READINGS, MAX_READINGS);
            Point3D[] readingsPositions = new Point3D[numReadings];
            List<WifiRssiReadingLocated3D<WifiAccessPoint>> readings = new ArrayList<>();
            for (int i = 0; i < numReadings; i++) {
                readingsPositions[i] = new InhomogeneousPoint3D(
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS),
                        randomizer.nextDouble(MIN_POS, MAX_POS));

                double distance = readingsPositions[i].distanceTo(
                        accessPointPosition);

                double rssi = Utils.powerTodBm(receivedPower(
                        transmittedPower, distance,
                        accessPoint.getFrequency(),
                        pathLossExponent));

                double error;
                if (randomizer.nextInt(0, 100) < PERCENTAGE_OUTLIERS) {
                    //outlier
                    error = errorRandomizer.nextDouble();
                } else {
                    //inlier
                    error = 0.0;
                }

                readings.add(new WifiRssiReadingLocated3D<>(accessPoint, rssi + error,
                        readingsPositions[i]));
            }

            RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                    new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D(
                            readings, this);
            estimator.setInitialPathLossExponent(pathLossExponent);
            estimator.setResultRefined(true);
            estimator.setComputeAndKeepInliersEnabled(true);
            estimator.setComputeAndKeepResidualsEnabled(true);

            reset();
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());
            assertNull(estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedTransmittedPower(), 1.0,
                    0.0);
            assertEquals(estimator.getEstimatedTransmittedPowerdBm(), 0.0,
                    0.0);
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    MAX_PATH_LOSS_EXPONENT, 0.0);
            assertEquals(estimateStart, 0);
            assertEquals(estimateEnd, 0);
            assertEquals(estimateNextIteration, 0);
            assertEquals(estimateProgressChange, 0);

            estimator.estimate();

            //check
            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
            assertTrue(estimateNextIteration > 0);
            assertTrue(estimateProgressChange >= 0);
            assertTrue(estimator.isReady());
            assertFalse(estimator.isLocked());

            assertNotNull(estimator.getInliersData());

            if (estimator.getCovariance() == null) {
                continue;
            }

            assertNotNull(estimator.getCovariance());
            assertNotNull(estimator.getEstimatedPositionCovariance());

            WifiAccessPointWithPowerAndLocated3D estimatedAccessPoint =
                    estimator.getEstimatedAccessPoint();

            assertEquals(estimatedAccessPoint.getBssid(), "bssid");
            assertEquals(estimatedAccessPoint.getFrequency(), FREQUENCY, 0.0);
            assertNull(estimatedAccessPoint.getSsid());
            assertEquals(estimatedAccessPoint.getTransmittedPower(),
                    estimator.getEstimatedTransmittedPowerdBm(), 0.0);
            assertEquals(estimatedAccessPoint.getPosition(),
                    estimator.getEstimatedPosition());
            assertEquals(estimator.getEstimatedPathLossExponent(),
                    estimatedAccessPoint.getPathLossExponent(), 0.0);
            assertEquals(estimatedAccessPoint.getTransmittedPowerStandardDeviation(),
                    Math.sqrt(estimator.getEstimatedTransmittedPowerVariance()), 0.0);
            assertEquals(estimatedAccessPoint.getPositionCovariance(),
                    estimator.getEstimatedPositionCovariance());

            double powerVariance = estimator.getEstimatedTransmittedPowerVariance();
            assertTrue(powerVariance > 0.0);

            SingularValueDecomposer decomposer = new SingularValueDecomposer(
                    estimator.getEstimatedPositionCovariance());
            decomposer.decompose();
            double[] v = decomposer.getSingularValues();
            double positionStd = 0.0;
            for (double aV : v) {
                positionStd += Math.sqrt(aV);
            }
            positionStd /= v.length;
            double powerStd = Math.sqrt(powerVariance);

            boolean validPosition, validPower;
            double positionDistance = estimator.getEstimatedPosition().
                    distanceTo(accessPointPosition);
            if (positionDistance <= ABSOLUTE_ERROR) {
                assertTrue(estimator.getEstimatedPosition().equals(accessPointPosition,
                        ABSOLUTE_ERROR));
                validPosition = true;
                numValidPosition++;

                avgValidPositionError += positionDistance;
                avgValidPositionStd += positionStd;
            } else {
                validPosition = false;

                avgInvalidPositionError += positionDistance;
                avgInvalidPositionStd += positionStd;
            }

            avgPositionError += positionDistance;
            avgPositionStd += positionStd;

            double powerError = Math.abs(
                    estimator.getEstimatedTransmittedPowerdBm() -
                            transmittedPowerdBm);
            if (powerError <= ABSOLUTE_ERROR) {
                assertEquals(estimator.getEstimatedTransmittedPower(), transmittedPower,
                        ABSOLUTE_ERROR);
                assertEquals(estimator.getEstimatedTransmittedPowerdBm(),
                        transmittedPowerdBm, ABSOLUTE_ERROR);
                validPower = true;
                numValidPower++;

                avgValidPowerError += powerError;
                avgValidPowerStd += powerStd;
            } else {
                validPower = false;

                avgInvalidPowerError += powerError;
                avgInvalidPowerStd += powerStd;
            }

            avgPowerError += powerError;
            avgPowerStd += powerStd;

            if (validPosition && validPower) {
                numValid++;
            }

            assertEquals(estimateStart, 1);
            assertEquals(estimateEnd, 1);
        }

        assertTrue(numValidPosition > 0);
        assertTrue(numValidPower > 0);
        assertTrue(numValid > 0);

        avgValidPositionError /= numValidPosition;
        avgInvalidPositionError /= (TIMES - numValidPosition);
        avgPositionError /= TIMES;

        avgValidPowerError /= numValidPower;
        avgInvalidPowerError /= (TIMES - numValidPower);
        avgPowerError /= TIMES;

        avgValidPositionStd /= numValidPosition;
        avgInvalidPositionStd /= (TIMES - numValidPosition);
        avgPositionStd /= TIMES;

        avgValidPowerStd /= numValidPower;
        avgInvalidPowerStd /= (TIMES - numValidPower);
        avgPowerStd /= TIMES;

        LOGGER.log(Level.INFO, "Percentage valid position: {0} %",
                (double)numValidPosition / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage valid power: {0} %",
                (double)numValidPower / (double)TIMES * 100.0);
        LOGGER.log(Level.INFO, "Percentage both valid: {0} %",
                (double)numValid / (double)TIMES * 100.0);

        LOGGER.log(Level.INFO, "Avg. valid position error: {0} meters",
                avgValidPositionError);
        LOGGER.log(Level.INFO, "Avg. invalid position error: {0} meters",
                avgInvalidPositionError);
        LOGGER.log(Level.INFO, "Avg. position error: {0} meters",
                avgPositionError);

        LOGGER.log(Level.INFO, "Valid position standard deviation {0} meters",
                avgValidPositionStd);
        LOGGER.log(Level.INFO, "Invalid position standard deviation {0} meters",
                avgInvalidPositionStd);
        LOGGER.log(Level.INFO, "Position standard deviation {0} meters",
                avgPositionStd);

        LOGGER.log(Level.INFO, "Avg. valid power error: {0} dB",
                avgValidPowerError);
        LOGGER.log(Level.INFO, "Avg. invalid power error: {0} dB",
                avgInvalidPowerError);
        LOGGER.log(Level.INFO, "Avg. power error: {0} dB",
                avgPowerError);

        LOGGER.log(Level.INFO, "Valid power standard deviation {0} dB",
                avgValidPowerStd);
        LOGGER.log(Level.INFO, "Invalid power standard deviation {0} dB",
                avgInvalidPowerStd);
        LOGGER.log(Level.INFO, "Power standard deviation {0} dB",
                avgPowerStd);

        //force NotReadyException
        RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator =
                new RANSACRobustWifiAccessPointPowerAndPositionEstimator3D();
        try {
            estimator.estimate();
            fail("NotReadyException expected but not thrown");
        } catch (NotReadyException ignore) { }
    }

    @Override
    public void onEstimateStart(RobustWifiAccessPointPowerAndPositionEstimator<Point3D> estimator) {
        estimateStart++;
        checkLocked((RANSACRobustWifiAccessPointPowerAndPositionEstimator3D)estimator);
    }

    @Override
    public void onEstimateEnd(RobustWifiAccessPointPowerAndPositionEstimator<Point3D> estimator) {
        estimateEnd++;
        checkLocked((RANSACRobustWifiAccessPointPowerAndPositionEstimator3D)estimator);
    }

    @Override
    public void onEstimateNextIteration(RobustWifiAccessPointPowerAndPositionEstimator<Point3D> estimator,
                                        int iteration) {
        estimateNextIteration++;
        checkLocked((RANSACRobustWifiAccessPointPowerAndPositionEstimator3D)estimator);
    }

    @Override
    public void onEstimateProgressChange(RobustWifiAccessPointPowerAndPositionEstimator<Point3D> estimator,
                                         float progress) {
        estimateProgressChange++;
        checkLocked((RANSACRobustWifiAccessPointPowerAndPositionEstimator3D)estimator);
    }

    private void reset() {
        estimateStart = estimateEnd = estimateNextIteration = estimateProgressChange = 0;
    }

    private double receivedPower(double equivalentTransmittedPower, double distance, double frequency,
                                 double pathLossExponent) {
        //Pr = Pt*Gt*Gr*lambda^2/(4*pi*d)^2,    where Pr is the received power
        // lambda = c/f, where lambda is wavelength,
        // Pte = Pt*Gt*Gr, is the equivalent transmitted power, Gt is the transmitted Gain and Gr is the received Gain
        //Pr = Pte*c^2/((4*pi*f)^2 * d^2)
        double k = Math.pow(SPEED_OF_LIGHT / (4.0 * Math.PI * frequency), pathLossExponent);
        return equivalentTransmittedPower * k /
                Math.pow(distance, pathLossExponent);
    }

    private void checkLocked(RANSACRobustWifiAccessPointPowerAndPositionEstimator3D estimator) {
        try {
            estimator.setThreshold(0.5);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setComputeAndKeepInliersEnabled(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setComputeAndKeepResidualsEnabled(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setInitialTransmittedPowerdBm(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setInitialTransmittedPower(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setInitialPosition(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setProgressDelta(0.5f);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setConfidence(0.8);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setMaxIterations(10);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setResultRefined(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setCovarianceKept(false);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setReadings(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.setListener(null);
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) { }
        try {
            estimator.estimate();
            fail("LockedException expected but not thrown");
        } catch (LockedException ignore) {
        } catch (Exception e) {
            fail("LockedException expected but not thrown");
        }
    }
}
