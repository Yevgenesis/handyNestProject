package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
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
    public PerformerResponseDto createPerformer(@RequestBody PerformerRequestDto performerDTO) {
        User user = userRepository.findById(performerDTO.getUserId()).orElseThrow(() -> new UserNotFoundException("Not Found User with id: " + performerDTO.getUserId()));
        Performer performer = new Performer();

        performer.setPhoneNumber(performerDTO.getPhoneNumber());
        performer.setDescription(performerDTO.getDescription());
        performer.setUser(user);

        Set<Category> categories = categoryRepository.findCategoriesByIdIn(performerDTO.getCategoryIDs());

        performer.setCategories(categories);
        Address address = addressMapper.dtoToAddress(performerDTO.getAddressDto());
        performer.setAddress(address);

        performer = performerRepository.save(performer);

        Performer performer1 = performerRepository.save(performer);
        return performerMapper.performerToDto(performer1);
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
