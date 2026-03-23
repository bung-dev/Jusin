package com.jusin.controller;

import com.jusin.dto.response.ApiResponse;
import com.jusin.dto.response.CorpCodeSyncResponse;
import com.jusin.service.AdminSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminSyncService adminSyncService;

    @GetMapping("/sync/corp-codes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CorpCodeSyncResponse>> syncCorpCodes() {
        log.info("기업코드 전체 동기화 요청");
        CorpCodeSyncResponse result = adminSyncService.syncCorpCodes();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
