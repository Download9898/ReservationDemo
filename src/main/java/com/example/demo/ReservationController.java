package com.example.demo;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class ReservationController {

    private final static Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }



    @GetMapping("/{id}") // чтобы обрабатывал входящие http запросы
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id
    ){
       log.info("Called getReservationById: id = "+ id);
       return ResponseEntity.status(HttpStatus.OK) // //тоже самое что и код 200
               .body(reservationService.getReservationById(id));
    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations(){
        log.info("Called getAllReservations");
        return ResponseEntity.ok( reservationService.findAllReservations());//тоже самое что и код 200
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservationToCreate //нужно взять из тела запроса
    ){
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)//тоже что и код 201
                        .header("test-header","123")
                        .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate
    ){
        log.info("Called updateReservation id={}, reservationToUpdate = {}", id, reservationToUpdate);
        var updated = reservationService.updateReservation(id,reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called deleteReservation id = {}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ){
        log.info("Called approveReservation id = {}", id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
