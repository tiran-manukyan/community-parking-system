package tir.parkingsystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import tir.parkingsystem.entity.domain.SpotBookingEntity;
import tir.parkingsystem.entity.dto.request.CreateBookingRequest;
import tir.parkingsystem.entity.dto.response.BookingResponse;

import java.math.BigDecimal;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "spot", ignore = true)
    @Mapping(target = "status", constant = "BOOKED")
    SpotBookingEntity toEntity(long userId, CreateBookingRequest request, long spotId, BigDecimal parkingCost);

    BookingResponse toResponse(SpotBookingEntity entity);
}
