package com.campusfind.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Claim Entity Tests")
class ClaimTest {

    @Test
    @DisplayName("toString() MUST NOT leak private verification response")
    void testClaimToStringRedaction() {
        String secretAnswer = "Initials DK scratched behind the flap";
        Claim claim = new Claim(1, "Deepak Kumar", "deepak@campus.edu",
                "Lost my calculator during physics exam", secretAnswer);

        String result = claim.toString();

        assertFalse(result.contains(secretAnswer),
                "Privacy violation: toString() revealed the confidential claimant answer!");
        assertTrue(result.contains("[REDACTED]"),
                "toString() should replace the verification response with [REDACTED].");
    }

    @Test
    @DisplayName("Default claim status is PENDING with timestamp populated")
    void testDefaultClaimState() {
        Claim claim = new Claim();
        assertEquals(ClaimStatus.PENDING, claim.getStatus());
        assertNotNull(claim.getSubmittedAt());
        assertNull(claim.getReviewedAt());
    }

    @Test
    @DisplayName("Authorized getter retrieves the verification response")
    void testAuthorizedResponseGetter() {
        Claim claim = new Claim(5, "Ananya", "ananya@campus.edu", "Dropped wallet", "Has student ID 445");
        assertEquals("Has student ID 445", claim.getVerificationResponse());
    }
}
