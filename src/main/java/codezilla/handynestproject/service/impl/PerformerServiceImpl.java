package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.PerformerNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.AddressMapper;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.model.entity.Address;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.service.PerformerService;
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
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AddressMapper addressMapper;


    @Override
    @Transactional
    public PerformerResponseDto create(@RequestBody PerformerRequestDto performerDTO) {
        User user = userRepository.findById(performerDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Not Found User with id: " + performerDTO.getUserId()));
        Set<Category> categories = categoryRepository.findCategoriesByIdIn(performerDTO.getCategoryIDs());

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
    public PerformerResponseDto update(PerformerRequestDto performerDto) {
        User user = userRepository.findById(performerDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Not Found User with id: " + performerDto.getUserId()));
        Set<Category> categories = categoryRepository.findCategoriesByIdIn(performerDto.getCategoryIDs());

        // ToDo тут должна быть проверка роли (performerRole)

        Performer performer = performerRepository.findById(performerDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Not Found Performer with id: " + performerDto.getUserId()));

        performer.setPhoneNumber(performerDto.getPhoneNumber());
        performer.setDescription(performerDto.getDescription());
        performer.setCategories(categories);

        Address address = performer.getAddress();
        address.setCity(performerDto.getAddressDto().getCity());
        address.setCountry(performerDto.getAddressDto().getCountry());
        address.setStreet(performerDto.getAddressDto().getStreet());
        address.setZip(performerDto.getAddressDto().getZip());

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
        Optional<Performer> performer = performerRepository.findById(id);
        PerformerResponseDto dtos = performerMapper.performerToDto(performer.get()); // ToDo exception
        return dtos;
    }

    @Override
    public PerformerResponseDto updateAvailability(Long id, boolean isPublish) {
        Performer performer = performerRepository.findById(id).orElseThrow(() -> new PerformerNotFoundException("Not Found Performer id: " + id));
        performer.setAvailable(isPublish);
        return performerMapper.performerToDto(performerRepository.save(performer));
    }

    // ---------------------------------------------------------------------------------

    @Override
    public void updateRating(Performer performer) {
        Double newRating = performerRepository.findAverageRatingByPerformerId(performer.getId());
        performer.setPositiveFeedbackPercent(newRating);
        performerRepository.save(performer);
    }

    @Override
    public void increaseTaskCounterUp(Performer performer) {
        performer.increaseTaskCounter();
        performerRepository.save(performer);
    }


}
