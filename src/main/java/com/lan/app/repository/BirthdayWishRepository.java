package com.lan.app.repository;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.service.command.CreateBirthdayWishCommand;

public interface BirthdayWishRepository {
    BirthdayWish create(CreateBirthdayWishCommand cmd);
}
