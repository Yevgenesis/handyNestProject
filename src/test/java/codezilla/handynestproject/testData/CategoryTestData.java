package codezilla.handynestproject.testData;

import codezilla.handynestproject.dto.category.CategoryResponseDto;
import codezilla.handynestproject.dto.category.CategoryTitleDto;
import codezilla.handynestproject.model.entity.Category;

public class CategoryTestData {
    /**
     * CategoryTitleDto
     */

    public static final CategoryTitleDto CATEGORY_TITLE_DTO2 = new CategoryTitleDto(
            2L,
            "Строительство");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO4 = new CategoryTitleDto(
            4L,
            "Грузоперевозки");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO8 = new CategoryTitleDto(
            8L,
            "Дизайн интерьера");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO18 = new CategoryTitleDto(
            18L,
            "Капитальный ремонт");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO19 = new CategoryTitleDto(
            19L,
            "Ремонт кухни");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO20 = new CategoryTitleDto(
            20L,
            "Ремонт ванной");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO28 = new CategoryTitleDto(
            28L,
            "Офисный переезд");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO35 = new CategoryTitleDto(
            35L,
            "Уход за больными");

    public static final CategoryTitleDto CATEGORY_TITLE_DTO40 = new CategoryTitleDto(
            40L,
            "Дизайн офисов");

    /**
     * test data Category
     */

    public static final Category TEST_CATEGORY2 = Category.builder()
            .id(2L)
            .title("Строительство")
            .parentId(null)
            .weight(20)
            .build();

    public static final Category TEST_CATEGORY4 = Category.builder()
            .id(4L)
            .title("Грузоперевозки")
            .parentId(null)
            .weight(40)
            .build();

    public static final Category TEST_CATEGORY8 = Category.builder()
            .id(8L)
            .title("Дизайн интерьера")
            .parentId(null)
            .weight(80)
            .build();

    public static final Category TEST_CATEGORY18 = Category.builder()
            .id(18L)
            .title("Капитальный ремонт")
            .parentId(1L)
            .weight(70)
            .build();

    public static final Category TEST_CATEGORY19 = Category.builder()
            .id(19L)
            .title("Ремонт кухни")
            .parentId(1L)
            .weight(80)
            .build();

    public static final Category TEST_CATEGORY20 = Category.builder()
            .id(20L)
            .title("Ремонт ванной")
            .parentId(1L)
            .weight(90)
            .build();

    public static final Category TEST_CATEGORY28 = Category.builder()
            .id(28L)
            .title("Офисный переезд")
            .parentId(4L)
            .weight(20)
            .build();

    public static final Category TEST_CATEGORY35 = Category.builder()
            .id(35L)
            .title("Уход за больными")
            .parentId(6L)
            .weight(30)
            .build();

    public static final Category TEST_CATEGORY40 = Category.builder()
            .id(40L)
            .title("Дизайн офисов")
            .parentId(8L)
            .weight(30)
            .build();

    /**
     * CategoryResponseDTO
     */

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO2 = new CategoryResponseDto(
            2L,
            "Строительство",
            null,
            20);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO4 = new CategoryResponseDto(
            4L,
            "Грузоперевозки",
            null,
            40);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO8 = new CategoryResponseDto(
            8L,
            "Дизайн интерьера",
            null,
            80);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO18 = new CategoryResponseDto(
            18L,
            "Капитальный ремонт",
            null,
            70);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO19 = new CategoryResponseDto(
            19L,
            "Ремонт кухни",
            null,
            80);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO20 = new CategoryResponseDto(
            20L,
            "Ремонт ванной",
            null,
            90);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO28 = new CategoryResponseDto(
            28L,
            "Офисный переезд",
            null,
            20);

    public static final CategoryResponseDto CATEGORY_RESPONSE_DTO35 = new CategoryResponseDto(
            35L,
            "Уход за больными",
            null,
            30);
}
