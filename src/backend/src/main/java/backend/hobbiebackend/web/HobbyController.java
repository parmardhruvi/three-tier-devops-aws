package backend.hobbiebackend.web;

import backend.hobbiebackend.model.dto.HobbyInfoDto;
import backend.hobbiebackend.model.dto.HobbyInfoUpdateDto;
import backend.hobbiebackend.model.entities.*;
import backend.hobbiebackend.service.CategoryService;
import backend.hobbiebackend.service.HobbyService;
import backend.hobbiebackend.service.LocationService;
import backend.hobbiebackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/hobbies")
@CrossOrigin(origins = "*")
public class HobbyController {
    private final HobbyService hobbyService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public HobbyController(HobbyService hobbyService, CategoryService categoryService, LocationService locationService, UserService userService, ModelMapper modelMapper) {
        this.hobbyService = hobbyService;
        this.categoryService = categoryService;
        this.locationService = locationService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @Operation(summary = "Create new hobby", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<HttpStatus> saveHobby(@RequestBody HobbyInfoDto info) {
        Hobby offer = this.modelMapper.map(info, Hobby.class);
        Category category = this.categoryService.findByName(info.getCategory());
        Location location = this.locationService.getLocationByName(info.getLocation());
        offer.setLocation(location);
        offer.setCategory(category);
        BusinessOwner business = this.userService.findBusinessByUsername(info.getCreator());
        Set<Hobby> hobby_offers = business.getHobby_offers();
        hobby_offers.add(offer);
        business.setHobby_offers(hobby_offers);
        this.hobbyService.createHobby(offer);
        this.userService.saveUpdatedUser(business);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/is-saved")
    @Operation(summary = "Show if hobby is saved in favorites", security = @SecurityRequirement(name = "bearerAuth"))
    public boolean isHobbySaved(@RequestParam Long id, @RequestParam String username) {
        return this.hobbyService.isHobbySaved(id, username);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Show hobby details", security = @SecurityRequirement(name = "bearerAuth"))
    public Hobby getHobbyDetails(@PathVariable Long id) {
        return this.hobbyService.findHobbieById(id);
    }


    @PostMapping("/save")
    @Operation(summary = "Save hobby in favorites", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Long> save(@RequestParam Long id, @RequestParam String username) {
        Hobby hobby = this.hobbyService.findHobbieById(id);
        boolean isSaved = this.hobbyService.saveHobbyForClient(hobby, username);
        if (!isSaved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Remove hobby from favorites", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Long> removeHobby(@RequestParam Long id, @RequestParam String username) {
        Hobby hobby = this.hobbyService.findHobbieById(id);
        boolean isRemoved = this.hobbyService.removeHobbyForClient(hobby, username);
        if (!isRemoved) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(id, HttpStatus.OK);
    }


    @PutMapping
    @Operation(summary = "Update hobby,(use existing hobby id)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> updateHobby(@RequestBody HobbyInfoUpdateDto info) {
        try {
            Category category = this.categoryService.findByName(info.getCategory());
            Location location = this.locationService.getLocationByName(info.getLocation());

            Hobby offer = new Hobby();
            offer.setId(info.getId());
            offer.setName(info.getName());
            offer.setSlogan(info.getSlogan());
            offer.setIntro(info.getIntro());
            offer.setDescription(info.getDescription());
            offer.setPrice(info.getPrice());
            offer.setContactInfo(info.getContactInfo());
            offer.setCreator(info.getCreator());
            offer.setCategory(category);
            offer.setLocation(location);
            offer.setProfileImgUrl(info.getProfileImgUrl());
            offer.setProfileImg_id(info.getProfileImg_id());
            offer.setGalleryImgUrl1(info.getGalleryImgUrl1());
            offer.setGalleryImg1_id(info.getGalleryImg1_id());
            offer.setGalleryImgUrl2(info.getGalleryImgUrl2());
            offer.setGalleryImg2_id(info.getGalleryImg2_id());
            offer.setGalleryImgUrl3(info.getGalleryImgUrl3());
            offer.setGalleryImg3_id(info.getGalleryImg3_id());

            this.hobbyService.saveUpdatedHobby(offer);
            return new ResponseEntity<Hobby>(offer, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error updating hobby with ID " + info.getId() + ":");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating hobby with ID " + info.getId() + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hobby", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> deleteHobby(@PathVariable Long id) {
        try {
            boolean isRemoved = this.hobbyService.deleteHobby(id);
            if (!isRemoved) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(id, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error deleting hobby with ID " + id + ":");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting hobby with ID " + id + ": " + e.getMessage());
        }
    }

    @GetMapping("/saved")
    @Operation(summary = "Show hobbies that are saved in favorites", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Hobby> savedHobbies(@RequestParam String username) {
        AppClient appClient = this.userService.findAppClientByUsername(username);
        return this.hobbyService.findSavedHobbies(appClient);

    }
}

