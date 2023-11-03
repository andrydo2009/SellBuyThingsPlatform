package ru.skypro.homework.service;

import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.model.AdEntity;

import java.util.List;

public interface AdMapper {
    AdEntity toAdEntity(CreateOrUpdateAd createOrUpdateAd, AdEntity ad);
    Ad toAd(AdEntity adEntity);
    ExtendedAd toExtendedAd(AdEntity adEntity);
    Ads toAds(List<AdEntity> adEntityList);
}
