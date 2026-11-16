package com.lan.app.service;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.repository.BirthdayWishRepository;
import com.lan.app.service.command.CreateBirthdayWishCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class BirthdayWishService {

    private final BirthdayWishRepository repo;

    public BirthdayWishService(BirthdayWishRepository repo) {
        this.repo = repo;
    }

    public List<BirthdayWish> list() {
        return repo.list();
    }

    public BirthdayWish create(CreateBirthdayWishCommand cmd) {
        return repo.create(cmd);
    }
}
