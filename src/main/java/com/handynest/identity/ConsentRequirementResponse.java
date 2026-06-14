package com.handynest.identity;

public record ConsentRequirementResponse(
    ConsentType type, String documentVersion, boolean requiredAtRegistration) {}
