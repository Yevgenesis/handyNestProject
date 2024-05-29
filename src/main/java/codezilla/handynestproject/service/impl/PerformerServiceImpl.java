package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.PerformerNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.service.CategoryService;
import codezilla.handynestproject.service.PerformerService;
import codezilla.handynestproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class PerformerServiceImpl implements PerformerService {

    private final PerformerRepository performerRepository;
    private final PerformerMapper performerMapper;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AddressMapper addressMapper;


    @Override
    @Transactional
    public PerformerResponseDto create(@RequestBody PerformerRequestDto performerDTO) {
        User user = userService.findByIdReturnUser(performerDTO.getUserId());
        Set<Category> categories = categoryService.findCategoriesByIdIn(performerDTO.getCategoryIDs());

        // проверяем все ли категории нашлись
        if (categories.size() != performerDTO.getCategoryIDs().size())
            throw new CategoryNotFoundException("Wrong Category IDs");

        // ToDo оптимизировать запрос
        Performer performer = Performer.builder()
                .phoneNumber(performerDTO.getPhoneNumber())
                .description(performerDTO.getDescription())
                .user(user)
                .categories(categories)
                .address(addressMapper.dtoToAddress(performerDTO.getAddressDto()))
                .build();
        Performer responsePerformer = performerRepository.save(performer);
        return performerMapper.performerToDto(responsePerformer);
    }

    @Override
    @Transactional
    public PerformerResponseDto update(PerformerUpdateRequestDto updateDto) {
        User user = userService.findByIdReturnUser(updateDto.getPerformerId());

        // ToDo тут должна быть проверка роли (performerRole)

        Set<Category> categories = categoryService.findCategoriesByIdIn(updateDto.getCategoryIDs());

        Performer performer = performerRepository.findById(updateDto.getPerformerId())
                .orElseThrow(() -> new UserNotFoundException("Not Found Performer with id: " + updateDto.getPerformerId()));

        performer.setPhoneNumber(updateDto.getPhoneNumber());
        performer.setDescription(updateDto.getDescription());
        performer.setCategories(categories);

        Address address = performer.getAddress();
        address.setCity(updateDto.getAddressDto().getCity());
        address.setCountry(updateDto.getAddressDto().getCountry());
        address.setStreet(updateDto.getAddressDto().getStreet());
        address.setZip(updateDto.getAddressDto().getZip());

        Performer responsePerformer = performerRepository.save(performer);
        return performerMapper.performerToDto(responsePerformer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformerResponseDto> findAll() {
        List<Performer> performers = performerRepository.findAll();
        List<PerformerResponseDto> dtos = performerMapper.performersToListDto(performers);
        return dtos;
    }


    @Override
    @Transactional(readOnly = true)
    public PerformerResponseDto findById(Long id) {
        Performer performer = performerRepository.findById(id)
                .orElseThrow(() -> new PerformerNotFoundException("Not Found Performer id: " + id));
        return performerMapper.performerToDto(performer);
    }

    @Override
    public Performer findByIdReturnPerformer(Long id) {
        return performerRepository.findById(id)
                .orElseThrow(() -> new PerformerNotFoundException("Not Found Performer id: " + id));
    }

    @Override
    public PerformerResponseDto updateAvailability(Long id, boolean isPublish) {
        Performer performer = performerRepository.findById(id)
                .orElseThrow(() -> new PerformerNotFoundException("Not Found Performer id: " + id));
        performer.setAvailable(isPublish);
        return performerMapper.performerToDto(performerRepository.save(performer));
    }

    @Override
    public boolean existsById(Long id) {
        return performerRepository.existsById(id);
    }

    // ---------------------------------------------------------------------------------

    @Override
    public void updateRating(Performer performer) {
        Double newRating = performerRepository.getRatingByPerformerId(performer.getId());
        performer.setPositiveFeedbackPercent(newRating);
        performerRepository.save(performer);
    }

    @Override
    public void increaseTaskCounterUp(Performer performer) {
        performer.increaseTaskCounter();
        performerRepository.save(performer);
    }


}
