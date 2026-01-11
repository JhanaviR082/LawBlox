package com.lawblox.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalCaseSuggestion {
    private String caseName;
    private String caseUrl;
    private String relevance;
    private String legalDomain;
    private String actionAdvice;
    private String keyTakeaway;      // Add this field
    private String domain;            // Add this field
    private String practicalAdvice;   
}