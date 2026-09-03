package com.example.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TrendAnalysisResult
import com.example.domain.model.TrendHistoryPoint
import com.example.domain.model.TrendItem
import com.example.domain.repository.AIRepository
import com.example.domain.repository.TrendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DetailTimeframe(val title: String) {
    TODAY("Today"),
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days")
}

/** Descriptive steps shown while the AI analyzes a trend. */
enum class DetailAnalysisStep(val label: String) {
    IDLE(""),
    COLLECTING("Collecting information..."),
    UNDERSTANDING("Understanding discussions..."),
    DETECTING("Detecting sentiment..."),
    PREDICTING("Predicting growth..."),
    COMPLETE("Analysis complete!")
}

data class TrendDetailUiState(
    val trend: TrendItem? = null,
    val selectedTimeframe: DetailTimeframe = DetailTimeframe.TODAY,
    val activeHistoryPoints: List<TrendHistoryPoint> = emptyList(),
    val isAnalyzingWithAI: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    // Structured AI analysis state
    val aiAnalysis: TrendAnalysisResult? = null,
    val aiAnalysisError: String? = null,
    val analysisStep: DetailAnalysisStep = DetailAnalysisStep.IDLE,
    val isAIConfigured: Boolean = false
)

class TrendDetailViewModel(
    private val trendRepository: TrendRepository,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendDetailUiState(isLoading = true))
    val uiState: StateFlow<TrendDetailUiState> = _uiState.asStateFlow()

    fun loadTrend(trendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val trend = trendRepository.getTrendById(trendId)
            if (trend != null) {
                trendRepository.recordTrendView(trend)

                // Load cached AI analysis if available (for offline/snappy display)
                val cachedAnalysis = aiRepository.getCachedAnalysis(trendId)

                _uiState.value = TrendDetailUiState(
                    trend = trend,
                    selectedTimeframe = DetailTimeframe.TODAY,
                    activeHistoryPoints = trend.historyToday.ifEmpty { listOf(TrendHistoryPoint("Now", trend.score.totalScore.toFloat())) },
                    isSaved = trend.isSaved,
                    isLoading = false,
                    aiAnalysis = cachedAnalysis,
                    isAIConfigured = aiRepository.isConfigured
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setTimeframe(timeframe: DetailTimeframe) {
        val currentTrend = _uiState.value.trend ?: return
        val points = when (timeframe) {
            DetailTimeframe.TODAY -> currentTrend.historyToday
            DetailTimeframe.SEVEN_DAYS -> currentTrend.history7Days
            DetailTimeframe.THIRTY_DAYS -> currentTrend.history30Days
        }
        _uiState.value = _uiState.value.copy(
            selectedTimeframe = timeframe,
            activeHistoryPoints = points
        )
    }

    fun toggleSave() {
        val currentTrend = _uiState.value.trend ?: return
        viewModelScope.launch {
            val newSavedState = trendRepository.toggleSaveTrend(currentTrend)
            _uiState.value = _uiState.value.copy(
                isSaved = newSavedState,
                trend = currentTrend.copy(isSaved = newSavedState)
            )
        }
    }

    /**
     * Run a structured AI analysis. Uses cache when fresh unless [forceRefresh]
     * is true. Shows animated progress steps. Prevents duplicate requests.
     */
    fun analyzeWithAI(forceRefresh: Boolean = false) {
        val currentTrend = _uiState.value.trend ?: return
        if (_uiState.value.isAnalyzingWithAI) return // prevent duplicate requests

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzingWithAI = true,
                aiAnalysisError = null,
                analysisStep = DetailAnalysisStep.COLLECTING
            )

            // Animate steps while the real request runs
            val steps = listOf(
                DetailAnalysisStep.COLLECTING,
                DetailAnalysisStep.UNDERSTANDING,
                DetailAnalysisStep.DETECTING,
                DetailAnalysisStep.PREDICTING
            )

            val analysisDeferred = kotlinx.coroutines.async {
                aiRepository.analyzeTrendStructured(
                    trendId = currentTrend.id,
                    trendTitle = currentTrend.title,
                    category = currentTrend.category.displayName,
                    headlines = currentTrend.relatedNews.map { it.headline },
                    descriptions = currentTrend.relatedNews.map { it.summary },
                    trendScore = currentTrend.score.totalScore,
                    growthInfo = "+${currentTrend.growthPercentage}%",
                    forceRefresh = forceRefresh
                )
            }

            for (step in steps) {
                _uiState.value = _uiState.value.copy(analysisStep = step)
                kotlinx.coroutines.delay(550)
            }

            try {
                val result = analysisDeferred.await()
                val updatedTrend = currentTrend.copy(aiAnalysis = result.toAIAnalysis())
                _uiState.value = _uiState.value.copy(
                    trend = updatedTrend,
                    aiAnalysis = result,
                    analysisStep = DetailAnalysisStep.COMPLETE,
                    isAnalyzingWithAI = false
                )
                kotlinx.coroutines.delay(400)
                _uiState.value = _uiState.value.copy(analysisStep = DetailAnalysisStep.IDLE)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzingWithAI = false,
                    aiAnalysisError = "AI analysis failed. Please try again.",
                    analysisStep = DetailAnalysisStep.IDLE
                )
            }
        }
    }

    /** Keep the existing refresh method working (used by the legacy deep-dive card). */
    fun refreshAIAnalysis() {
        analyzeWithAI(forceRefresh = true)
    }

    fun clearAIError() {
        _uiState.value = _uiState.value.copy(aiAnalysisError = null)
    }
}

/** Maps a structured result back to the existing [com.example.domain.model.AIAnalysis] model. */
private fun TrendAnalysisResult.toAIAnalysis(): com.example.domain.model.AIAnalysis {
    val positive = when (sentiment) {
        "Positive" -> 75
        "Mixed" -> 55
        "Negative" -> 25
        else -> 45
    }
    val negative = (100 - positive - 20).coerceIn(5, 30)
    val neutral = 100 - positive - negative

    return com.example.domain.model.AIAnalysis(
        summary = trendSummary.ifBlank { "AI analysis generated for this trend." },
        whyTrending = whyTrending.ifBlank { "Increased media coverage and public interest." },
        sentiment = com.example.domain.model.SentimentBreakdown(
            positivePercent = positive,
            neutralPercent = neutral,
            negativePercent = negative,
            summary = "Overall sentiment: $sentiment ($sentimentScore/100)"
        ),
        expectedGrowthPercent = predictedGrowth,
        viralProbabilityPercent = viralProbability,
        aiConfidencePercent = aiConfidence,
        growthTrajectory = when (growthPrediction) {
            "Rising" -> "Rapid Exponential"
            "Declining" -> "Sustained Peak"
            else -> "Steady Climb"
        },
        relatedTopics = relatedTopics,
        timeline = keyInsights
    )
}
