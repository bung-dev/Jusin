package com.jusin.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", unique = true, nullable = false, length = 8)
    private String companyId;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "stock_code", unique = true, nullable = false, length = 6)
    private String stockCode;

    @Column(length = 50)
    private String sector;

    @Column(name = "list_date")
    private LocalDate listDate;

    @Column(length = 100)
    private String representative;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String website;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Company(String companyId, String companyName, String stockCode,
                   String sector, LocalDate listDate, String representative,
                   String address, String website, String phoneNumber) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.stockCode = stockCode;
        this.sector = sector;
        this.listDate = listDate;
        this.representative = representative;
        this.address = address;
        this.website = website;
        this.phoneNumber = phoneNumber;
    }

    public void update(String companyName, String representative, String address) {
        this.companyName = companyName;
        this.representative = representative;
        this.address = address;
    }

    /**
     * 상세 정보 업데이트 (enrichment용)
     */
    public void updateDetail(String sector, LocalDate listDate, String representative,
                             String address, String website, String phoneNumber) {
        if (sector != null) this.sector = sector;
        if (listDate != null) this.listDate = listDate;
        if (representative != null) this.representative = representative;
        if (address != null) this.address = address;
        if (website != null) this.website = website;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
    }

    /**
     * 상세 정보가 필요한지 확인 (sector가 없으면 enrichment 필요)
     */
    public boolean needsEnrichment() {
        return sector == null || sector.isBlank();
    }
}
