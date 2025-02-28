package com.github.togrul2.booklet.mappers;

import com.github.togrul2.booklet.dtos.reservation.ReservationRequestDto;
import com.github.togrul2.booklet.dtos.reservation.ReservationDto;
import com.github.togrul2.booklet.entities.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReservationMapper {
    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "user", ignore = true)
    Reservation toReservation(ReservationRequestDto reservationDto);

    ReservationDto toReservationDto(Reservation reservation);
}
