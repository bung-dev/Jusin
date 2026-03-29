package com.jusin.service;

import com.jusin.domain.entity.Company;
import com.jusin.domain.entity.FinancialIndicator;
import com.jusin.domain.entity.FinancialStatement;
import com.jusin.domain.entity.PredictionResult;
import com.jusin.domain.enums.Signal;
import com.jusin.domain.enums.SignalLevel;
import com.jusin.dto.response.AnalysisResponse;
import com.jusin.dto.response.IndicatorDto;
import com.jusin.repository.CompanyRepository;
import com.jusin.repository.FinancialIndicatorRepository;
import com.jusin.repository.FinancialStatementRepository;
import com.jusin.repository.PredictionResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionService {

    private final FinancialStatementRepository fsRepository;
    private final FinancialIndicatorRepository indicatorRepository;
    private final PredictionResultRepository predictionRepository;
    private final CompanyRepository companyRepository;
    private final IndicatorCalculationService indicatorService;
    private final IndicatorScoreCalculator scoreCalculator;

    @Transactional
    public AnalysisResponse analyzeAndPredict(String stockCode, String companyId, String period) {

        Optional<PredictionResult> cached = predictionRepository
                .findByCompanyIdAndPeriod(companyId, period);
        if (cached.isPresent()) {
            PredictionResult pr = cached.get();
            if (pr.getTotalScore() > 0) {
                log.debug("예측 결과 캐시 히트: companyId={}, period={}", companyId, period);
                FinancialIndicator cachedIndicator = indicatorRepository
                        .findByCompanyIdAndPeriod(companyId, period).orElse(null);
                return buildAnalysisResponse(stockCode, companyId, period, pr, cachedIndicator);
            }
            log.info("캐시 무효화 (score=0): companyId={}, period={}", companyId, period);
            predictionRepository.delete(pr);
            predictionRepository.flush();
        }

        FinancialIndicator indicator = indicatorRepository
                .findByCompanyIdAndPeriod(companyId, period)
                .filter(ind -> !isStaleIndicator(ind))
                .orElseGet(() -> indicatorService.calculateAndSave(companyId, period));

        ScoreBreakdown breakdown = calculateScores(indicator);
        SignalResult signalResult = mapSignal(breakdown.totalScore());

        PredictionResult result = PredictionResult.builder()
                .companyId(companyId)
                .period(period)
                .totalScore(breakdown.totalScore())
                .signal(signalResult.signal())
                .signalLevel(signalResult.level())
                .emoji(signalResult.emoji())
                .perScore(breakdown.perScore())
                .roeScore(breakdown.roeScore())
                .debtRatioScore(breakdown.debtRatioScore())
                .epsGrowthScore(breakdown.epsGrowthScore())
                .pbrScore(breakdown.pbrScore())
                .operatingMarginScore(breakdown.operatingMarginScore())
                .currentRatioScore(breakdown.currentRatioScore())
                .build();

        predictionRepository.save(result);
        log.info("예측 결과 저장: companyId={}, period={}, score={}, signal={}",
                companyId, period, breakdown.totalScore(), signalResult.signal());

        return buildAnalysisResponse(stockCode, companyId, period, result, indicator);
    }

    private boolean isStaleIndicator(FinancialIndicator ind) {
        return ind.getRoe() == null && ind.getEps() == null && ind.getOperatingMargin() == null;
    }

    private ScoreBreakdown calculateScores(FinancialIndicator ind) {
        int perScore             = scoreCalculator.scorePer(ind.getPer());
        int roeScore             = scoreCalculator.scoreRoe(ind.getRoe());
        int debtRatioScore       = scoreCalculator.scoreDebtRatio(ind.getDebtRatio());
        int epsGrowthScore       = scoreCalculator.scoreEpsGrowth(ind.getEpsGrowth());
        int pbrScore             = scoreCalculator.scorePbr(ind.getPbr());
        int operatingMarginScore = scoreCalculator.scoreOperatingMargin(ind.getOperatingMargin());
        int currentRatioScore    = scoreCalculator.scoreCurrentRatio(ind.getCurrentRatio());

        int total = perScore + roeScore + debtRatioScore + epsGrowthScore
                + pbrScore + operatingMarginScore + currentRatioScore;

        return new ScoreBreakdown(total, perScore, roeScore, debtRatioScore,
                epsGrowthScore, pbrScore, operatingMarginScore, currentRatioScore);
    }

    private SignalResult mapSignal(int score) {
        if (score >= 80) return new SignalResult(Signal.UP,      SignalLevel.STRONG,  "🟢", "강한 상승 신호");
        if (score >= 60) return new SignalResult(Signal.UP,      SignalLevel.WEAK,    "🟡", "약한 상승 신호");
        if (score >= 40) return new SignalResult(Signal.NEUTRAL, SignalLevel.NEUTRAL, "⚪", "중립 신호");
        if (score >= 20) return new SignalResult(Signal.DOWN,    SignalLevel.WEAK,    "🟠", "약한 하락 신호");
        return           new SignalResult(Signal.DOWN,    SignalLevel.STRONG,  "🔴", "강한 하락 신호");
    }

    private AnalysisResponse buildAnalysisResponse(String stockCode, String companyId,
                                                     String period, PredictionResult result,
                                                     FinancialIndicator indicator) {
        String companyName = companyRepository.findByCompanyId(companyId)
                .map(Company::getCompanyName)
                .orElse("알 수 없음");

        AnalysisResponse.FinancialDataDto financialData = fsRepository
                .findByCompanyIdAndPeriod(companyId, period)
                .map(fs -> AnalysisResponse.FinancialDataDto.builder()
                        .period(fs.getPeriod())
                        .revenue(formatAmount(fs.getRevenue()))
                        .operatingIncome(formatAmount(fs.getOperatingIncome()))
                        .netIncome(formatAmount(fs.getNetIncome()))
                        .totalAssets(formatAmount(fs.getTotalAssets()))
                        .equity(formatAmount(fs.getEquity()))
                        .build())
                .orElse(null);

        Map<String, IndicatorDto> indicators = indicator != null
                ? buildIndicatorMap(result, indicator)
                : new HashMap<>();

        return AnalysisResponse.builder()
                .companyName(companyName)
                .stockCode(stockCode)
                .analysisDate(LocalDate.now().toString())
                .lastDataDate(period)
                .prediction(AnalysisResponse.PredictionDto.builder()
                        .signal(result.getSignal().name())
                        .signalLevel(result.getSignalLevel().name())
                        .score(result.getTotalScore())
                        .scoreLevel(resolveScoreLevel(result))
                        .emoji(result.getEmoji())
                        .build())
                .indicators(indicators)
                .financialData(financialData)
                .notes(SCORE_NOTES)
                .build();
    }

    private Map<String, IndicatorDto> buildIndicatorMap(PredictionResult result,
                                                          FinancialIndicator ind) {
        Map<String, IndicatorDto> map = new HashMap<>();
        map.put("per",            IndicatorDto.of(ind.getPer(),            result.getPerScore(),            20, evaluateScore(result.getPerScore(),            20)));
        map.put("pbr",            IndicatorDto.of(ind.getPbr(),            result.getPbrScore(),            10, evaluateScore(result.getPbrScore(),            10)));
        map.put("roe",            IndicatorDto.of(ind.getRoe(),            result.getRoeScore(),            20, evaluateScore(result.getRoeScore(),            20)));
        map.put("debtRatio",      IndicatorDto.of(ind.getDebtRatio(),      result.getDebtRatioScore(),      15, evaluateScore(result.getDebtRatioScore(),      15)));
        map.put("eps",            IndicatorDto.of(ind.getEps(),            0,                               0,  "neutral"));
        map.put("epsGrowth",      IndicatorDto.of(ind.getEpsGrowth(),      result.getEpsGrowthScore(),      15, evaluateScore(result.getEpsGrowthScore(),      15)));
        map.put("operatingMargin",IndicatorDto.of(ind.getOperatingMargin(),result.getOperatingMarginScore(),10, evaluateScore(result.getOperatingMarginScore(),10)));
        map.put("currentRatio",   IndicatorDto.of(ind.getCurrentRatio(),   result.getCurrentRatioScore(),   10, evaluateScore(result.getCurrentRatioScore(),   10)));
        return map;
    }

    private String evaluateScore(int score, int maxScore) {
        double ratio = (double) score / maxScore;
        if (ratio >= 0.8) return "우수";
        if (ratio >= 0.5) return "양호";
        if (ratio > 0)    return "미흡";
        return "불량";
    }

    private static final List<String> SCORE_NOTES = List.of(
            "점수 기준: 80+ 강한상승 / 60+ 약한상승 / 40+ 중립 / 20+ 약한하락 / ~19 강한하락"
    );

    private String resolveScoreLevel(PredictionResult result) {
        Signal signal = result.getSignal();
        SignalLevel level = result.getSignalLevel();
        if (signal == Signal.UP && level == SignalLevel.STRONG) return "강한 상승 신호";
        if (signal == Signal.UP && level == SignalLevel.WEAK)   return "약한 상승 신호";
        if (signal == Signal.NEUTRAL)                           return "중립 신호";
        if (signal == Signal.DOWN && level == SignalLevel.WEAK) return "약한 하락 신호";
        return "강한 하락 신호";
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? amount.toPlainString() : null;
    }

    private record ScoreBreakdown(
            int totalScore, int perScore, int roeScore, int debtRatioScore,
            int epsGrowthScore, int pbrScore, int operatingMarginScore, int currentRatioScore) {}

    private record SignalResult(Signal signal, SignalLevel level, String emoji, String scoreLevel) {}
}
