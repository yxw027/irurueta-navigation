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
package com.irurueta.navigation.indoor;

import com.irurueta.algebra.AlgebraException;
import com.irurueta.algebra.Matrix;
import com.irurueta.geometry.InhomogeneousPoint3D;
import com.irurueta.geometry.Point3D;
import com.irurueta.statistics.UniformRandomizer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class WifiAccessPointWithPowerAndLocated3DTest {

    private static final String BSSID = "bssid";
    private static final String SSID = "ssid";
    private static final double FREQUENCY = 2.4e9;
    private static final double MIN_POS = -50.0;
    private static final double MAX_POS = 50.0;
    private static final double TRANSMITTED_POWER = -50.0;
    private static final double TRANSMITTED_POWER_STD = 0.5;
    private static final double PATHLOSS_STD = 0.1;

    private static final double MIN_PATH_LOSS_EXPONENT = 1.6;
    private static final double MAX_PATH_LOSS_EXPONENT = 2.0;

    public WifiAccessPointWithPowerAndLocated3DTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() throws AlgebraException {
        // test empty constructor
        WifiAccessPointWithPowerAndLocated3D ap =
                new WifiAccessPointWithPowerAndLocated3D();

        // check default values
        assertNull(ap.getBssid());
        assertEquals(ap.getFrequency(), 0.0, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), 0.0, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertNull(ap.getPosition());
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);


        // test with bssid, frequency, transmitted power and position
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS));
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, ssid, transmitted power and position
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    SSID, TRANSMITTED_POWER, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    SSID, TRANSMITTED_POWER, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, transmitted power, transmitted power
        // standard deviation
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, null, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(-TRANSMITTED_POWER_STD),
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, ssid, transmitted power and transmitted power
        // standard deviation
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, null, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY, SSID,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY, SSID,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                    TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, transmitted power, position and position
        // covariance
        Matrix cov = new Matrix(Point3D.POINT3D_INHOMOGENEOUS_COORDINATES_LENGTH,
                Point3D.POINT3D_INHOMOGENEOUS_COORDINATES_LENGTH);
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, position, null);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, position, new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, ssid, transmitted power, position and
        // position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, position, null);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    SSID, TRANSMITTED_POWER, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    SSID, TRANSMITTED_POWER, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, position, new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, transmitted power, transmitted power standard
        // deviation, position and position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, null, position,
                null);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(-TRANSMITTED_POWER_STD),
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, Double.valueOf(TRANSMITTED_POWER_STD),
                    position, new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, ssid, transmitted power,
        // transmitted power standard deviation, position and position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, null, position,
                null);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(),
                WifiAccessPointWithPower.DEFAULT_PATH_LOSS_EXPONENT, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position,
                    cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position,
                    cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, position,
                    cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, null,
                    cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, position,
                    new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);

        // test with bssid, frequency, transmitted power, pathloss and position
        final double pathLossExponent = randomizer.nextDouble(
                MIN_PATH_LOSS_EXPONENT, MAX_PATH_LOSS_EXPONENT);
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, pathLossExponent, position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, transmitted power, transmitted power
        // standard deviation, path loss exponent and position
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, null,
                pathLossExponent, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, pathLossExponent,
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, ssid, transmitted power, transmitted power
        // standard deviation, path loss exponent and position
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, null, pathLossExponent,
                position);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY, SSID,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY, SSID,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                    TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, pathLossExponent,
                    position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, transmitted power, path loss, position and
        // position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, pathLossExponent, position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, pathLossExponent, position,
                null);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, pathLossExponent, position,
                    new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, transmitted power, transmitted power standard
        // deviation, path loss exponent, position and position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);


        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, null, pathLossExponent,
                position, null);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, pathLossExponent,
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    null,
                    cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position,
                    new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test with bssid, frequency, ssid, transmitted power, transmitted power
        // standard deviation, path loss exponent, position and position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                position, cov);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);


        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, null, pathLossExponent,
                position, null);

        // check default values
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // force IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, pathLossExponent,
                    position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    position, new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test constructor with bssid, frequency, tx power, tx power std deviation,
        // pathloss, pathloss std deviation and position
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                PATHLOSS_STD, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertEquals(ap.getPathLossExponentStandardDeviation(), PATHLOSS_STD,
                0.0);
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, null, pathLossExponent,
                null, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // fail IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    -PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test constructor with bssid, frequency, ssid, tx power, tx power std deviation,
        // pathloss, pathloss std deviation and position
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                PATHLOSS_STD, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertEquals(ap.getPathLossExponentStandardDeviation(), PATHLOSS_STD,
                0.0);
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, null, pathLossExponent,
                null, position);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // fail IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, -TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, -PATHLOSS_STD, position);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, null);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test constructor with bssid, frequency, tx power, tx power std deviation,
        // pathloss, pathloss std deviation, position and position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                PATHLOSS_STD, position, cov);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertEquals(ap.getPathLossExponentStandardDeviation(), PATHLOSS_STD,
                0.0);
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                TRANSMITTED_POWER, null, pathLossExponent,
                null, position, null);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertNull(ap.getSsid());
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // fail IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, -TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    -PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                    PATHLOSS_STD, position, new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);


        // test constructor with bssid, frequency, ssid, tx power, tx power std deviation,
        // pathloss, pathloss std deviation, position and position covariance
        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, TRANSMITTED_POWER_STD, pathLossExponent,
                PATHLOSS_STD, position, cov);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertEquals(ap.getTransmittedPowerStandardDeviation(),
                TRANSMITTED_POWER_STD, 0.0);
        assertSame(ap.getPosition(), position);
        assertSame(ap.getPositionCovariance(), cov);
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertEquals(ap.getPathLossExponentStandardDeviation(), PATHLOSS_STD,
                0.0);
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY, SSID,
                TRANSMITTED_POWER, null, pathLossExponent,
                null, position, null);

        // check
        assertEquals(ap.getBssid(), BSSID);
        assertEquals(ap.getFrequency(), FREQUENCY, 0.0);
        assertEquals(ap.getSsid(), SSID);
        assertEquals(ap.getTransmittedPower(), TRANSMITTED_POWER, 0.0);
        assertNull(ap.getTransmittedPowerStandardDeviation());
        assertSame(ap.getPosition(), position);
        assertNull(ap.getPositionCovariance());
        assertEquals(ap.getPathLossExponent(), pathLossExponent, 0.0);
        assertNull(ap.getPathLossExponentStandardDeviation());
        assertEquals(ap.getType(), RadioSourceType.WIFI_ACCESS_POINT);

        // fail IllegalArgumentException
        ap = null;
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(null, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, -FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, -TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, -PATHLOSS_STD, position, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, null, cov);
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        try {
            ap = new WifiAccessPointWithPowerAndLocated3D(BSSID, FREQUENCY,
                    SSID, TRANSMITTED_POWER, TRANSMITTED_POWER_STD,
                    pathLossExponent, PATHLOSS_STD, position,
                    new Matrix(1, 1));
            fail("IllegalArgumentException expected but not thrown");
        } catch (final IllegalArgumentException ignore) {
        }
        assertNull(ap);
    }

    @Test
    public void testEquals() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS));
        final WifiAccessPointWithPowerAndLocated3D ap1 =
                new WifiAccessPointWithPowerAndLocated3D("bssid1", FREQUENCY,
                        TRANSMITTED_POWER, position);
        final WifiAccessPointWithPowerAndLocated3D ap2 =
                new WifiAccessPointWithPowerAndLocated3D("bssid1", FREQUENCY,
                        TRANSMITTED_POWER, position);
        final WifiAccessPointWithPowerAndLocated3D ap3 =
                new WifiAccessPointWithPowerAndLocated3D("bssid2", FREQUENCY,
                        TRANSMITTED_POWER, position);

        // check
        assertEquals(ap1, ap1);
        assertEquals(ap1, ap2);
        assertNotEquals(ap1, ap3);
    }

    @Test
    public void testHashCode() {
        final UniformRandomizer randomizer = new UniformRandomizer(new Random());
        final InhomogeneousPoint3D position = new InhomogeneousPoint3D(
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS),
                randomizer.nextDouble(MIN_POS, MAX_POS));
        final WifiAccessPointWithPowerAndLocated3D ap1 =
                new WifiAccessPointWithPowerAndLocated3D("bssid1", FREQUENCY,
                        TRANSMITTED_POWER, position);
        final WifiAccessPointWithPowerAndLocated3D ap2 =
                new WifiAccessPointWithPowerAndLocated3D("bssid1", FREQUENCY,
                        TRANSMITTED_POWER, position);
        final WifiAccessPointWithPowerAndLocated3D ap3 =
                new WifiAccessPointWithPowerAndLocated3D("bssid2", FREQUENCY,
                        TRANSMITTED_POWER, position);

        // check
        assertEquals(ap1.hashCode(), ap2.hashCode());
        assertNotEquals(ap1.hashCode(), ap3.hashCode());
    }

}
