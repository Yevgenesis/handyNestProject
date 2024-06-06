package codezilla.handynestproject.mapper;


import codezilla.handynestproject.dto.address.AddressDto;
import codezilla.handynestproject.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDto addressToDto(Address address);

    @Mapping(target = "id", ignore = true)
    Address dtoToAddress(AddressDto addressDto);

    List<AddressDto> addressesToListDto(List<Address> addresses);

    List<Address> dtoToListAddresses(List<AddressDto> addressDtos);
}
