package ai.timefold.solver.constraint.streams.common;

import java.math.BigDecimal;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.score.stream.Constraint;

public abstract class AbstractConstraint<Solution_, Constraint_ extends AbstractConstraint<Solution_, Constraint_, ConstraintFactory_>, ConstraintFactory_ extends InnerConstraintFactory<Solution_, Constraint_>>
        implements Constraint {

    private final ConstraintFactory_ constraintFactory;
    private final String constraintPackage;
    private final String constraintName;
    private final String constraintId;
    private final Function<Solution_, Score<?>> constraintWeightExtractor;
    private final ScoreImpactType scoreImpactType;
    private final boolean isConstraintWeightConfigurable;
    // Constraint is not generic in uni/bi/..., therefore these can not be typed.
    private final Object justificationMapping;
    private final Object indictedObjectsMapping;

    protected AbstractConstraint(ConstraintFactory_ constraintFactory, String constraintPackage, String constraintName,
            Function<Solution_, Score<?>> constraintWeightExtractor, ScoreImpactType scoreImpactType,
            boolean isConstraintWeightConfigurable, Object justificationMapping, Object indictedObjectsMapping) {
        this.constraintFactory = constraintFactory;
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        this.constraintId = ConstraintMatchTotal.composeConstraintId(constraintPackage, constraintName);
        this.constraintWeightExtractor = constraintWeightExtractor;
        this.scoreImpactType = scoreImpactType;
        this.isConstraintWeightConfigurable = isConstraintWeightConfigurable;
        this.justificationMapping = justificationMapping;
        this.indictedObjectsMapping = indictedObjectsMapping;
    }

    public final <Score_ extends Score<Score_>> Score_ extractConstraintWeight(Solution_ workingSolution) {
        if (isConstraintWeightConfigurable && workingSolution == null) {
            /*
             * In constraint verifier API, we allow for testing constraint providers without having a planning solution.
             * However, constraint weights may be configurable and in that case the solution is required to read the
             * weights from.
             * For these cases, we set the constraint weight to the softest possible value, just to make sure that the
             * constraint is not ignored.
             * The actual value is not used in any way.
             */
            return (Score_) constraintFactory.getSolutionDescriptor().getScoreDefinition().getOneSoftestScore();
        }
        var constraintWeight = (Score_) constraintWeightExtractor.apply(workingSolution);
        constraintFactory.getSolutionDescriptor().validateConstraintWeight(constraintPackage, constraintName, constraintWeight);
        return switch (scoreImpactType) {
            case PENALTY -> constraintWeight.negate();
            case REWARD, MIXED -> constraintWeight;
        };
    }

    public final void assertCorrectImpact(int impact) {
        if (impact >= 0) {
            return;
        }
        if (scoreImpactType != ScoreImpactType.MIXED) {
            throw new IllegalStateException("Negative match weight (" + impact + ") for constraint ("
                    + getConstraintId() + "). " +
                    "Check constraint provider implementation.");
        }
    }

    public final void assertCorrectImpact(long impact) {
        if (impact >= 0L) {
            return;
        }
        if (scoreImpactType != ScoreImpactType.MIXED) {
            throw new IllegalStateException("Negative match weight (" + impact + ") for constraint ("
                    + getConstraintId() + "). " +
                    "Check constraint provider implementation.");
        }
    }

    public final void assertCorrectImpact(BigDecimal impact) {
        if (impact.signum() >= 0) {
            return;
        }
        if (scoreImpactType != ScoreImpactType.MIXED) {
            throw new IllegalStateException("Negative match weight (" + impact + ") for constraint ("
                    + getConstraintId() + "). " +
                    "Check constraint provider implementation.");
        }
    }

    @Override
    public final ConstraintFactory_ getConstraintFactory() {
        return constraintFactory;
    }

    @Override
    public final String getConstraintPackage() {
        return constraintPackage;
    }

    @Override
    public final String getConstraintName() {
        return constraintName;
    }

    @Override
    public final String getConstraintId() { // Overridden in order to cache the string concatenation.
        return constraintId;
    }

    public final ScoreImpactType getScoreImpactType() {
        return scoreImpactType;
    }

    public <JustificationMapping_> JustificationMapping_ getJustificationMapping() {
        // It is the job of the code constructing the constraint to ensure that this cast is correct.
        return (JustificationMapping_) justificationMapping;
    }

    public <IndictedObjectsMapping_> IndictedObjectsMapping_ getIndictedObjectsMapping() {
        // It is the job of the code constructing the constraint to ensure that this cast is correct.
        return (IndictedObjectsMapping_) indictedObjectsMapping;
    }

}
