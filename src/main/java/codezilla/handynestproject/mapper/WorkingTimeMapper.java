package codezilla.handynestproject.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkingTimeMapper {

    default String getTitleById(Integer id) {
        switch (id) {
            case 1:
                return "с 8 до 12";
            case 2:
                return "с 12 до 16";
            case 3:
                return "с 16 до 22";
            case 4:
                return "в любое время";
            default:
                return null;
        }
    }
}
