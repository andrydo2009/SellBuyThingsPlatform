package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdMapperImplTest {

    AdMapperImpl adMapperTest=new AdMapperImpl();
    UserEntity userEntityTest = new UserEntity();
    AdEntity adEntityTest = new AdEntity();
    CommentEntity commentEntity = new CommentEntity();
    Ad adTest = new Ad();
    ExtendedAd extendedAdTest=new ExtendedAd();
    Ads adsTest=new Ads();
    CreateOrUpdateAd orUpdateAdTest=new CreateOrUpdateAd("testTitle",55555,"testDescription");
    List<Ad> adListTest=new ArrayList<>();

    List<AdEntity> adEntityListTest=new ArrayList<>();

    @BeforeEach
    void setUp(){
        userEntityTest.setEmail("testEmail@gmail.com");
        userEntityTest.setPassword("$2a$12$nbPi.P3rcqDsL4xDcVru2OXnqZX81vVvUvaFRONeYDnyaeK4c/TbS");
        userEntityTest.setFirstName("testFirstName");
        userEntityTest.setLastName("testLastName");
        userEntityTest.setPhoneUser("+77777777777");
        userEntityTest.setImagePath("/users/image/" + userEntityTest.getId());
        userEntityTest.setRole(Role.USER);

        adEntityTest.setDescription("testDescription");
        adEntityTest.setPrice(55555);
        adEntityTest.setTitle("testTitle");
        adEntityTest.setImagePath("/ads/image/" + adEntityTest.getId());
        adEntityTest.setUserEntity(userEntityTest);

        commentEntity.setText("testText");
        commentEntity.setCreatedAt(Instant.now().toEpochMilli()); // or we can use System.currentTimeMillis()
        commentEntity.setUserEntity(userEntityTest);
        commentEntity.setAdEntity(adEntityTest);

        adTest.setAuthor(adEntityTest.getUserEntity().getId());
        adTest.setPk(adEntityTest.getId());
        adTest.setImage(adEntityTest.getImagePath());
        adTest.setPrice(adEntityTest.getPrice());
        adTest.setTitle(adEntityTest.getTitle());

        adListTest.add(adTest);

        adsTest.setCount(1);
        adsTest.setResults(adListTest);

        adEntityListTest.add(adEntityTest);
    }

    @Test
    void toAdEntityTest() {
        AdEntity result=adMapperTest.toAdEntity(orUpdateAdTest,adEntityTest);
        assertEquals(adEntityTest.getTitle(),result.getTitle());
        assertEquals(adEntityTest.getPrice(),result.getPrice());
        assertEquals(adEntityTest.getDescription(),result.getDescription());
    }

    @Test
    void toAdTest() {
        Ad result=adMapperTest.toAd(adEntityTest);
        assertEquals(adEntityTest.getUserEntity().getId(),result.getAuthor());
        assertEquals(adEntityTest.getImagePath(),result.getImage());
        assertEquals(adEntityTest.getId(),result.getPk());
        assertEquals(adEntityTest.getTitle(),result.getTitle());
        assertEquals(adEntityTest.getPrice(),result.getPrice());
    }

    @Test
    void toExtendedAd() {
        ExtendedAd result=adMapperTest.toExtendedAd(adEntityTest);
        assertEquals(adEntityTest.getId(),result.getPk());
        assertEquals(adEntityTest.getUserEntity().getFirstName(),result.getAuthorFirstName());
        assertEquals(adEntityTest.getUserEntity().getLastName(),result.getAuthorLastName());
        assertEquals(adEntityTest.getUserEntity().getPhoneUser(),result.getPhone());
        assertEquals(adEntityTest.getUserEntity().getEmail(),result.getEmail());
        assertEquals(adEntityTest.getDescription(),result.getDescription());
        assertEquals(adEntityTest.getImagePath(),result.getImage());
        assertEquals(adEntityTest.getPrice(),result.getPrice());
        assertEquals(adEntityTest.getTitle(),result.getTitle());
    }

    @Test
    void toAds() {
        Ads result=adMapperTest.toAds(adEntityListTest);
        assertEquals(adsTest.getCount(),result.getCount());
        assertEquals(adsTest,result);
    }
}