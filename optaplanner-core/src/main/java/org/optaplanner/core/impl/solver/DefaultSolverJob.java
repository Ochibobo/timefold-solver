/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.solver;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverStatus;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}.
 */
public class DefaultSolverJob<Solution_, ProblemId_> implements SolverJob<Solution_, ProblemId_>, Callable<Solution_> {

    private final DefaultSolverManager<Solution_, ProblemId_> solverManager;
    private final ProblemId_ problemId;
    private final Solver<Solution_> solver;
    private final Function<ProblemId_, Solution_> problemFinder;
    private final Consumer<Solution_> finalBestSolutionConsumer;
    private final BiConsumer<ProblemId_, Throwable> exceptionHandler;

    private volatile SolverStatus solverStatus;

    public DefaultSolverJob(DefaultSolverManager<Solution_, ProblemId_> solverManager, ProblemId_ problemId,
            Solver<Solution_> solver,
            Function<ProblemId_, Solution_> problemFinder,
            Consumer<Solution_> finalBestSolutionConsumer,
            BiConsumer<ProblemId_, Throwable> exceptionHandler) {
        this.solverManager = solverManager;
        this.problemId = problemId;
        this.solver = solver;
        this.problemFinder = problemFinder;
        this.finalBestSolutionConsumer = finalBestSolutionConsumer;
        this.exceptionHandler = exceptionHandler;
        solverStatus = SolverStatus.SOLVING_SCHEDULED;
    }

    @Override
    public ProblemId_ getProblemId() {
        return problemId;
    }

    @Override
    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    @Override
    public Solution_ call() {
        solverStatus = SolverStatus.SOLVING_ACTIVE;
        try {
            Solution_ problem = problemFinder.apply(problemId);
            final Solution_ finalBestSolution = solver.solve(problem);
            if (finalBestSolutionConsumer != null) {
                // TODO consumption should happen on different thread than solver thread
                finalBestSolutionConsumer.accept(finalBestSolution);
            }
            return finalBestSolution;
        } catch (Exception e) {
            exceptionHandler.accept(problemId, e);
            throw new IllegalStateException("Solving failed for problemId (" + problemId + ").", e);
        } finally {
            // TODO What happens if this code throws an exception? Is it eaten?
            solverManager.getProblemIdToSolverJobMap().remove(problemId);
            solverStatus = SolverStatus.NOT_SOLVING; // TODO FIXME race condition, lock on problemId
        }
    }

    // TODO Future features
//    @Override
//    public void reloadProblem(Function<ProblemId_, Solution_> problemFinder) {
//        throw new UnsupportedOperationException("The solver is still solving and reloadProblem() is not yet supported.");
//    }

    // TODO Future features
//    @Override
//    public void addProblemFactChange(ProblemFactChange<Solution_> problemFactChange) {
//        solver.addProblemFactChange(problemFactChange);
//    }

    @Override
    public void terminateEarly() {
        solver.terminateEarly();
    }

}