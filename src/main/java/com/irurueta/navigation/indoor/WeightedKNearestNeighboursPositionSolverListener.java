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

import com.irurueta.geometry.Point;

/**
 * Listener to be notified of events such as when position estimation
 * starts or ends.
 *
 * @param <P> a {@link Point} type.
 */
public interface WeightedKNearestNeighboursPositionSolverListener<P extends Point<?>> {

    /**
     * Called when solving starts.
     *
     * @param solver solver raising the event.
     */
    void onSolveStart(final WeightedKNearestNeighboursPositionSolver<P> solver);

    /**
     * Called when solving ends.
     *
     * @param solver solver raising the event.
     */
    void onSolveEnd(final WeightedKNearestNeighboursPositionSolver<P> solver);
}
