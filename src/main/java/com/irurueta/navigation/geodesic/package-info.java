/**
 * Contains geodesic algorithms.
 * Makes easy to do geodesic computations for an ellipsoid of revolution.
 * <p>
 * The important classes are:
 * <ul>
 *     <li>
 *         {@link Geodesic}, for direct and inverse geodesic calculations.
 *     </li>
 *     <li>
 *         {@link GeodesicLine}, an efficient way of calculating multiple points on a single geodesic.
 *     </li>
 *     <li>
 *         {@link com.irurueta.navigation.geodesic.GeodesicData}, object containing the results of
 *         geodesic calculations.
 *     </li>
 *     <li>
 *         {@link com.irurueta.navigation.geodesic.GeodesicMask} constants that let you specify
 *         the variables to return in {@link com.irurueta.navigation.geodesic.GeodesicData} and
 *         the capabilities of a {@link GeodesicLine}.
 *     </li>
 *     <li>
 *         {@link com.irurueta.navigation.geodesic.Constants} the parameters for the WGS84 ellipsoid.
 *     </li>
 *     <li>
 *         {@link PolygonArea}, a class to compute the perimeter and area of a geodesic polygon
 *         (returned as a {@link PolygonResult}).
 *     </li>
 * </ul>
 *
 * <h2>External links</h2>
 * <ul>
 *     <li>
 *         These algorithms are derived in C.F.F. Karney, <a href="https://doi.org/10.1007/s00190-012-0578-z">
 *         Algorithms for geodesics</a>, J.Geodesy <b>87</b>, 43&ndash;55 (2013)
 *         (<a href="https://geographiclib.sourceforge.io/geod-addenda.html">addenda</a>
 *     </li>
 *     <li>
 *         A longer paper on geodesics: C.F.F. Karney,
 *         <a href="https://arxic.org/abs/1102.1215v1">Geodesics on an ellipsoid of revolution</a>,
 *         Feb. 2011 (<a href="https://geographiclib.sourceforge.io/geod-addenda.html#geod-errata">
 *         errata</a>).
 *     </li>
 *     <li>
 *         <a href="https://geographiclib.sourceforge.io">The GeographicLib web site</a>.
 *     </li>
 *     <li>
 *         The wikipedia page,
 *         <a href="https://en.wikipedia.org/wiki/Geodesics_on_an_ellipsoid">
 *         Geodesics on an ellipsoid</a>
 *     </li>
 * </ul>
 */
package com.irurueta.navigation.geodesic;