package ru.skypro.homework.dto.ads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrUpdateAd {
    private String title;
    private int price;
    private String description;
}
