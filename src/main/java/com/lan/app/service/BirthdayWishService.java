package com.lan.app.service;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.repository.BirthdayWishRepository;
import com.lan.app.service.command.CreateBirthdayWishCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BirthdayWishService {

    private final BirthdayWishRepository repo;

    public BirthdayWishService(BirthdayWishRepository repo) {
        this.repo = repo;
    }

    public BirthdayWish create(CreateBirthdayWishCommand cmd) {
        return repo.create(cmd);
    }
}
