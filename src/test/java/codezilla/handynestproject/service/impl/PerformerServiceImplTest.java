package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.exception.CategoryNotFoundException;
import codezilla.handynestproject.exception.UserNotFoundException;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.repository.PerformerRepository;
import codezilla.handynestproject.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@SpringBootTest
public class PerformerServiceImplTest {

    @Mock
    private PerformerRepository performerRepository;

    @Mock
    private PerformerMapper performerMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PerformerServiceImpl performerService;

    private PerformerRequestDto performerDto;
    private User user;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId(1L);
        user.setDeleted(false);
        user.setEmail("test@test.com");
        user.setFirstName("TestName");
        user.setLastName("TestLastName");
        user.setPassword("password");
        user.setCreated_on(new Timestamp(System.currentTimeMillis()));
        user.setUpdated_on(new Timestamp(System.currentTimeMillis()));
        user.setEmailVerified(true);
        this.user = user;

        // Подготавливаем данные для каждого теста
        performerDto = new PerformerRequestDto();
        performerDto.setUserId(1L); // Устанавливаем id пользователя
        performerDto.setPhoneNumber("123456789");
        performerDto.setDescription("Test performer description");
        performerDto.setCategoryIDs(Arrays.asList(2L, 15L)); // Устанавливаем id категории
    }

    @Test
    public void testCreatePerformer_Success() {
        // Arrange

        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));

        Category category = new Category();
        category.setId(12L);
        category.setName("Test Дизайн интерьера");
        category.setParentId(1L);
        category.setWeight(1);

        Mockito.when(categoryRepository.findCategoriesByIdIn(Mockito.any())).thenReturn(Collections.singleton(category));

        Performer performer = new Performer();
        Mockito.when(performerRepository.save(Mockito.any())).thenReturn(performer);

        PerformerResponseDto responseDto = new PerformerResponseDto();
        Mockito.when(performerMapper.performerToDto(Mockito.any())).thenReturn(responseDto);

        // Act
        PerformerResponseDto result = performerService.createPerformer(performerDto);

        // Assert
        Assertions.assertNotNull(result);
        // Дополните проверки на ожидаемый результат, если необходимо
    }


    @Test
    public void testCreatePerformer_UserNotFound() {
        // Arrange
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(UserNotFoundException.class, () -> performerService.createPerformer(performerDto));
    }

    @Test
    public void testCreatePerformer_CategoryNotFound() {
        // Arrange
        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new User()));
        Mockito.when(categoryRepository.findCategoriesByIdIn(Mockito.any())).thenReturn(Collections.emptySet());

        // Act & Assert
        Assertions.assertThrows(CategoryNotFoundException.class, () -> performerService.createPerformer(performerDto));
    }


    // Дополните тесты для других методов аналогичным образом

}
