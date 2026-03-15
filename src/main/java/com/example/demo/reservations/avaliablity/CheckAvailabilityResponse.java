package com.example.demo.reservations.avaliablity;

import org.springframework.boot.availability.AvailabilityState;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {
}
