package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.MealLogForm;
import com.example.demo.entity.MealRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.MealRecordRepository;

@Service
public class MealService {

    @Autowired
    private MealRecordRepository mealRecordRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public MealRecord saveMealRecord(MealLogForm form, User user) {
        MealRecord record = new MealRecord();
        record.setUser(user);
        record.setMealType(form.getMealType());
        record.setContent(form.getContent());
        record.setCalories(form.getCalories());
        record.setProtein(form.getProtein());
        record.setFat(form.getFat());
        record.setCarbohydrate(form.getCarbohydrate());
        record.setImageUrl(form.getImageUrl());

        // 日付と時刻を結合してLocalDateTimeに変換
        String dateTimeString = form.getDate() + " " + form.getTime();
        record.setMealDateTime(LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER));

        return mealRecordRepository.save(record);
    }

    public List<MealRecord> getMealRecordsByUser(User user) {
        return mealRecordRepository.findByUserOrderByMealDateTimeDesc(user);
    }
}