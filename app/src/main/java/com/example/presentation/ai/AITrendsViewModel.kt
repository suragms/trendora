package com.example.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TrendAnalysisResult
import com.example.domain.model.TrendItem
import com.example.domain.repository.AIRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AITrendsUiState(
    val dailySummary: String = "",
    val aiTrends: List<TrendItem> = emptyList(),
    val isAskingAI: Boolean = false,
    val aiAssistantQuery: String = "",
    val aiAssistantAnswer: String? = null,
    val selectedTrendForAnalysis: TrendItem? = null,
    val isLoading: Boolean = false,
    // Structured analysis state
    val selectedAnalysis: TrendAnalysisResult? = null,
    val isAnalyzing: Boolean = false,
    val analysisError: String? = null,
    val analysisStep: AnalysisStep = AnalysisStep.IDLE,
    // Dashboard insights
    val overallSentiment: String = "Loading...",
    val risingTrendCount: Int = 0,
    val highViralCount: Int = 0,
    val avgConfidence: Int = 0,
    // Config state
    val isConfigured: Boolean = false
)

/** Descriptive steps shown during the analysis animation. */
enum class AnalysisStep(val label: String) {
    IDLE(""),
    COLLECTING("Collecting information..."),
    UNDERSTANDING("Understanding discussions..."),
    DETECTING("Detecting sentiment..."),
    PREDICTING("Predicting growth..."),
    COMPLETE("Analysis complete!")
}

private data class AIAssistantState(
    val isAskingAI: Boolean,
    val query: String,
    val answer: String?,
    val selectedTrend: TrendItem?
)

class AITrendsViewModel(
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _isAskingAI = MutableStateFlow(false)
    private val _aiAssistantQuery = MutableStateFlow("")
    private val _aiAssistantAnswer = MutableStateFlow<String?>(null)
    private val _selectedTrend = MutableStateFlow<TrendItem?>(null)
    private val _selectedAnalysis = MutableStateFlow<TrendAnalysisResult?>(null)
    private val _isAnalyzing = MutableStateFlow(false)
    private val _analysisError = MutableStateFlow<String?>(null)
    private val _analysisStep = MutableStateFlow(AnalysisStep.IDLE)

    private val assistantStateFlow = combine(
        _isAskingAI,
        _aiAssistantQuery,
        _aiAssistantAnswer,
        _selectedTrend
    ) { isAsking, query, answer, selected ->
        AIAssistantState(isAsking, query, answer, selected)
    }

    val uiState: StateFlow<AITrendsUiState> = combine(
        aiRepository.getDailyAISummary(),
        aiRepository.getAITrendsOverview(),
        assistantStateFlow
    ) { summary, trends, assistantState ->
        val selectedTrend = assistantState.selectedTrend ?: trends.firstOrNull()

        AITrendsUiState(
            dailySummary = summary,
            aiTrends = trends,
            isAskingAI = assistantState.isAskingAI,
            aiAssistantQuery = assistantState.query,
            aiAssistantAnswer = assistantState.answer,
            selectedTrendForAnalysis = selectedTrend,
            isLoading = false,
            selectedAnalysis = _selectedAnalysis.value,
            isAnalyzing = _isAnalyzing.value,
            analysisError = _analysisError.value,
            analysisStep = _analysisStep.value,
            overallSentiment = computeOverallSentiment(trends),
            risingTrendCount = trends.count { it.isTrendingUp },
            highViralCount = trends.count { it.aiAnalysis.viralProbabilityPercent >= 70 },
            avgConfidence = if (trends.isNotEmpty()) {
                trends.map { it.aiAnalysis.aiConfidencePercent }.average().toInt()
            } else 0,
            isConfigured = aiRepository.isConfigured
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AITrendsUiState(isLoading = true)
    )

    fun onSelectTrendForAnalysis(trend: TrendItem) {
        _selectedTrend.value = trend
        // Clear previous analysis when selecting a new trend
        _selectedAnalysis.value = null
        _analysisError.value = null
    }

    fun onQueryChanged(query: String) {
        _aiAssistantQuery.value = query
    }

    fun askAIAssistant(prompt: String, trendTitle: String? = null) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isAskingAI.value = true
            _aiAssistantQuery.value = prompt
            val answer = aiRepository.askAIAssistant(prompt, trendTitle)
            _aiAssistantAnswer.value = answer
            _isAskingAI.value = false
        }
    }

    /**
     * Run structured AI analysis on the selected trend.
     * Shows animated steps while the analysis is in progress.
     */
    fun analyzeSelectedTrend(forceRefresh: Boolean = false) {
        val trend = _selectedTrend.value ?: return
        if (_isAnalyzing.value) return // Prevent duplicate requests

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisError.value = null
            _analysisStep.value = AnalysisStep.COLLECTING

            // Animate through steps while the real API call happens
            val steps = listOf(
                AnalysisStep.COLLECTING,
                AnalysisStep.UNDERSTANDING,
                AnalysisStep.DETECTING,
                AnalysisStep.PREDICTING
            )

            // Run the real analysis first, then animate steps — keeps the UX
            // smooth without an unscoped `async` inside another flow scope.
            try {
                val analysisResult: TrendAnalysisResult = aiRepository.analyzeTrendStructured(
                    trendId = trend.id,
                    trendTitle = trend.title,
                    category = trend.category.displayName,
                    headlines = trend.relatedNews.map { it.headline },
                    descriptions = trend.relatedNews.map { it.summary },
                    trendScore = trend.score.totalScore,
                    growthInfo = "+${trend.growthPercentage}%",
                    forceRefresh = forceRefresh
                )
                // Animate steps while "presenting" the result
                for (step in steps) {
                    _analysisStep.value = step
                    kotlinx.coroutines.delay(180)
                }
                _selectedAnalysis.value = analysisResult
                _analysisStep.value = AnalysisStep.COMPLETE
                kotlinx.coroutines.delay(500)
                _analysisStep.value = AnalysisStep.IDLE
            } catch (e: Exception) {
                _analysisError.value = "Analysis failed: ${e.message ?: "Unknown error"}"
                _analysisStep.value = AnalysisStep.IDLE
            }

            _isAnalyzing.value = false
        }
    }

    fun clearAnalysis() {
        _selectedAnalysis.value = null
        _analysisError.value = null
        _analysisStep.value = AnalysisStep.IDLE
    }

    fun clearAIAnswer() {
        _aiAssistantAnswer.value = null
        _aiAssistantQuery.value = ""
    }

    fun refreshDailySummary() {
        viewModelScope.launch {
            aiRepository.refreshDailySummary()
        }
    }

    private fun computeOverallSentiment(trends: List<TrendItem>): String {
        if (trends.isEmpty()) return "No data"
        val avgPositive = trends.map { it.aiAnalysis.sentiment.positivePercent }.average()
        return when {
            avgPositive >= 70 -> "😊 Predominantly Positive"
            avgPositive >= 50 -> "😐 Mixed"
            avgPositive >= 30 -> "🤔 Leaning Negative"
            else -> "😟 Predominantly Negative"
        }
    }
}
