package shop.francesinha.auth.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import shop.francesinha.auth.model.User;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true) // e.g., let DB assign ID
    @Mapping(target = "encryptedPassword", ignore = true) // we will take care of encryption separately
    User toEntity(UserDTO dto);

    @Mapping(target = "password", ignore = true) // or map encryptedPassword back if needed
    UserDTO toDTO(User user);
}