package com.lan.app.repository;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.service.command.CreateBirthdayWishCommand;

import java.util.List;

public interface BirthdayWishRepository {
    List<BirthdayWish> list();
    BirthdayWish create(CreateBirthdayWishCommand cmd);
}
