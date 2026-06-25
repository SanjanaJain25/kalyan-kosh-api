package com.example.kalyan_kosh_api.controller;

import com.example.kalyan_kosh_api.dto.PageResponse;
import com.example.kalyan_kosh_api.dto.PublicMemberListResponse;
import com.example.kalyan_kosh_api.dto.PublicSahyogListResponse;
import com.example.kalyan_kosh_api.service.MonthlySahyogService;
import com.example.kalyan_kosh_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicListController {

    private final UserService userService;
    private final MonthlySahyogService monthlySahyogService;

    public PublicListController(
            UserService userService,
            MonthlySahyogService monthlySahyogService) {
        this.userService = userService;
        this.monthlySahyogService = monthlySahyogService;
    }

    @GetMapping("/members/filter")
    public ResponseEntity<PageResponse<PublicMemberListResponse>> getPublicMembers(
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                userService.getPublicMembersFiltered(
                        sambhagId,
                        districtId,
                        blockId,
                        name,
                        mobile,
                        userId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/pending-profiles/filter")
    public ResponseEntity<PageResponse<PublicMemberListResponse>> getPublicPendingProfiles(
            @RequestParam(required = false) String sambhagId,
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String blockId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                userService.getPublicPendingProfileUsersFiltered(
                        sambhagId,
                        districtId,
                        blockId,
                        name,
                        mobile,
                        userId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/monthly-sahyog/donors/beneficiaries-all")
    public ResponseEntity<?> getPublicDonorBeneficiaries() {
        return ResponseEntity.ok(monthlySahyogService.getAllBeneficiaryOptions());
    }

    @GetMapping("/monthly-sahyog/non-donors/beneficiaries-all")
    public ResponseEntity<?> getPublicNonDonorBeneficiaries() {
        return ResponseEntity.ok(monthlySahyogService.getAllBeneficiaryOptions());
    }

    @GetMapping("/monthly-sahyog/donors/search-by-beneficiary")
    public ResponseEntity<PageResponse<PublicSahyogListResponse>> searchPublicDonorsByBeneficiary(
            @RequestParam(required = false) Long beneficiaryId,
            @RequestParam(required = false, defaultValue = "false") boolean openOnly,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sambhag,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String block,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                monthlySahyogService.searchPublicDonorsByBeneficiary(
                        beneficiaryId,
                        openOnly,
                        name,
                        mobile,
                        userId,
                        sambhag,
                        district,
                        block,
                        page,
                        size
                )
        );
    }

    @GetMapping("/monthly-sahyog/non-donors/search-by-beneficiary")
    public ResponseEntity<PageResponse<PublicMemberListResponse>> searchPublicNonDonorsByBeneficiary(
            @RequestParam(required = false) Long beneficiaryId,
            @RequestParam(required = false, defaultValue = "false") boolean openOnly,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sambhag,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String block,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                monthlySahyogService.searchPublicNonDonorsByBeneficiary(
                        beneficiaryId,
                        openOnly,
                        name,
                        mobile,
                        userId,
                        sambhag,
                        district,
                        block,
                        page,
                        size
                )
        );
    }

    @GetMapping("/monthly-sahyog/no-utr-ever")
public ResponseEntity<PageResponse<PublicMemberListResponse>> getPublicNoUtrEverUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

    return ResponseEntity.ok(
            monthlySahyogService.getPublicNoUtrEverUsers(page, size)
    );
}

@GetMapping("/monthly-sahyog/no-utr-ever/search")
public ResponseEntity<PageResponse<PublicMemberListResponse>> searchPublicNoUtrEverUsers(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String mobile,
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String sambhag,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String block,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

    return ResponseEntity.ok(
            monthlySahyogService.searchPublicNoUtrEverUsers(
                    name,
                    mobile,
                    userId,
                    sambhag,
                    district,
                    block,
                    page,
                    size
            )
    );
}
}