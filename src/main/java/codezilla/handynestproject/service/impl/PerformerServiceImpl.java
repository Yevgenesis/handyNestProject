package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PerformerServiceImpl implements PerformerService {

    private final PerformerRepository performerRepository;
    private final PerformerMapper performerMapper;

    @Override
    public PerformerResponseDto createPerformer(@RequestBody PerformerRequestDto performerDto) {
        Performer newPerformer = performerMapper.dtoToPerformer(performerDto);
//        Performer performer = new Performer();
//        performer.setId(performerDto.getUserId());
//        performer.setPhoneNumber(performerDto.getPhoneNumber());
//        performer.setDescription(performerDto.getDescription());
//
//        // Создание множества категорий
//        Set<Category> categories = new HashSet<>();
//        for (Long categoryId : performerDto.getCategoryIDs()) {
//            Category category = new Category();
//            category.setId(categoryId);
//            categories.add(category);
//        }
//        performer.setCategories(categories);
//
//        // Создание адреса
//        AddressDto addressDto = performerDto.getAddressDto();
//        if (addressDto != null) {
//            Address address = new Address();
//            address.setStreet(addressDto.getStreet());
//            address.setCity(addressDto.getCity());
//            address.setZip(addressDto.getZip());
//            address.setCountry(addressDto.getCountry());
//            performer.setAddress(address);
//        }
        return performerMapper.performerToDto(performerRepository.save(newPerformer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformerResponseDto> getPerformers() {
        List<Performer> performers = performerRepository.findAll();
        List<PerformerResponseDto> dtos = performerMapper.performersToListDto(performers);
        return dtos;
    }


    @Override
    public PerformerResponseDto getPerformerById(Long id) {
        Optional<Performer> performer = performerRepository.findById(id);
        PerformerResponseDto dtos = performerMapper.performerToDto(performer.get()); // ToDo exception
        return dtos;
    }

}
