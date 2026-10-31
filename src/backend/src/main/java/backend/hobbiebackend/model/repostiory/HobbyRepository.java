package backend.hobbiebackend.model.repostiory;

import backend.hobbiebackend.model.entities.Hobby;
import backend.hobbiebackend.model.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HobbyRepository extends JpaRepository<Hobby, Long> {
    Set<Hobby> findAllByCreator(String creator);

    List<Hobby> findAllByLocation(Location location);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM app_clients_saved_hobbies WHERE saved_hobbies_id = :id", nativeQuery = true)
    void deleteFromSavedHobbies(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM app_clients_hobby_matches WHERE hobby_matches_id = :id", nativeQuery = true)
    void deleteFromHobbyMatches(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM business_owners_hobby_offers WHERE hobby_offers_id = :id", nativeQuery = true)
    void deleteFromBusinessOwnerOffers(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM hobbies WHERE id = :id", nativeQuery = true)
    void deleteHobbyByIdNative(@Param("id") Long id);
}
