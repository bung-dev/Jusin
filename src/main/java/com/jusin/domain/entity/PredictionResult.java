package com.jusin.domain.entity;

import com.jusin.domain.enums.Signal;
import com.jusin.domain.enums.SignalLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, length = 8)
    private String companyId;

    @Column(nullable = false, length = 10)
    private String period;

    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "`signal`", length = 20)
    private Signal signal;

    @Enumerated(EnumType.STRING)
    @Column(name = "`signal_level`", length = 20)
    private SignalLevel signalLevel;

    @Column(length = 10)
    private String emoji;

    private Integer perScore;
    private Integer roeScore;
    private Integer debtRatioScore;
    private Integer epsGrowthScore;
    private Integer pbrScore;
    private Integer operatingMarginScore;
    private Integer currentRatioScore;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Builder
    public PredictionResult(String companyId, String period, Integer totalScore,
                             Signal signal, SignalLevel signalLevel, String emoji,
                             Integer perScore, Integer roeScore, Integer debtRatioScore,
                             Integer epsGrowthScore, Integer pbrScore,
                             Integer operatingMarginScore, Integer currentRatioScore) {
        this.companyId = companyId;
        this.period = period;
        this.totalScore = totalScore;
        this.signal = signal;
        this.signalLevel = signalLevel;
        this.emoji = emoji;
        this.perScore = perScore;
        this.roeScore = roeScore;
        this.debtRatioScore = debtRatioScore;
        this.epsGrowthScore = epsGrowthScore;
        this.pbrScore = pbrScore;
        this.operatingMarginScore = operatingMarginScore;
        this.currentRatioScore = currentRatioScore;
        this.calculatedAt = LocalDateTime.now();
    }
}
