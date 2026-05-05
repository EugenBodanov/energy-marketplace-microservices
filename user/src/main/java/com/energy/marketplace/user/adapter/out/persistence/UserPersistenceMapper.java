package com.energy.marketplace.user.adapter.out.persistence;

import com.energy.marketplace.user.domain.model.User;
import com.energy.marketplace.user.domain.valueObject.Email;
import com.energy.marketplace.user.domain.valueObject.Id;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        Long id = user.getId() == null ? null : user.getId().value();

        return new UserJpaEntity(
                id,
                user.getName(),
                user.getEmail().value(),
                user.getPassword(),
                user.getRole(),
                user.getStatus()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new User(
                new Id(entity.getId()),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getStatus()
        );
    }
}