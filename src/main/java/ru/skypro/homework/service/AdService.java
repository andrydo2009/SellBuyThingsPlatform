package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;

import java.io.IOException;

public interface AdService {
    /**
     * Создание объявления
     * @param createOrUpdateAd объект, содержащий необходимую информацию для создания объявления
     * @param image загружаемое изображение
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    Ad createAd(CreateOrUpdateAd createOrUpdateAd, MultipartFile image) throws IOException;

    /**
     * Получение всех объявлений
     */
    Ads getAllAdvertising();

    /**
     * Получение объявления по id
     * @param id идентификатор объявления в БД
     */
    ExtendedAd getAdvertisingById(int id);

    /**
     * Удаление объявления
     * @param id идентификатор объявления в БД
     * @throws IOException выбрасывается при ошибках, возникающих во время удаления изображения
     */
    boolean deleteAdvertisingById(int id) throws IOException;

    /**
     * Обновление объявления
     * @param id идентификатор объявления в БД
     * @param createOrUpdateAd объект, содержащий необходимую информацию для обновления объявления
     */
    Ad updateAdvertising(int id, CreateOrUpdateAd createOrUpdateAd);

    /**
     * Получение всех объявлений аутентифицированного пользователя
     */
    Ads getAllAuthenticatedUserAdvertising();

    /**
     * Обновление изображения объявления
     * @param id идентификатор объявления в БД
     * @param image загружаемое изображение
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    boolean updateAdvertisingImage(int id, MultipartFile image) throws IOException;

    /**
     * Выгрузка изображения объявления из файловой системы
     * @param adId идентификатор объявления в БД
     * @throws IOException выбрасывается при ошибках, возникающих во время выгрузки изображения
     */
    byte[] downloadAdImageFromFS(int adId) throws IOException;

    /**
     * Поиск объявления по заголовку
     * @param title заголовок объявления
     */
    Ads findByTitle(String title);
}
