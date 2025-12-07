package com.example.erp.approvalrequest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ApprovalCreateRequest {

    @NotNull
    @Min(1)
    private Integer requesterId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotEmpty
    @Valid
    private List<StepRequest> steps;

    @Getter
    @NoArgsConstructor
    public static class StepRequest {

        @NotNull
        @Min(1)
        private Integer step;

        @NotNull
        @Min(1)
        private Integer approverId;
    }
}