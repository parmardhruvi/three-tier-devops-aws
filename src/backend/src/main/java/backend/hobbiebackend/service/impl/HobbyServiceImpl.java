package backend.hobbiebackend.service.impl;

import backend.hobbiebackend.handler.NotFoundException;
import backend.hobbiebackend.model.entities.*;
import backend.hobbiebackend.model.entities.enums.CategoryNameEnum;
import backend.hobbiebackend.model.entities.enums.LocationEnum;
import backend.hobbiebackend.model.repostiory.HobbyRepository;
import backend.hobbiebackend.service.CategoryService;
import backend.hobbiebackend.service.HobbyService;
import backend.hobbiebackend.service.LocationService;
import backend.hobbiebackend.service.UserService;
import com.cloudinary.Cloudinary;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class HobbyServiceImpl implements HobbyService {
    private final HobbyRepository hobbyRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final LocationService locationService;
    private final Cloudinary cloudinary;

    @Autowired
    public HobbyServiceImpl(HobbyRepository hobbyRepository, CategoryService categoryService, UserService userService, LocationService locationService, Cloudinary cloudinary) {
        this.hobbyRepository = hobbyRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.locationService = locationService;
        this.cloudinary = cloudinary;
    }

    @Override
    public Hobby findHobbieById(Long id) {
        Optional<Hobby> hobby = this.hobbyRepository.findById(id);
        if (hobby.isPresent()) {
            return hobby.get();
        } else {
            throw new NotFoundException("This hobby does not exist");
        }
    }

    @SneakyThrows
    @Override
    public void saveUpdatedHobby(Hobby hobby) {
        Hobby existing = this.hobbyRepository.findById(hobby.getId())
                .orElseThrow(() -> new NotFoundException("This hobby does not exist"));

        existing.setName(hobby.getName());
        existing.setSlogan(hobby.getSlogan());
        existing.setIntro(hobby.getIntro());
        existing.setDescription(hobby.getDescription());
        existing.setPrice(hobby.getPrice());
        existing.setContactInfo(hobby.getContactInfo());
        if (hobby.getCategory() != null) {
            existing.setCategory(hobby.getCategory());
        }
        if (hobby.getLocation() != null) {
            existing.setLocation(hobby.getLocation());
        }
        if (hobby.getProfileImgUrl() != null && !hobby.getProfileImgUrl().trim().isEmpty()) {
            existing.setProfileImgUrl(hobby.getProfileImgUrl());
        }
        if (hobby.getProfileImg_id() != null && !hobby.getProfileImg_id().trim().isEmpty()) {
            existing.setProfileImg_id(hobby.getProfileImg_id());
        }
        if (hobby.getGalleryImgUrl1() != null && !hobby.getGalleryImgUrl1().trim().isEmpty()) {
            existing.setGalleryImgUrl1(hobby.getGalleryImgUrl1());
        }
        if (hobby.getGalleryImg1_id() != null && !hobby.getGalleryImg1_id().trim().isEmpty()) {
            existing.setGalleryImg1_id(hobby.getGalleryImg1_id());
        }
        if (hobby.getGalleryImgUrl2() != null && !hobby.getGalleryImgUrl2().trim().isEmpty()) {
            existing.setGalleryImgUrl2(hobby.getGalleryImgUrl2());
        }
        if (hobby.getGalleryImg2_id() != null && !hobby.getGalleryImg2_id().trim().isEmpty()) {
            existing.setGalleryImg2_id(hobby.getGalleryImg2_id());
        }
        if (hobby.getGalleryImgUrl3() != null && !hobby.getGalleryImgUrl3().trim().isEmpty()) {
            existing.setGalleryImgUrl3(hobby.getGalleryImgUrl3());
        }
        if (hobby.getGalleryImg3_id() != null && !hobby.getGalleryImg3_id().trim().isEmpty()) {
            existing.setGalleryImg3_id(hobby.getGalleryImg3_id());
        }

        this.hobbyRepository.save(existing);
    }

    @Override
    @Transactional
    public boolean deleteHobby(long id) throws Exception {
        Optional<Hobby> byId = this.hobbyRepository.findById(id);
        if (byId.isPresent()) {
            Hobby hobby = byId.get();

            // 1. Delete Cloudinary media safely
            deleteResourcesById(hobby);

            // 2. Remove all foreign key associations from join tables
            this.hobbyRepository.deleteFromSavedHobbies(id);
            this.hobbyRepository.deleteFromHobbyMatches(id);
            this.hobbyRepository.deleteFromBusinessOwnerOffers(id);

            // 3. Delete the hobby entity using direct native query
            this.hobbyRepository.deleteHobbyByIdNative(id);
            return true;
        }
        return false;
    }

    private void deleteResourcesById(Hobby byId) {
        try {
            List<String> resourceIds = new ArrayList<>();
            if (byId.getProfileImg_id() != null && !byId.getProfileImg_id().trim().isEmpty()) {
                resourceIds.add(byId.getProfileImg_id());
            }
            if (byId.getGalleryImg1_id() != null && !byId.getGalleryImg1_id().trim().isEmpty()) {
                resourceIds.add(byId.getGalleryImg1_id());
            }
            if (byId.getGalleryImg2_id() != null && !byId.getGalleryImg2_id().trim().isEmpty()) {
                resourceIds.add(byId.getGalleryImg2_id());
            }
            if (byId.getGalleryImg3_id() != null && !byId.getGalleryImg3_id().trim().isEmpty()) {
                resourceIds.add(byId.getGalleryImg3_id());
            }

            if (cloudinary != null && cloudinary.api() != null && !resourceIds.isEmpty()) {
                cloudinary.api().deleteResources(resourceIds, Map.of("invalidate", true));
            }
        } catch (Exception e) {
            System.err.println("Cloudinary deleteResources failed (non-critical): " + e.getMessage());
        }
    }


    @Override
    public Set<Hobby> findHobbyMatches(String username) {
        AppClient currentUserAppClient = this.userService.findAppClientByUsername(username);
        Set<Hobby> hobby_matches = new HashSet<>();
        if (currentUserAppClient.getTestResults() != null) {
            boolean isAdded = false;
            Random rand = new Random();
            LocationEnum location = currentUserAppClient.getTestResults().getLocation();
            Location locationByName = this.locationService.getLocationByName(location);
            List<Hobby> allByLocation = this.hobbyRepository.findAllByLocation(locationByName);
            List<CategoryNameEnum> testCategoryResults = new ArrayList<>();

            testCategoryResults.add(currentUserAppClient.getTestResults().getCategoryOne());
            testCategoryResults.add(currentUserAppClient.getTestResults().getCategoryTwo());
            testCategoryResults.add(currentUserAppClient.getTestResults().getCategoryThree());
            testCategoryResults.add(currentUserAppClient.getTestResults().getCategoryFour());
            testCategoryResults.add(currentUserAppClient.getTestResults().getCategoryFive());
            testCategoryResults.add(currentUserAppClient.getTestResults().getCategorySix());

            if (allByLocation.size() > 0) {

                for (int i = 0; i < 10; i++) {
                    int randomIndex = rand.nextInt(allByLocation.size());
                    Hobby randomHobby = allByLocation.get(randomIndex);
                    if (hobby_matches.contains(randomHobby)) {
                        continue;
                    }
                    for (CategoryNameEnum testCategory : testCategoryResults) {
                        if (randomHobby.getCategory().getName().equals(testCategory)) {
                            hobby_matches.add(randomHobby);
                            isAdded = true;
                        }
                        if (isAdded) {
                            isAdded = false;
                            break;
                        }
                    }
                }
            }
        }
        return hobby_matches;
    }

    @Override
    public boolean saveHobbyForClient(Hobby hobby, String username) {
        AppClient currentUserAppClient = this.userService.findAppClientByUsername(username);
        Optional<Hobby> hobbyById = this.hobbyRepository.findById(hobby.getId());
        List<Hobby> saved_hobbies = currentUserAppClient.getSaved_hobbies();
        if (hobbyById.isPresent() && !(saved_hobbies.contains(hobbyById.get()))) {
            saved_hobbies.add(hobbyById.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeHobbyForClient(Hobby hobby, String username) {
        AppClient currentUserAppClient = this.userService.findAppClientByUsername(username);
        Optional<Hobby> hobbyById = this.hobbyRepository.findById(hobby.getId());
        if (currentUserAppClient != null) {
            hobbyById.ifPresent(value -> currentUserAppClient.getSaved_hobbies().remove(value));
            return true;
        }
        return false;
    }

    @Override
    public boolean isHobbySaved(Long hobbyId, String username) {
        Optional<Hobby> byId = this.hobbyRepository.findById(hobbyId);
        if (byId.isPresent()) {
            AppClient currentUserAppClient = this.userService.findAppClientByUsername(username);
            return currentUserAppClient.getSaved_hobbies().contains(byId.get());
        }
        return false;
    }

    @Override
    public List<Hobby> findSavedHobbies(AppClient currentAppClient) {
        return currentAppClient.getSaved_hobbies();
    }

    @Override
    public Set<Hobby> getAllHobbiesForBusiness(String username) {
        return this.hobbyRepository.findAllByCreator(username);
    }

    @Override
    public Set<Hobby> getAllHobbieMatchesForClient(String username) {
        AppClient currentUserAppClient = this.userService.findAppClientByUsername(username);
        return currentUserAppClient.getHobby_matches();
    }

    @Override
    public void createHobby(Hobby offer) {
        this.hobbyRepository.save(offer);
    }

}

