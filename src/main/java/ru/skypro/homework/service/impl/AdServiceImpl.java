package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdMapper;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.FileService;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

/**
 * Класс для осуществления операций с базой данных объявлений
 */
@Slf4j
@Service
public class AdServiceImpl implements AdService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final UserDetails userDetails;
    private final FileService fileService;
    private static final String USER_NOT_FOUND = "User not found";
    private static final String AD_NOT_FOUND = "Ad not found";
    @Value("${ad.image.dir.path}")
    private String adImageDirPath;

    public AdServiceImpl(CommentRepository commentRepository,
                         UserRepository userRepository,
                         AdRepository adRepository,
                         AdMapper adMapper,
                         UserDetails userDetails, FileService fileService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.adRepository = adRepository;
        this.adMapper = adMapper;
        this.userDetails = userDetails;
        this.fileService = fileService;
    }

    /**
     * Создание объявления и сохранение его в базе данных.<br>
     * - Поиск пользователя в базе данных по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Создание объявления из входных данных {@link AdMapper#toAdEntity(CreateOrUpdateAd, AdEntity)}
     * и сохранение его в базе данных {@link AdRepository#save(Object)}.<br>
     * - Создание пути для загрузки изображения объявления {@link #createPath(MultipartFile, AdEntity)}.<br>
     * - Загрузка с сайта и сохранение в файловой системе изображения объявления {@link FileService#uploadImage(MultipartFile, Path)}.<br>
     * - Задание необходимых параметров созданному объявлению {@link AdEntity#setUserEntity(UserEntity)}, {@link AdEntity#setImagePath(String)}.<br>
     * - Сохранение созданного объявления в базе данных {@link AdRepository#save(Object)}.<br>
     * - Преобразование (маппинг) созданного объявления в объект возвращаемого класса {@link AdMapper#toAd(AdEntity)}.
     * @param createOrUpdateAd объект, содержащий необходимую информацию для создания объявления
     * @param image загружаемое изображение
     * @return объект {@link Ad}, содержащий необходимую информацию о созданном объявлении
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    @Override
    @Transactional
    public Ad createAd(CreateOrUpdateAd createOrUpdateAd, MultipartFile image) throws IOException {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        AdEntity adEntity = adRepository.save(adMapper.toAdEntity(createOrUpdateAd, new AdEntity()));
        Path filePath = createPath(image, adEntity);
        adEntity.setUserEntity(user);
        adEntity.setImagePath(filePath.toAbsolutePath().toString());
        Ad savedAd = adMapper.toAd(adRepository.save(adEntity));
        log.info("Advertisement was created successfully.");
        return savedAd;
    }

    /**
     * Получение всех объявлений.<br>
     * - Поиск всех объявлений в базе данных {@link AdRepository#findAll()}.<br>
     * - Преобразование (маппинг) списка найденных объявлений в объект возвращаемого класса {@link AdMapper#toAds(List)}.
     * @return объект {@link Ads}, содержащий количество объявлений и список объявлений
     */
    @Override
    @Transactional(readOnly = true)
    public Ads getAllAdvertising() {
        List<AdEntity> adEntityList = adRepository.findAll();
        Ads ads = adMapper.toAds(adEntityList);
        log.info("Advertisements were received successfully.");
        return ads;
    }

    /** Получение объявления по id.<br>
     * - Поиск объявления по id {@link AdRepository#findById(Object)}.<br>
     * - Преобразование (маппинг) найденного объявления в объект возвращаемого класса {@link AdMapper#toExtendedAd(AdEntity)}.
     * @param id идентификатор объявления в БД
     * @return объект {@link ExtendedAd}, содержащий необходимую информацию о запрашиваемом объявлении
     */
    @Override
    @Transactional(readOnly = true)
    public ExtendedAd getAdvertisingById(int id) {
        AdEntity adEntity = adRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(AD_NOT_FOUND));
        ExtendedAd ad = adMapper.toExtendedAd(adEntity);
        log.info("Advertisement with id: {} was received successfully.", id);
        return ad;
    }

    /**
     * Удаление объявления.<br>
     * - Поиск пользователя в базе данных по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск объявления в базе данных по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Удаление из базы данных всех комментариев найденного объявления {@link CommentRepository#deleteAllByAdEntity_Id(int)}.<br>
     * - Удаление из базы данных объявления по id {@link AdRepository#deleteById(Object)}.<br>
     * - Удаление из файловой системы изображения объявления {@link Files#deleteIfExists(Path)}.
     * @param id идентификатор объявления в БД
     * @return <B>true</B>, если у пользователя есть права на удаление объявления.<br>
     * В противном случае <B>false</B>
     * @throws IOException выбрасывается при ошибках, возникающих во время удаления изображения
     */
    @Override
    @Transactional
    public boolean deleteAdvertisingById(int id) throws IOException {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AD_NOT_FOUND));
        if (userEntity.getRole() == Role.ADMIN || adEntity.getUserEntity().equals(userEntity)){
            commentRepository.deleteAllByAdEntity_Id(id);
            adRepository.deleteById(id);
            Files.deleteIfExists(Path.of(adEntity.getImagePath()));
            log.info("Advertisement with id: {} was deleted successfully.", id);
            return true;
        }
        return false;
    }

    /**
     * Обновление объявления.<br>
     * - Поиск пользователя в базе данных по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск объявления в базе данных по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Преобразование (маппинг) найденного объявления и входных данных в обновленное объявление {@link AdMapper#toAdEntity(CreateOrUpdateAd, AdEntity)}.<br>
     * - Сохранение обновленного объявления в базе данных {@link AdRepository#save(Object)}.<br>
     * - Преобразование (маппинг) обновленного объявления в объект возвращаемого класса {@link AdMapper#toAd(AdEntity)}.
     * @param id идентификатор объявления в БД
     * @param createOrUpdateAd объект, содержащий необходимую информацию для обновления объявления
     * @return объект {@link Ad}, содержащий необходимую информацию об обновленном объявлении.<br>
     * В противном случае <B>null</B>
     */
    @Override
    @Transactional
    public Ad updateAdvertising(int id, CreateOrUpdateAd createOrUpdateAd) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AD_NOT_FOUND));
        if (userEntity.getRole() == Role.ADMIN || adEntity.getUserEntity().equals(userEntity)){
            Ad updatedAd = adMapper.toAd(adRepository.save(adMapper.toAdEntity(createOrUpdateAd, adEntity)));
            log.info("Advertisement with id: {} was updated successfully.", id);
            return updatedAd;
        }
        return null;
    }

    /**
     * Получение всех объявлений аутентифицированного пользователя.<br>
     * - Поиск в базе данных всех объявлений текущего пользователя {@link AdRepository#findAllByUserEntityEmail(String)}.<br>
     * - Преобразование (маппинг) списка найденных объявлений в объект возвращаемого класса {@link AdMapper#toAds(List)}.
     * @return объект {@link Ads}, содержащий количество объявлений и список объявлений текущего пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public Ads getAllAuthenticatedUserAdvertising() {
        List<AdEntity> adEntityList = adRepository.findAllByUserEntityEmail(userDetails.getUsername());
        Ads userAds = adMapper.toAds(adEntityList);
        log.info("Advertisements for user: {} were successfully received.", userDetails.getUsername());
        return userAds;
    }

    /**
     * Обновление изображения объявления.<br>
     * - Поиск объявления в базе данных по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Удаление текущего изображения объявления {@link Files#deleteIfExists(Path)}, если в объявлении сохранен путь к изображению в файловой системе.<br>
     * - Создание пути для загрузки обновленного изображения объявления {@link #createPath(MultipartFile, AdEntity)}.<br>
     * - Загрузка с сайта и сохранение в файловой системе обновленного изображения объявления {@link FileService#uploadImage(MultipartFile, Path)}.<br>
     * - Задание необходимых параметров найденному объявлению {@link AdEntity#setUserEntity(UserEntity)}, {@link AdEntity#setImagePath(String)}.<br>
     * - Сохранение в базе данных объявления с обновленным изображением {@link AdRepository#save(Object)}.
     * @param id идентификатор объявления в БД
     * @param image загружаемое изображение
     * @return <B>true</B>
     * @throws IOException выбрасывается при ошибках, возникающих во время загрузки изображения
     */
    @Override
    @Transactional
    public boolean updateAdvertisingImage(int id, MultipartFile image) throws IOException {
        AdEntity adEntity = adRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(AD_NOT_FOUND));
        if (adEntity.getImagePath() != null) {
            Files.deleteIfExists(Path.of(adEntity.getImagePath()));
        }
        adEntity.setImagePath(createPath(image, adEntity).toAbsolutePath().toString());
        adRepository.save(adEntity);
        log.info("Advertisement image was updated successfully.");
        return true;
    }

    /**
     * Вспомогательный метод. Создание пути для загрузки изображения объявления.<br>
     * - Создание пути из папки хранения изображений объявлений, идентификатора объявления и расширения изображения.
     * - Копирование данных изображения. Входной поток получаем из метода {@link Files#newInputStream(Path, OpenOption...)}. Выходной поток получаем из метода {@link HttpServletResponse#getOutputStream()}
     * @param image загружаемое изображение
     * @param adEntity объявление, для которого загружается изображение
     * @return объект класса {@link Path}
     * @throws IOException выбрасывается при ошибках, возникающих во время выгрузки изображения
     */
    private Path createPath(MultipartFile image, AdEntity adEntity) throws IOException {
        Path filePath = Path.of(adImageDirPath, "Advertisement_" + adEntity.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        fileService.uploadImage(image, filePath);
        log.info("Upload image from database method was invoked.");
        return filePath;
    }

    /**
     * Выгрузка изображения объявления из файловой системы.<br>
     * - Поиск объявления в базе данных по идентификатору объявления {@link AdRepository#findById(Object)}.<br>
     * - Копирование данных изображения. Входной поток получаем из метода {@link Files#newInputStream(Path, OpenOption...)}
     * @param adId идентификатор объявления в БД
     * @return image - массив байт картинки
     * @throws IOException выбрасывается при ошибках, возникающих во время выгрузки изображения
     */
    @Override
    public byte[] downloadAdImageFromFS(int adId) throws IOException {
        AdEntity adEntity = adRepository.findById(adId).orElseThrow(() -> new IllegalArgumentException(AD_NOT_FOUND));
        byte[] image = fileService.downloadImage(adEntity.getImagePath());
        log.info("Download advertisement image from database method was invoked.");
        return image;
    }

    /**
     * Поиск объявления по заголовку.<br>
     * - Поиск объявления в базе данных по заголовку объявления {@link AdRepository#findAllByTitleLike(String)}.<br>
     * - Преобразование (маппинг) списка найденных объявлений в объект возвращаемого класса {@link AdMapper#toAds(List)}.
     * @param title заголовок объявления
     * @return объект {@link Ads}, содержащий количество объявлений и список объявлений
     */
    @Override
    @Transactional(readOnly = true)
    public Ads findByTitle(String title) {
        Ads foundAds = adMapper.toAds(adRepository.findAllByTitleLike(title));
        log.info("Advertisement/s with title: {} was/were successfully found.", title);
        return foundAds;
    }

}
