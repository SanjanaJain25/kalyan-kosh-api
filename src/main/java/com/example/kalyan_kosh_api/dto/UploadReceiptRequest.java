package com.example.kalyan_kosh_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UploadReceiptRequest {

    /*
     * Optional target user.
     *
     * For logged-in normal user upload:
     * - userId can be blank, backend will use logged-in user.
     *
     * For TeachersList / public Home upload:
     * - frontend will send selected teacher/member userId.
     */
    private String userId;

    /*
     * Optional mobile number fallback for public upload.
     * If userId is not available, backend can find user by mobile number.
     */
    private String mobileNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String referenceName;

    @NotBlank(message = "UTR number is required")
    private String utrNumber;
}