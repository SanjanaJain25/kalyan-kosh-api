package com.example.kalyan_kosh_api.dto;

import com.example.kalyan_kosh_api.entity.Receipt;
import com.example.kalyan_kosh_api.entity.ReceiptStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class AdminReceiptResponse {

    /*
     * Receipt information
     */
    private Long receiptId;
    private String utrNumber;
    private String referenceName;
    private LocalDate paymentDate;
    private Instant uploadedAt;
    private double amount;
    private ReceiptStatus status;

    /*
     * User/member information
     *
     * This tells the admin which member uploaded/submitted the UTR.
     */
    private String regNo;
    private String departmentUniqueId;
    private String name;
    private String mobileNumber;

    /*
     * Member location and department
     */
    private String sambhag;
    private String district;
    private String block;
    private String department;

    /*
     * Death case information
     */
    private Long deathCaseId;
    private String beneficiary;

    public static AdminReceiptResponse from(Receipt receipt) {

        AdminReceiptResponse response = new AdminReceiptResponse();

        /*
         * Receipt details
         */
        response.setReceiptId(receipt.getId());
        response.setUtrNumber(receipt.getUtrNumber());
        response.setReferenceName(receipt.getReferenceName());
        response.setPaymentDate(receipt.getPaymentDate());
        response.setUploadedAt(receipt.getUploadedAt());
        response.setAmount(receipt.getAmount());
        response.setStatus(receipt.getStatus());

        /*
         * User/member details
         */
        if (receipt.getUser() != null) {

            response.setRegNo(receipt.getUser().getId());
            response.setDepartmentUniqueId(
                    receipt.getUser().getDepartmentUniqueId()
            );
            response.setMobileNumber(
                    receipt.getUser().getMobileNumber()
            );
            response.setDepartment(
                    receipt.getUser().getDepartment()
            );

            String firstName = receipt.getUser().getName() == null
                    ? ""
                    : receipt.getUser().getName().trim();

            String surname = receipt.getUser().getSurname() == null
                    ? ""
                    : receipt.getUser().getSurname().trim();

            response.setName(
                    (firstName + " " + surname).trim()
            );

            /*
             * Member location details
             */
            if (receipt.getUser().getDepartmentSambhag() != null) {
                response.setSambhag(
                        receipt.getUser()
                                .getDepartmentSambhag()
                                .getName()
                );
            }

            if (receipt.getUser().getDepartmentDistrict() != null) {
                response.setDistrict(
                        receipt.getUser()
                                .getDepartmentDistrict()
                                .getName()
                );
            }

            if (receipt.getUser().getDepartmentBlock() != null) {
                response.setBlock(
                        receipt.getUser()
                                .getDepartmentBlock()
                                .getName()
                );
            }
        }

        /*
         * Death case/beneficiary details
         */
        if (receipt.getDeathCase() != null) {
            response.setDeathCaseId(
                    receipt.getDeathCase().getId()
            );

            response.setBeneficiary(
                    receipt.getDeathCase().getDeceasedName()
            );
        }

        return response;
    }
}