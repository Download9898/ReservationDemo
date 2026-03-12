package com.example.demo;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    //private final AtomicLong idCounter; // long который корректно работает в многопоточной среде

    private final static Logger log = LoggerFactory.getLogger(ReservationService.class);


    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository){
        this.repository = repository;
    }
    private Reservation toDomainReservation(ReservationEntity reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id =" + id));
        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream()
                .map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.status()!=null){
            throw new IllegalArgumentException("status should bu empty");
        }
        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalArgumentException("Start date must be 1 day earlier tan end date");
        }

        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );
        var savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id =\" + id"));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation: status = "+reservationEntity.getStatus());
        }
        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())){
            throw new IllegalArgumentException("Start date must be 1 day earlier tan end date");
        }
        var reservationToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updatedReservation = repository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        if(!repository.existsById(id)){
            throw new EntityNotFoundException("Not found reservation by id =\" + id");
        }
        var reservation = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found"+id));
        if(reservation.getStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Contact with manager");
        }
        if(reservation.getStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Reservation was cancelled");
        }
        repository.setStatus(id,ReservationStatus.CANCELLED);
        log.info("Successefullu canceled: id={}", id);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id =\" + id"));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status = "+reservationEntity.getStatus());
        }
        var isConflict = isReservationConflict(reservationEntity);
        if(isConflict){
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);
        return toDomainReservation(reservationEntity);
    }

    private boolean isReservationConflict(ReservationEntity reservation){
        var allReservations = repository.findAll();
        for(ReservationEntity existingReservation: allReservations){
            if(reservation.getId().equals(existingReservation.getId())){
                continue;
            }
            if(reservation.getRoomId().equals(existingReservation.getRoomId())){
                continue;
            }
            if(!existingReservation.getStatus().equals(ReservationStatus.APPROVED)){
                continue;
            }
            if(reservation.getStartDate().isBefore(existingReservation.getEndDate())
                    && existingReservation.getStartDate().isBefore(reservation.getEndDate())){
                    return true;
            }
        }
        return false;
    }
}
