package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.service.AdMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdMapperImpl implements AdMapper {
    @Override
    public AdEntity toAdEntity(CreateOrUpdateAd createOrUpdateAd, AdEntity ad) {
        ad.setDescription(createOrUpdateAd.getDescription());
        ad.setPrice(createOrUpdateAd.getPrice());
        ad.setTitle(createOrUpdateAd.getTitle());
        return ad;
    }

    @Override
    public Ad toAd(AdEntity adEntity) {
        Ad ad = new Ad();
        ad.setAuthor(adEntity.getUserEntity().getId());
        ad.setImage("/ads/image/" + adEntity.getId());
        ad.setPk(adEntity.getId());
        ad.setPrice(adEntity.getPrice());
        ad.setTitle(adEntity.getTitle());
        return ad;
    }

    @Override
    public ExtendedAd toExtendedAd(AdEntity adEntity) {
        ExtendedAd extendedAd = new ExtendedAd();
        extendedAd.setPk(adEntity.getId());
        extendedAd.setAuthorFirstName(adEntity.getUserEntity().getFirstName());
        extendedAd.setAuthorLastName(adEntity.getUserEntity().getLastName());
        extendedAd.setDescription(adEntity.getDescription());
        extendedAd.setEmail(adEntity.getUserEntity().getEmail());
        extendedAd.setImage("/ads/image/" + adEntity.getId());
        extendedAd.setPhone(adEntity.getUserEntity().getPhoneUser());
        extendedAd.setPrice(adEntity.getPrice());
        extendedAd.setTitle(adEntity.getTitle());
        return extendedAd;
    }

    @Override
    public Ads toAds(List<AdEntity> adEntityList) {
        Ads ads = new Ads();
        ads.setCount(adEntityList.size());
        ads.setResults(adEntityList.stream()
                .map(this::toAd)
                .collect(Collectors.toList()));
        return ads;
    }
}
