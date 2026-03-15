package com.example.demo.reservations.avaliablity;

import com.example.demo.reservations.ReservationRepository;
import com.example.demo.reservations.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {
    private final ReservationRepository repository;
    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ){
        if(!endDate.isAfter(startDate)){
            throw new IllegalArgumentException("Start date must be 1 day earlier tan end date");
        }
        List<Long> condlictingIds = repository.findConflictReservations(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );
        if(condlictingIds.isEmpty()){
            return false;
        }
        log.info("Conflicting with: ids = {}",condlictingIds);
        return true;
    }
}
