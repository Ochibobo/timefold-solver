package ai.timefold.solver.core.api.score.buildin.bendable;

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight;
import ai.timefold.solver.core.api.score.holder.ScoreHolder;

import org.kie.api.runtime.rule.RuleContext;

/**
 *
 * @see BendableScore
 * @deprecated Score DRL is deprecated and will be removed in a future major version of Timefold.
 *             See <a href="https://timefold.ai/docs/">DRL to
 *             Constraint Streams migration recipe</a>.
 */
@Deprecated(forRemoval = true)
public interface BendableScoreHolder extends ScoreHolder<BendableScore> {

    int getHardLevelsSize();

    int getSoftLevelsSize();

    /**
     * Penalize a match by the {@link ConstraintWeight} negated and multiplied with the weightMultiplier for all score levels.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param weightMultiplier at least 0
     */
    void penalize(RuleContext kcontext, int weightMultiplier);

    /**
     * Penalize a match by the {@link ConstraintWeight} negated and multiplied with the specific weightMultiplier per score
     * level.
     * Slower than {@link #penalize(RuleContext, int)}.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeightsMultiplier elements at least 0
     * @param softWeightsMultiplier elements at least 0
     */
    void penalize(RuleContext kcontext, int[] hardWeightsMultiplier, int[] softWeightsMultiplier);

    /**
     * Reward a match by the {@link ConstraintWeight} multiplied with the weightMultiplier for all score levels.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param weightMultiplier at least 0
     */
    void reward(RuleContext kcontext, int weightMultiplier);

    /**
     * Reward a match by the {@link ConstraintWeight} multiplied with the specific weightMultiplier per score level.
     * Slower than {@link #reward(RuleContext, int)}.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeightsMultiplier elements at least 0
     * @param softWeightsMultiplier elements at least 0
     */
    void reward(RuleContext kcontext, int[] hardWeightsMultiplier, int[] softWeightsMultiplier);

    void impactScore(RuleContext kcontext, int weightMultiplier);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param hardLevel {@code 0 <= hardLevel <} {@link #getHardLevelsSize()}.
     *        The {@code scoreLevel} is {@code hardLevel} for hard levels and {@code softLevel + hardLevelSize} for soft levels.
     * @param weight higher is better, negative for a penalty, positive for a reward
     */
    void addHardConstraintMatch(RuleContext kcontext, int hardLevel, int weight);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param softLevel {@code 0 <= softLevel <} {@link #getSoftLevelsSize()}.
     *        The {@code scoreLevel} is {@code hardLevel} for hard levels and {@code softLevel + hardLevelSize} for soft levels.
     * @param weight higher is better, negative for a penalty, positive for a reward
     */
    void addSoftConstraintMatch(RuleContext kcontext, int softLevel, int weight);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeights never null, array of length {@link #getHardLevelsSize()}
     * @param softWeights never null, array of length {@link #getSoftLevelsSize()}
     */
    void addMultiConstraintMatch(RuleContext kcontext, int[] hardWeights, int[] softWeights);
}
